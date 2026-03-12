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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4",
            "video/mpeg",
            "video/quicktime",
            "video/x-msvideo",
            "video/x-ms-wmv",
            "video/webm",
            "video/avi"
    );

    private static final List<String> ALLOWED_VIDEO_EXTENSIONS = Arrays.asList(
            ".mp4",
            ".mpeg",
            ".mpg",
            ".mov",
            ".avi",
            ".wmv",
            ".webm"
    );

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

        // Validar nome do arquivo
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidFileException("Nome do arquivo inválido");
        }

        // Validar extensão do arquivo
        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_VIDEO_EXTENSIONS.contains(fileExtension)) {
            throw new InvalidFileException(
                    String.format("Extensão de arquivo não permitida. Extensões aceitas: %s",
                            String.join(", ", ALLOWED_VIDEO_EXTENSIONS))
            );
        }

        // Validar tipo de conteúdo
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_VIDEO_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(
                    String.format("Tipo de arquivo não permitido. Apenas vídeos são aceitos. Tipos aceitos: %s",
                            String.join(", ", ALLOWED_VIDEO_TYPES))
            );
        }

        // Validar tamanho
        long fileSizeMB = file.getSize() / (1024 * 1024);
        if (fileSizeMB > maxVideoSizeMB) {
            throw new InvalidFileException(
                    String.format("Arquivo muito grande. Tamanho máximo: %d MB, Tamanho enviado: %d MB",
                            maxVideoSizeMB, fileSizeMB)
            );
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
