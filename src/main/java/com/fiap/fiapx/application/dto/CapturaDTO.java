package com.fiap.fiapx.application.dto;

import com.fiap.fiapx.domain.entities.CapturaStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de resposta de captura")
public class CapturaDTO {
    
    @Schema(description = "ID da captura")
    private Long id;
    
    @Schema(description = "ID do usuário")
    private Long idUser;
    
    @Schema(description = "Email do usuário")
    private String email;
    
    @Schema(description = "Status da captura")
    private CapturaStatus status;
    
    @Schema(description = "Caminho do arquivo")
    private String path;
    
    @Schema(description = "Data de criação")
    private LocalDateTime createdAt;
    
    @Schema(description = "Data de atualização")
    private LocalDateTime updatedAt;
}
