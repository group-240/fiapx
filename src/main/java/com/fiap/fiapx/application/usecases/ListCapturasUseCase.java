package com.fiap.fiapx.application.usecases;

import com.fiap.fiapx.application.dto.CapturaDTO;
import com.fiap.fiapx.domain.entities.Captura;
import com.fiap.fiapx.domain.repositories.CapturaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListCapturasUseCase {

    private final CapturaRepository capturaRepository;

    public ListCapturasUseCase(CapturaRepository capturaRepository) {
        this.capturaRepository = capturaRepository;
    }

    public List<CapturaDTO> execute(Long userId) {
        List<Captura> capturas = capturaRepository.findByUserId(userId);
        return capturas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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
