#!/bin/sh
# Arranque de la imagen unica.
#
#   APP_MODULE=all       (por defecto) customer + products en 127.0.0.1 y BFF en $PORT
#   APP_MODULE=bff       solo bff-service en $PORT
#   APP_MODULE=customer  solo customer-service en $PORT
#   APP_MODULE=products  solo financial-products-service en $PORT
set -eu

APP_MODULE="${APP_MODULE:-all}"
PORT="${PORT:-8080}"
CUSTOMER_PORT="${CUSTOMER_PORT:-8081}"
PRODUCTS_PORT="${PRODUCTS_PORT:-8082}"
JAVA_OPTS_BFF="${JAVA_OPTS_BFF:--Xms64m -Xmx280m -XX:+UseSerialGC}"
JAVA_OPTS_INTERNAL="${JAVA_OPTS_INTERNAL:--Xms48m -Xmx220m -XX:+UseSerialGC}"
JAVA_OPTS_SINGLE="${JAVA_OPTS:--XX:MaxRAMPercentage=75 -XX:+UseSerialGC}"
STARTUP_TIMEOUT="${STARTUP_TIMEOUT:-120}"

log() { echo "[entrypoint] $*"; }

# Espera a que un puerto local responda al health de actuator.
wait_for_health() {
  port="$1"; name="$2"; pid="$3"
  i=0
  while [ "$i" -lt "$STARTUP_TIMEOUT" ]; do
    if ! kill -0 "$pid" 2>/dev/null; then
      log "ERROR: $name termino durante el arranque"
      return 1
    fi
    if wget -q -O /dev/null "http://127.0.0.1:${port}/actuator/health" 2>/dev/null; then
      log "$name listo en 127.0.0.1:${port} (${i}s)"
      return 0
    fi
    i=$((i + 1))
    sleep 1
  done
  log "ERROR: $name no respondio en ${STARTUP_TIMEOUT}s"
  return 1
}

case "$APP_MODULE" in
  all)
    log "modo all: customer + products internos, BFF en el puerto ${PORT}"

    # Los servicios internos escuchan SOLO en loopback: no se publican fuera del contenedor.
    # shellcheck disable=SC2086
    java $JAVA_OPTS_INTERNAL -jar /app/customer-service.jar \
      --server.port="${CUSTOMER_PORT}" --server.address=127.0.0.1 &
    CUSTOMER_PID=$!

    # shellcheck disable=SC2086
    java $JAVA_OPTS_INTERNAL -jar /app/financial-products-service.jar \
      --server.port="${PRODUCTS_PORT}" --server.address=127.0.0.1 &
    PRODUCTS_PID=$!

    # customer-service es quien crea el esquema y carga el seed de la demo, asi que se espera a
    # que este listo antes de exponer el BFF.
    wait_for_health "${CUSTOMER_PORT}" "customer-service" "${CUSTOMER_PID}"
    wait_for_health "${PRODUCTS_PORT}" "financial-products-service" "${PRODUCTS_PID}"

    export SERVICES_CUSTOMER_BASE_URL="${SERVICES_CUSTOMER_BASE_URL:-http://127.0.0.1:${CUSTOMER_PORT}}"
    export SERVICES_FINANCIAL_PRODUCTS_BASE_URL="${SERVICES_FINANCIAL_PRODUCTS_BASE_URL:-http://127.0.0.1:${PRODUCTS_PORT}}"

    log "arrancando bff-service en 0.0.0.0:${PORT}"
    # shellcheck disable=SC2086
    exec java $JAVA_OPTS_BFF -jar /app/bff-service.jar --server.port="${PORT}"
    ;;
  bff)
    # shellcheck disable=SC2086
    exec java $JAVA_OPTS_SINGLE -jar /app/bff-service.jar --server.port="${PORT}"
    ;;
  customer)
    # shellcheck disable=SC2086
    exec java $JAVA_OPTS_SINGLE -jar /app/customer-service.jar --server.port="${PORT}"
    ;;
  products)
    # shellcheck disable=SC2086
    exec java $JAVA_OPTS_SINGLE -jar /app/financial-products-service.jar --server.port="${PORT}"
    ;;
  *)
    log "ERROR: APP_MODULE='${APP_MODULE}' no valido (all | bff | customer | products)"
    exit 2
    ;;
esac
