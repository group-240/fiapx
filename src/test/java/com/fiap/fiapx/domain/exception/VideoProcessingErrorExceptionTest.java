package com.fiap.fiapx.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VideoProcessingErrorExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com mensagem")
    void deveCriarExcecaoComMensagem() {
        // Arrange & Act
        VideoProcessingErrorException exception = new VideoProcessingErrorException("Erro no processamento do vídeo");

        // Assert
        assertNotNull(exception);
        assertEquals("Erro no processamento do vídeo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void deveSerUmaRuntimeException() {
        // Arrange & Act
        VideoProcessingErrorException exception = new VideoProcessingErrorException("Erro");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Deve aceitar mensagem vazia")
    void deveAceitarMensagemVazia() {
        // Arrange & Act
        VideoProcessingErrorException exception = new VideoProcessingErrorException("");

        // Assert
        assertNotNull(exception);
        assertEquals("", exception.getMessage());
    }

    @Test
    @DisplayName("Deve aceitar mensagem nula")
    void deveAceitarMensagemNula() {
        // Arrange & Act
        VideoProcessingErrorException exception = new VideoProcessingErrorException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Deve aceitar mensagens longas")
    void deveAceitarMensagensLongas() {
        // Arrange
        String longMessage = "Erro no processamento do vídeo. ".repeat(50);

        // Act
        VideoProcessingErrorException exception = new VideoProcessingErrorException(longMessage);

        // Assert
        assertNotNull(exception);
        assertEquals(longMessage, exception.getMessage());
        assertTrue(exception.getMessage().length() > 100);
    }

    @Test
    @DisplayName("Deve aceitar diferentes mensagens")
    void deveAceitarDiferentesMensagens() {
        // Act
        VideoProcessingErrorException exception1 = new VideoProcessingErrorException("Erro no processamento do vídeo. Não é possível realizar o download.");
        VideoProcessingErrorException exception2 = new VideoProcessingErrorException("Falha no processamento");
        VideoProcessingErrorException exception3 = new VideoProcessingErrorException("Processamento falhou");

        // Assert
        assertEquals("Erro no processamento do vídeo. Não é possível realizar o download.", exception1.getMessage());
        assertEquals("Falha no processamento", exception2.getMessage());
        assertEquals("Processamento falhou", exception3.getMessage());
    }
}
