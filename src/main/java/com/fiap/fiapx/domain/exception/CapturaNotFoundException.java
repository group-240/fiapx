package com.fiap.fiapx.domain.exception;

public class CapturaNotFoundException extends RuntimeException {
    public CapturaNotFoundException(Long id) {
        super("Captura não encontrada com ID: " + id);
    }
}
