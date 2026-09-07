# Imagen unica para los tres microservicios.
#
# Cloud Run solo admite un contenedor por servicio, asi que la demo publica arranca los tres
# modulos dentro de la misma imagen (APP_MODULE=all): customer-service y
# financial-products-service escuchan en 127.0.0.1:8081 / 127.0.0.1:8082 y el BFF ocupa $PORT,
# sirviendo en el mismo origen la pagina de demo, la API y Swagger.
#
# La misma imagen sirve para el despliegue clasico de un servicio por contenedor:
#   APP_MODULE=bff | customer | products
#
# Los Dockerfile por modulo que habia antes copiaban target/*.jar, es decir solo funcionaban si
# alguien habia ejecutado "mvn package" a mano previamente. Esta build es autocontenida.

# ---------- etapa de compilacion ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Los POM primero para aprovechar la cache de capas cuando solo cambia el codigo.
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY customer-service/pom.xml customer-service/pom.xml
COPY financial-products-service/pom.xml financial-products-service/pom.xml
COPY bff-service/pom.xml bff-service/pom.xml

COPY common/src common/src
COPY customer-service/src customer-service/src
COPY financial-products-service/src financial-products-service/src
COPY bff-service/src bff-service/src

ARG SKIP_TESTS=true
RUN mvn -B -ntp -DskipTests=${SKIP_TESTS} clean package

# ---------- etapa de ejecucion ----------
FROM eclipse-temurin:21-jre-alpine

# Sin Maven, sin JDK, sin codigo fuente y sin dependencias de test.
RUN addgroup -g 1001 -S financial \
 && adduser -u 1001 -S financial -G financial \
 && mkdir -p /app \
 && chown financial:financial /app

WORKDIR /app

COPY --from=build --chown=financial:financial /build/bff-service/target/bff-service-*.jar                             bff-service.jar
COPY --from=build --chown=financial:financial /build/customer-service/target/customer-service-*.jar                   customer-service.jar
COPY --from=build --chown=financial:financial /build/financial-products-service/target/financial-products-service-*.jar financial-products-service.jar
COPY --chown=financial:financial docker/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

USER financial

# all | bff | customer | products
ENV APP_MODULE=all \
    SPRING_PROFILES_ACTIVE=demo \
    PORT=8080 \
    JAVA_OPTS_BFF="-Xms64m -Xmx280m -XX:+UseSerialGC -XX:MaxMetaspaceSize=192m" \
    JAVA_OPTS_INTERNAL="-Xms48m -Xmx220m -XX:+UseSerialGC -XX:MaxMetaspaceSize=160m"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD wget -q -O /dev/null "http://127.0.0.1:${PORT}/health" || exit 1

ENTRYPOINT ["/app/entrypoint.sh"]
