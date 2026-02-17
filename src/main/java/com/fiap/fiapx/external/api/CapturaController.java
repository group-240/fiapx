package com.fiap.fiapx.external.api;

import com.fiap.fiapx.application.dto.CapturaDTO;
import com.fiap.fiapx.application.dto.UpdateStatusRequest;
import com.fiap.fiapx.application.dto.UploadResponse;
import com.fiap.fiapx.application.usecases.DownloadCapturaUseCase;
import com.fiap.fiapx.application.usecases.ListCapturasUseCase;
import com.fiap.fiapx.application.usecases.UpdateCapturaStatusUseCase;
import com.fiap.fiapx.application.usecases.UploadCapturaUseCase;
import com.fiap.fiapx.domain.entities.Captura;
import com.fiap.fiapx.domain.entities.CapturaStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

@RestController
@RequestMapping("/capturas")
@Tag(name = "Capturas", description = "Gerenciamento de capturas de vídeo")
public class CapturaController {

    private final UploadCapturaUseCase uploadCapturaUseCase;
    private final ListCapturasUseCase listCapturasUseCase;
    private final DownloadCapturaUseCase downloadCapturaUseCase;
    private final UpdateCapturaStatusUseCase updateCapturaStatusUseCase;

    public CapturaController(UploadCapturaUseCase uploadCapturaUseCase,
                            ListCapturasUseCase listCapturasUseCase,
                            DownloadCapturaUseCase downloadCapturaUseCase,
                            UpdateCapturaStatusUseCase updateCapturaStatusUseCase) {
        this.uploadCapturaUseCase = uploadCapturaUseCase;
        this.listCapturasUseCase = listCapturasUseCase;
        this.downloadCapturaUseCase = downloadCapturaUseCase;
        this.updateCapturaStatusUseCase = updateCapturaStatusUseCase;
    }

        @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de vídeos", description = "Permite upload de um ou múltiplos vídeos simultaneamente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Arquivo inválido ou muito grande"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
        @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))))
        public ResponseEntity<UploadResponse> upload(
            @Parameter(description = "ID do usuário (simulado para testes)", example = "1")
            @RequestParam(defaultValue = "1") Long userId,

            @Parameter(description = "Email do usuário (simulado para testes)", example = "user@example.com")
            @RequestParam(defaultValue = "user@fiap.com.br") String email,

            @Parameter(description = "Arquivos de vídeo para upload")
            @RequestPart("files") MultipartFile[] files) {

        List<CapturaDTO> capturas = uploadCapturaUseCase.execute(userId, email, files);

        UploadResponse response = new UploadResponse(
                "Upload realizado com sucesso",
                capturas,
                files.length
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    @Operation(summary = "Listar capturas", description = "Lista todas as capturas do usuário autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<List<CapturaDTO>> list(
            @Parameter(description = "ID do usuário (simulado para testes)", example = "1")
            @RequestParam(defaultValue = "1") Long userId) {

        List<CapturaDTO> capturas = listCapturasUseCase.execute(userId);
        return ResponseEntity.ok(capturas);
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "Download de vídeo", description = "Permite o download do vídeo da captura")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Arquivo retornado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso não autorizado"),
        @ApiResponse(responseCode = "404", description = "Captura não encontrada")
    })
    public ResponseEntity<Resource> download(
            @Parameter(description = "ID da captura", required = true)
            @PathVariable Long id,

            @Parameter(description = "ID do usuário (simulado para testes)", example = "1")
            @RequestParam(defaultValue = "1") Long userId) {

        File file = downloadCapturaUseCase.execute(id, userId);
        Captura captura = downloadCapturaUseCase.getCaptura(id, userId);

        Resource resource = new FileSystemResource(file);

        String filename = file.getName();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PutMapping("/update-status/{id}")
    @Operation(summary = "Atualizar status", description = "Atualiza o status da captura")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Captura não encontrada")
    })
    public ResponseEntity<CapturaDTO> updateStatus(
            @Parameter(description = "ID da captura", required = true)
            @PathVariable Long id,

            @RequestBody UpdateStatusRequest request) {

        CapturaStatus novoStatus = CapturaStatus.valueOf(request.getStatus().toUpperCase());
        CapturaDTO captura = updateCapturaStatusUseCase.execute(id, novoStatus);

        return ResponseEntity.ok(captura);
    }

    @GetMapping
    @Operation(summary = "Info do recurso Capturas", description = "Retorna informações rápidas sobre os endpoints de capturas")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> info = new HashMap<>();
        info.put("base", "/api/capturas");
        info.put("endpoints", Arrays.asList(
                "POST /upload",
                "GET /list?userId={userId}",
                "GET /download/{id}?userId={userId}",
                "PUT /update-status/{id}"
        ));
        info.put("swagger", "/api/swagger-ui/index.html");
        return ResponseEntity.ok(info);
    }
}
