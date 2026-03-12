# FIAPX API (`fiapx`)

API principal do ecossistema FIAPX. Este serviço recebe uploads de vídeo, persiste metadados no PostgreSQL, armazena os arquivos no S3 e publica eventos no RabbitMQ para processamento assíncrono pelo `fiapx-ms-processing`.

---

## Responsabilidade no sistema

- Expor endpoints REST para o cliente (upload, listagem, download e status)
- Persistir estado da transação de vídeo no banco relacional
- Validar arquivo enviado (tamanho/tipo/extensão)
- Enviar mensagens para a fila de processamento
- Proteger API e expor métricas/saúde para observabilidade

---

## Arquitetura (Clean Architecture)

```text
src/main/java/com/fiap/fiapx/
├── domain/        -> Entidades, regras e exceções de domínio
├── application/   -> Casos de uso (upload/list/download/update)
├── adapters/      -> Implementações de gateway (persistência)
├── external/      -> API REST, storage S3, queue RabbitMQ, datasource
├── config/        -> Segurança, AWS, RabbitMQ, Swagger
└── controller/    -> Endpoints auxiliares (health/test)
```

### Componentes chave

- `CapturaController`: endpoints funcionais de capturas
- `UploadCapturaUseCase`: orquestra upload + persistência + fila
- `FileStorageService`: upload/download em S3
- `MessageQueueService`: publicação no RabbitMQ
- `GlobalExceptionHandler`: padronização de erros

---

## Endpoints principais

Base pública no cluster: `http://<LB>/api`

| Método | Rota | Responsável por |
|---|---|---|
| `GET` | `/health` | Health check da aplicação |
| `GET` | `/test/hello` | Sanidade rápida de API |
| `POST` | `/capturas/upload?userId=...&email=...` | Upload de um ou mais vídeos |
| `GET` | `/capturas/list?userId=...` | Listagem de capturas por usuário |
| `GET` | `/capturas/download/{id}?userId=...` | Download do ZIP processado |
| `PUT` | `/capturas/update-status/{id}` | Atualização de status (integração interna) |

Swagger:
- `/api/swagger-ui.html`
- `/api/api-docs`

Actuator/Prometheus:
- `/api/actuator/health`
- `/api/actuator/prometheus`

---

## Fluxo ponta a ponta

1. Cliente envia vídeo em `POST /api/capturas/upload`
2. `FileStorageService` salva no S3 (`uploads/{uuid}.mp4`)
3. `UploadCapturaUseCase` grava registro em `capturas` com status inicial
4. `MessageQueueService` publica evento no RabbitMQ
5. `fiapx-ms-processing` consome, extrai frames e atualiza status
6. Cliente consulta em `/api/capturas/list` e baixa em `/api/capturas/download/{id}`

---

## Variáveis de ambiente importantes

| Variável | Uso |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Conexão PostgreSQL |
| `AWS_REGION`, `S3_BUCKET` | Acesso ao S3 |
| `RABBITMQ_HOST`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD` | Conexão RabbitMQ |
| `RABBITMQ_QUEUE`, `RABBITMQ_EXCHANGE`, `RABBITMQ_ROUTING_KEY` | Roteamento de mensagens |
| `CAPTURAS_MAX_VIDEO_SIZE` | Limite de tamanho do upload |

---

## Banco de dados (referência de schema)

> O projeto usa JPA com `ddl-auto=update`. Abaixo um DDL de referência para entrega/documentação.

```sql
CREATE TABLE IF NOT EXISTS capturas (
  id BIGSERIAL PRIMARY KEY,
  id_user BIGINT NOT NULL,
  email VARCHAR(255) NOT NULL,
  status VARCHAR(20) NOT NULL,
  path VARCHAR(1024) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
```

---

## Testes de API

- Insomnia: `insomnia_capturas_export.json`
- Bruno: pasta `Capturas FIAPX/` com fluxo E2E ordenado

Ordem sugerida:
1. `01 - Health`
2. `03 - Upload`
3. `04 - List` (polling)
4. `05 - Download ZIP`

---

## CI/CD

Workflow: `.github/workflows/ci-cd.yml`

- Build Maven (com `-Dmaven.test.skip=true` no pipeline)
- Build/push imagem para ECR
- Apply do deployment no EKS
- Rollout status

Branches com gatilho automático:
- `fix`
- `main`
