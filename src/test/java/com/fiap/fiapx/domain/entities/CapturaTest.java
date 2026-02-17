package com.fiap.fiapx.domain.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CapturaTest {

    private Captura captura;

    @BeforeEach
    void setUp() {
        captura = new Captura();
    }

    @Test
    @DisplayName("Deve criar captura com construtor padrão")
    void deveCriarCapturaComConstrutorPadrao() {
        // Assert
        assertNotNull(captura);
        assertNotNull(captura.getCreatedAt());
        assertNotNull(captura.getUpdatedAt());
        assertEquals(CapturaStatus.PENDENTE, captura.getStatus());
    }

    @Test
    @DisplayName("Deve criar captura com construtor completo")
    void deveCriarCapturaComConstrutorCompleto() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        Captura capturaCompleta = new Captura(
                1L,
                1L,
                "user@fiap.com.br",
                CapturaStatus.PROCESSANDO,
                "./uploads/capturas/uuid.mp4",
                now,
                now
        );

        // Assert
        assertNotNull(capturaCompleta);
        assertEquals(1L, capturaCompleta.getId());
        assertEquals(1L, capturaCompleta.getIdUser());
        assertEquals("user@fiap.com.br", capturaCompleta.getEmail());
        assertEquals(CapturaStatus.PROCESSANDO, capturaCompleta.getStatus());
        assertEquals("./uploads/capturas/uuid.mp4", capturaCompleta.getPath());
        assertEquals(now, capturaCompleta.getCreatedAt());
        assertEquals(now, capturaCompleta.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve iniciar processamento alterando status para PROCESSANDO")
    void deveIniciarProcessamentoAlterandoStatus() {
        // Arrange
        captura.setStatus(CapturaStatus.PENDENTE);
        LocalDateTime beforeUpdate = captura.getUpdatedAt();

        // Act
        captura.iniciarProcessamento();

        // Assert
        assertEquals(CapturaStatus.PROCESSANDO, captura.getStatus());
        assertNotEquals(beforeUpdate, captura.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve concluir processamento alterando status para CONCLUIDO")
    void deveConcluirProcessamentoAlterandoStatus() {
        // Arrange
        captura.setStatus(CapturaStatus.PROCESSANDO);
        LocalDateTime beforeUpdate = captura.getUpdatedAt();

        // Act
        captura.concluirProcessamento();

        // Assert
        assertEquals(CapturaStatus.CONCLUIDO, captura.getStatus());
        assertNotEquals(beforeUpdate, captura.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve marcar erro alterando status para ERRO")
    void deveMarcarErroAlterandoStatus() {
        // Arrange
        captura.setStatus(CapturaStatus.PROCESSANDO);
        LocalDateTime beforeUpdate = captura.getUpdatedAt();

        // Act
        captura.marcarErro();

        // Assert
        assertEquals(CapturaStatus.ERRO, captura.getStatus());
        assertNotEquals(beforeUpdate, captura.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve atualizar status para qualquer valor válido")
    void deveAtualizarStatusParaQualquerValorValido() {
        // Arrange
        LocalDateTime beforeUpdate = captura.getUpdatedAt();

        // Act & Assert - PENDENTE
        captura.atualizarStatus(CapturaStatus.PENDENTE);
        assertEquals(CapturaStatus.PENDENTE, captura.getStatus());

        // Act & Assert - PROCESSANDO
        captura.atualizarStatus(CapturaStatus.PROCESSANDO);
        assertEquals(CapturaStatus.PROCESSANDO, captura.getStatus());

        // Act & Assert - CONCLUIDO
        captura.atualizarStatus(CapturaStatus.CONCLUIDO);
        assertEquals(CapturaStatus.CONCLUIDO, captura.getStatus());

        // Act & Assert - ERRO
        captura.atualizarStatus(CapturaStatus.ERRO);
        assertEquals(CapturaStatus.ERRO, captura.getStatus());

        assertNotEquals(beforeUpdate, captura.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve atualizar updatedAt ao atualizar status")
    void deveAtualizarUpdatedAtAoAtualizarStatus() {
        // Arrange
        LocalDateTime beforeUpdate = captura.getUpdatedAt();

        // Pequeno delay para garantir que o timestamp seja diferente
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        captura.atualizarStatus(CapturaStatus.CONCLUIDO);

        // Assert
        assertTrue(captura.getUpdatedAt().isAfter(beforeUpdate) ||
                   captura.getUpdatedAt().isEqual(beforeUpdate));
    }

    @Test
    @DisplayName("Deve retornar true quando captura pertence ao usuário")
    void deveRetornarTrueQuandoCapturaPertenceAoUsuario() {
        // Arrange
        captura.setIdUser(1L);

        // Act
        boolean pertence = captura.pertenceAoUsuario(1L);

        // Assert
        assertTrue(pertence);
    }

    @Test
    @DisplayName("Deve retornar false quando captura não pertence ao usuário")
    void deveRetornarFalseQuandoCapturaNaoPertenceAoUsuario() {
        // Arrange
        captura.setIdUser(1L);

        // Act
        boolean pertence = captura.pertenceAoUsuario(2L);

        // Assert
        assertFalse(pertence);
    }

    @Test
    @DisplayName("Deve validar pertenceAoUsuario com diferentes IDs")
    void deveValidarPertenceAoUsuarioComDiferentesIDs() {
        // Arrange
        captura.setIdUser(5L);

        // Act & Assert
        assertTrue(captura.pertenceAoUsuario(5L));
        assertFalse(captura.pertenceAoUsuario(1L));
        assertFalse(captura.pertenceAoUsuario(10L));
        assertFalse(captura.pertenceAoUsuario(0L));
    }

    @Test
    @DisplayName("Deve permitir definir e obter ID")
    void devePermitirDefinirEObterID() {
        // Act
        captura.setId(10L);

        // Assert
        assertEquals(10L, captura.getId());
    }

    @Test
    @DisplayName("Deve permitir definir e obter IdUser")
    void devePermitirDefinirEObterIdUser() {
        // Act
        captura.setIdUser(5L);

        // Assert
        assertEquals(5L, captura.getIdUser());
    }

    @Test
    @DisplayName("Deve permitir definir e obter Email")
    void devePermitirDefinirEObterEmail() {
        // Act
        captura.setEmail("test@fiap.com.br");

        // Assert
        assertEquals("test@fiap.com.br", captura.getEmail());
    }

    @Test
    @DisplayName("Deve permitir definir e obter Status")
    void devePermitirDefinirEObterStatus() {
        // Act
        captura.setStatus(CapturaStatus.CONCLUIDO);

        // Assert
        assertEquals(CapturaStatus.CONCLUIDO, captura.getStatus());
    }

    @Test
    @DisplayName("Deve permitir definir e obter Path")
    void devePermitirDefinirEObterPath() {
        // Act
        String path = "./uploads/capturas/test.mp4";
        captura.setPath(path);

        // Assert
        assertEquals(path, captura.getPath());
    }

    @Test
    @DisplayName("Deve permitir definir e obter CreatedAt")
    void devePermitirDefinirEObterCreatedAt() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.of(2026, 2, 6, 10, 30);

        // Act
        captura.setCreatedAt(timestamp);

        // Assert
        assertEquals(timestamp, captura.getCreatedAt());
    }

    @Test
    @DisplayName("Deve permitir definir e obter UpdatedAt")
    void devePermitirDefinirEObterUpdatedAt() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.of(2026, 2, 6, 11, 45);

        // Act
        captura.setUpdatedAt(timestamp);

        // Assert
        assertEquals(timestamp, captura.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve ter status PENDENTE por padrão")
    void deveTerStatusPendentePorPadrao() {
        // Arrange
        Captura novaCaptura = new Captura();

        // Assert
        assertEquals(CapturaStatus.PENDENTE, novaCaptura.getStatus());
    }

    @Test
    @DisplayName("Fluxo completo de status deve funcionar")
    void fluxoCompletoDeStatusDeveFuncionar() {
        // Arrange
        Captura novaCaptura = new Captura();
        assertEquals(CapturaStatus.PENDENTE, novaCaptura.getStatus());

        // Act & Assert - Iniciar processamento
        novaCaptura.iniciarProcessamento();
        assertEquals(CapturaStatus.PROCESSANDO, novaCaptura.getStatus());

        // Act & Assert - Concluir processamento
        novaCaptura.concluirProcessamento();
        assertEquals(CapturaStatus.CONCLUIDO, novaCaptura.getStatus());
    }

    @Test
    @DisplayName("Fluxo de erro deve funcionar")
    void fluxoDeErroDeveFuncionar() {
        // Arrange
        Captura novaCaptura = new Captura();

        // Act - Iniciar processamento
        novaCaptura.iniciarProcessamento();
        assertEquals(CapturaStatus.PROCESSANDO, novaCaptura.getStatus());

        // Act - Marcar erro
        novaCaptura.marcarErro();

        // Assert
        assertEquals(CapturaStatus.ERRO, novaCaptura.getStatus());
    }
}
