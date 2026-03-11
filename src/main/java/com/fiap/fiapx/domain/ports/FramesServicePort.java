package com.fiap.fiapx.domain.ports;

import java.io.InputStream;

public interface FramesServicePort {
    /**
     * Busca o arquivo zip de frames processados de um vídeo
     * @param videoId ID da transação/vídeo
     * @return InputStream do arquivo zip
     */
    InputStream downloadFramesZip(Long videoId);
}
