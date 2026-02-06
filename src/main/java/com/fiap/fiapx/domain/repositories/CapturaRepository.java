package com.fiap.fiapx.domain.repositories;

import com.fiap.fiapx.domain.entities.Captura;

import java.util.List;
import java.util.Optional;

public interface CapturaRepository {
    Captura save(Captura captura);
    Optional<Captura> findById(Long id);
    List<Captura> findByUserId(Long userId);
    void deleteById(Long id);
    List<Captura> findAll();
}
