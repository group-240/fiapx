package com.fiap.fiapx.external.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço de fila de mensagens
 * Implementação inicial simples que processa de forma assíncrona
 * Preparado para migração futura para SQS/RabbitMQ
 */
@Service
public class MessageQueueService {

    private static final Logger logger = LoggerFactory.getLogger(MessageQueueService.class);
    private final ObjectMapper objectMapper;

    public MessageQueueService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void sendToProcessingQueue(Long capturaId, Long userId, String email, String videoPath) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", capturaId);
        payload.put("id_user", userId);
        payload.put("email", email);
        payload.put("video", videoPath);

        // Processar de forma assíncrona (simulando fila)
        CompletableFuture.runAsync(() -> {
            try {
                String jsonPayload = objectMapper.writeValueAsString(payload);
                logger.info("Mensagem enviada para fila de processamento: {}", jsonPayload);

                // Simular processamento
                Thread.sleep(2000);
                logger.info("Processamento concluído para captura ID: {}", capturaId);

            } catch (Exception e) {
                logger.error("Erro ao processar mensagem da fila", e);
            }
        });
    }
}
