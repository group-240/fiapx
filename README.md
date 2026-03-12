# FIAPX - Plataforma de Gerenciamento de Capturas de Vídeo

## Visão Geral

O FIAPX é uma plataforma de processamento de vídeo composta por dois microsserviços com responsabilidades distintas, comunicação assíncrona via RabbitMQ e armazenamento em nuvem (AWS S3). O sistema segue os princípios de **Arquitetura Limpa** e foi projetado para ser escalável, testável e desacoplado.

- [EventStorming](https://miro.com/app/board/uXjVGUWBbfY=/?share_link_id=812892120415)
- [Documentação da API](http://localhost:8888/api/swagger-ui/index.html)
- [Guia de Testes](./TEST.md)

---

## Arquitetura do Sistema

```
┌─────────────────────────────────────────────────────────────────────┐
│                          Cliente / Frontend                         │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ HTTP REST
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│             fiapx-captura-manager  (porta 8888)                     │
│                                                                     │
│  ┌─────────────┐  ┌─────────────┐  ┌────────────┐  ┌───────────┐    │
│  │  Controller │→ │  Use Cases  │→ │  Gateway   │→ │PostgreSQL │    │
│  │   (REST)    │  │ (Aplicação) │  │Repositório │  │  (DB)     │    │
│  └─────────────┘  └─────┬───────┘  └────────────┘  └───────────┘    │
│                          │ Publica mensagem                         │
│                          ▼                                          │
│                  ┌──────────────────┐                               │
│                  │FileStorageService│ (armazenamento local)         │
│                  └──────────────────┘                               │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ AMQP (video-processing-exchange)
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         RabbitMQ (porta 5672)                       │
│          Queue: video-processing-queue                              │
│          Exchange: video-processing-exchange                        │
│          Routing Key: video.processing                              │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ Consome mensagem
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│            fiapx-ms-processing  (porta 8080)                        │
│                                                                     │
│  ┌───────────────┐  ┌──────────────┐  ┌───────────────────────────┐ │
│  │VideoController│→ │UploadCaptura │→ │  FFmpegVideoProcessor     │ │
│  │   (REST)      │  │  UseCase     │  │  (extração de frames)     │ │
│  └───────────────┘  └──────┬───────┘  └────────────┬──────────────┘ │
│                            │                       │                │
│                    ┌───────▼─────────┐      ┌────────▼──────────┐   │
│                    │EmailNotification│      │ S3StorageAdapter  │   │
│                    │   Adapter       │      │    (AWS S3)       │   │
│                    └─────────────────┘      └───────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                                │ PUT /update-status
                                ▼
                    fiapx-captura-manager (callback de status)
```

---

## Microsserviços

### 1. fiapx-captura-manager (porta 8888)

Responsável pelo ciclo de vida das capturas de vídeo: recebimento, listagem, download e gerenciamento de status.

**Stack:** Java 17 · Spring Boot 3.3.5 · PostgreSQL 15 · RabbitMQ · Maven

**Arquitetura interna (Clean Architecture):**

```
src/main/java/com/fiap/fiapx/
├── domain/                             # Camada de Domínio
│   ├── entities/
│   │   ├── Captura.java
│   │   └── CapturaStatus.java
│   ├── repositories/
│   │   └── CapturaRepository.java
│   └── exception/
│       ├── CapturaNotFoundException.java
│       ├── InvalidFileException.java
│       ├── UnauthorizedAccessException.java
│       ├── VideoProcessingException.java
│       ├── VideoProcessingErrorException.java
│       └── ExternalServiceUnavailableException.java
│
├── application/                        # Camada de Aplicação
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
├── adapters/                           # Camada de Adaptadores
│   └── gateway/
│       └── CapturaRepositoryImpl.java
│
└── external/                           # Camada de Frameworks/Drivers
    ├── api/
    │   ├── CapturaController.java
    │   └── GlobalExceptionHandler.java
    ├── datasource/
    │   ├── entities/CapturaEntity.java
    │   └── repositories/JpaCapturaRepository.java
    ├── storage/
    │   └── FileStorageService.java
    ├── queue/
    │   └── MessageQueueService.java
    └── http/
        └── FramesServiceHttpAdapter.java
```

**Banco de Dados — tabela `capturas`:**

| Campo        | Tipo          | Descrição                             |
|--------------|---------------|---------------------------------------|
| `id`         | BIGINT (PK)   | Identificador único (auto-incremento) |
| `id_user`    | BIGINT        | ID do usuário                         |
| `email`      | VARCHAR       | Email do usuário                      |
| `status`     | VARCHAR(20)   | `PENDENTE` · `PROCESSANDO` · `CONCLUIDO` · `ERRO` |
| `path`       | VARCHAR       | Caminho do arquivo armazenado         |
| `created_at` | TIMESTAMP     | Data de criação                       |
| `updated_at` | TIMESTAMP     | Data de atualização                   |

---

### 2. fiapx-ms-processing

Responsável pela extração de frames dos vídeos e armazenamento no AWS S3.

---

## Comunicação entre Microsserviços

### Fluxo Completo

```
1. Cliente envia vídeo via POST /api/capturas/upload
2. fiapx-captura-manager:
   a. Valida o arquivo (tipo, tamanho)
   b. Registra no banco com status PROCESSANDO
   c. Publica mensagem na fila RabbitMQ

3. RabbitMQ entrega a mensagem ao fiapx-ms-processing
4. fiapx-ms-processing:
   a. Recebe o vídeo
   b. Extrai frames com FFmpeg (JavaCV)
   c. Compacta frames em ZIP
   d. Faz upload do ZIP para AWS S3
   e. Envia PUT /api/capturas/update-status/{id} com status CONCLUIDO ou ERRO
   f. Em caso de ERRO: envia e-mail ao usuário

5. Cliente consulta status via GET /api/capturas/list?userId={userId}
6. Cliente baixa o resultado via GET /api/capturas/download/{id}?userId={userId}
```

### Mensagem na Fila (RabbitMQ)

```json
{
  "id": 1,
  "id_user": 123,
  "email": "user@fiap.com.br",
  "videoPath": "./uploads/capturas/video-uuid.mp4",
  "video": "<byte array do vídeo>"
}
```

**Configuração RabbitMQ:**

| Parâmetro    | Valor                      |
|--------------|----------------------------|
| Exchange     | `video-processing-exchange` |
| Queue        | `video-processing-queue`   |
| Routing Key  | `video.processing`         |
| Tipo         | Topic (durável)            |
| Formato      | JSON                       |

---
