package com.fiap.fiapx.application.usecases;

import com.fiap.fiapx.domain.entities.Captura;
import com.fiap.fiapx.domain.exception.CapturaNotFoundException;
import com.fiap.fiapx.domain.exception.UnauthorizedAccessException;
import com.fiap.fiapx.domain.repositories.CapturaRepository;
import com.fiap.fiapx.external.storage.FileStorageService;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class DownloadCapturaUseCase {
    
    private final CapturaRepository capturaRepository;
    private final FileStorageService fileStorageService;
    
    public DownloadCapturaUseCase(CapturaRepository capturaRepository,
                                  FileStorageService fileStorageService) {
        this.capturaRepository = capturaRepository;
        this.fileStorageService = fileStorageService;
    }
    
    public File execute(Long capturaId, Long userId) {
        // Buscar captura
        Captura captura = capturaRepository.findById(capturaId)
                .orElseThrow(() -> new CapturaNotFoundException(capturaId));
        
        // Validar se pertence ao usuário
        if (!captura.pertenceAoUsuario(userId)) {
            throw new UnauthorizedAccessException("Você não tem permissão para acessar esta captura");
        }
        
        // Retornar arquivo
        return fileStorageService.load(captura.getPath());
    }
    
    public Captura getCaptura(Long capturaId, Long userId) {
        Captura captura = capturaRepository.findById(capturaId)
                .orElseThrow(() -> new CapturaNotFoundException(capturaId));
        
        if (!captura.pertenceAoUsuario(userId)) {
            throw new UnauthorizedAccessException("Você não tem permissão para acessar esta captura");
        }
        
        return captura;
    }
}
