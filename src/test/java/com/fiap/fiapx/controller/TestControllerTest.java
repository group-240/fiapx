package com.fiap.fiapx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.fiapx.dto.MessageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestController.class)
@WithMockUser
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /test/hello - deve retornar saudação padrão")
    void deveRetornarSaudacaoPadrao() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/test/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Hello FIAPX!")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /test/hello - deve retornar timestamp válido")
    void deveRetornarTimestampValido() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/test/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    @DisplayName("GET /test/hello/{name} - deve retornar saudação personalizada")
    void deveRetornarSaudacaoPersonalizada() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/test/hello/Lorraine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Hello Lorraine!")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /test/hello/{name} - deve funcionar com nomes diferentes")
    void deveFuncionarComNomesDiferentes() throws Exception {
        // Test 1
        mockMvc.perform(get("/test/hello/João"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Hello João!")));

        // Test 2
        mockMvc.perform(get("/test/hello/Maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Hello Maria!")));
    }

    @Test
    @DisplayName("GET /test/hello/{name} - deve funcionar com nomes compostos")
    void deveFuncionarComNomesCompostos() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/test/hello/Maria Silva"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Hello Maria Silva!")));
    }

    @Test
    @DisplayName("POST /test/echo - deve retornar a mesma mensagem enviada")
    void deveRetornarMesmaMensagem() throws Exception {
        // Arrange
        MessageResponse request = new MessageResponse("Test message", LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(post("/test/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Test message")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("POST /test/echo - deve preservar timestamp original")
    void devePreservarTimestampOriginal() throws Exception {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.of(2026, 2, 6, 10, 30);
        MessageResponse request = new MessageResponse("Test", timestamp);

        // Act & Assert
        mockMvc.perform(post("/test/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Test")))
                .andExpect(jsonPath("$.timestamp").value(containsString("2026-02-06")));
    }

    @Test
    @DisplayName("POST /test/echo - deve funcionar com mensagens vazias")
    void deveFuncionarComMensagensVazias() throws Exception {
        // Arrange
        MessageResponse request = new MessageResponse("", LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(post("/test/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("")));
    }

    @Test
    @DisplayName("GET /test/info - deve retornar informações da aplicação")
    void deveRetornarInformacoesDaAplicacao() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/test/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application", is("FIAPX API")))
                .andExpect(jsonPath("$.version", is("1.0.0")))
                .andExpect(jsonPath("$.status", is("running")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /test/info - deve ter todos os campos obrigatórios")
    void deveTerTodosCamposObrigatorios() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/test/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").exists())
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.application").isNotEmpty())
                .andExpect(jsonPath("$.version").isNotEmpty())
                .andExpect(jsonPath("$.status").isNotEmpty());
    }

    @Test
    @DisplayName("GET /test/info - deve retornar timestamp recente")
    void deveRetornarTimestampRecente() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/test/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isString());
    }
}
