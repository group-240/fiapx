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
@Schema(
        description = "DTO de resposta de captura de vídeo",
        example = """
                {
                  "id": 1,
                  "idUser": 1,
                  "email": "user@fiap.com.br",
                  "status": "PROCESSANDO",
                  "path": "./uploads/capturas/123e4567-e89b-12d3-a456-426614174000.mp4",
                  "createdAt": "2026-03-05T10:30:00",
                  "updatedAt": "2026-03-05T10:30:00"
                }
                """
)
public class CapturaDTO {

    @Schema(
            description = "ID único da captura no banco de dados",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "ID do usuário proprietário da captura",
            example = "1"
    )
    private Long idUser;

    @Schema(
            description = "Email do usuário proprietário da captura",
            example = "user@fiap.com.br"
    )
    private String email;

    @Schema(
            description = "Status atual do processamento da captura. Valores possíveis: PROCESSANDO, CONCLUIDO, ERRO",
            example = "PROCESSANDO",
            allowableValues = {"PROCESSANDO", "CONCLUIDO", "ERRO"}
    )
    private CapturaStatus status;

    @Schema(
            description = "Caminho do arquivo de vídeo no sistema de armazenamento",
            example = "./uploads/capturas/123e4567-e89b-12d3-a456-426614174000.mp4"
    )
    private String path;

    @Schema(
            description = "Data e hora de criação da captura",
            example = "2026-03-05T10:30:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Data e hora da última atualização da captura",
            example = "2026-03-05T10:30:00"
    )
    private LocalDateTime updatedAt;
}
