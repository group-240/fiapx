package com.fiap.fiapx.application.usecases;

import com.fiap.fiapx.domain.entities.Captura;
import com.fiap.fiapx.domain.entities.CapturaStatus;
import com.fiap.fiapx.domain.exception.CapturaNotFoundException;
import com.fiap.fiapx.domain.exception.UnauthorizedAccessException;
import com.fiap.fiapx.domain.exception.VideoProcessingException;
import com.fiap.fiapx.domain.exception.VideoProcessingErrorException;
import com.fiap.fiapx.domain.ports.FramesServicePort;
import com.fiap.fiapx.domain.repositories.CapturaRepository;
import com.fiap.fiapx.external.storage.FileStorageService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

@Service
public class DownloadCapturaUseCase {

    private final CapturaRepository capturaRepository;
    private final FileStorageService fileStorageService;
    private final FramesServicePort framesServicePort;

    public DownloadCapturaUseCase(CapturaRepository capturaRepository,
                                  FileStorageService fileStorageService,
                                  FramesServicePort framesServicePort) {
        this.capturaRepository = capturaRepository;
        this.fileStorageService = fileStorageService;
        this.framesServicePort = framesServicePort;
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

    public InputStream downloadFramesZip(Long capturaId, Long userId) {
        // Buscar captura
        Captura captura = capturaRepository.findById(capturaId)
                .orElseThrow(() -> new CapturaNotFoundException(capturaId));

        // Validar se pertence ao usuário
        if (!captura.pertenceAoUsuario(userId)) {
            throw new UnauthorizedAccessException("Você não tem permissão para acessar esta captura");
        }

        // Validar status da captura - APENAS permite download se status for CONCLUIDO
        CapturaStatus status = captura.getStatus();
        
        if (status == CapturaStatus.PENDENTE || status == CapturaStatus.PROCESSANDO) {
            throw new VideoProcessingException("Vídeo em processamento ainda. Por favor, aguarde a conclusão.");
        }
        
        if (status == CapturaStatus.ERRO) {
            throw new VideoProcessingErrorException("Erro no processamento do vídeo. Não é possível realizar o download.");
        }

        // Neste ponto, o status só pode ser CONCLUIDO
        // Buscar o zip do serviço externo
        return framesServicePort.downloadFramesZip(capturaId);
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
