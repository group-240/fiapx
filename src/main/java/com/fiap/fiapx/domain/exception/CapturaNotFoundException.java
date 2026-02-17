package com.fiap.fiapx.domain.exception;

public class CapturaNotFoundException extends RuntimeException {
    public CapturaNotFoundException(Long id) {
        super("Captura com ID " + id + " não encontrada");
    }
}
