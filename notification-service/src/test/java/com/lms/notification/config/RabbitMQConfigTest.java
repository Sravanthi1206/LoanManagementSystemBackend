package com.lms.notification.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@DisplayName("RabbitMQ Config Tests")
class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    @DisplayName("Bean creation")
    void testBeanCreation() {
        TopicExchange exchange = config.notificationExchange();
        Queue queue = config.notificationQueue();
        assertNotNull(exchange);
        assertNotNull(queue);

        Binding binding = config.notificationBinding(queue, exchange);
        assertNotNull(binding);
        
        MessageConverter converter = config.jsonMessageConverter();
        assertNotNull(converter);
        
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        RabbitTemplate template = config.rabbitTemplate(connectionFactory);
        assertNotNull(template);
    }
}
