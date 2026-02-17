package com.fiap.fiapx.external.api;

import com.fiap.fiapx.domain.exception.CapturaNotFoundException;
import com.fiap.fiapx.domain.exception.InvalidFileException;
import com.fiap.fiapx.domain.exception.UnauthorizedAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Deve tratar CapturaNotFoundException corretamente")
    void deveTratarCapturaNotFoundException() {
        // Arrange
        CapturaNotFoundException exception = new CapturaNotFoundException(1L);

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleCapturaNotFound(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Not Found", response.getBody().get("error"));
        assertTrue(response.getBody().get("message").toString().contains("Captura com ID 1 não encontrada"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar InvalidFileException corretamente")
    void deveTratarInvalidFileException() {
        // Arrange
        InvalidFileException exception = new InvalidFileException("Arquivo muito grande");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidFile(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Arquivo muito grande", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar UnauthorizedAccessException corretamente")
    void deveTratarUnauthorizedAccessException() {
        // Arrange
        UnauthorizedAccessException exception = new UnauthorizedAccessException(
                "Você não tem permissão para acessar esta captura"
        );

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleUnauthorizedAccess(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().get("status"));
        assertEquals("Forbidden", response.getBody().get("error"));
        assertEquals("Você não tem permissão para acessar esta captura", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException corretamente")
    void deveTratarIllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Argumento inválido", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar Exception genérica corretamente")
    void deveTratarExceptionGenerica() {
        // Arrange
        Exception exception = new Exception("Erro inesperado");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("Internal Server Error", response.getBody().get("error"));
        assertTrue(response.getBody().get("message").toString().contains("Erro interno no servidor"));
        assertTrue(response.getBody().get("message").toString().contains("Erro inesperado"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar RuntimeException como Exception genérica")
    void deveTratarRuntimeExceptionComoExceptionGenerica() {
        // Arrange
        RuntimeException exception = new RuntimeException("Erro em tempo de execução");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").toString().contains("Erro em tempo de execução"));
    }
}
