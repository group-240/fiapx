# Teste Gerenciamento de Capturas de Vídeo

## 🧪 Executar Testes

### Opção 1: Dentro do Container (Recomendado)

```bash
# Executar testes no container da aplicação
docker exec -it fiapx-app mvn clean test

# Gerar relatório de cobertura
docker exec -it fiapx-app mvn clean test jacoco:report

# Verificar cobertura mínima de 80%
docker exec -it fiapx-app mvn clean verify
```

### Opção 2: Localmente com Maven

Se você tem Maven instalado localmente:

```bash
# Executar testes
mvn clean test

# Gerar relatório de cobertura
mvn clean test jacoco:report

# Verificar cobertura mínima de 80%
mvn clean verify
```

### Opção 3: Copiar Relatórios do Container

Para visualizar os relatórios gerados dentro do container:

```bash
# Gerar relatórios dentro do container
docker exec -it fiapx-app mvn clean verify

# Copiar relatórios para sua máquina
docker cp fiapx-app:/app/target/site/jacoco ./jacoco-report

# Ver relatório HTML (macOS)
open jacoco-report/index.html

# Ver relatório HTML (Linux)
xdg-open jacoco-report/index.html
```

## 📊 Relatórios

Após executar os testes, os relatórios ficam em:
- **Jacoco HTML**: `target/site/jacoco/index.html`
- **Jacoco XML**: `target/site/jacoco/jacoco.xml`
- **Surefire Reports**: `target/surefire-reports/`

## 🎯 Cobertura Configurada

O projeto está configurado para exigir no mínimo **80% de cobertura**:
- ✅ 80% de cobertura de linhas (LINE)
- ✅ 80% de cobertura de branches (BRANCH)
