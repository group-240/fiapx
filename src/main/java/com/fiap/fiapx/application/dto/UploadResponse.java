package com.fiap.fiapx.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta de upload de vídeos")
public class UploadResponse {
    
    @Schema(description = "Mensagem de sucesso")
    private String message;
    
    @Schema(description = "Capturas criadas")
    private List<CapturaDTO> capturas;
    
    @Schema(description = "Total de arquivos enviados")
    private Integer totalFiles;
}
