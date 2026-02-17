package com.fiap.fiapx.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnauthorizedAccessExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com mensagem personalizada")
    void deveCriarExcecaoComMensagemPersonalizada() {
        // Arrange
        String message = "Você não tem permissão para acessar esta captura";

        // Act
        UnauthorizedAccessException exception = new UnauthorizedAccessException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void deveSerUmaRuntimeException() {
        // Arrange & Act
        UnauthorizedAccessException exception = new UnauthorizedAccessException("Acesso negado");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Deve aceitar diferentes mensagens")
    void deveAceitarDiferentesMensagens() {
        // Act
        UnauthorizedAccessException exception1 = new UnauthorizedAccessException("Acesso negado");
        UnauthorizedAccessException exception2 = new UnauthorizedAccessException("Usuário não autorizado");
        UnauthorizedAccessException exception3 = new UnauthorizedAccessException("Permissão insuficiente");

        // Assert
        assertEquals("Acesso negado", exception1.getMessage());
        assertEquals("Usuário não autorizado", exception2.getMessage());
        assertEquals("Permissão insuficiente", exception3.getMessage());
    }

    @Test
    @DisplayName("Deve aceitar mensagem vazia")
    void deveAceitarMensagemVazia() {
        // Act
        UnauthorizedAccessException exception = new UnauthorizedAccessException("");

        // Assert
        assertNotNull(exception);
        assertEquals("", exception.getMessage());
    }

    @Test
    @DisplayName("Deve aceitar mensagem nula")
    void deveAceitarMensagemNula() {
        // Act
        UnauthorizedAccessException exception = new UnauthorizedAccessException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }
}
