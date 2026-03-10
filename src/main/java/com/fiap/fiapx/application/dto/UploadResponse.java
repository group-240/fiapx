package com.fiap.fiapx.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Resposta de upload de vídeos",
        example = """
                {
                  "message": "Upload realizado com sucesso",
                  "totalFiles": 2,
                  "capturas": [
                    {
                      "id": 1,
                      "idUser": 1,
                      "email": "user@fiap.com.br",
                      "status": "PROCESSANDO",
                      "path": "./uploads/capturas/123e4567-e89b-12d3-a456-426614174000.mp4",
                      "createdAt": "2026-03-05T10:30:00",
                      "updatedAt": "2026-03-05T10:30:00"
                    }
                  ]
                }
                """
)
public class UploadResponse {

    @Schema(
            description = "Mensagem de sucesso da operação",
            example = "Upload realizado com sucesso"
    )
    private String message;

    @Schema(
            description = "Lista de capturas criadas com os detalhes de cada vídeo enviado",
            implementation = CapturaDTO.class
    )
    private List<CapturaDTO> capturas;

    @Schema(
            description = "Total de arquivos enviados na requisição",
            example = "2",
            minimum = "1"
    )
    private Integer totalFiles;
}
