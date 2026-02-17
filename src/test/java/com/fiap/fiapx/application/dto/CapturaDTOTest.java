package com.fiap.fiapx.application.dto;

import com.fiap.fiapx.domain.entities.CapturaStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CapturaDTOTest {

    @Test
    @DisplayName("Deve criar DTO com construtor padrão")
    void deveCriarDTOComConstrutorPadrao() {
        // Act
        CapturaDTO dto = new CapturaDTO();

        // Assert
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getIdUser());
        assertNull(dto.getEmail());
        assertNull(dto.getStatus());
        assertNull(dto.getPath());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve criar DTO com todos os argumentos")
    void deveCriarDTOComTodosArgumentos() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        CapturaDTO dto = new CapturaDTO(
                1L,
                1L,
                "user@fiap.com.br",
                CapturaStatus.PROCESSANDO,
                "./uploads/capturas/uuid.mp4",
                now,
                now
        );

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getIdUser());
        assertEquals("user@fiap.com.br", dto.getEmail());
        assertEquals(CapturaStatus.PROCESSANDO, dto.getStatus());
        assertEquals("./uploads/capturas/uuid.mp4", dto.getPath());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve permitir definir e obter todos os campos")
    void devePermitirDefinirEObterTodosCampos() {
        // Arrange
        CapturaDTO dto = new CapturaDTO();
        LocalDateTime now = LocalDateTime.now();

        // Act
        dto.setId(10L);
        dto.setIdUser(5L);
        dto.setEmail("test@fiap.com.br");
        dto.setStatus(CapturaStatus.CONCLUIDO);
        dto.setPath("./test.mp4");
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);

        // Assert
        assertEquals(10L, dto.getId());
        assertEquals(5L, dto.getIdUser());
        assertEquals("test@fiap.com.br", dto.getEmail());
        assertEquals(CapturaStatus.CONCLUIDO, dto.getStatus());
        assertEquals("./test.mp4", dto.getPath());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve suportar todos os status")
    void deveSuportarTodosStatus() {
        // Arrange
        CapturaDTO dto = new CapturaDTO();

        // Test cada status
        dto.setStatus(CapturaStatus.PENDENTE);
        assertEquals(CapturaStatus.PENDENTE, dto.getStatus());

        dto.setStatus(CapturaStatus.PROCESSANDO);
        assertEquals(CapturaStatus.PROCESSANDO, dto.getStatus());

        dto.setStatus(CapturaStatus.CONCLUIDO);
        assertEquals(CapturaStatus.CONCLUIDO, dto.getStatus());

        dto.setStatus(CapturaStatus.ERRO);
        assertEquals(CapturaStatus.ERRO, dto.getStatus());
    }
}
