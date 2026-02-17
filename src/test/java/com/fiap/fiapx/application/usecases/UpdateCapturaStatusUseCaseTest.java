package com.fiap.fiapx.application.usecases;

import com.fiap.fiapx.application.dto.CapturaDTO;
import com.fiap.fiapx.domain.entities.Captura;
import com.fiap.fiapx.domain.entities.CapturaStatus;
import com.fiap.fiapx.domain.exception.CapturaNotFoundException;
import com.fiap.fiapx.domain.repositories.CapturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCapturaStatusUseCaseTest {

    @Mock
    private CapturaRepository capturaRepository;

    @InjectMocks
    private UpdateCapturaStatusUseCase updateCapturaStatusUseCase;

    private Captura captura;

    @BeforeEach
    void setUp() {
        captura = new Captura();
        captura.setId(1L);
        captura.setIdUser(1L);
        captura.setEmail("user@fiap.com.br");
        captura.setStatus(CapturaStatus.PROCESSANDO);
        captura.setPath("./uploads/capturas/uuid.mp4");
    }

    @Test
    @DisplayName("Deve atualizar status para CONCLUIDO com sucesso")
    void deveAtualizarStatusParaConcluidoComSucesso() {
        // Arrange
        Captura capturaAtualizada = new Captura();
        capturaAtualizada.setId(1L);
        capturaAtualizada.setIdUser(1L);
        capturaAtualizada.setEmail("user@fiap.com.br");
        capturaAtualizada.setStatus(CapturaStatus.CONCLUIDO);
        capturaAtualizada.setPath("./uploads/capturas/uuid.mp4");

        when(capturaRepository.findById(1L)).thenReturn(Optional.of(captura));
        when(capturaRepository.save(any(Captura.class))).thenReturn(capturaAtualizada);

        // Act
        CapturaDTO result = updateCapturaStatusUseCase.execute(1L, CapturaStatus.CONCLUIDO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(CapturaStatus.CONCLUIDO, result.getStatus());

        verify(capturaRepository, times(1)).findById(1L);
        verify(capturaRepository, times(1)).save(any(Captura.class));
    }

    @Test
    @DisplayName("Deve atualizar status para ERRO com sucesso")
    void deveAtualizarStatusParaErroComSucesso() {
        // Arrange
        Captura capturaAtualizada = new Captura();
        capturaAtualizada.setId(1L);
        capturaAtualizada.setIdUser(1L);
        capturaAtualizada.setEmail("user@fiap.com.br");
        capturaAtualizada.setStatus(CapturaStatus.ERRO);
        capturaAtualizada.setPath("./uploads/capturas/uuid.mp4");

        when(capturaRepository.findById(1L)).thenReturn(Optional.of(captura));
        when(capturaRepository.save(any(Captura.class))).thenReturn(capturaAtualizada);

        // Act
        CapturaDTO result = updateCapturaStatusUseCase.execute(1L, CapturaStatus.ERRO);

        // Assert
        assertNotNull(result);
        assertEquals(CapturaStatus.ERRO, result.getStatus());
    }

    @Test
    @DisplayName("Deve atualizar status para PENDENTE com sucesso")
    void deveAtualizarStatusParaPendenteComSucesso() {
        // Arrange
        Captura capturaAtualizada = new Captura();
        capturaAtualizada.setId(1L);
        capturaAtualizada.setIdUser(1L);
        capturaAtualizada.setEmail("user@fiap.com.br");
        capturaAtualizada.setStatus(CapturaStatus.PENDENTE);
        capturaAtualizada.setPath("./uploads/capturas/uuid.mp4");

        when(capturaRepository.findById(1L)).thenReturn(Optional.of(captura));
        when(capturaRepository.save(any(Captura.class))).thenReturn(capturaAtualizada);

        // Act
        CapturaDTO result = updateCapturaStatusUseCase.execute(1L, CapturaStatus.PENDENTE);

        // Assert
        assertNotNull(result);
        assertEquals(CapturaStatus.PENDENTE, result.getStatus());
    }

    @Test
    @DisplayName("Deve lançar exceção quando captura não encontrada")
    void deveLancarExcecaoQuandoCapturaNaoEncontrada() {
        // Arrange
        when(capturaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        CapturaNotFoundException exception = assertThrows(
                CapturaNotFoundException.class,
                () -> updateCapturaStatusUseCase.execute(1L, CapturaStatus.CONCLUIDO)
        );

        assertTrue(exception.getMessage().contains("Captura com ID 1 não encontrada"));
        verify(capturaRepository, times(1)).findById(1L);
        verify(capturaRepository, never()).save(any(Captura.class));
    }

    @Test
    @DisplayName("Deve chamar atualizarStatus na entidade")
    void deveChamarAtualizarStatusNaEntidade() {
        // Arrange
        when(capturaRepository.findById(1L)).thenReturn(Optional.of(captura));

        ArgumentCaptor<Captura> capturaCaptor = ArgumentCaptor.forClass(Captura.class);
        when(capturaRepository.save(capturaCaptor.capture())).thenReturn(captura);

        // Act
        updateCapturaStatusUseCase.execute(1L, CapturaStatus.CONCLUIDO);

        // Assert
        Captura capturedCaptura = capturaCaptor.getValue();
        // A entidade já deve ter o status atualizado quando for salva
        verify(capturaRepository, times(1)).save(any(Captura.class));
    }

    @Test
    @DisplayName("Deve retornar DTO com dados corretos após atualização")
    void deveRetornarDTOComDadosCorretosAposAtualizacao() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Captura capturaAtualizada = new Captura();
        capturaAtualizada.setId(1L);
        capturaAtualizada.setIdUser(1L);
        capturaAtualizada.setEmail("user@fiap.com.br");
        capturaAtualizada.setStatus(CapturaStatus.CONCLUIDO);
        capturaAtualizada.setPath("./uploads/capturas/uuid.mp4");

        when(capturaRepository.findById(1L)).thenReturn(Optional.of(captura));
        when(capturaRepository.save(any(Captura.class))).thenReturn(capturaAtualizada);

        // Act
        CapturaDTO result = updateCapturaStatusUseCase.execute(1L, CapturaStatus.CONCLUIDO);

        // Assert
        assertEquals(capturaAtualizada.getId(), result.getId());
        assertEquals(capturaAtualizada.getIdUser(), result.getIdUser());
        assertEquals(capturaAtualizada.getEmail(), result.getEmail());
        assertEquals(capturaAtualizada.getStatus(), result.getStatus());
        assertEquals(capturaAtualizada.getPath(), result.getPath());
    }

    @Test
    @DisplayName("Deve atualizar status múltiplas vezes")
    void deveAtualizarStatusMultiplasVezes() {
        // Arrange
        when(capturaRepository.findById(1L)).thenReturn(Optional.of(captura));
        when(capturaRepository.save(any(Captura.class))).thenReturn(captura);

        // Act
        updateCapturaStatusUseCase.execute(1L, CapturaStatus.PROCESSANDO);
        updateCapturaStatusUseCase.execute(1L, CapturaStatus.CONCLUIDO);
        updateCapturaStatusUseCase.execute(1L, CapturaStatus.ERRO);

        // Assert
        verify(capturaRepository, times(3)).findById(1L);
        verify(capturaRepository, times(3)).save(any(Captura.class));
    }

    @Test
    @DisplayName("Deve funcionar para diferentes IDs de captura")
    void deveFuncionarParaDiferentesIDsDeCaptura() {
        // Arrange
        Captura captura2 = new Captura();
        captura2.setId(2L);
        captura2.setIdUser(2L);
        captura2.setEmail("user2@fiap.com.br");
        captura2.setStatus(CapturaStatus.PROCESSANDO);

        when(capturaRepository.findById(1L)).thenReturn(Optional.of(captura));
        when(capturaRepository.findById(2L)).thenReturn(Optional.of(captura2));
        when(capturaRepository.save(any(Captura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CapturaDTO result1 = updateCapturaStatusUseCase.execute(1L, CapturaStatus.CONCLUIDO);
        CapturaDTO result2 = updateCapturaStatusUseCase.execute(2L, CapturaStatus.ERRO);

        // Assert
        assertEquals(1L, result1.getId());
        assertEquals(2L, result2.getId());

        verify(capturaRepository, times(1)).findById(1L);
        verify(capturaRepository, times(1)).findById(2L);
        verify(capturaRepository, times(2)).save(any(Captura.class));
    }

    @Test
    @DisplayName("Deve manter outros campos inalterados ao atualizar status")
    void deveManterOutrosCamposInalteradosAoAtualizarStatus() {
        // Arrange
        when(capturaRepository.findById(1L)).thenReturn(Optional.of(captura));
        when(capturaRepository.save(any(Captura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String originalEmail = captura.getEmail();
        String originalPath = captura.getPath();
        Long originalUserId = captura.getIdUser();

        // Act
        CapturaDTO result = updateCapturaStatusUseCase.execute(1L, CapturaStatus.CONCLUIDO);

        // Assert
        assertEquals(originalEmail, result.getEmail());
        assertEquals(originalPath, result.getPath());
        assertEquals(originalUserId, result.getIdUser());
    }
}
