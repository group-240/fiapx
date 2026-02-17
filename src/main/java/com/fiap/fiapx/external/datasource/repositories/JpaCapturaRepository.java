package com.fiap.fiapx.external.datasource.repositories;

import com.fiap.fiapx.external.datasource.entities.CapturaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaCapturaRepository extends JpaRepository<CapturaEntity, Long> {
    List<CapturaEntity> findByIdUser(Long idUser);
}
