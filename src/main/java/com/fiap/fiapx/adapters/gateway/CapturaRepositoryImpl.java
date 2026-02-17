package com.fiap.fiapx.adapters.gateway;

import com.fiap.fiapx.domain.entities.Captura;
import com.fiap.fiapx.domain.repositories.CapturaRepository;
import com.fiap.fiapx.external.datasource.entities.CapturaEntity;
import com.fiap.fiapx.external.datasource.repositories.JpaCapturaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CapturaRepositoryImpl implements CapturaRepository {

    private final JpaCapturaRepository jpaCapturaRepository;

    public CapturaRepositoryImpl(JpaCapturaRepository jpaCapturaRepository) {
        this.jpaCapturaRepository = jpaCapturaRepository;
    }

    @Override
    public Captura save(Captura captura) {
        CapturaEntity entity = toEntity(captura);
        CapturaEntity savedEntity = jpaCapturaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Captura> findById(Long id) {
        return jpaCapturaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<Captura> findByUserId(Long userId) {
        return jpaCapturaRepository.findByIdUser(userId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaCapturaRepository.deleteById(id);
    }

    @Override
    public List<Captura> findAll() {
        return jpaCapturaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private CapturaEntity toEntity(Captura captura) {
        CapturaEntity entity = new CapturaEntity();
        entity.setId(captura.getId());
        entity.setIdUser(captura.getIdUser());
        entity.setEmail(captura.getEmail());
        entity.setStatus(captura.getStatus());
        entity.setPath(captura.getPath());
        entity.setCreatedAt(captura.getCreatedAt());
        entity.setUpdatedAt(captura.getUpdatedAt());
        return entity;
    }

    private Captura toDomain(CapturaEntity entity) {
        return new Captura(
                entity.getId(),
                entity.getIdUser(),
                entity.getEmail(),
                entity.getStatus(),
                entity.getPath(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
