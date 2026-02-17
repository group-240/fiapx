package com.fiap.fiapx.application.dto;

import com.fiap.fiapx.domain.entities.CapturaStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UploadResponseTest {

    @Test
    @DisplayName("Deve criar response com construtor padrão")
    void deveCriarResponseComConstrutorPadrao() {
        // Act
        UploadResponse response = new UploadResponse();

        // Assert
        assertNotNull(response);
        assertNull(response.getMessage());
        assertNull(response.getCapturas());
        assertNull(response.getTotalFiles());
    }

    @Test
    @DisplayName("Deve criar response com todos os argumentos")
    void deveCriarResponseComTodosArgumentos() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        CapturaDTO dto = new CapturaDTO(
                1L, 1L, "user@fiap.com.br",
                CapturaStatus.PROCESSANDO,
                "./uploads/uuid.mp4",
                now, now
        );
        List<CapturaDTO> capturas = Arrays.asList(dto);

        // Act
        UploadResponse response = new UploadResponse(
                "Upload realizado com sucesso",
                capturas,
                1
        );

        // Assert
        assertNotNull(response);
        assertEquals("Upload realizado com sucesso", response.getMessage());
        assertEquals(capturas, response.getCapturas());
        assertEquals(1, response.getTotalFiles());
    }

    @Test
    @DisplayName("Deve permitir definir e obter todos os campos")
    void devePermitirDefinirEObterTodosCampos() {
        // Arrange
        UploadResponse response = new UploadResponse();
        LocalDateTime now = LocalDateTime.now();
        CapturaDTO dto = new CapturaDTO(
                1L, 1L, "user@fiap.com.br",
                CapturaStatus.PROCESSANDO,
                "./uploads/uuid.mp4",
                now, now
        );
        List<CapturaDTO> capturas = Arrays.asList(dto);

        // Act
        response.setMessage("Sucesso");
        response.setCapturas(capturas);
        response.setTotalFiles(1);

        // Assert
        assertEquals("Sucesso", response.getMessage());
        assertEquals(capturas, response.getCapturas());
        assertEquals(1, response.getTotalFiles());
    }

    @Test
    @DisplayName("Deve suportar múltiplas capturas")
    void deveSuportarMultiplasCapturas() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        CapturaDTO dto1 = new CapturaDTO(
                1L, 1L, "user@fiap.com.br",
                CapturaStatus.PROCESSANDO,
                "./uploads/uuid1.mp4",
                now, now
        );
        CapturaDTO dto2 = new CapturaDTO(
                2L, 1L, "user@fiap.com.br",
                CapturaStatus.PROCESSANDO,
                "./uploads/uuid2.mp4",
                now, now
        );
        List<CapturaDTO> capturas = Arrays.asList(dto1, dto2);

        // Act
        UploadResponse response = new UploadResponse(
                "Upload realizado com sucesso",
                capturas,
                2
        );

        // Assert
        assertEquals(2, response.getCapturas().size());
        assertEquals(2, response.getTotalFiles());
    }

    @Test
    @DisplayName("Deve suportar lista vazia de capturas")
    void deveSuportarListaVaziaDeCapturas() {
        // Act
        UploadResponse response = new UploadResponse(
                "Nenhum arquivo enviado",
                Collections.emptyList(),
                0
        );

        // Assert
        assertNotNull(response.getCapturas());
        assertTrue(response.getCapturas().isEmpty());
        assertEquals(0, response.getTotalFiles());
    }

    @Test
    @DisplayName("Deve conter mensagem de sucesso padrão")
    void deveConterMensagemDeSucessoPadrao() {
        // Arrange
        List<CapturaDTO> capturas = Collections.emptyList();

        // Act
        UploadResponse response = new UploadResponse(
                "Upload realizado com sucesso",
                capturas,
                0
        );

        // Assert
        assertEquals("Upload realizado com sucesso", response.getMessage());
    }
}
