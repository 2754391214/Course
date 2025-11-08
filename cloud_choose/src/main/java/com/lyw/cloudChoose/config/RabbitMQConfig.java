package com.lyw.cloudChoose.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Slf4j
@Configuration
public class RabbitMQConfig {

    // 选课相关交换机和队列
    public static final String ENROLLMENT_EXCHANGE = "enrollment.exchange";
    public static final String ENROLLMENT_QUEUE = "enrollment.queue";
    public static final String ENROLLMENT_ROUTING_KEY = "enrollment.routing.key";

    // 死信队列配置
    public static final String ENROLLMENT_DLX_EXCHANGE = "enrollment.dlx.exchange";
    public static final String ENROLLMENT_DLX_QUEUE = "enrollment.dlx.queue";
    public static final String ENROLLMENT_DLX_ROUTING_KEY = "enrollment.dlx.routing.key";

    // 用户行为相关交换器和路由键（与推荐模块保持一致）
    public static final String USER_BEHAVIOR_EXCHANGE = "user.behavior.exchange";
    public static final String USER_BEHAVIOR_ROUTING_KEY = "user.behavior.routing.key";
    /**
     * 选课消息交换机
     */
    @Bean
    public DirectExchange enrollmentExchange() {
        return new DirectExchange(ENROLLMENT_EXCHANGE, true, false);
    }

    /**
     * 选课消息队列
     */
    @Bean
    public Queue enrollmentQueue() {
        return QueueBuilder.durable(ENROLLMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", ENROLLMENT_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ENROLLMENT_DLX_ROUTING_KEY)
                .withArgument("x-message-ttl", 60000) // 1分钟过期
                .build();
    }

    /**
     * 绑定选课队列到交换机
     */
    @Bean
    public Binding enrollmentBinding() {
        return BindingBuilder.bind(enrollmentQueue())
                .to(enrollmentExchange())
                .with(ENROLLMENT_ROUTING_KEY);
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange enrollmentDLXExchange() {
        return new DirectExchange(ENROLLMENT_DLX_EXCHANGE, true, false);
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue enrollmentDLXQueue() {
        return QueueBuilder.durable(ENROLLMENT_DLX_QUEUE).build();
    }

    /**
     * 绑定死信队列
     */
    @Bean
    public Binding enrollmentDLXBinding() {
        return BindingBuilder.bind(enrollmentDLXQueue())
                .to(enrollmentDLXExchange())
                .with(ENROLLMENT_DLX_ROUTING_KEY);
    }

    /**
     * JSON消息转换器
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // 支持Java 8时间类型
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * RabbitTemplate配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("消息发送失败: {}", cause);
            }
        });
        // 开启返回模式
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("消息路由失败: {}, 回复码: {}, 回复文本: {}, 交换器: {}, 路由键: {}",
                    returned.getMessage(), returned.getReplyCode(), returned.getReplyText(),
                    returned.getExchange(), returned.getRoutingKey());
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
        factory.setPrefetchCount(1); // 每次预取消息数量
        return factory;
    }
}