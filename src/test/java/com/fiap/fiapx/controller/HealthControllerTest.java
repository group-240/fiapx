package com.fiap.fiapx.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
@WithMockUser
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /health - deve retornar status UP")
    void deveRetornarStatusUp() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.application", is("FIAPX")));
    }

    @Test
    @DisplayName("GET /health - deve retornar Content-Type JSON")
    void deveRetornarContentTypeJson() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    @DisplayName("GET /health - deve ter os campos obrigatórios")
    void deveTerCamposObrigatorios() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.application").exists())
                .andExpect(jsonPath("$.status").isNotEmpty())
                .andExpect(jsonPath("$.application").isNotEmpty());
    }
}
