package com.fiap.fiapx.external.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessageQueueService {

    private static final Logger logger = LoggerFactory.getLogger(MessageQueueService.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange:video-processing-exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key:video.processing}")
    private String routingKey;

    public MessageQueueService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendToProcessingQueue(Long capturaId, Long userId, String email, String s3Key) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", capturaId);
            payload.put("userId", userId);
            payload.put("email", email);
            payload.put("s3Key", s3Key);

            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
            logger.info("Mensagem enviada para RabbitMQ — capturaId={} s3Key={}", capturaId, s3Key);

        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem para RabbitMQ — capturaId={}: {}", capturaId, e.getMessage());
            throw new RuntimeException("Falha ao enfileirar vídeo para processamento", e);
        }
    }
}
