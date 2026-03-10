package com.fiap.fiapx.external.storage;

import com.fiap.fiapx.domain.exception.InvalidFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${capturas.max-video-size:100}")
    private Long maxVideoSizeMB;

    public FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String store(MultipartFile file) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String s3Key = "uploads/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Upload para S3 concluído: {}/{}", bucket, s3Key);
            return s3Key;

        } catch (IOException e) {
            throw new InvalidFileException("Erro ao fazer upload para S3: " + e.getMessage(), e);
        }
    }

    public byte[] load(String s3Key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build();

        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
        return response.asByteArray();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Arquivo vazio ou nulo");
        }

        long fileSizeMB = file.getSize() / (1024 * 1024);
        if (fileSizeMB > maxVideoSizeMB) {
            throw new InvalidFileException(
                    String.format("Arquivo muito grande. Máximo: %d MB, enviado: %d MB", maxVideoSizeMB, fileSizeMB));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new InvalidFileException("Apenas arquivos de vídeo são permitidos");
        }
    }
}
