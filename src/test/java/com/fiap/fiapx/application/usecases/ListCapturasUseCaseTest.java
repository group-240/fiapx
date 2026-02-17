package com.fiap.fiapx.application.usecases;

import com.fiap.fiapx.application.dto.CapturaDTO;
import com.fiap.fiapx.domain.entities.Captura;
import com.fiap.fiapx.domain.entities.CapturaStatus;
import com.fiap.fiapx.domain.repositories.CapturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListCapturasUseCaseTest {

    @Mock
    private CapturaRepository capturaRepository;

    @InjectMocks
    private ListCapturasUseCase listCapturasUseCase;

    private Captura captura1;
    private Captura captura2;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        captura1 = new Captura();
        captura1.setId(1L);
        captura1.setIdUser(1L);
        captura1.setEmail("user@fiap.com.br");
        captura1.setStatus(CapturaStatus.CONCLUIDO);
        captura1.setPath("./uploads/capturas/uuid1.mp4");

        captura2 = new Captura();
        captura2.setId(2L);
        captura2.setIdUser(1L);
        captura2.setEmail("user@fiap.com.br");
        captura2.setStatus(CapturaStatus.PROCESSANDO);
        captura2.setPath("./uploads/capturas/uuid2.mp4");
    }

    @Test
    @DisplayName("Deve listar todas as capturas do usuário")
    void deveListarTodasCapturasDoUsuario() {
        // Arrange
        List<Captura> capturas = Arrays.asList(captura1, captura2);
        when(capturaRepository.findByUserId(1L)).thenReturn(capturas);

        // Act
        List<CapturaDTO> result = listCapturasUseCase.execute(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals("user@fiap.com.br", result.get(0).getEmail());
        assertEquals(CapturaStatus.CONCLUIDO, result.get(0).getStatus());
        assertEquals(CapturaStatus.PROCESSANDO, result.get(1).getStatus());

        verify(capturaRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tem capturas")
    void deveRetornarListaVaziaQuandoUsuarioNaoTemCapturas() {
        // Arrange
        when(capturaRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<CapturaDTO> result = listCapturasUseCase.execute(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(capturaRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("Deve converter Captura para DTO corretamente")
    void deveConverterCapturaParaDTOCorretamente() {
        // Arrange
        List<Captura> capturas = Arrays.asList(captura1);
        when(capturaRepository.findByUserId(1L)).thenReturn(capturas);

        // Act
        List<CapturaDTO> result = listCapturasUseCase.execute(1L);

        // Assert
        CapturaDTO dto = result.get(0);
        assertEquals(captura1.getId(), dto.getId());
        assertEquals(captura1.getIdUser(), dto.getIdUser());
        assertEquals(captura1.getEmail(), dto.getEmail());
        assertEquals(captura1.getStatus(), dto.getStatus());
        assertEquals(captura1.getPath(), dto.getPath());
        assertEquals(captura1.getCreatedAt(), dto.getCreatedAt());
        assertEquals(captura1.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve listar capturas de diferentes status")
    void deveListarCapturasDeDiferentesStatus() {
        // Arrange
        Captura captura3 = new Captura();
        captura3.setId(3L);
        captura3.setIdUser(1L);
        captura3.setEmail("user@fiap.com.br");
        captura3.setStatus(CapturaStatus.ERRO);
        captura3.setPath("./uploads/capturas/uuid3.mp4");

        Captura captura4 = new Captura();
        captura4.setId(4L);
        captura4.setIdUser(1L);
        captura4.setEmail("user@fiap.com.br");
        captura4.setStatus(CapturaStatus.PENDENTE);
        captura4.setPath("./uploads/capturas/uuid4.mp4");

        List<Captura> capturas = Arrays.asList(captura1, captura2, captura3, captura4);
        when(capturaRepository.findByUserId(1L)).thenReturn(capturas);

        // Act
        List<CapturaDTO> result = listCapturasUseCase.execute(1L);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals(CapturaStatus.CONCLUIDO, result.get(0).getStatus());
        assertEquals(CapturaStatus.PROCESSANDO, result.get(1).getStatus());
        assertEquals(CapturaStatus.ERRO, result.get(2).getStatus());
        assertEquals(CapturaStatus.PENDENTE, result.get(3).getStatus());
    }

    @Test
    @DisplayName("Deve listar apenas capturas do usuário especificado")
    void deveListarApenasCapturasDoUsuarioEspecificado() {
        // Arrange
        List<Captura> capturas = Arrays.asList(captura1, captura2);
        when(capturaRepository.findByUserId(1L)).thenReturn(capturas);
        when(capturaRepository.findByUserId(2L)).thenReturn(Collections.emptyList());

        // Act
        List<CapturaDTO> resultUser1 = listCapturasUseCase.execute(1L);
        List<CapturaDTO> resultUser2 = listCapturasUseCase.execute(2L);

        // Assert
        assertEquals(2, resultUser1.size());
        assertTrue(resultUser2.isEmpty());

        verify(capturaRepository, times(1)).findByUserId(1L);
        verify(capturaRepository, times(1)).findByUserId(2L);
    }

    @Test
    @DisplayName("Deve manter ordem retornada pelo repositório")
    void deveManterOrdemRetornadaPeloRepositorio() {
        // Arrange
        List<Captura> capturas = Arrays.asList(captura2, captura1); // Ordem invertida
        when(capturaRepository.findByUserId(1L)).thenReturn(capturas);

        // Act
        List<CapturaDTO> result = listCapturasUseCase.execute(1L);

        // Assert
        assertEquals(2L, result.get(0).getId());
        assertEquals(1L, result.get(1).getId());
    }

    @Test
    @DisplayName("Deve listar captura única corretamente")
    void deveListarCapturaUnicaCorretamente() {
        // Arrange
        List<Captura> capturas = Arrays.asList(captura1);
        when(capturaRepository.findByUserId(1L)).thenReturn(capturas);

        // Act
        List<CapturaDTO> result = listCapturasUseCase.execute(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
