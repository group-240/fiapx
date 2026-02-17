package com.fiap.fiapx.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class InvalidFileExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com mensagem")
    void deveCriarExcecaoComMensagem() {
        // Arrange
        String message = "Arquivo inválido";

        // Act
        InvalidFileException exception = new InvalidFileException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve criar exceção com mensagem e causa")
    void deveCriarExcecaoComMensagemECausa() {
        // Arrange
        String message = "Erro ao processar arquivo";
        IOException cause = new IOException("Erro de I/O");

        // Act
        InvalidFileException exception = new InvalidFileException(message, cause);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void deveSerUmaRuntimeException() {
        // Arrange & Act
        InvalidFileException exception = new InvalidFileException("Erro");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Deve aceitar diferentes mensagens")
    void deveAceitarDiferentesMensagens() {
        // Act
        InvalidFileException exception1 = new InvalidFileException("Arquivo muito grande");
        InvalidFileException exception2 = new InvalidFileException("Tipo de arquivo não suportado");
        InvalidFileException exception3 = new InvalidFileException("Arquivo corrompido");

        // Assert
        assertEquals("Arquivo muito grande", exception1.getMessage());
        assertEquals("Tipo de arquivo não suportado", exception2.getMessage());
        assertEquals("Arquivo corrompido", exception3.getMessage());
    }

    @Test
    @DisplayName("Deve aceitar diferentes tipos de causa")
    void deveAceitarDiferentesTiposDeCausa() {
        // Act
        InvalidFileException exception1 = new InvalidFileException("Erro 1", new IOException("IO Error"));
        InvalidFileException exception2 = new InvalidFileException("Erro 2", new RuntimeException("Runtime Error"));
        InvalidFileException exception3 = new InvalidFileException("Erro 3", new IllegalArgumentException("Argumento inválido"));

        // Assert
        assertTrue(exception1.getCause() instanceof IOException);
        assertTrue(exception2.getCause() instanceof RuntimeException);
        assertTrue(exception3.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("Deve aceitar mensagem vazia")
    void deveAceitarMensagemVazia() {
        // Act
        InvalidFileException exception = new InvalidFileException("");

        // Assert
        assertNotNull(exception);
        assertEquals("", exception.getMessage());
    }

    @Test
    @DisplayName("Deve aceitar mensagem nula")
    void deveAceitarMensagemNula() {
        // Act
        InvalidFileException exception = new InvalidFileException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Deve aceitar causa nula no construtor com causa")
    void deveAceitarCausaNulaNoConstrutorComCausa() {
        // Act
        InvalidFileException exception = new InvalidFileException("Mensagem", null);

        // Assert
        assertNotNull(exception);
        assertEquals("Mensagem", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve preservar causa original")
    void devePreservarCausaOriginal() {
        // Arrange
        String causeMessage = "Causa raiz do erro";
        IOException cause = new IOException(causeMessage);

        // Act
        InvalidFileException exception = new InvalidFileException("Erro ao processar", cause);

        // Assert
        assertEquals(cause, exception.getCause());
        assertEquals(causeMessage, exception.getCause().getMessage());
    }
}
