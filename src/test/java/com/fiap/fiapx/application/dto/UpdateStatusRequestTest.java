package com.fiap.fiapx.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdateStatusRequestTest {

    @Test
    @DisplayName("Deve criar request com construtor padrão")
    void deveCriarRequestComConstrutorPadrao() {
        // Act
        UpdateStatusRequest request = new UpdateStatusRequest();

        // Assert
        assertNotNull(request);
        assertNull(request.getStatus());
    }

    @Test
    @DisplayName("Deve criar request com todos os argumentos")
    void deveCriarRequestComTodosArgumentos() {
        // Act
        UpdateStatusRequest request = new UpdateStatusRequest("CONCLUIDO");

        // Assert
        assertNotNull(request);
        assertEquals("CONCLUIDO", request.getStatus());
    }

    @Test
    @DisplayName("Deve permitir definir e obter status")
    void devePermitirDefinirEObterStatus() {
        // Arrange
        UpdateStatusRequest request = new UpdateStatusRequest();

        // Act
        request.setStatus("PROCESSANDO");

        // Assert
        assertEquals("PROCESSANDO", request.getStatus());
    }

    @Test
    @DisplayName("Deve aceitar diferentes valores de status")
    void deveAceitarDiferentesValoresDeStatus() {
        // Arrange & Act & Assert
        UpdateStatusRequest request1 = new UpdateStatusRequest("PENDENTE");
        assertEquals("PENDENTE", request1.getStatus());

        UpdateStatusRequest request2 = new UpdateStatusRequest("PROCESSANDO");
        assertEquals("PROCESSANDO", request2.getStatus());

        UpdateStatusRequest request3 = new UpdateStatusRequest("CONCLUIDO");
        assertEquals("CONCLUIDO", request3.getStatus());

        UpdateStatusRequest request4 = new UpdateStatusRequest("ERRO");
        assertEquals("ERRO", request4.getStatus());
    }

    @Test
    @DisplayName("Deve aceitar status em minúsculas")
    void deveAceitarStatusEmMinusculas() {
        // Act
        UpdateStatusRequest request = new UpdateStatusRequest("concluido");

        // Assert
        assertEquals("concluido", request.getStatus());
    }
}
