package com.fiap.fiapx.application.usecases;

import com.fiap.fiapx.domain.entities.Captura;
import com.fiap.fiapx.domain.entities.CapturaStatus;
import com.fiap.fiapx.domain.exception.CapturaNotFoundException;
import com.fiap.fiapx.domain.exception.UnauthorizedAccessException;
import com.fiap.fiapx.domain.repositories.CapturaRepository;
import com.fiap.fiapx.external.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadCapturaUseCaseTest {

    @Mock
    private CapturaRepository capturaRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private DownloadCapturaUseCase downloadCapturaUseCase;

    private Captura captura;
    private File file;

    @BeforeEach
    void setUp() {
        captura = new Captura();
        captura.setId(1L);
        captura.setIdUser(1L);
        captura.setEmail("user@fiap.com.br");
        captura.setStatus(CapturaStatus.CONCLUIDO);
        captura.setPath("./uploads/capturas/uuid.mp4");

        file = new File("./uploads/capturas/uuid.mp4");
    }

    @Test
    @DisplayName("Deve fazer download com sucesso quando usuário é o dono")
    void deveFazerDownloadComSucesso() {
        // Arrange
        when(capturaRepository.findById(1L)).thenReturn(Optional.of(captura));
        when(fileStorageService.load("./uploads/capturas/uuid.mp4")).thenReturn(file);

        // Act
        File result = downloadCapturaUseCase.execute(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(file.getPath(), result.getPath());

        verify(capturaRepository, times(1)).findById(1L);
        verify(fileStorageService, times(1)).load("./uploads/capturas/uuid.mp4");
    }

    @Test
    @DisplayName("Deve lançar exceção quando captura não encontrada")
    void deveLancarExcecaoQuandoCapturaNaoEncontrada() {
        // Arrange
        when(capturaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        CapturaNotFoundException exception = assertThrows(
                CapturaNotFoundException.class,
                () -> downloadCapturaUseCase.execute(1L, 1L)
        );

        assertTrue(exception.getMessage().contains("Captura com ID 1 não encontrada"));
        verify(capturaRepository, times(1)).findById(1L);
        verify(fileStorageService, never()).load(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não é o dono")
    void deveLancarExcecaoQuandoUsuarioNaoEoDono() {
        // Arrange
        when(capturaRepository.findById(1L)).thenReturn(Optional.of(captura));

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> downloadCapturaUseCase.execute(1L, 2L)
        );

        assertEquals("Você não tem permissão para acessar esta captura", exception.getMessage());
        verify(capturaRepository, times(1)).findById(1L);
        verify(fileStorageService, never()).load(anyString());
    }

    @Test
    @DisplayName("Deve retornar captura quando getCaptura é chamado com usuário correto")
    void deveRetornarCapturaQuandoGetCapturaComUsuarioCorreto() {
        // Arrange
        when(capturaRepository.findById(1L)).thenReturn(Optional.of(captura));

        // Act
        Captura result = downloadCapturaUseCase.getCaptura(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(captura.getId(), result.getId());
        assertEquals(captura.getIdUser(), result.getIdUser());
        assertEquals(captura.getEmail(), result.getEmail());

        verify(capturaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção no getCaptura quando captura não encontrada")
    void deveLancarExcecaoNoGetCapturaQuandoNaoEncontrada() {
        // Arrange
        when(capturaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        CapturaNotFoundException exception = assertThrows(
                CapturaNotFoundException.class,
                () -> downloadCapturaUseCase.getCaptura(1L, 1L)
        );

        assertTrue(exception.getMessage().contains("Captura com ID 1 não encontrada"));
        verify(capturaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção no getCaptura quando usuário não autorizado")
    void deveLancarExcecaoNoGetCapturaQuandoUsuarioNaoAutorizado() {
        // Arrange
        when(capturaRepository.findById(1L)).thenReturn(Optional.of(captura));

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> downloadCapturaUseCase.getCaptura(1L, 2L)
        );

        assertEquals("Você não tem permissão para acessar esta captura", exception.getMessage());
        verify(capturaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve validar pertenceAoUsuario corretamente")
    void deveValidarPertenceAoUsuarioCorretamente() {
        // Arrange
        when(capturaRepository.findById(1L)).thenReturn(Optional.of(captura));
        when(fileStorageService.load(anyString())).thenReturn(file);

        // Act - usuário correto
        assertDoesNotThrow(() -> downloadCapturaUseCase.execute(1L, 1L));

        // Act - usuário incorreto
        assertThrows(UnauthorizedAccessException.class,
                () -> downloadCapturaUseCase.execute(1L, 999L));

        verify(capturaRepository, times(2)).findById(1L);
        verify(fileStorageService, times(1)).load(anyString());
    }

    @Test
    @DisplayName("Deve buscar arquivo pelo path correto")
    void deveBuscarArquivoPeloPathCorreto() {
        // Arrange
        String customPath = "./uploads/capturas/custom-uuid.mp4";
        captura.setPath(customPath);

        when(capturaRepository.findById(1L)).thenReturn(Optional.of(captura));
        when(fileStorageService.load(customPath)).thenReturn(file);

        // Act
        downloadCapturaUseCase.execute(1L, 1L);

        // Assert
        verify(fileStorageService, times(1)).load(customPath);
    }

    @Test
    @DisplayName("Deve funcionar para diferentes IDs de captura")
    void deveFuncionarParaDiferentesIDsDeCaptura() {
        // Arrange
        Captura captura2 = new Captura();
        captura2.setId(2L);
        captura2.setIdUser(1L);
        captura2.setPath("./uploads/capturas/uuid2.mp4");

        File file2 = new File("./uploads/capturas/uuid2.mp4");

        when(capturaRepository.findById(1L)).thenReturn(Optional.of(captura));
        when(capturaRepository.findById(2L)).thenReturn(Optional.of(captura2));
        when(fileStorageService.load("./uploads/capturas/uuid.mp4")).thenReturn(file);
        when(fileStorageService.load("./uploads/capturas/uuid2.mp4")).thenReturn(file2);

        // Act
        File result1 = downloadCapturaUseCase.execute(1L, 1L);
        File result2 = downloadCapturaUseCase.execute(2L, 1L);

        // Assert
        assertEquals(file.getPath(), result1.getPath());
        assertEquals(file2.getPath(), result2.getPath());

        verify(capturaRepository, times(1)).findById(1L);
        verify(capturaRepository, times(1)).findById(2L);
    }
}
