package com.fiap.fiapx.application.usecases;

import com.fiap.fiapx.application.dto.CapturaDTO;
import com.fiap.fiapx.domain.entities.Captura;
import com.fiap.fiapx.domain.entities.CapturaStatus;
import com.fiap.fiapx.domain.exception.CapturaNotFoundException;
import com.fiap.fiapx.domain.repositories.CapturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCapturaStatusUseCase {
    
    private final CapturaRepository capturaRepository;
    
    public UpdateCapturaStatusUseCase(CapturaRepository capturaRepository) {
        this.capturaRepository = capturaRepository;
    }
    
    @Transactional
    public CapturaDTO execute(Long capturaId, CapturaStatus novoStatus) {
        Captura captura = capturaRepository.findById(capturaId)
                .orElseThrow(() -> new CapturaNotFoundException(capturaId));
        
        captura.atualizarStatus(novoStatus);
        
        Captura updatedCaptura = capturaRepository.save(captura);
        
        return toDTO(updatedCaptura);
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
