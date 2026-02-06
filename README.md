# Módulo de Gerenciamento de Capturas de Vídeo

## Visão Geral
Módulo completo para gerenciamento de capturas de vídeo com upload, listagem, download e processamento assíncrono, seguindo os princípios de **Arquitetura Limpa**.

## Arquitetura

```
src/main/java/com/fiap/fiapx/
├── domain/                           # Camada de Domínio (Regras de Negócio)
│   ├── entities/
│   │   ├── Captura.java             # Entidade de domínio
│   │   └── CapturaStatus.java       # Enum de status
│   ├── repositories/
│   │   └── CapturaRepository.java   # Interface de repositório
│   └── exception/
│       ├── CapturaNotFoundException.java
│       ├── InvalidFileException.java
│       └── UnauthorizedAccessException.java
│
├── application/                      # Camada de Aplicação (Casos de Uso)
│   ├── usecases/
│   │   ├── UploadCapturaUseCase.java
│   │   ├── ListCapturasUseCase.java
│   │   ├── DownloadCapturaUseCase.java
│   │   └── UpdateCapturaStatusUseCase.java
│   └── dto/
│       ├── CapturaDTO.java
│       ├── UpdateStatusRequest.java
│       └── UploadResponse.java
│
├── adapters/                         # Camada de Adaptadores
│   └── gateway/
│       └── CapturaRepositoryImpl.java
│
└── external/                         # Camada Externa (Frameworks)
    ├── api/
    │   ├── CapturaController.java
    │   └── GlobalExceptionHandler.java
    ├── datasource/
    │   ├── entities/
    │   │   └── CapturaEntity.java
    │   └── repositories/
    │       └── JpaCapturaRepository.java
    ├── storage/
    │   └── FileStorageService.java
    └── queue/
        └── MessageQueueService.java
```

## Banco de Dados

### Tabela: capturas

| Campo      | Tipo          | Descrição                              |
|------------|---------------|----------------------------------------|
| id         | BIGINT        | Identificador único (auto-incremento)  |
| id_user    | BIGINT        | ID do usuário                          |
| email      | VARCHAR       | Email do usuário                       |
| status     | VARCHAR(20)   | Status da captura (enum)               |
| path       | VARCHAR       | Caminho do arquivo                     |
| created_at | TIMESTAMP     | Data de criação                        |
| updated_at | TIMESTAMP     | Data de atualização                    |

### Status Disponíveis
- `PENDENTE` - Captura pendente de processamento
- `PROCESSANDO` - Captura em processamento
- `CONCLUIDO` - Processamento concluído
- `ERRO` - Erro no processamento

## API REST

### Base URL
```
http://localhost:8888/api/capturas
```

### Endpoints

#### 1. Upload de Vídeos
```http
POST /upload
Content-Type: multipart/form-data
```

**Query Parameters:**
- `userId` (Long) - ID do usuário
- `email` (String) - Email do usuário

**Form Data:**
- `files` (MultipartFile[]) - Um ou mais arquivos de vídeo

**Exemplo:**
```bash
curl -X POST "http://localhost:8888/api/capturas/upload?userId=1&email=user@fiap.com.br" \
  -F "files=@video1.mp4;type=video/mp4" \
  -F "files=@video2.mp4;type=video/mp4"
```

**Resposta:**
```json
{
  "message": "Upload realizado com sucesso",
  "capturas": [
    {
      "id": 1,
      "idUser": 1,
      "email": "user@fiap.com.br",
      "status": "PROCESSANDO",
      "path": "./uploads/capturas/uuid.mp4",
      "createdAt": "2026-02-06T22:10:29.679377",
      "updatedAt": "2026-02-06T22:10:29.679386"
    }
  ],
  "totalFiles": 1
}
```

#### 2. Listar Capturas
```http
GET /list?userId={userId}
```

**Exemplo:**
```bash
curl "http://localhost:8888/api/capturas/list?userId=1"
```

**Resposta:**
```json
[
  {
    "id": 1,
    "idUser": 1,
    "email": "user@fiap.com.br",
    "status": "CONCLUIDO",
    "path": "./uploads/capturas/uuid.mp4",
    "createdAt": "2026-02-06T22:10:29.679377",
    "updatedAt": "2026-02-06T22:10:36.533675"
  }
]
```

#### 3. Download de Vídeo
```http
GET /download/{id}?userId={userId}
```

**Exemplo:**
```bash
curl -O "http://localhost:8888/api/capturas/download/1?userId=1"
```

**Resposta:** Arquivo binário com headers:
```
Content-Disposition: attachment; filename="uuid.mp4"
Content-Type: application/octet-stream
```

#### 4. Atualizar Status
```http
PUT /update-status/{id}
Content-Type: application/json
```

**Body:**
```json
{
  "status": "CONCLUIDO"
}
```

**Exemplo:**
```bash
curl -X PUT "http://localhost:8888/api/capturas/update-status/1" \
  -H "Content-Type: application/json" \
  -d '{"status": "CONCLUIDO"}'
```

