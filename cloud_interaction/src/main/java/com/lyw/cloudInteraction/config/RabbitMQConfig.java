package com.lyw.cloudInteraction.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Slf4j
@Configuration
public class RabbitMQConfig {
    // 用户行为相关交换器和路由键（与推荐模块保持一致）
    public static final String USER_BEHAVIOR_EXCHANGE = "user.behavior.exchange";
    public static final String USER_BEHAVIOR_ROUTING_KEY = "user.behavior.routing.key";
    /**
     * JSON消息转换器
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