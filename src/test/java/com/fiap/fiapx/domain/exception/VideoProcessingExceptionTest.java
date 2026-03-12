package com.fiap.fiapx.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VideoProcessingExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com mensagem")
    void deveCriarExcecaoComMensagem() {
        // Arrange & Act
        VideoProcessingException exception = new VideoProcessingException("Vídeo em processamento");

        // Assert
        assertNotNull(exception);
        assertEquals("Vídeo em processamento", exception.getMessage());
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void deveSerUmaRuntimeException() {
        // Arrange & Act
        VideoProcessingException exception = new VideoProcessingException("Erro");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Deve aceitar mensagem vazia")
    void deveAceitarMensagemVazia() {
        // Arrange & Act
        VideoProcessingException exception = new VideoProcessingException("");

        // Assert
        assertNotNull(exception);
        assertEquals("", exception.getMessage());
    }

    @Test
    @DisplayName("Deve aceitar mensagem nula")
    void deveAceitarMensagemNula() {
        // Arrange & Act
        VideoProcessingException exception = new VideoProcessingException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Deve aceitar mensagens longas")
    void deveAceitarMensagensLongas() {
        // Arrange
        String longMessage = "Vídeo em processamento. ".repeat(50);

        // Act
        VideoProcessingException exception = new VideoProcessingException(longMessage);

        // Assert
        assertNotNull(exception);
        assertEquals(longMessage, exception.getMessage());
        assertTrue(exception.getMessage().length() > 100);
    }

    @Test
    @DisplayName("Deve aceitar diferentes mensagens")
    void deveAceitarDiferentesMensagens() {
        // Act
        VideoProcessingException exception1 = new VideoProcessingException("Vídeo em processamento ainda. Por favor, aguarde a conclusão.");
        VideoProcessingException exception2 = new VideoProcessingException("Processamento em andamento");
        VideoProcessingException exception3 = new VideoProcessingException("Aguarde o processamento");

        // Assert
        assertEquals("Vídeo em processamento ainda. Por favor, aguarde a conclusão.", exception1.getMessage());
        assertEquals("Processamento em andamento", exception2.getMessage());
        assertEquals("Aguarde o processamento", exception3.getMessage());
    }
}
