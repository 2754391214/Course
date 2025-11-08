package com.lyw.cloudRanking.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 交换机和队列定义
    public static final String COURSE_HEAT_EXCHANGE = "course.heat.exchange";
    public static final String COURSE_HEAT_QUEUE = "course.heat.queue";
    public static final String COURSE_HEAT_DLQ = "course.heat.dlq";
    public static final String COURSE_HEAT_ROUTING_KEY = "course.heat.#";

    @Bean
    public TopicExchange courseHeatExchange() {
        return new TopicExchange(COURSE_HEAT_EXCHANGE, true, false);
    }

    @Bean
    public Queue courseHeatQueue() {
        return QueueBuilder.durable(COURSE_HEAT_QUEUE)
                .deadLetterExchange("") // 使用默认交换机
                .deadLetterRoutingKey(COURSE_HEAT_DLQ)
                .ttl(60000) // 60秒后进入死信队列
                .maxLength(10000) // 最大队列长度
                .build();
    }

    @Bean
    public Queue courseHeatDLQ() {
        return new Queue(COURSE_HEAT_DLQ, true);
    }
    @Bean
    public Binding courseHeatBinding() {
        return BindingBuilder.bind(courseHeatQueue())
                .to(courseHeatExchange())
                .with(COURSE_HEAT_ROUTING_KEY);
    }

    // 批量消费容器工厂
    @Bean
    public SimpleRabbitListenerContainerFactory batchContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setBatchListener(true); // 开启批量消费
        factory.setBatchSize(100); // 每批处理100条消息
        factory.setConsumerBatchEnabled(true);
        factory.setReceiveTimeout(5000L); // 5秒超时
        factory.setConcurrentConsumers(5); // 并发消费者
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }

    /**
     * JSON消息转换器
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        // 开启确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                // 消息发送失败处理
                if (correlationData != null) {
                    String transactionId = correlationData.getId();
                    // 记录日志或进行其他处理
                }
            }
        });
        // 开启返回回调
        rabbitTemplate.setReturnsCallback(returned -> {
            // 消息无法路由到队列时的处理
        });
        return rabbitTemplate;
    }
}