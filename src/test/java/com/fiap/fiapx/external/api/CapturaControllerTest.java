package com.fiap.fiapx.external.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.fiapx.application.dto.CapturaDTO;
import com.fiap.fiapx.application.dto.UpdateStatusRequest;
import com.fiap.fiapx.application.dto.UploadResponse;
import com.fiap.fiapx.application.usecases.DownloadCapturaUseCase;
import com.fiap.fiapx.application.usecases.ListCapturasUseCase;
import com.fiap.fiapx.application.usecases.UpdateCapturaStatusUseCase;
import com.fiap.fiapx.application.usecases.UploadCapturaUseCase;
import com.fiap.fiapx.domain.entities.Captura;
import com.fiap.fiapx.domain.entities.CapturaStatus;
import com.fiap.fiapx.domain.exception.CapturaNotFoundException;
import com.fiap.fiapx.domain.exception.InvalidFileException;
import com.fiap.fiapx.domain.exception.UnauthorizedAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CapturaController.class)
@WithMockUser
class CapturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UploadCapturaUseCase uploadCapturaUseCase;

    @MockBean
    private ListCapturasUseCase listCapturasUseCase;

    @MockBean
    private DownloadCapturaUseCase downloadCapturaUseCase;

    @MockBean
    private UpdateCapturaStatusUseCase updateCapturaStatusUseCase;

    private CapturaDTO capturaDTO;
    private Captura captura;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        capturaDTO = new CapturaDTO(
                1L,
                1L,
                "user@fiap.com.br",
                CapturaStatus.PROCESSANDO,
                "./uploads/capturas/uuid.mp4",
                now,
                now
        );

        captura = new Captura();
        captura.setId(1L);
        captura.setIdUser(1L);
        captura.setEmail("user@fiap.com.br");
        captura.setStatus(CapturaStatus.PROCESSANDO);
        captura.setPath("./uploads/capturas/uuid.mp4");
    }

    @Test
    @DisplayName("POST /capturas/upload - deve fazer upload de arquivo único com sucesso")
    void deveRealizarUploadUnicoComSucesso() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "video.mp4",
                "video/mp4",
                "conteudo do video".getBytes()
        );

        List<CapturaDTO> capturas = Arrays.asList(capturaDTO);
        when(uploadCapturaUseCase.execute(eq(1L), eq("user@fiap.com.br"), any()))
                .thenReturn(capturas);

        // Act & Assert
        mockMvc.perform(multipart("/capturas/upload")
                        .file(file)
                        .param("userId", "1")
                        .param("email", "user@fiap.com.br")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Upload realizado com sucesso"))
                .andExpect(jsonPath("$.totalFiles").value(1))
                .andExpect(jsonPath("$.capturas", hasSize(1)))
                .andExpect(jsonPath("$.capturas[0].id").value(1))
                .andExpect(jsonPath("$.capturas[0].email").value("user@fiap.com.br"))
                .andExpect(jsonPath("$.capturas[0].status").value("PROCESSANDO"));

        verify(uploadCapturaUseCase, times(1))
                .execute(eq(1L), eq("user@fiap.com.br"), any());
    }

    @Test
    @DisplayName("POST /capturas/upload - deve fazer upload de múltiplos arquivos com sucesso")
    void deveRealizarUploadMultiploComSucesso() throws Exception {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "video1.mp4",
                "video/mp4",
                "conteudo do video1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "video2.mp4",
                "video/mp4",
                "conteudo do video2".getBytes()
        );

        CapturaDTO captura2 = new CapturaDTO(
                2L,
                1L,
                "user@fiap.com.br",
                CapturaStatus.PROCESSANDO,
                "./uploads/capturas/uuid2.mp4",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        List<CapturaDTO> capturas = Arrays.asList(capturaDTO, captura2);
        when(uploadCapturaUseCase.execute(eq(1L), eq("user@fiap.com.br"), any()))
                .thenReturn(capturas);

        // Act & Assert
        mockMvc.perform(multipart("/capturas/upload")
                        .file(file1)
                        .file(file2)
                        .param("userId", "1")
                        .param("email", "user@fiap.com.br")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Upload realizado com sucesso"))
                .andExpect(jsonPath("$.totalFiles").value(2))
                .andExpect(jsonPath("$.capturas", hasSize(2)));

        verify(uploadCapturaUseCase, times(1))
                .execute(eq(1L), eq("user@fiap.com.br"), any());
    }

    @Test
    @DisplayName("POST /capturas/upload - deve retornar erro quando arquivo inválido")
    void deveRetornarErroCasoArquivoInvalido() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "video.mp4",
                "video/mp4",
                "conteudo do video".getBytes()
        );

        when(uploadCapturaUseCase.execute(eq(1L), eq("user@fiap.com.br"), any()))
                .thenThrow(new InvalidFileException("Apenas arquivos de vídeo são permitidos"));

        // Act & Assert
        mockMvc.perform(multipart("/capturas/upload")
                        .file(file)
                        .param("userId", "1")
                        .param("email", "user@fiap.com.br")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Apenas arquivos de vídeo são permitidos"));
    }

    @Test
    @DisplayName("POST /capturas/upload - deve usar valores padrão quando não informados")
    void deveUsarValoresPadrao() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "video.mp4",
                "video/mp4",
                "conteudo do video".getBytes()
        );

        List<CapturaDTO> capturas = Arrays.asList(capturaDTO);
        when(uploadCapturaUseCase.execute(eq(1L), eq("user@fiap.com.br"), any()))
                .thenReturn(capturas);

        // Act & Assert
        mockMvc.perform(multipart("/capturas/upload")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Upload realizado com sucesso"));

        verify(uploadCapturaUseCase, times(1))
                .execute(eq(1L), eq("user@fiap.com.br"), any());
    }

    @Test
    @DisplayName("GET /capturas/list - deve listar capturas do usuário")
    void deveListarCapturasDoUsuario() throws Exception {
        // Arrange
        List<CapturaDTO> capturas = Arrays.asList(capturaDTO);
        when(listCapturasUseCase.execute(1L)).thenReturn(capturas);

        // Act & Assert
        mockMvc.perform(get("/capturas/list")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("user@fiap.com.br"))
                .andExpect(jsonPath("$[0].status").value("PROCESSANDO"));

        verify(listCapturasUseCase, times(1)).execute(1L);
    }

    @Test
    @DisplayName("GET /capturas/list - deve retornar lista vazia quando usuário não tem capturas")
    void deveRetornarListaVazia() throws Exception {
        // Arrange
        when(listCapturasUseCase.execute(1L)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/capturas/list")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(listCapturasUseCase, times(1)).execute(1L);
    }

    @Test
    @DisplayName("GET /capturas/list - deve usar valor padrão para userId")
    void deveUsarValorPadraoParaUserId() throws Exception {
        // Arrange
        when(listCapturasUseCase.execute(1L)).thenReturn(Arrays.asList(capturaDTO));

        // Act & Assert
        mockMvc.perform(get("/capturas/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(listCapturasUseCase, times(1)).execute(1L);
    }

    @Test
    @DisplayName("GET /capturas/download/{id} - deve fazer download com sucesso")
    void deveFazerDownloadComSucesso() throws Exception {
        // Arrange
        // Criar arquivo temporário para teste
        File tempFile = File.createTempFile("uuid", ".mp4");
        tempFile.deleteOnExit();

        when(downloadCapturaUseCase.execute(1L, 1L)).thenReturn(tempFile);
        when(downloadCapturaUseCase.getCaptura(1L, 1L)).thenReturn(captura);

        // Act & Assert
        mockMvc.perform(get("/capturas/download/1")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));

        verify(downloadCapturaUseCase, times(1)).execute(1L, 1L);
        verify(downloadCapturaUseCase, times(1)).getCaptura(1L, 1L);
    }

    @Test
    @DisplayName("GET /capturas/download/{id} - deve retornar erro quando captura não encontrada")
    void deveRetornarErroQuandoCapturaNaoEncontrada() throws Exception {
        // Arrange
        when(downloadCapturaUseCase.execute(1L, 1L))
                .thenThrow(new CapturaNotFoundException(1L));

        // Act & Assert
        mockMvc.perform(get("/capturas/download/1")
                        .param("userId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Captura com ID 1 não encontrada"));

        verify(downloadCapturaUseCase, times(1)).execute(1L, 1L);
    }

    @Test
    @DisplayName("GET /capturas/download/{id} - deve retornar erro quando usuário não autorizado")
    void deveRetornarErroQuandoUsuarioNaoAutorizado() throws Exception {
        // Arrange
        when(downloadCapturaUseCase.execute(1L, 2L))
                .thenThrow(new UnauthorizedAccessException("Você não tem permissão para acessar esta captura"));

        // Act & Assert
        mockMvc.perform(get("/capturas/download/1")
                        .param("userId", "2"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Você não tem permissão para acessar esta captura"));

        verify(downloadCapturaUseCase, times(1)).execute(1L, 2L);
    }

    @Test
    @DisplayName("GET /capturas/download/{id} - deve usar valor padrão para userId")
    void deveUsarValorPadraoParaUserIdNoDownload() throws Exception {
        // Arrange
        // Criar arquivo temporário para teste
        File tempFile = File.createTempFile("uuid", ".mp4");
        tempFile.deleteOnExit();

        when(downloadCapturaUseCase.execute(1L, 1L)).thenReturn(tempFile);
        when(downloadCapturaUseCase.getCaptura(1L, 1L)).thenReturn(captura);

        // Act & Assert
        mockMvc.perform(get("/capturas/download/1"))
                .andExpect(status().isOk());

        verify(downloadCapturaUseCase, times(1)).execute(1L, 1L);
    }

    @Test
    @DisplayName("PUT /capturas/update-status/{id} - deve atualizar status com sucesso")
    void deveAtualizarStatusComSucesso() throws Exception {
        // Arrange
        CapturaDTO capturaAtualizada = new CapturaDTO(
                1L,
                1L,
                "user@fiap.com.br",
                CapturaStatus.CONCLUIDO,
                "./uploads/capturas/uuid.mp4",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus("CONCLUIDO");

        when(updateCapturaStatusUseCase.execute(1L, CapturaStatus.CONCLUIDO))
                .thenReturn(capturaAtualizada);

        // Act & Assert
        mockMvc.perform(put("/capturas/update-status/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));

        verify(updateCapturaStatusUseCase, times(1))
                .execute(1L, CapturaStatus.CONCLUIDO);
    }

    @Test
    @DisplayName("PUT /capturas/update-status/{id} - deve retornar erro quando captura não encontrada")
    void deveRetornarErroAoAtualizarStatusQuandoCapturaNaoEncontrada() throws Exception {
        // Arrange
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus("CONCLUIDO");

        when(updateCapturaStatusUseCase.execute(1L, CapturaStatus.CONCLUIDO))
                .thenThrow(new CapturaNotFoundException(1L));

        // Act & Assert
        mockMvc.perform(put("/capturas/update-status/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Captura com ID 1 não encontrada"));
    }

    @Test
    @DisplayName("PUT /capturas/update-status/{id} - deve aceitar todos os status válidos")
    void deveAceitarTodosStatusValidos() throws Exception {
        // Test PENDENTE
        UpdateStatusRequest requestPendente = new UpdateStatusRequest();
        requestPendente.setStatus("PENDENTE");

        CapturaDTO capturaPendente = new CapturaDTO(
                1L, 1L, "user@fiap.com.br", CapturaStatus.PENDENTE,
                "./uploads/capturas/uuid.mp4", LocalDateTime.now(), LocalDateTime.now()
        );

        when(updateCapturaStatusUseCase.execute(1L, CapturaStatus.PENDENTE))
                .thenReturn(capturaPendente);

        mockMvc.perform(put("/capturas/update-status/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPendente))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        // Test ERRO
        UpdateStatusRequest requestErro = new UpdateStatusRequest();
        requestErro.setStatus("ERRO");

        CapturaDTO capturaErro = new CapturaDTO(
                1L, 1L, "user@fiap.com.br", CapturaStatus.ERRO,
                "./uploads/capturas/uuid.mp4", LocalDateTime.now(), LocalDateTime.now()
        );

        when(updateCapturaStatusUseCase.execute(1L, CapturaStatus.ERRO))
                .thenReturn(capturaErro);

        mockMvc.perform(put("/capturas/update-status/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestErro))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ERRO"));
    }

    @Test
    @DisplayName("GET /capturas - deve retornar informações sobre o recurso")
    void deveRetornarInformacoesDoRecurso() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/capturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.base").value("/api/capturas"))
                .andExpect(jsonPath("$.swagger").value("/api/swagger-ui/index.html"))
                .andExpect(jsonPath("$.endpoints", hasSize(4)))
                .andExpect(jsonPath("$.endpoints[0]").value("POST /upload"))
                .andExpect(jsonPath("$.endpoints[1]").value("GET /list?userId={userId}"))
                .andExpect(jsonPath("$.endpoints[2]").value("GET /download/{id}?userId={userId}"))
                .andExpect(jsonPath("$.endpoints[3]").value("PUT /update-status/{id}"));

        verifyNoInteractions(uploadCapturaUseCase, listCapturasUseCase,
                downloadCapturaUseCase, updateCapturaStatusUseCase);
    }
}
