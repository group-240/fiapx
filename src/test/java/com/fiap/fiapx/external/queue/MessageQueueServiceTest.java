package com.fiap.fiapx.external.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageQueueServiceTest {

    private MessageQueueService messageQueueService;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        messageQueueService = new MessageQueueService(new ObjectMapper());
    }

    @Test
    @DisplayName("Deve enviar mensagem para fila com sucesso")
    void deveEnviarMensagemParaFilaComRecesso() {
        // Arrange
        Long capturaId = 1L;
        Long userId = 1L;
        String email = "user@fiap.com.br";
        String videoPath = "./uploads/capturas/uuid.mp4";

        // Act & Assert
        assertDoesNotThrow(() -> {
            messageQueueService.sendToProcessingQueue(capturaId, userId, email, videoPath);
        });
    }

    @Test
    @DisplayName("Deve enviar mensagem com todos os parâmetros corretos")
    void deveEnviarMensagemComTodosParametrosCorretos() {
        // Arrange
        Long capturaId = 10L;
        Long userId = 5L;
        String email = "test@fiap.com.br";
        String videoPath = "./uploads/test.mp4";

        // Act & Assert
        assertDoesNotThrow(() -> {
            messageQueueService.sendToProcessingQueue(capturaId, userId, email, videoPath);
        });
    }

    @Test
    @DisplayName("Deve processar mensagem de forma assíncrona")
    void deveProcessarMensagemDeFormaAssincrona() throws InterruptedException {
        // Arrange
        Long capturaId = 1L;
        Long userId = 1L;
        String email = "user@fiap.com.br";
        String videoPath = "./uploads/capturas/uuid.mp4";

        // Act
        long startTime = System.currentTimeMillis();
        messageQueueService.sendToProcessingQueue(capturaId, userId, email, videoPath);
        long endTime = System.currentTimeMillis();

        // Assert
        // O método deve retornar rapidamente (em menos de 1 segundo)
        // pois processa de forma assíncrona
        long executionTime = endTime - startTime;
        assert executionTime < 1000;
    }

    @Test
    @DisplayName("Deve aceitar diferentes IDs de captura")
    void deveAceitarDiferentesIDsDeCaptura() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            messageQueueService.sendToProcessingQueue(1L, 1L, "user1@fiap.com.br", "./video1.mp4");
            messageQueueService.sendToProcessingQueue(2L, 1L, "user1@fiap.com.br", "./video2.mp4");
            messageQueueService.sendToProcessingQueue(3L, 2L, "user2@fiap.com.br", "./video3.mp4");
        });
    }

    @Test
    @DisplayName("Deve aceitar emails diferentes")
    void deveAceitarEmailsDiferentes() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            messageQueueService.sendToProcessingQueue(1L, 1L, "user1@fiap.com.br", "./video.mp4");
            messageQueueService.sendToProcessingQueue(2L, 2L, "user2@example.com", "./video.mp4");
            messageQueueService.sendToProcessingQueue(3L, 3L, "test@domain.com", "./video.mp4");
        });
    }

    @Test
    @DisplayName("Deve aceitar caminhos de arquivo diferentes")
    void deveAceitarCaminhosDeArquivoDiferentes() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            messageQueueService.sendToProcessingQueue(1L, 1L, "user@fiap.com.br", "./uploads/video1.mp4");
            messageQueueService.sendToProcessingQueue(2L, 1L, "user@fiap.com.br", "/var/uploads/video2.mp4");
            messageQueueService.sendToProcessingQueue(3L, 1L, "user@fiap.com.br", "C:\\uploads\\video3.mp4");
        });
    }

    @Test
    @DisplayName("Deve enviar múltiplas mensagens sem erro")
    void deveEnviarMultiplasMensagensSemErro() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            for (int i = 1; i <= 10; i++) {
                messageQueueService.sendToProcessingQueue(
                        (long) i,
                        1L,
                        "user@fiap.com.br",
                        "./uploads/video" + i + ".mp4"
                );
            }
        });
    }

    @Test
    @DisplayName("Deve aceitar userId zero")
    void deveAceitarUserIdZero() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            messageQueueService.sendToProcessingQueue(1L, 0L, "user@fiap.com.br", "./video.mp4");
        });
    }

    @Test
    @DisplayName("Deve aceitar capturaId zero")
    void deveAceitarCapturaIdZero() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            messageQueueService.sendToProcessingQueue(0L, 1L, "user@fiap.com.br", "./video.mp4");
        });
    }

    @Test
    @DisplayName("Deve aceitar email vazio")
    void deveAceitarEmailVazio() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            messageQueueService.sendToProcessingQueue(1L, 1L, "", "./video.mp4");
        });
    }

    @Test
    @DisplayName("Deve aceitar videoPath vazio")
    void deveAceitarVideoPathVazio() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            messageQueueService.sendToProcessingQueue(1L, 1L, "user@fiap.com.br", "");
        });
    }
}
