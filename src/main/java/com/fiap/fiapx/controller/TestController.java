package com.fiap.fiapx.controller;

import com.fiap.fiapx.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@Tag(name = "Test", description = "Endpoints de teste da aplicação")
public class TestController {

    @GetMapping("/hello")
    @Operation(summary = "Hello World", description = "Endpoint simples para testar se a API está funcionando")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sucesso")
    })
    public ResponseEntity<MessageResponse> hello() {
        return ResponseEntity.ok(new MessageResponse("Hello FIAPX!", LocalDateTime.now()));
    }

    @GetMapping("/hello/{name}")
    @Operation(summary = "Hello com nome", description = "Retorna uma saudação personalizada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sucesso")
    })
    public ResponseEntity<MessageResponse> helloName(
            @Parameter(description = "Nome para personalizar a saudação", required = true)
            @PathVariable String name) {
        return ResponseEntity.ok(new MessageResponse("Hello " + name + "!", LocalDateTime.now()));
    }

    @PostMapping("/echo")
    @Operation(summary = "Echo", description = "Retorna a mesma mensagem enviada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sucesso")
    })
    public ResponseEntity<MessageResponse> echo(@RequestBody MessageResponse message) {
        return ResponseEntity.ok(message);
    }

    @GetMapping("/info")
    @Operation(summary = "Informações da API", description = "Retorna informações sobre a aplicação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sucesso")
    })
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "FIAPX API");
        info.put("version", "1.0.0");
        info.put("timestamp", LocalDateTime.now());
        info.put("status", "running");
        return ResponseEntity.ok(info);
    }
}
