package com.fiap.fiapx.adapters.gateway;

import com.fiap.fiapx.domain.entities.Captura;
import com.fiap.fiapx.domain.entities.CapturaStatus;
import com.fiap.fiapx.external.datasource.entities.CapturaEntity;
import com.fiap.fiapx.external.datasource.repositories.JpaCapturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapturaRepositoryImplTest {

    @Mock
    private JpaCapturaRepository jpaCapturaRepository;

    @InjectMocks
    private CapturaRepositoryImpl capturaRepository;

    private Captura captura;
    private CapturaEntity capturaEntity;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        captura = new Captura();
        captura.setId(1L);
        captura.setIdUser(1L);
        captura.setEmail("user@fiap.com.br");
        captura.setStatus(CapturaStatus.PROCESSANDO);
        captura.setPath("./uploads/capturas/uuid.mp4");

        capturaEntity = new CapturaEntity();
        capturaEntity.setId(1L);
        capturaEntity.setIdUser(1L);
        capturaEntity.setEmail("user@fiap.com.br");
        capturaEntity.setStatus(CapturaStatus.PROCESSANDO);
        capturaEntity.setPath("./uploads/capturas/uuid.mp4");
        capturaEntity.setCreatedAt(now);
        capturaEntity.setUpdatedAt(now);
    }

    @Test
    @DisplayName("Deve salvar captura com sucesso")
    void deveSalvarCapturaComSucesso() {
        // Arrange
        when(jpaCapturaRepository.save(any(CapturaEntity.class)))
                .thenReturn(capturaEntity);

        // Act
        Captura result = capturaRepository.save(captura);

        // Assert
        assertNotNull(result);
        assertEquals(capturaEntity.getId(), result.getId());
        assertEquals(capturaEntity.getIdUser(), result.getIdUser());
        assertEquals(capturaEntity.getEmail(), result.getEmail());
        assertEquals(capturaEntity.getStatus(), result.getStatus());
        assertEquals(capturaEntity.getPath(), result.getPath());

        verify(jpaCapturaRepository, times(1)).save(any(CapturaEntity.class));
    }

    @Test
    @DisplayName("Deve converter Captura para Entity corretamente ao salvar")
    void deveConverterCapturaParaEntityCorretamente() {
        // Arrange
        ArgumentCaptor<CapturaEntity> entityCaptor = ArgumentCaptor.forClass(CapturaEntity.class);
        when(jpaCapturaRepository.save(entityCaptor.capture()))
                .thenReturn(capturaEntity);

        // Act
        capturaRepository.save(captura);

        // Assert
        CapturaEntity capturedEntity = entityCaptor.getValue();
        assertEquals(captura.getId(), capturedEntity.getId());
        assertEquals(captura.getIdUser(), capturedEntity.getIdUser());
        assertEquals(captura.getEmail(), capturedEntity.getEmail());
        assertEquals(captura.getStatus(), capturedEntity.getStatus());
        assertEquals(captura.getPath(), capturedEntity.getPath());
    }

    @Test
    @DisplayName("Deve buscar captura por ID com sucesso")
    void deveBuscarCapturaPorIDComSucesso() {
        // Arrange
        when(jpaCapturaRepository.findById(1L))
                .thenReturn(Optional.of(capturaEntity));

        // Act
        Optional<Captura> result = capturaRepository.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("user@fiap.com.br", result.get().getEmail());

        verify(jpaCapturaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando captura não encontrada")
    void deveRetornarOptionalVazioQuandoCapturaNaoEncontrada() {
        // Arrange
        when(jpaCapturaRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act
        Optional<Captura> result = capturaRepository.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(jpaCapturaRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Deve buscar capturas por ID de usuário")
    void deveBuscarCapturasPorIDDeUsuario() {
        // Arrange
        CapturaEntity entity2 = new CapturaEntity();
        entity2.setId(2L);
        entity2.setIdUser(1L);
        entity2.setEmail("user@fiap.com.br");
        entity2.setStatus(CapturaStatus.CONCLUIDO);
        entity2.setPath("./uploads/capturas/uuid2.mp4");
        entity2.setCreatedAt(LocalDateTime.now());
        entity2.setUpdatedAt(LocalDateTime.now());

        List<CapturaEntity> entities = Arrays.asList(capturaEntity, entity2);
        when(jpaCapturaRepository.findByIdUser(1L)).thenReturn(entities);

        // Act
        List<Captura> result = capturaRepository.findByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertTrue(result.stream().allMatch(c -> c.getIdUser().equals(1L)));

        verify(jpaCapturaRepository, times(1)).findByIdUser(1L);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tem capturas")
    void deveRetornarListaVaziaQuandoUsuarioNaoTemCapturas() {
        // Arrange
        when(jpaCapturaRepository.findByIdUser(1L))
                .thenReturn(Arrays.asList());

        // Act
        List<Captura> result = capturaRepository.findByUserId(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jpaCapturaRepository, times(1)).findByIdUser(1L);
    }

    @Test
    @DisplayName("Deve deletar captura por ID")
    void deveDeletarCapturaPorID() {
        // Arrange
        doNothing().when(jpaCapturaRepository).deleteById(1L);

        // Act
        capturaRepository.deleteById(1L);

        // Assert
        verify(jpaCapturaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Deve buscar todas as capturas")
    void deveBuscarTodasAsCapturas() {
        // Arrange
        CapturaEntity entity2 = new CapturaEntity();
        entity2.setId(2L);
        entity2.setIdUser(2L);
        entity2.setEmail("user2@fiap.com.br");
        entity2.setStatus(CapturaStatus.CONCLUIDO);
        entity2.setPath("./uploads/capturas/uuid2.mp4");
        entity2.setCreatedAt(LocalDateTime.now());
        entity2.setUpdatedAt(LocalDateTime.now());

        List<CapturaEntity> entities = Arrays.asList(capturaEntity, entity2);
        when(jpaCapturaRepository.findAll()).thenReturn(entities);

        // Act
        List<Captura> result = capturaRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(jpaCapturaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há capturas")
    void deveRetornarListaVaziaQuandoNaoHaCapturas() {
        // Arrange
        when(jpaCapturaRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Captura> result = capturaRepository.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jpaCapturaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve converter Entity para Domain corretamente")
    void deveConverterEntityParaDomainCorretamente() {
        // Arrange
        when(jpaCapturaRepository.findById(1L))
                .thenReturn(Optional.of(capturaEntity));

        // Act
        Optional<Captura> result = capturaRepository.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        Captura capturaDomain = result.get();
        assertEquals(capturaEntity.getId(), capturaDomain.getId());
        assertEquals(capturaEntity.getIdUser(), capturaDomain.getIdUser());
        assertEquals(capturaEntity.getEmail(), capturaDomain.getEmail());
        assertEquals(capturaEntity.getStatus(), capturaDomain.getStatus());
        assertEquals(capturaEntity.getPath(), capturaDomain.getPath());
        assertEquals(capturaEntity.getCreatedAt(), capturaDomain.getCreatedAt());
        assertEquals(capturaEntity.getUpdatedAt(), capturaDomain.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve manter todos os status ao converter")
    void deveManterTodosStatusAoConverter() {
        // Test para cada status
        CapturaStatus[] statuses = {
                CapturaStatus.PENDENTE,
                CapturaStatus.PROCESSANDO,
                CapturaStatus.CONCLUIDO,
                CapturaStatus.ERRO
        };

        for (CapturaStatus status : statuses) {
            CapturaEntity entity = new CapturaEntity();
            entity.setId(1L);
            entity.setIdUser(1L);
            entity.setEmail("user@fiap.com.br");
            entity.setStatus(status);
            entity.setPath("./uploads/test.mp4");
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());

            when(jpaCapturaRepository.findById(1L))
                    .thenReturn(Optional.of(entity));

            Optional<Captura> result = capturaRepository.findById(1L);

            assertTrue(result.isPresent());
            assertEquals(status, result.get().getStatus());
        }
    }

    @Test
    @DisplayName("Deve preservar timestamps ao converter")
    void devePreservarTimestampsAoConverter() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2026, 2, 6, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 2, 6, 15, 30);

        capturaEntity.setCreatedAt(createdAt);
        capturaEntity.setUpdatedAt(updatedAt);

        when(jpaCapturaRepository.findById(1L))
                .thenReturn(Optional.of(capturaEntity));

        // Act
        Optional<Captura> result = capturaRepository.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(createdAt, result.get().getCreatedAt());
        assertEquals(updatedAt, result.get().getUpdatedAt());
    }
}
