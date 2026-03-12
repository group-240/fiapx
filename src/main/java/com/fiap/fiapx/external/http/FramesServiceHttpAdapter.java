package com.fiap.fiapx.external.http;

import com.fiap.fiapx.domain.exception.ExternalServiceUnavailableException;
import com.fiap.fiapx.domain.ports.FramesServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
public class FramesServiceHttpAdapter implements FramesServicePort {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String downloadEndpoint;

    public FramesServiceHttpAdapter(
            RestTemplate restTemplate,
            @Value("${external.frames-service.base-url}") String baseUrl,
            @Value("${external.frames-service.download-endpoint}") String downloadEndpoint) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.downloadEndpoint = downloadEndpoint;
    }

    @Override
    public InputStream downloadFramesZip(Long videoId) {
        String url = baseUrl + downloadEndpoint + "/" + videoId;
        
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    byte[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return new ByteArrayInputStream(response.getBody());
            }

            throw new ExternalServiceUnavailableException(
                    "O serviço de processamento de vídeos não retornou o arquivo esperado. " +
                    "Por favor, tente novamente mais tarde."
            );
            
        } catch (ResourceAccessException e) {
            throw new ExternalServiceUnavailableException(
                    "O serviço de processamento de vídeos está temporariamente indisponível. " +
                    "Por favor, tente novamente em alguns instantes.",
                    e
            );
        } catch (HttpClientErrorException.NotFound e) {
            throw new ExternalServiceUnavailableException(
                    "Os frames processados do vídeo não foram encontrados no serviço externo. " +
                    "O processamento pode ainda não ter sido concluído.",
                    e
            );
        } catch (HttpServerErrorException e) {
            throw new ExternalServiceUnavailableException(
                    "O serviço de processamento de vídeos está com problemas técnicos. " +
                    "Por favor, tente novamente mais tarde.",
                    e
            );
        } catch (Exception e) {
            throw new ExternalServiceUnavailableException(
                    "Não foi possível comunicar com o serviço de processamento de vídeos. " +
                    "Por favor, contacte o suporte se o problema persistir.",
                    e
            );
        }
    }
}
