package com.fiap.fiapx.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta com mensagem")
public class MessageResponse {
    
    @Schema(description = "Mensagem de resposta", example = "Hello FIAPX!")
    private String message;
    
    @Schema(description = "Timestamp da resposta")
    private LocalDateTime timestamp;
}
