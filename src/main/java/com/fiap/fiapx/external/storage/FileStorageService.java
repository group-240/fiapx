package com.fiap.fiapx.external.storage;

import com.fiap.fiapx.domain.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    
    @Value("${capturas.storage.disk:local}")
    private String storageDisk;
    
    @Value("${capturas.storage.local-path:./uploads/capturas}")
    private String localStoragePath;
    
    @Value("${capturas.max-video-size:100}")
    private Long maxVideoSizeMB;
    
    public String store(MultipartFile file) {
        validateFile(file);
        
        try {
            // Criar diretório se não existir
            Path uploadPath = Paths.get(localStoragePath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Gerar nome único para o arquivo
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
            String filename = UUID.randomUUID().toString() + extension;
            
            // Salvar arquivo
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return filePath.toString();
            
        } catch (IOException e) {
            throw new InvalidFileException("Erro ao salvar arquivo: " + e.getMessage(), e);
        }
    }
    
    public File load(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new InvalidFileException("Arquivo não encontrado: " + path);
        }
        return file;
    }
    
    public void delete(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            throw new InvalidFileException("Erro ao deletar arquivo: " + e.getMessage(), e);
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Arquivo vazio ou nulo");
        }
        
        // Validar tamanho
        long fileSizeMB = file.getSize() / (1024 * 1024);
        if (fileSizeMB > maxVideoSizeMB) {
            throw new InvalidFileException(
                    String.format("Arquivo muito grande. Tamanho máximo: %d MB, Tamanho enviado: %d MB", 
                            maxVideoSizeMB, fileSizeMB)
            );
        }
        
        // Validar tipo de arquivo (vídeos)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new InvalidFileException("Apenas arquivos de vídeo são permitidos");
        }
    }
}