## Variáveis de Ambiente

### Configuração de Storage
```yaml
CAPTURAS_STORAGE_DISK=local          # Tipo de storage (local ou s3 no futuro)
CAPTURAS_STORAGE_PATH=./uploads/capturas  # Caminho local de armazenamento
CAPTURAS_MAX_VIDEO_SIZE=100          # Tamanho máximo em MB
```

### Configuração no application.yml
```yaml
capturas:
  storage:
    disk: ${CAPTURAS_STORAGE_DISK:local}
    local-path: ${CAPTURAS_STORAGE_PATH:./uploads/capturas}
  max-video-size: ${CAPTURAS_MAX_VIDEO_SIZE:100}

spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 100MB
      max-request-size: 500MB
```

## Fluxo de Upload e Processamento

1. **Recebimento**: Controller recebe um ou múltiplos arquivos
2. **Validação**: FileStorageService valida tamanho e tipo
3. **Armazenamento**: Arquivo salvo localmente com UUID único
4. **Registro**: Captura criada no banco com status `PROCESSANDO`
5. **Fila**: Mensagem enviada para fila de processamento assíncrono
6. **Payload da Fila**:
```json
{
  "id": 1,
  "id_user": 1,
  "email": "user@fiap.com.br",
  "video": "./uploads/capturas/uuid.mp4"
}
```

## Segurança e Validações

### Validações Implementadas
- ✅ Validação de tipo de arquivo (apenas vídeos)
- ✅ Validação de tamanho máximo configurável
- ✅ Validação de propriedade (usuário só acessa suas capturas)
- ✅ Validação de existência de captura

### Controle de Acesso
- Listagem: Retorna apenas capturas do usuário autenticado
- Download: Valida se a captura pertence ao usuário solicitante
- Resposta 403 (Forbidden) em caso de acesso não autorizado

## Testes Realizados

### 1. Upload Único
```bash
curl -X POST "http://localhost:8888/api/capturas/upload?userId=1&email=user@fiap.com.br" \
  -F "files=@video.mp4;type=video/mp4"
```
✅ Status: 200 OK

### 2. Upload Múltiplo
```bash
curl -X POST "http://localhost:8888/api/capturas/upload?userId=1&email=user@fiap.com.br" \
  -F "files=@video1.mp4;type=video/mp4" \
  -F "files=@video2.mp4;type=video/mp4"
```
✅ Status: 200 OK - 2 capturas criadas

### 3. Listagem
```bash
curl "http://localhost:8888/api/capturas/list?userId=1"
```
✅ Status: 200 OK - Retornou 3 capturas

### 4. Download Autorizado
```bash
curl -I "http://localhost:8888/api/capturas/download/1?userId=1"
```
✅ Status: 200 OK

### 5. Download Não Autorizado
```bash
curl "http://localhost:8888/api/capturas/download/1?userId=2"
```
✅ Status: 403 Forbidden - Mensagem: "Você não tem permissão para acessar esta captura"

### 6. Atualização de Status
```bash
curl -X PUT "http://localhost:8888/api/capturas/update-status/1" \
  -H "Content-Type: application/json" \
  -d '{"status": "CONCLUIDO"}'
```
✅ Status: 200 OK - Status atualizado

### 7. Processamento Assíncrono
✅ Logs confirmam envio para fila:
```
INFO c.f.f.e.queue.MessageQueueService : Mensagem enviada para fila de processamento
```

## Swagger UI

Acesse a documentação interativa em:
```
http://localhost:8888/api/swagger-ui/index.html
```

## Migração Futura para AWS S3

A arquitetura está preparada para migração para S3:

1. Atualizar `CAPTURAS_STORAGE_DISK=s3`
2. Implementar novo `S3StorageService`
3. Configurar credenciais AWS
4. Nenhuma alteração nas regras de negócio necessária

### Exemplo de Implementação S3 (Futuro)
```java
@Service
@ConditionalOnProperty(name = "capturas.storage.disk", havingValue = "s3")
public class S3StorageService implements StorageService {
    @Value("${aws.s3.bucket}")
    private String bucketName;

    // Implementação com AWS SDK
}
```

## Docker

### Executar a aplicação
```bash
docker-compose up --build -d
```

### Verificar logs
```bash
docker-compose logs -f app
```

### Parar containers
```bash
docker-compose down
```

## Resumo de Funcionalidades Implementadas

✅ Tabela `capturas` com todos os campos solicitados
✅ Endpoints REST: GET /list, GET /download/{id}, PUT /update-status/{id}, POST /upload
✅ Upload único e múltiplo de vídeos
✅ Validação de tamanho configurável
✅ Validação de tipo de arquivo
✅ Storage local funcional
✅ Processamento assíncrono com fila
✅ Controle de acesso por usuário
✅ Arquitetura limpa totalmente implementada
✅ Variáveis de ambiente configuráveis
✅ Documentação Swagger
✅ Tratamento de exceções global
✅ Preparado para migração futura S3
