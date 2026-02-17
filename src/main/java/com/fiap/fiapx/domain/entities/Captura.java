package com.fiap.fiapx.domain.entities;

import java.time.LocalDateTime;

public class Captura {
    private Long id;
    private Long idUser;
    private String email;
    private CapturaStatus status;
    private String path;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Captura() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = CapturaStatus.PENDENTE;
    }

    public Captura(Long id, Long idUser, String email, CapturaStatus status, String path,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.idUser = idUser;
        this.email = email;
        this.status = status;
        this.path = path;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Métodos de negócio
    public void iniciarProcessamento() {
        this.status = CapturaStatus.PROCESSANDO;
        this.updatedAt = LocalDateTime.now();
    }

    public void concluirProcessamento() {
        this.status = CapturaStatus.CONCLUIDO;
        this.updatedAt = LocalDateTime.now();
    }

    public void marcarErro() {
        this.status = CapturaStatus.ERRO;
        this.updatedAt = LocalDateTime.now();
    }

    public void atualizarStatus(CapturaStatus novoStatus) {
        this.status = novoStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean pertenceAoUsuario(Long userId) {
        return this.idUser.equals(userId);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public CapturaStatus getStatus() {
        return status;
    }

    public void setStatus(CapturaStatus status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
