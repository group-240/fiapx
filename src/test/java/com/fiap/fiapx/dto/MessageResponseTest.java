package com.fiap.fiapx.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MessageResponseTest {

    @Test
    @DisplayName("Deve criar response com construtor padrão")
    void deveCriarResponseComConstrutorPadrao() {
        // Act
        MessageResponse response = new MessageResponse();

        // Assert
        assertNotNull(response);
        assertNull(response.getMessage());
        assertNull(response.getTimestamp());
    }

    @Test
    @DisplayName("Deve criar response com todos os argumentos")
    void deveCriarResponseComTodosArgumentos() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        MessageResponse response = new MessageResponse("Hello FIAPX!", now);

        // Assert
        assertNotNull(response);
        assertEquals("Hello FIAPX!", response.getMessage());
        assertEquals(now, response.getTimestamp());
    }

    @Test
    @DisplayName("Deve permitir definir e obter mensagem")
    void devePermitirDefinirEObterMensagem() {
        // Arrange
        MessageResponse response = new MessageResponse();

        // Act
        response.setMessage("Test message");

        // Assert
        assertEquals("Test message", response.getMessage());
    }

    @Test
    @DisplayName("Deve permitir definir e obter timestamp")
    void devePermitirDefinirEObterTimestamp() {
        // Arrange
        MessageResponse response = new MessageResponse();
        LocalDateTime now = LocalDateTime.now();

        // Act
        response.setTimestamp(now);

        // Assert
        assertEquals(now, response.getTimestamp());
    }

    @Test
    @DisplayName("Deve aceitar mensagem vazia")
    void deveAceitarMensagemVazia() {
        // Act
        MessageResponse response = new MessageResponse("", LocalDateTime.now());

        // Assert
        assertEquals("", response.getMessage());
    }

    @Test
    @DisplayName("Deve aceitar diferentes mensagens")
    void deveAceitarDiferentesMensagens() {
        // Arrange & Act & Assert
        MessageResponse response1 = new MessageResponse("Hello", LocalDateTime.now());
        assertEquals("Hello", response1.getMessage());

        MessageResponse response2 = new MessageResponse("Goodbye", LocalDateTime.now());
        assertEquals("Goodbye", response2.getMessage());

        MessageResponse response3 = new MessageResponse("Test 123", LocalDateTime.now());
        assertEquals("Test 123", response3.getMessage());
    }

    @Test
    @DisplayName("Deve preservar timestamp exato")
    void devePreservarTimestampExato() {
        // Arrange
        LocalDateTime specificTime = LocalDateTime.of(2026, 2, 6, 10, 30, 45);

        // Act
        MessageResponse response = new MessageResponse("Message", specificTime);

        // Assert
        assertEquals(specificTime, response.getTimestamp());
        assertEquals(2026, response.getTimestamp().getYear());
        assertEquals(2, response.getTimestamp().getMonthValue());
        assertEquals(6, response.getTimestamp().getDayOfMonth());
    }
}
