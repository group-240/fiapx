FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM maven:3.9-eclipse-temurin-17
WORKDIR /app

# Copiar o JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Copiar código fonte e pom.xml para permitir execução de testes
COPY pom.xml .
COPY src ./src

# Instalar curl para health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Variáveis de ambiente padrão para JVM (podem ser sobrescritas no Kubernetes)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC"

EXPOSE 8888

# Usar JAVA_OPTS no entrypoint
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]