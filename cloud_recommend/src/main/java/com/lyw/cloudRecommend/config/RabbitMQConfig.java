package com.lyw.cloudRecommend.config;

import com.lyw.commonUtil.constant.RabbitmqKeyConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 用户行为交换器
     */
    @Bean
    public DirectExchange userBehaviorExchange() {
        return new DirectExchange(RabbitmqKeyConstant.USER_BEHAVIOR_EXCHANGE, true, false);
    }

    /**
     * 用户行为队列
     */
    @Bean
    public Queue userBehaviorQueue() {
        return QueueBuilder.durable(RabbitmqKeyConstant.USER_BEHAVIOR_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitmqKeyConstant.USER_BEHAVIOR_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitmqKeyConstant.USER_BEHAVIOR_DLQ_ROUTING_KEY)
                .withArgument("x-message-ttl", 60000) // 1分钟过期
                .build();
    }

    /**
     * 绑定用户行为队列到交换器
     */
    @Bean
    public Binding userBehaviorBinding() {
        return BindingBuilder.bind(userBehaviorQueue())
                .to(userBehaviorExchange())
                .with(RabbitmqKeyConstant.USER_BEHAVIOR_ROUTING_KEY);
    }

    /**
     * 死信交换器
     */
    @Bean
    public DirectExchange userBehaviorDlqExchange() {
        return new DirectExchange(RabbitmqKeyConstant.USER_BEHAVIOR_DLQ_EXCHANGE, true, false);
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue userBehaviorDlqQueue() {
        return QueueBuilder.durable(RabbitmqKeyConstant.USER_BEHAVIOR_DLQ_QUEUE).build();
    }

    /**
     * 绑定死信队列
     */
    @Bean
    public Binding userBehaviorDlqBinding() {
        return BindingBuilder.bind(userBehaviorDlqQueue())
                .to(userBehaviorDlqExchange())
                .with(RabbitmqKeyConstant.USER_BEHAVIOR_DLQ_ROUTING_KEY);
    }

    /**
     * JSON 消息转换器
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                // 消息发送失败处理
                System.err.println("消息发送失败: " + cause);
            }
        });
        return rabbitTemplate;
    }

    /**
     * 消费者容器工厂配置
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3); // 并发消费者数量
        factory.setMaxConcurrentConsumers(10); // 最大并发消费者数量
        factory.setPrefetchCount(5); // 每次预取消息数量
        return factory;
    }
}