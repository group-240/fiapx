package com.fiap.fiapx.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExternalServiceUnavailableExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com mensagem")
    void deveCriarExcecaoComMensagem() {
        // Arrange & Act
        ExternalServiceUnavailableException exception = new ExternalServiceUnavailableException("Serviço indisponível");

        // Assert
        assertNotNull(exception);
        assertEquals("Serviço indisponível", exception.getMessage());
    }

    @Test
    @DisplayName("Deve criar exceção com mensagem e causa")
    void deveCriarExcecaoComMensagemECausa() {
        // Arrange
        Throwable cause = new RuntimeException("Causa raiz");

        // Act
        ExternalServiceUnavailableException exception = new ExternalServiceUnavailableException("Serviço indisponível", cause);

        // Assert
        assertNotNull(exception);
        assertEquals("Serviço indisponível", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("Causa raiz", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void deveSerUmaRuntimeException() {
        // Arrange & Act
        ExternalServiceUnavailableException exception = new ExternalServiceUnavailableException("Erro");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Deve aceitar mensagem vazia")
    void deveAceitarMensagemVazia() {
        // Arrange & Act
        ExternalServiceUnavailableException exception = new ExternalServiceUnavailableException("");

        // Assert
        assertNotNull(exception);
        assertEquals("", exception.getMessage());
    }

    @Test
    @DisplayName("Deve aceitar mensagem nula")
    void deveAceitarMensagemNula() {
        // Arrange & Act
        ExternalServiceUnavailableException exception = new ExternalServiceUnavailableException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Deve aceitar causa nula")
    void deveAceitarCausaNula() {
        // Arrange & Act
        ExternalServiceUnavailableException exception = new ExternalServiceUnavailableException("Erro", null);

        // Assert
        assertNotNull(exception);
        assertEquals("Erro", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve aceitar mensagens longas")
    void deveAceitarMensagensLongas() {
        // Arrange
        String longMessage = "O serviço de processamento de vídeos está temporariamente indisponível. ".repeat(10);

        // Act
        ExternalServiceUnavailableException exception = new ExternalServiceUnavailableException(longMessage);

        // Assert
        assertNotNull(exception);
        assertEquals(longMessage, exception.getMessage());
        assertTrue(exception.getMessage().length() > 100);
    }

    @Test
    @DisplayName("Deve aceitar diferentes mensagens")
    void deveAceitarDiferentesMensagens() {
        // Act
        ExternalServiceUnavailableException exception1 = new ExternalServiceUnavailableException("O serviço de processamento de vídeos está temporariamente indisponível.");
        ExternalServiceUnavailableException exception2 = new ExternalServiceUnavailableException("Serviço externo não disponível");
        ExternalServiceUnavailableException exception3 = new ExternalServiceUnavailableException("Erro de comunicação com o serviço");

        // Assert
        assertEquals("O serviço de processamento de vídeos está temporariamente indisponível.", exception1.getMessage());
        assertEquals("Serviço externo não disponível", exception2.getMessage());
        assertEquals("Erro de comunicação com o serviço", exception3.getMessage());
    }

    @Test
    @DisplayName("Deve preservar a causa original")
    void devePreservarCausaOriginal() {
        // Arrange
        Exception originalCause = new IllegalStateException("Estado inválido");

        // Act
        ExternalServiceUnavailableException exception = new ExternalServiceUnavailableException("Serviço indisponível", originalCause);

        // Assert
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("Estado inválido", exception.getCause().getMessage());
    }
}
