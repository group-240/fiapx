package com.fiap.fiapx.external.storage;

import com.fiap.fiapx.domain.exception.InvalidFileException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private String testStoragePath;

    @BeforeEach
    void setUp() throws IOException {
        fileStorageService = new FileStorageService();
        testStoragePath = "./test-uploads/capturas";

        ReflectionTestUtils.setField(fileStorageService, "storageDisk", "local");
        ReflectionTestUtils.setField(fileStorageService, "localStoragePath", testStoragePath);
        ReflectionTestUtils.setField(fileStorageService, "maxVideoSizeMB", 100L);

        // Criar diretório de teste
        Path path = Paths.get(testStoragePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Limpar arquivos de teste
        Path path = Paths.get(testStoragePath);
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            // Ignorar erros de limpeza
                        }
                    });
        }
    }

    @Test
    @DisplayName("Deve armazenar arquivo de vídeo com sucesso")
    void deveArmazenarArquivoComSucesso() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "conteudo do video".getBytes()
        );

        // Act
        String path = fileStorageService.store(file);

        // Assert
        assertNotNull(path);
        assertTrue(path.contains(testStoragePath));
        assertTrue(path.endsWith(".mp4"));
        assertTrue(new File(path).exists());
    }

    @Test
    @DisplayName("Deve gerar nome único para cada arquivo")
    void deveGerarNomeUnicoParaCadaArquivo() {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "conteudo1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "conteudo2".getBytes()
        );

        // Act
        String path1 = fileStorageService.store(file1);
        String path2 = fileStorageService.store(file2);

        // Assert
        assertNotEquals(path1, path2);
        assertTrue(new File(path1).exists());
        assertTrue(new File(path2).exists());
    }

    @Test
    @DisplayName("Deve preservar extensão do arquivo original")
    void devePreservarExtensaoDoArquivoOriginal() {
        // Arrange
        MockMultipartFile mp4File = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "conteudo".getBytes()
        );

        MockMultipartFile aviFile = new MockMultipartFile(
                "file",
                "video.avi",
                "video/avi",
                "conteudo".getBytes()
        );

        // Act
        String mp4Path = fileStorageService.store(mp4File);
        String aviPath = fileStorageService.store(aviFile);

        // Assert
        assertTrue(mp4Path.endsWith(".mp4"));
        assertTrue(aviPath.endsWith(".avi"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando arquivo é nulo")
    void deveLancarExcecaoQuandoArquivoNulo() {
        // Act & Assert
        InvalidFileException exception = assertThrows(
                InvalidFileException.class,
                () -> fileStorageService.store(null)
        );

        assertEquals("Arquivo vazio ou nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando arquivo está vazio")
    void deveLancarExcecaoQuandoArquivoVazio() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                new byte[0]
        );

        // Act & Assert
        InvalidFileException exception = assertThrows(
                InvalidFileException.class,
                () -> fileStorageService.store(emptyFile)
        );

        assertEquals("Arquivo vazio ou nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando tipo de arquivo não é vídeo")
    void deveLancarExcecaoQuandoTipoNaoEVideo() {
        // Arrange
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "image.jpg",
                "image/jpeg",
                "conteudo da imagem".getBytes()
        );

        // Act & Assert
        InvalidFileException exception = assertThrows(
                InvalidFileException.class,
                () -> fileStorageService.store(imageFile)
        );

        // Extension is validated first
        assertTrue(exception.getMessage().contains("Extensão de arquivo não permitida"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando content type não é vídeo mas extensão é válida")
    void deveLancarExcecaoQuandoContentTypeNaoEVideoMasExtensaoValida() {
        // Arrange - file with valid extension but wrong content type
        MockMultipartFile fileWithWrongType = new MockMultipartFile(
                "file",
                "video.mp4",
                "application/pdf",
                "conteudo".getBytes()
        );

        // Act & Assert
        InvalidFileException exception = assertThrows(
                InvalidFileException.class,
                () -> fileStorageService.store(fileWithWrongType)
        );

        assertTrue(exception.getMessage().contains("Tipo de arquivo não permitido"));
        assertTrue(exception.getMessage().contains("Apenas vídeos são aceitos"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando content type é nulo")
    void deveLancarExcecaoQuandoContentTypeNulo() {
        // Arrange
        MockMultipartFile fileWithoutType = new MockMultipartFile(
                "file",
                "video.mp4",
                null,
                "conteudo".getBytes()
        );

        // Act & Assert
        InvalidFileException exception = assertThrows(
                InvalidFileException.class,
                () -> fileStorageService.store(fileWithoutType)
        );

        assertTrue(exception.getMessage().contains("Tipo de arquivo não permitido"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando arquivo excede tamanho máximo")
    void deveLancarExcecaoQuandoArquivoExcedeTamanhoMaximo() {
        // Arrange
        ReflectionTestUtils.setField(fileStorageService, "maxVideoSizeMB", 1L);

        // 2 MB de dados
        byte[] largeContent = new byte[2 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-video.mp4",
                "video/mp4",
                largeContent
        );

        // Act & Assert
        InvalidFileException exception = assertThrows(
                InvalidFileException.class,
                () -> fileStorageService.store(largeFile)
        );

        assertTrue(exception.getMessage().contains("Arquivo muito grande"));
        assertTrue(exception.getMessage().contains("Tamanho máximo: 1 MB"));
    }

    @Test
    @DisplayName("Deve aceitar arquivo no limite do tamanho máximo")
    void deveAceitarArquivoNoLimiteDoTamanhoMaximo() {
        // Arrange
        ReflectionTestUtils.setField(fileStorageService, "maxVideoSizeMB", 1L);

        // 1 MB de dados (exatamente no limite)
        byte[] content = new byte[1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                content
        );

        // Act
        String path = fileStorageService.store(file);

        // Assert
        assertNotNull(path);
        assertTrue(new File(path).exists());
    }

    @Test
    @DisplayName("Deve carregar arquivo existente com sucesso")
    void deveCarregarArquivoExistenteComSucesso() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "conteudo do video".getBytes()
        );
        String storedPath = fileStorageService.store(file);

        // Act
        File loadedFile = fileStorageService.load(storedPath);

        // Assert
        assertNotNull(loadedFile);
        assertTrue(loadedFile.exists());
        assertEquals(storedPath, loadedFile.getPath());
    }

    @Test
    @DisplayName("Deve lançar exceção ao carregar arquivo inexistente")
    void deveLancarExcecaoAoCarregarArquivoInexistente() {
        // Arrange
        String nonExistentPath = "./test-uploads/capturas/nonexistent.mp4";

        // Act & Assert
        InvalidFileException exception = assertThrows(
                InvalidFileException.class,
                () -> fileStorageService.load(nonExistentPath)
        );

        assertTrue(exception.getMessage().contains("Arquivo não encontrado"));
    }

    @Test
    @DisplayName("Deve deletar arquivo existente com sucesso")
    void deveDeletarArquivoExistenteComSucesso() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "conteudo do video".getBytes()
        );
        String storedPath = fileStorageService.store(file);
        assertTrue(new File(storedPath).exists());

        // Act
        fileStorageService.delete(storedPath);

        // Assert
        assertFalse(new File(storedPath).exists());
    }

    @Test
    @DisplayName("Deve não lançar exceção ao deletar arquivo inexistente")
    void deveNaoLancarExcecaoAoDeletarArquivoInexistente() {
        // Arrange
        String nonExistentPath = "./test-uploads/capturas/nonexistent.mp4";

        // Act & Assert
        assertDoesNotThrow(() -> fileStorageService.delete(nonExistentPath));
    }

    @Test
    @DisplayName("Deve criar diretório se não existir")
    void deveCriarDiretorioSeNaoExistir() throws IOException {
        // Arrange
        String newStoragePath = "./test-uploads/new-capturas";
        ReflectionTestUtils.setField(fileStorageService, "localStoragePath", newStoragePath);

        // Garantir que o diretório não existe
        Path path = Paths.get(newStoragePath);
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            // Ignorar
                        }
                    });
        }

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "conteudo".getBytes()
        );

        // Act
        String storedPath = fileStorageService.store(file);

        // Assert
        assertTrue(Files.exists(Paths.get(newStoragePath)));
        assertTrue(new File(storedPath).exists());

        // Cleanup
        Files.walk(path)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        // Ignorar
                    }
                });
    }

    @Test
    @DisplayName("Deve aceitar diferentes tipos de vídeo")
    void deveAceitarDiferentesTiposDeVideo() {
        // Arrange & Act & Assert
        String[] videoTypes = {"video/mp4", "video/avi", "video/quicktime", "video/webm"};
        String[] extensions = {".mp4", ".avi", ".mov", ".webm"};

        for (int i = 0; i < videoTypes.length; i++) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "video" + extensions[i],
                    videoTypes[i],
                    "conteudo".getBytes()
            );

            String path = fileStorageService.store(file);
            assertNotNull(path);
            assertTrue(new File(path).exists());
        }
    }

    @Test
    @DisplayName("Deve lançar exceção quando extensão não é permitida")
    void deveLancarExcecaoQuandoExtensaoNaoPermitida() {
        // Arrange
        MockMultipartFile fileWithBadExtension = new MockMultipartFile(
                "file",
                "video.txt",
                "video/mp4",
                "conteudo".getBytes()
        );

        // Act & Assert
        InvalidFileException exception = assertThrows(
                InvalidFileException.class,
                () -> fileStorageService.store(fileWithBadExtension)
        );

        assertTrue(exception.getMessage().contains("Extensão de arquivo não permitida"));
        assertTrue(exception.getMessage().contains("Extensões aceitas"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome do arquivo é nulo")
    void deveLancarExcecaoQuandoNomeArquivoNulo() {
        // Arrange
        MockMultipartFile fileWithNullName = new MockMultipartFile(
                "file",
                null,
                "video/mp4",
                "conteudo".getBytes()
        );

        // Act & Assert
        InvalidFileException exception = assertThrows(
                InvalidFileException.class,
                () -> fileStorageService.store(fileWithNullName)
        );

        assertEquals("Nome do arquivo inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome do arquivo é vazio")
    void deveLancarExcecaoQuandoNomeArquivoVazio() {
        // Arrange
        MockMultipartFile fileWithBlankName = new MockMultipartFile(
                "file",
                "   ",
                "video/mp4",
                "conteudo".getBytes()
        );

        // Act & Assert
        InvalidFileException exception = assertThrows(
                InvalidFileException.class,
                () -> fileStorageService.store(fileWithBlankName)
        );

        assertEquals("Nome do arquivo inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando arquivo não tem extensão")
    void deveLancarExcecaoQuandoArquivoSemExtensao() {
        // Arrange
        MockMultipartFile fileWithoutExtension = new MockMultipartFile(
                "file",
                "video",
                "video/mp4",
                "conteudo".getBytes()
        );

        // Act & Assert
        InvalidFileException exception = assertThrows(
                InvalidFileException.class,
                () -> fileStorageService.store(fileWithoutExtension)
        );

        assertTrue(exception.getMessage().contains("Extensão de arquivo não permitida"));
    }

    @Test
    @DisplayName("Deve aceitar arquivo MPEG com sucesso")
    void deveAceitarArquivoMpegComSucesso() {
        // Arrange
        MockMultipartFile mpegFile = new MockMultipartFile(
                "file",
                "video.mpeg",
                "video/mpeg",
                "conteudo do video mpeg".getBytes()
        );

        // Act
        String path = fileStorageService.store(mpegFile);

        // Assert
        assertNotNull(path);
        assertTrue(path.endsWith(".mpeg"));
        assertTrue(new File(path).exists());
    }

    @Test
    @DisplayName("Deve aceitar arquivo AVI com sucesso")
    void deveAceitarArquivoAviComSucesso() {
        // Arrange
        MockMultipartFile aviFile = new MockMultipartFile(
                "file",
                "video.avi",
                "video/x-msvideo",
                "conteudo do video avi".getBytes()
        );

        // Act
        String path = fileStorageService.store(aviFile);

        // Assert
        assertNotNull(path);
        assertTrue(path.endsWith(".avi"));
        assertTrue(new File(path).exists());
    }

    @Test
    @DisplayName("Deve aceitar arquivo MOV com sucesso")
    void deveAceitarArquivoMovComSucesso() {
        // Arrange
        MockMultipartFile movFile = new MockMultipartFile(
                "file",
                "video.mov",
                "video/quicktime",
                "conteudo do video mov".getBytes()
        );

        // Act
        String path = fileStorageService.store(movFile);

        // Assert
        assertNotNull(path);
        assertTrue(path.endsWith(".mov"));
        assertTrue(new File(path).exists());
    }

    @Test
    @DisplayName("Deve aceitar arquivo WebM com sucesso")
    void deveAceitarArquivoWebMComSucesso() {
        // Arrange
        MockMultipartFile webmFile = new MockMultipartFile(
                "file",
                "video.webm",
                "video/webm",
                "conteudo do video webm".getBytes()
        );

        // Act
        String path = fileStorageService.store(webmFile);

        // Assert
        assertNotNull(path);
        assertTrue(path.endsWith(".webm"));
        assertTrue(new File(path).exists());
    }

    @Test
    @DisplayName("Deve validar extensão case-insensitive")
    void deveValidarExtensaoCaseInsensitive() {
        // Arrange
        MockMultipartFile fileUpperCase = new MockMultipartFile(
                "file",
                "video.MP4",
                "video/mp4",
                "conteudo".getBytes()
        );

        // Act
        String path = fileStorageService.store(fileUpperCase);

        // Assert
        assertNotNull(path);
        assertTrue(new File(path).exists());
    }
}

