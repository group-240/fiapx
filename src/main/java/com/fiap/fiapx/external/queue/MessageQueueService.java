package com.fiap.fiapx.external.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessageQueueService {

    private static final Logger logger = LoggerFactory.getLogger(MessageQueueService.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    public MessageQueueService(SqsClient sqsClient, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    public void sendToProcessingQueue(Long capturaId, Long userId, String email, String s3Key) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", capturaId);
            payload.put("userId", userId);
            payload.put("email", email);
            payload.put("s3Key", s3Key);

            String messageBody = objectMapper.writeValueAsString(payload);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .messageGroupId("video-processing")
                    .messageDeduplicationId(capturaId.toString())
                    .build();

            sqsClient.sendMessage(request);
            logger.info("Mensagem enviada para SQS — capturaId={} s3Key={}", capturaId, s3Key);

        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem para SQS — capturaId={}: {}", capturaId, e.getMessage());
            throw new RuntimeException("Falha ao enfileirar vídeo para processamento", e);
        }
    }
}
