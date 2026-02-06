package com.fiap.fiapx.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de requisição para atualização de status")
public class UpdateStatusRequest {
    
    @Schema(description = "Novo status da captura", example = "CONCLUIDO")
    private String status;
}
