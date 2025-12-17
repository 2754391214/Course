package com.lyw.cloudCourse.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lyw.commonUtil.constant.RabbitmqKeyConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Slf4j
@Configuration
public class RabbitMQConfig {
    /**
     * 选课消息交换机
     */
    @Bean
    public DirectExchange enrollmentExchange() {
        return new DirectExchange(RabbitmqKeyConstant.ENROLLMENT_EXCHANGE, true, false);
    }

    /**
     * 选课消息队列
     */
    @Bean
    public Queue enrollmentQueue() {
        return QueueBuilder.durable(RabbitmqKeyConstant.ENROLLMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitmqKeyConstant.ENROLLMENT_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitmqKeyConstant.ENROLLMENT_DLX_ROUTING_KEY)
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
                .with(RabbitmqKeyConstant.ENROLLMENT_ROUTING_KEY);
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange enrollmentDLXExchange() {
        return new DirectExchange(RabbitmqKeyConstant.ENROLLMENT_DLX_EXCHANGE, true, false);
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue enrollmentDLXQueue() {
        return QueueBuilder.durable(RabbitmqKeyConstant.ENROLLMENT_DLX_QUEUE).build();
    }

    /**
     * 绑定死信队列
     */
    @Bean
    public Binding enrollmentDLXBinding() {
        return BindingBuilder.bind(enrollmentDLXQueue())
                .to(enrollmentDLXExchange())
                .with(RabbitmqKeyConstant.ENROLLMENT_DLX_ROUTING_KEY);
    }

    /**
     * JSON消息转换器
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * 优化的 RabbitTemplate 配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        // 关键：设置确认模式（替代过时的 publisher-confirms）
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                // 消息成功到达Broker
                if (correlationData != null) {
                    log.debug("消息发送成功: {}", correlationData.getId());
                }
            } else {
                // 消息发送失败
                log.error("消息发送失败: correlationData={}, cause={}", correlationData, cause);
                // 这里可以添加重试逻辑或记录到数据库
            }
        });

        // 关键：设置返回模式（替代过时的 publisher-returns）
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("消息无法路由到队列: 消息被退回 - exchange={}, routingKey={}, replyCode={}, replyText={}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyCode(),
                    returned.getReplyText());
            // 处理无法路由的消息
            handleUndeliveredMessage(returned);
        });

        // 必须设置为true才能触发ReturnsCallback
        rabbitTemplate.setMandatory(true);

        return rabbitTemplate;
    }

    /**
     * 优化的消费者容器工厂配置
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());

        // 并发配置 - 根据您的业务需求调整
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(20);
        factory.setPrefetchCount(10); // 适当增加预取数量提高性能

        // 确认模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        // 添加异常处理
        factory.setErrorHandler(new ConditionalRejectingErrorHandler(
                new CustomExceptionStrategy()
        ));

        return factory;
    }

    /**
     * 处理无法投递的消息
     */
    private void handleUndeliveredMessage(ReturnedMessage returned) {
        // 实现您的死信处理逻辑
        // 例如：记录到数据库、发送到死信队列等
        try {
            log.warn("处理无法投递的消息: {}", new String(returned.getMessage().getBody()));
            // 这里可以保存到数据库或发送到专门的死信队列
        } catch (Exception e) {
            log.error("处理无法投递消息时发生异常", e);
        }
    }

    /**
     * 自定义异常处理策略
     */
    private static class CustomExceptionStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {
        @Override
        public boolean isFatal(Throwable t) {
            if (t instanceof org.springframework.amqp.AmqpConnectException ||
                    t instanceof java.net.ConnectException) {
                log.error("RabbitMQ连接异常", t);
                return true;
            }
            if (t instanceof org.springframework.amqp.AmqpIOException) {
                log.error("RabbitMQ IO异常", t);
                return true;
            }
            return super.isFatal(t);
        }
    }
}