package com.fiap.fiapx.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RabbitMQConfigTest {

    private RabbitMQConfig rabbitMQConfig;

    @BeforeEach
    void setUp() {
        rabbitMQConfig = new RabbitMQConfig();
        ReflectionTestUtils.setField(rabbitMQConfig, "queueName", "video-processing-queue");
        ReflectionTestUtils.setField(rabbitMQConfig, "exchangeName", "video-processing-exchange");
        ReflectionTestUtils.setField(rabbitMQConfig, "routingKey", "video.processing");
    }

    @Test
    @DisplayName("Deve criar queue com nome correto")
    void deveCriarQueueComNomeCorreto() {
        // Act
        Queue queue = rabbitMQConfig.queue();

        // Assert
        assertNotNull(queue);
        assertEquals("video-processing-queue", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    @DisplayName("Deve criar exchange com nome correto")
    void deveCriarExchangeComNomeCorreto() {
        // Act
        TopicExchange exchange = rabbitMQConfig.exchange();

        // Assert
        assertNotNull(exchange);
        assertEquals("video-processing-exchange", exchange.getName());
    }

    @Test
    @DisplayName("Deve criar binding entre queue e exchange")
    void deveCriarBindingEntreQueueEExchange() {
        // Arrange
        Queue queue = rabbitMQConfig.queue();
        TopicExchange exchange = rabbitMQConfig.exchange();

        // Act
        Binding binding = rabbitMQConfig.binding(queue, exchange);

        // Assert
        assertNotNull(binding);
        assertEquals("video-processing-queue", binding.getDestination());
        assertEquals("video.processing", binding.getRoutingKey());
    }

    @Test
    @DisplayName("Deve criar message converter JSON")
    void deveCriarMessageConverterJson() {
        // Act
        Jackson2JsonMessageConverter converter = rabbitMQConfig.messageConverter();

        // Assert
        assertNotNull(converter);
        assertInstanceOf(Jackson2JsonMessageConverter.class, converter);
    }

    @Test
    @DisplayName("Deve criar RabbitTemplate com converter configurado")
    void deveCriarRabbitTemplateComConverterConfigurado() {
        // Arrange
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);

        // Act
        RabbitTemplate rabbitTemplate = rabbitMQConfig.rabbitTemplate(connectionFactory);

        // Assert
        assertNotNull(rabbitTemplate);
        assertNotNull(rabbitTemplate.getMessageConverter());
        assertInstanceOf(Jackson2JsonMessageConverter.class, rabbitTemplate.getMessageConverter());
    }

    @Test
    @DisplayName("Deve criar queue durável")
    void deveCriarQueueDuravel() {
        // Act
        Queue queue = rabbitMQConfig.queue();

        // Assert
        assertTrue(queue.isDurable());
        assertFalse(queue.isAutoDelete());
    }

    @Test
    @DisplayName("Deve criar exchange do tipo topic")
    void deveCriarExchangeDoTipoTopic() {
        // Act
        TopicExchange exchange = rabbitMQConfig.exchange();

        // Assert
        assertEquals("topic", exchange.getType());
    }

    @Test
    @DisplayName("Deve aceitar configuração com valores customizados")
    void deveAceitarConfiguracaoComValoresCustomizados() {
        // Arrange
        RabbitMQConfig customConfig = new RabbitMQConfig();
        ReflectionTestUtils.setField(customConfig, "queueName", "custom-queue");
        ReflectionTestUtils.setField(customConfig, "exchangeName", "custom-exchange");
        ReflectionTestUtils.setField(customConfig, "routingKey", "custom.routing");

        // Act
        Queue queue = customConfig.queue();
        TopicExchange exchange = customConfig.exchange();
        Binding binding = customConfig.binding(queue, exchange);

        // Assert
        assertEquals("custom-queue", queue.getName());
        assertEquals("custom-exchange", exchange.getName());
        assertEquals("custom.routing", binding.getRoutingKey());
    }
}

