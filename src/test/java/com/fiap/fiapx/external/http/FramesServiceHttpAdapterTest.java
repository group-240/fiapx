package com.fiap.fiapx.external.http;

import com.fiap.fiapx.domain.exception.ExternalServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FramesServiceHttpAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    private FramesServiceHttpAdapter framesServiceHttpAdapter;

    private final String baseUrl = "http://localhost:8080";
    private final String downloadEndpoint = "/api/capturas/download";

    @BeforeEach
    void setUp() {
        framesServiceHttpAdapter = new FramesServiceHttpAdapter(
                restTemplate,
                baseUrl,
                downloadEndpoint
        );
    }

    @Test
    @DisplayName("Deve fazer download do zip com sucesso")
    void deveFazerDownloadDoZipComSucesso() throws IOException {
        // Arrange
        byte[] zipData = "PK\u0003\u0004test".getBytes();
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(zipData, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(baseUrl + downloadEndpoint + "/1"),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class)
        )).thenReturn(responseEntity);

        // Act
        InputStream result = framesServiceHttpAdapter.downloadFramesZip(1L);

        // Assert
        assertNotNull(result);
        
        // Verificar que o conteúdo está correto
        byte[] resultBytes = result.readAllBytes();
        assertArrayEquals(zipData, resultBytes);
        
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + downloadEndpoint + "/1"),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando resposta não contém body")
    void deveLancarExcecaoQuandoRespostaNaoContemBody() {
        // Arrange
        byte[] nullBody = null;
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(nullBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(baseUrl + downloadEndpoint + "/1"),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class)
        )).thenReturn(responseEntity);

        // Act & Assert
        assertThrows(
                ExternalServiceUnavailableException.class,
                () -> framesServiceHttpAdapter.downloadFramesZip(1L)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando resposta não é 2xx")
    void deveLancarExcecaoQuandoRespostaNao2xx() {
        // Arrange
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(new byte[0], HttpStatus.BAD_REQUEST);

        when(restTemplate.exchange(
                eq(baseUrl + downloadEndpoint + "/1"),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class)
        )).thenReturn(responseEntity);

        // Act & Assert
        assertThrows(
                ExternalServiceUnavailableException.class,
                () -> framesServiceHttpAdapter.downloadFramesZip(1L)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando serviço está indisponível (ResourceAccessException)")
    void deveLancarExcecaoQuandoServicoIndisponivel() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class)
        )).thenThrow(new ResourceAccessException("Connection refused"));

        // Act & Assert
        ExternalServiceUnavailableException exception = assertThrows(
                ExternalServiceUnavailableException.class,
                () -> framesServiceHttpAdapter.downloadFramesZip(1L)
        );

        assertTrue(exception.getMessage().contains("temporariamente indisponível"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof ResourceAccessException);
    }

    @Test
    @DisplayName("Deve lançar exceção quando frames não encontrados (404)")
    void deveLancarExcecaoQuandoFramesNaoEncontrados() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class)
        )).thenThrow(HttpClientErrorException.NotFound.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                null,
                null
        ));

        // Act & Assert
        ExternalServiceUnavailableException exception = assertThrows(
                ExternalServiceUnavailableException.class,
                () -> framesServiceHttpAdapter.downloadFramesZip(1L)
        );

        assertTrue(exception.getMessage().contains("não foram encontrados"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof HttpClientErrorException.NotFound);
    }

    @Test
    @DisplayName("Deve lançar exceção quando servidor externo tem erro (5xx)")
    void deveLancarExcecaoQuandoServidorExternoTemErro() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class)
        )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // Act & Assert
        ExternalServiceUnavailableException exception = assertThrows(
                ExternalServiceUnavailableException.class,
                () -> framesServiceHttpAdapter.downloadFramesZip(1L)
        );

        assertTrue(exception.getMessage().contains("problemas técnicos"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof HttpServerErrorException);
    }

    @Test
    @DisplayName("Deve lançar exceção genérica para outros erros")
    void deveLancarExcecaoGenericaParaOutrosErros() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class)
        )).thenThrow(new RuntimeException("Erro desconhecido"));

        // Act & Assert
        ExternalServiceUnavailableException exception = assertThrows(
                ExternalServiceUnavailableException.class,
                () -> framesServiceHttpAdapter.downloadFramesZip(1L)
        );

        assertTrue(exception.getMessage().contains("Não foi possível comunicar"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof RuntimeException);
    }

    @Test
    @DisplayName("Deve construir URL correta para download")
    void deveConstruirUrlCorretaParaDownload() {
        // Arrange
        byte[] zipData = "test".getBytes();
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(zipData, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8080/api/capturas/download/123"),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class)
        )).thenReturn(responseEntity);

        // Act
        framesServiceHttpAdapter.downloadFramesZip(123L);

        // Assert
        verify(restTemplate, times(1)).exchange(
                eq("http://localhost:8080/api/capturas/download/123"),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class)
        );
    }

    @Test
    @DisplayName("Deve funcionar com diferentes IDs de vídeo")
    void deveFuncionarComDiferentesIdsDeVideo() {
        // Arrange
        byte[] zipData = "test".getBytes();
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(zipData, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class)
        )).thenReturn(responseEntity);

        // Act
        framesServiceHttpAdapter.downloadFramesZip(1L);
        framesServiceHttpAdapter.downloadFramesZip(999L);
        framesServiceHttpAdapter.downloadFramesZip(12345L);

        // Assert
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + downloadEndpoint + "/1"),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class)
        );
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + downloadEndpoint + "/999"),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class)
        );
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + downloadEndpoint + "/12345"),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class)
        );
    }
}
