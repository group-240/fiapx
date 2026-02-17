package com.fiap.fiapx.application.usecases;

import com.fiap.fiapx.application.dto.CapturaDTO;
import com.fiap.fiapx.domain.entities.Captura;
import com.fiap.fiapx.domain.entities.CapturaStatus;
import com.fiap.fiapx.domain.repositories.CapturaRepository;
import com.fiap.fiapx.external.queue.MessageQueueService;
import com.fiap.fiapx.external.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadCapturaUseCaseTest {

    @Mock
    private CapturaRepository capturaRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private MessageQueueService messageQueueService;

    @InjectMocks
    private UploadCapturaUseCase uploadCapturaUseCase;

    private MockMultipartFile file;
    private Captura captura;

    @BeforeEach
    void setUp() {
        file = new MockMultipartFile(
                "files",
                "video.mp4",
                "video/mp4",
                "conteudo do video".getBytes()
        );

        captura = new Captura();
        captura.setId(1L);
        captura.setIdUser(1L);
        captura.setEmail("user@fiap.com.br");
        captura.setStatus(CapturaStatus.PROCESSANDO);
        captura.setPath("./uploads/capturas/uuid.mp4");
    }

    @Test
    @DisplayName("Deve fazer upload de arquivo único com sucesso")
    void deveFazerUploadUnicoComSucesso() {
        // Arrange
        MultipartFile[] files = {file};
        when(fileStorageService.store(any(MultipartFile.class)))
                .thenReturn("./uploads/capturas/uuid.mp4");
        when(capturaRepository.save(any(Captura.class)))
                .thenReturn(captura);

        // Act
        List<CapturaDTO> result = uploadCapturaUseCase.execute(1L, "user@fiap.com.br", files);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("user@fiap.com.br", result.get(0).getEmail());
        assertEquals(CapturaStatus.PROCESSANDO, result.get(0).getStatus());

        verify(fileStorageService, times(1)).store(any(MultipartFile.class));
        verify(capturaRepository, times(1)).save(any(Captura.class));
        verify(messageQueueService, times(1)).sendToProcessingQueue(
                eq(1L), eq(1L), eq("user@fiap.com.br"), eq("./uploads/capturas/uuid.mp4")
        );
    }

    @Test
    @DisplayName("Deve fazer upload de múltiplos arquivos com sucesso")
    void deveFazerUploadMultiploComSucesso() {
        // Arrange
        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "video2.mp4",
                "video/mp4",
                "conteudo do video2".getBytes()
        );

        MultipartFile[] files = {file, file2};

        Captura captura2 = new Captura();
        captura2.setId(2L);
        captura2.setIdUser(1L);
        captura2.setEmail("user@fiap.com.br");
        captura2.setStatus(CapturaStatus.PROCESSANDO);
        captura2.setPath("./uploads/capturas/uuid2.mp4");

        when(fileStorageService.store(any(MultipartFile.class)))
                .thenReturn("./uploads/capturas/uuid.mp4")
                .thenReturn("./uploads/capturas/uuid2.mp4");
        when(capturaRepository.save(any(Captura.class)))
                .thenReturn(captura)
                .thenReturn(captura2);

        // Act
        List<CapturaDTO> result = uploadCapturaUseCase.execute(1L, "user@fiap.com.br", files);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(fileStorageService, times(2)).store(any(MultipartFile.class));
        verify(capturaRepository, times(2)).save(any(Captura.class));
        verify(messageQueueService, times(2)).sendToProcessingQueue(
                anyLong(), eq(1L), eq("user@fiap.com.br"), anyString()
        );
    }

    @Test
    @DisplayName("Deve configurar status como PROCESSANDO ao salvar")
    void deveConfigurarStatusComoProcessando() {
        // Arrange
        MultipartFile[] files = {file};
        when(fileStorageService.store(any(MultipartFile.class)))
                .thenReturn("./uploads/capturas/uuid.mp4");

        ArgumentCaptor<Captura> capturaCaptor = ArgumentCaptor.forClass(Captura.class);
        when(capturaRepository.save(capturaCaptor.capture()))
                .thenReturn(captura);

        // Act
        uploadCapturaUseCase.execute(1L, "user@fiap.com.br", files);

        // Assert
        Captura capturedCaptura = capturaCaptor.getValue();
        assertEquals(CapturaStatus.PROCESSANDO, capturedCaptura.getStatus());
        assertEquals(1L, capturedCaptura.getIdUser());
        assertEquals("user@fiap.com.br", capturedCaptura.getEmail());
        assertEquals("./uploads/capturas/uuid.mp4", capturedCaptura.getPath());
    }

    @Test
    @DisplayName("Deve enviar mensagem para fila após salvar")
    void deveEnviarMensagemParaFila() {
        // Arrange
        MultipartFile[] files = {file};
        when(fileStorageService.store(any(MultipartFile.class)))
                .thenReturn("./uploads/capturas/uuid.mp4");
        when(capturaRepository.save(any(Captura.class)))
                .thenReturn(captura);

        // Act
        uploadCapturaUseCase.execute(1L, "user@fiap.com.br", files);

        // Assert
        verify(messageQueueService, times(1)).sendToProcessingQueue(
                1L,
                1L,
                "user@fiap.com.br",
                "./uploads/capturas/uuid.mp4"
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando erro ao armazenar arquivo")
    void deveLancarExcecaoQuandoErroAoArmazenarArquivo() {
        // Arrange
        MultipartFile[] files = {file};
        when(fileStorageService.store(any(MultipartFile.class)))
                .thenThrow(new RuntimeException("Erro ao salvar arquivo"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            uploadCapturaUseCase.execute(1L, "user@fiap.com.br", files);
        });

        assertTrue(exception.getMessage().contains("Erro ao processar arquivo"));
        verify(capturaRepository, never()).save(any(Captura.class));
        verify(messageQueueService, never()).sendToProcessingQueue(
                anyLong(), anyLong(), anyString(), anyString()
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando erro ao salvar no banco")
    void deveLancarExcecaoQuandoErroAoSalvarNoBanco() {
        // Arrange
        MultipartFile[] files = {file};
        when(fileStorageService.store(any(MultipartFile.class)))
                .thenReturn("./uploads/capturas/uuid.mp4");
        when(capturaRepository.save(any(Captura.class)))
                .thenThrow(new RuntimeException("Erro ao salvar no banco"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            uploadCapturaUseCase.execute(1L, "user@fiap.com.br", files);
        });

        assertTrue(exception.getMessage().contains("Erro ao processar arquivo"));
        verify(messageQueueService, never()).sendToProcessingQueue(
                anyLong(), anyLong(), anyString(), anyString()
        );
    }

    @Test
    @DisplayName("Deve retornar DTO com dados corretos")
    void deveRetornarDTOComDadosCorretos() {
        // Arrange
        MultipartFile[] files = {file};
        when(fileStorageService.store(any(MultipartFile.class)))
                .thenReturn("./uploads/capturas/uuid.mp4");
        when(capturaRepository.save(any(Captura.class)))
                .thenReturn(captura);

        // Act
        List<CapturaDTO> result = uploadCapturaUseCase.execute(1L, "user@fiap.com.br", files);

        // Assert
        CapturaDTO dto = result.get(0);
        assertEquals(captura.getId(), dto.getId());
        assertEquals(captura.getIdUser(), dto.getIdUser());
        assertEquals(captura.getEmail(), dto.getEmail());
        assertEquals(captura.getStatus(), dto.getStatus());
        assertEquals(captura.getPath(), dto.getPath());
        assertEquals(captura.getCreatedAt(), dto.getCreatedAt());
        assertEquals(captura.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve processar array vazio sem erro")
    void deveProcessarArrayVazioSemErro() {
        // Arrange
        MultipartFile[] files = {};

        // Act
        List<CapturaDTO> result = uploadCapturaUseCase.execute(1L, "user@fiap.com.br", files);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(fileStorageService, never()).store(any(MultipartFile.class));
        verify(capturaRepository, never()).save(any(Captura.class));
        verify(messageQueueService, never()).sendToProcessingQueue(
                anyLong(), anyLong(), anyString(), anyString()
        );
    }
}
