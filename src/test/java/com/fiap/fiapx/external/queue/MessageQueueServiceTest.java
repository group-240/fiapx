package com.fiap.fiapx.external.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageQueueServiceTest {

    private MessageQueueService messageQueueService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        messageQueueService = new MessageQueueService(rabbitTemplate, objectMapper);

        // Configurar valores das propriedades
        ReflectionTestUtils.setField(messageQueueService, "exchange", "video-processing-exchange");
        ReflectionTestUtils.setField(messageQueueService, "routingKey", "video.processing");
    }

    @Test
    @DisplayName("Deve enviar mensagem para fila com sucesso")
    void deveEnviarMensagemParaFilaComSucesso() {
        // Arrange
        Long capturaId = 1L;
        Long userId = 1L;
        String email = "user@fiap.com.br";
        String videoPath = "./uploads/capturas/uuid.mp4";
        byte[] video = "fake video content".getBytes();

        // Act
        assertDoesNotThrow(() -> {
            messageQueueService.sendToProcessingQueue(capturaId, userId, email, videoPath, video);
        });

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("video-processing-exchange"),
                eq("video.processing"),
                any(Map.class)
        );
    }

    @Test
    @DisplayName("Deve enviar mensagem com todos os parâmetros corretos")
    void deveEnviarMensagemComTodosParametrosCorretos() {
        // Arrange
        Long capturaId = 10L;
        Long userId = 5L;
        String email = "test@fiap.com.br";
        String videoPath = "./uploads/test.mp4";
        byte[] video = "test video content".getBytes();

        ArgumentCaptor<Map> messageCaptor = ArgumentCaptor.forClass(Map.class);

        // Act
        messageQueueService.sendToProcessingQueue(capturaId, userId, email, videoPath, video);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("video-processing-exchange"),
                eq("video.processing"),
                messageCaptor.capture()
        );

        Map<String, Object> message = messageCaptor.getValue();
        assertEquals(capturaId, message.get("id"));
        assertEquals(userId, message.get("id_user"));
        assertEquals(email, message.get("email"));
        assertEquals(videoPath, message.get("videoPath"));
        assertArrayEquals(video, (byte[]) message.get("video"));
    }

    @Test
    @DisplayName("Deve enviar mensagem síncrona")
    void deveEnviarMensagemSincrona() {
        // Arrange
        Long capturaId = 1L;
        Long userId = 1L;
        String email = "user@fiap.com.br";
        String videoPath = "./uploads/capturas/uuid.mp4";
        byte[] video = "video data".getBytes();

        // Act
        messageQueueService.sendToProcessingQueue(capturaId, userId, email, videoPath, video);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("Deve aceitar diferentes IDs de captura")
    void deveAceitarDiferentesIDsDeCaptura() {
        // Arrange
        byte[] video = "video content".getBytes();

        // Act
        messageQueueService.sendToProcessingQueue(1L, 1L, "user1@fiap.com.br", "./video1.mp4", video);
        messageQueueService.sendToProcessingQueue(2L, 1L, "user1@fiap.com.br", "./video2.mp4", video);
        messageQueueService.sendToProcessingQueue(3L, 2L, "user2@fiap.com.br", "./video3.mp4", video);

        // Assert
        verify(rabbitTemplate, times(3)).convertAndSend(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("Deve aceitar emails diferentes")
    void deveAceitarEmailsDiferentes() {
        // Arrange
        byte[] video = "video content".getBytes();

        // Act
        messageQueueService.sendToProcessingQueue(1L, 1L, "user1@fiap.com.br", "./video.mp4", video);
        messageQueueService.sendToProcessingQueue(2L, 2L, "user2@example.com", "./video.mp4", video);
        messageQueueService.sendToProcessingQueue(3L, 3L, "test@domain.com", "./video.mp4", video);

        // Assert
        verify(rabbitTemplate, times(3)).convertAndSend(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("Deve aceitar caminhos de arquivo diferentes")
    void deveAceitarCaminhosDeArquivoDiferentes() {
        // Arrange
        byte[] video = "video content".getBytes();

        // Act
        messageQueueService.sendToProcessingQueue(1L, 1L, "user@fiap.com.br", "./uploads/video1.mp4", video);
        messageQueueService.sendToProcessingQueue(2L, 1L, "user@fiap.com.br", "/var/uploads/video2.mp4", video);
        messageQueueService.sendToProcessingQueue(3L, 1L, "user@fiap.com.br", "C:\\uploads\\video3.mp4", video);

        // Assert
        verify(rabbitTemplate, times(3)).convertAndSend(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("Deve enviar múltiplas mensagens sem erro")
    void deveEnviarMultiplasMensagensSemErro() {
        // Arrange
        byte[] video = "video content".getBytes();

        // Act
        for (int i = 1; i <= 10; i++) {
            messageQueueService.sendToProcessingQueue(
                    (long) i,
                    1L,
                    "user@fiap.com.br",
                    "./uploads/video" + i + ".mp4",
                    video
            );
        }

        // Assert
        verify(rabbitTemplate, times(10)).convertAndSend(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("Deve aceitar userId zero")
    void deveAceitarUserIdZero() {
        // Arrange
        byte[] video = "video content".getBytes();

        // Act
        messageQueueService.sendToProcessingQueue(1L, 0L, "user@fiap.com.br", "./video.mp4", video);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("Deve aceitar capturaId zero")
    void deveAceitarCapturaIdZero() {
        // Arrange
        byte[] video = "video content".getBytes();

        // Act
        messageQueueService.sendToProcessingQueue(0L, 1L, "user@fiap.com.br", "./video.mp4", video);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("Deve aceitar email vazio")
    void deveAceitarEmailVazio() {
        // Arrange
        byte[] video = "video content".getBytes();

        // Act
        messageQueueService.sendToProcessingQueue(1L, 1L, "", "./video.mp4", video);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("Deve aceitar videoPath vazio")
    void deveAceitarVideoPathVazio() {
        // Arrange
        byte[] video = "video content".getBytes();

        // Act
        messageQueueService.sendToProcessingQueue(1L, 1L, "user@fiap.com.br", "", video);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando RabbitTemplate falhar")
    void deveLancarExcecaoQuandoRabbitTemplateFalhar() {
        // Arrange
        Long capturaId = 1L;
        Long userId = 1L;
        String email = "user@fiap.com.br";
        String videoPath = "./uploads/capturas/uuid.mp4";
        byte[] video = "video content".getBytes();

        doThrow(new RuntimeException("Erro ao conectar com RabbitMQ"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            messageQueueService.sendToProcessingQueue(capturaId, userId, email, videoPath, video);
        });

        assertTrue(exception.getMessage().contains("Falha ao enviar para fila de processamento"));
    }

    @Test
    @DisplayName("Deve incluir todos os campos na mensagem")
    void deveIncluirTodosCamposNaMensagem() {
        // Arrange
        Long capturaId = 99L;
        Long userId = 88L;
        String email = "complete@test.com";
        String videoPath = "/path/to/video.mp4";
        byte[] video = "complete video data".getBytes();

        ArgumentCaptor<Map> messageCaptor = ArgumentCaptor.forClass(Map.class);

        // Act
        messageQueueService.sendToProcessingQueue(capturaId, userId, email, videoPath, video);

        // Assert
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), messageCaptor.capture());

        Map<String, Object> message = messageCaptor.getValue();
        assertEquals(5, message.size());
        assertTrue(message.containsKey("id"));
        assertTrue(message.containsKey("id_user"));
        assertTrue(message.containsKey("email"));
        assertTrue(message.containsKey("videoPath"));
        assertTrue(message.containsKey("video"));
    }
}
