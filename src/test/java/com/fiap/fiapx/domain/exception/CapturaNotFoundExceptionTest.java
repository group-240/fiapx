package com.fiap.fiapx.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CapturaNotFoundExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com mensagem formatada corretamente")
    void deveCriarExcecaoComMensagemFormatada() {
        // Arrange
        Long capturaId = 1L;

        // Act
        CapturaNotFoundException exception = new CapturaNotFoundException(capturaId);

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Captura com ID 1 não encontrada"));
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void deveSerUmaRuntimeException() {
        // Arrange & Act
        CapturaNotFoundException exception = new CapturaNotFoundException(1L);

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Deve criar exceção com diferentes IDs")
    void deveCriarExcecaoComDiferentesIDs() {
        // Act
        CapturaNotFoundException exception1 = new CapturaNotFoundException(1L);
        CapturaNotFoundException exception2 = new CapturaNotFoundException(99L);
        CapturaNotFoundException exception3 = new CapturaNotFoundException(1000L);

        // Assert
        assertTrue(exception1.getMessage().contains("1"));
        assertTrue(exception2.getMessage().contains("99"));
        assertTrue(exception3.getMessage().contains("1000"));
    }

    @Test
    @DisplayName("Deve ter mensagem válida")
    void deveTerMensagemValida() {
        // Act
        CapturaNotFoundException exception = new CapturaNotFoundException(5L);

        // Assert
        assertNotNull(exception.getMessage());
        assertFalse(exception.getMessage().isEmpty());
    }
}
