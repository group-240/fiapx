package com.fiap.fiapx.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Endpoints de health check")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health Check", description = "Verifica se a aplicação está rodando")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Aplicação está saudável")
    })
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "FIAPX");
        return ResponseEntity.ok(response);
    }
}
