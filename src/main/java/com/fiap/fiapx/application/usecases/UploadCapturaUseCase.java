package com.fiap.fiapx.application.usecases;

import com.fiap.fiapx.application.dto.CapturaDTO;
import com.fiap.fiapx.domain.entities.Captura;
import com.fiap.fiapx.domain.repositories.CapturaRepository;
import com.fiap.fiapx.external.queue.MessageQueueService;
import com.fiap.fiapx.external.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class UploadCapturaUseCase {
    
    private final CapturaRepository capturaRepository;
    private final FileStorageService fileStorageService;
    private final MessageQueueService messageQueueService;
    
    public UploadCapturaUseCase(CapturaRepository capturaRepository,
                                FileStorageService fileStorageService,
                                MessageQueueService messageQueueService) {
        this.capturaRepository = capturaRepository;
        this.fileStorageService = fileStorageService;
        this.messageQueueService = messageQueueService;
    }
    
    @Transactional
    public List<CapturaDTO> execute(Long userId, String email, MultipartFile[] files) {
        List<CapturaDTO> capturasList = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                // 1. Salvar arquivo no storage
                String path = fileStorageService.store(file);
                
                // 2. Criar registro no banco de dados
                Captura captura = new Captura();
                captura.setIdUser(userId);
                captura.setEmail(email);
                captura.iniciarProcessamento();
                captura.setPath(path);
                
                Captura savedCaptura = capturaRepository.save(captura);
                
                // 3. Enviar para fila de processamento
                messageQueueService.sendToProcessingQueue(
                        savedCaptura.getId(),
                        savedCaptura.getIdUser(),
                        savedCaptura.getEmail(),
                        savedCaptura.getPath()
                );
                
                // 4. Adicionar à lista de resposta
                capturasList.add(toDTO(savedCaptura));
                
            } catch (Exception e) {
                // Log do erro mas continua processando outros arquivos
                // (tratamento transacional parcial)
                throw new RuntimeException("Erro ao processar arquivo: " + file.getOriginalFilename(), e);
            }
        }
        
        return capturasList;
    }
    
    private CapturaDTO toDTO(Captura captura) {
        return new CapturaDTO(
                captura.getId(),
                captura.getIdUser(),
                captura.getEmail(),
                captura.getStatus(),
                captura.getPath(),
                captura.getCreatedAt(),
                captura.getUpdatedAt()
        );
    }
}
