package com.fiap.fiapx.external.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Serviço de fila de mensagens RabbitMQ
 * Envia mensagens de processamento de vídeo para a fila
 */
@Service
public class MessageQueueService {

    private static final Logger logger = LoggerFactory.getLogger(MessageQueueService.class);
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange:video-processing-exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key:video.processing}")
    private String routingKey;

    public MessageQueueService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendToProcessingQueue(Long capturaId, Long userId, String email, String videoPath, byte[] video) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("id", capturaId);
            message.put("id_user", userId);
            message.put("email", email);
            message.put("videoPath", videoPath);
            message.put("video", video);

            rabbitTemplate.convertAndSend(exchange, routingKey, message);

            logger.info("Mensagem enviada para fila de processamento - ID: {}, User: {}, Video size: {} bytes",
                capturaId, userId, video != null ? video.length : 0);
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem para fila", e);
            throw new RuntimeException("Falha ao enviar para fila de processamento", e);
        }
    }
}


