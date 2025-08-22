# Customer Service - Microservicio de Clientes
FROM openjdk:17-jdk-alpine

# Metadatos del contenedor
LABEL maintainer="financial-team@company.com"
LABEL description="Customer Service para gestión de información de clientes"
LABEL version="1.0.0"

# Instalar curl para health checks
RUN apk --no-cache add curl

# Crear usuario no-root por seguridad
RUN addgroup -g 1001 -S financial && \
    adduser -u 1001 -S financial -G financial

# Variables de entorno optimizadas
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=dev

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR compilado
COPY target/customer-service-*.jar app.jar

# Cambiar ownership
RUN chown financial:financial app.jar

# Cambiar al usuario no-root
USER financial

# Puerto expuesto
EXPOSE 8081

# Health check específico para customer service
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]