# BFF Service - Backend for Frontend
FROM openjdk:17-jdk-alpine

# Metadatos del contenedor
LABEL description="BFF Service para orquestación de microservicios financieros"
LABEL version="1.0.0"

# Crear usuario no-root por seguridad
RUN addgroup -g 1001 -S financial && \
    adduser -u 1001 -S financial -G financial

# Variables de entorno optimizadas para Java
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=docker

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR compilado (asumiendo que ya está construido)
COPY target/bff-service-*.jar app.jar

# Cambiar ownership del archivo
RUN chown financial:financial app.jar

# Cambiar al usuario no-root
USER financial

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]