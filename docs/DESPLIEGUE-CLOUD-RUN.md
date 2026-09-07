# Despliegue en Google Cloud Run

Guia para publicar este repositorio como **demo publica** en Cloud Run con PostgreSQL gestionado
(Neon). La demo es de **solo lectura** y con **datos ficticios**.

---

## 1. Como encaja el proyecto en Cloud Run

Cloud Run impone tres cosas: un contenedor por servicio, escuchar en el puerto de la variable
`PORT`, y nada de estado en disco. El proyecto son tres microservicios, asi que el `Dockerfile`
de la raiz construye **una unica imagen** con los tres JAR y decide en el arranque que ejecutar:

| `APP_MODULE` | Que arranca | Para que |
|---|---|---|
| `all` (por defecto) | `customer-service` en `127.0.0.1:8081`, `financial-products-service` en `127.0.0.1:8082` y `bff-service` en `$PORT` | **Demo publica**: un solo servicio de Cloud Run, todo en el mismo origen |
| `bff` | solo `bff-service` en `$PORT` | Despliegue clasico: un servicio de Cloud Run por microservicio |
| `customer` | solo `customer-service` en `$PORT` | idem |
| `products` | solo `financial-products-service` en `$PORT` | idem |

En modo `all` los dos servicios internos escuchan **solo en loopback**: no son accesibles desde
fuera del contenedor. El BFF sirve en el mismo origen la pagina de demo (`/`), la API
(`/api/...`), Swagger UI (`/swagger-ui.html`) y el health (`/health`, `/actuator/health`).

```
Internet -> Cloud Run (1 contenedor)
              |-- bff-service            :$PORT   (publico: web + API + Swagger)
              |-- customer-service       127.0.0.1:8081
              |-- financial-products-svc 127.0.0.1:8082
                        |
                        +--> PostgreSQL gestionado (Neon), fuera de Cloud Run
```

---

## 2. Base de datos en Neon

1. Crear un proyecto en <https://neon.tech> y una base de datos (por ejemplo `financial_db`).
2. Copiar la cadena de conexion. Neon la da en formato JDBC/psql:

   ```
   postgresql://USUARIO:PASSWORD@ep-xxx-yyy.eu-central-1.aws.neon.tech/financial_db?sslmode=require
   ```

3. Traducirla a R2DBC (**sin usuario ni contrasena en la URL**, van en variables aparte, y con
   `sslMode` en camelCase, que es como lo espera el driver r2dbc-postgresql):

   ```
   SPRING_R2DBC_URL=r2dbc:postgresql://ep-xxx-yyy.eu-central-1.aws.neon.tech:5432/financial_db?sslMode=require
   SPRING_R2DBC_USERNAME=USUARIO
   SPRING_R2DBC_PASSWORD=PASSWORD
   ```

**El esquema y los datos se crean solos.** Con el perfil `demo`, `customer-service` ejecuta al
arrancar `customer-service/src/main/resources/db/demo-schema.sql` y `db/demo-seed.sql`. Ambos son
idempotentes (`CREATE TABLE IF NOT EXISTS`, `INSERT ... ON CONFLICT DO NOTHING`), asi que se
pueden ejecutar en cada arranque sin duplicar filas. `financial-products-service` no ejecuta DDL
para evitar carreras entre los dos.

Si prefieres cargar el esquema a mano, ejecuta esos dos ficheros con `psql` y arranca con
`SQL_INIT_MODE=never`.

---

## 3. Desplegar

### Opcion A — un solo servicio (la demo)

```bash
PROJECT_ID=tu-proyecto
REGION=europe-west1

gcloud run deploy financial-api-demo \
  --source . \
  --project "$PROJECT_ID" \
  --region "$REGION" \
  --allow-unauthenticated \
  --port 8080 \
  --memory 2Gi \
  --cpu 2 \
  --min-instances 0 \
  --max-instances 3 \
  --timeout 60 \
  --set-env-vars 'APP_MODULE=all,SPRING_PROFILES_ACTIVE=demo,DEMO_ENABLED=true,SQL_INIT_MODE=always,ENCRYPTION_SECRET_KEY=MySecretKey12345' \
  --set-env-vars '^@^SPRING_R2DBC_URL=r2dbc:postgresql://ep-xxx.eu-central-1.aws.neon.tech:5432/financial_db?sslMode=require' \
  --set-secrets 'SPRING_R2DBC_USERNAME=neon-user:latest,SPRING_R2DBC_PASSWORD=neon-password:latest'
```

Notas:

- `^@^` cambia el separador de `--set-env-vars` a `@` para que la `,` de la URL no rompa el
  parseo. Alternativa: usar `--env-vars-file env.yaml`.
- Usuario y contrasena van en **Secret Manager**, no en `--set-env-vars`.
- **2 GiB y 2 vCPU** en modo `all`: son tres JVM en el mismo contenedor (medido en local:
  ~575 MiB en reposo). Con 1 vCPU el arranque en frio se alarga bastante.
- Arranque en frio medido: ~20 s hasta que `customer-service` responde y ~35-50 s hasta que el
  BFF acepta trafico. Si Cloud Run marca fallo de arranque, sube el *startup probe timeout* a
  120 s o usa `--min-instances 1`.

Construir y subir la imagen a mano en lugar de `--source .`:

```bash
gcloud builds submit --tag "$REGION-docker.pkg.dev/$PROJECT_ID/demos/financial-api:latest"
gcloud run deploy financial-api-demo \
  --image "$REGION-docker.pkg.dev/$PROJECT_ID/demos/financial-api:latest" ...
```

### Opcion B — tres servicios de Cloud Run desde la misma imagen

```bash
IMG="$REGION-docker.pkg.dev/$PROJECT_ID/demos/financial-api:latest"

# 1) customer-service (interno)
gcloud run deploy customer-service --image "$IMG" --region "$REGION" --no-allow-unauthenticated \
  --memory 1Gi --set-env-vars 'APP_MODULE=customer,SPRING_PROFILES_ACTIVE=demo,SQL_INIT_MODE=always' \
  --set-secrets 'SPRING_R2DBC_USERNAME=neon-user:latest,SPRING_R2DBC_PASSWORD=neon-password:latest'

# 2) financial-products-service (interno)
gcloud run deploy financial-products-service --image "$IMG" --region "$REGION" --no-allow-unauthenticated \
  --memory 1Gi --set-env-vars 'APP_MODULE=products,SPRING_PROFILES_ACTIVE=demo,SQL_INIT_MODE=never' \
  --set-secrets 'SPRING_R2DBC_USERNAME=neon-user:latest,SPRING_R2DBC_PASSWORD=neon-password:latest'

# 3) bff-service (publico), apuntando a las URL de los dos anteriores
gcloud run deploy bff-service --image "$IMG" --region "$REGION" --allow-unauthenticated \
  --memory 1Gi --set-env-vars "APP_MODULE=bff,SPRING_PROFILES_ACTIVE=demo,DEMO_ENABLED=true,SERVICES_CUSTOMER_BASE_URL=https://customer-service-xxx.run.app,SERVICES_FINANCIAL_PRODUCTS_BASE_URL=https://financial-products-service-xxx.run.app"
```

Con `--no-allow-unauthenticated` hay que dar al service account del BFF el rol
`roles/run.invoker` sobre los otros dos y adjuntar un token de identidad en las llamadas: el
`WebClient` actual **no lo hace**. Para una demo es mas simple la opcion A.

---

## 4. Variables de entorno

### Imprescindibles

| Variable | Ejemplo | Para que |
|---|---|---|
| `SPRING_R2DBC_URL` | `r2dbc:postgresql://ep-xxx.neon.tech:5432/financial_db?sslMode=require` | Base de datos (R2DBC, con TLS) |
| `SPRING_R2DBC_USERNAME` | *(secreto)* | Usuario de la base |
| `SPRING_R2DBC_PASSWORD` | *(secreto)* | Contrasena de la base |

### De la demo

| Variable | Valor | Notas |
|---|---|---|
| `APP_MODULE` | `all` | `all` \| `bff` \| `customer` \| `products` |
| `SPRING_PROFILES_ACTIVE` | `demo` | Activa seguridad permisiva, seed y logs INFO |
| `DEMO_ENABLED` | `true` | Publica `/api/demo/customers` (codigos cifrados) |
| `SQL_INIT_MODE` | `always` | `never` si el esquema ya esta cargado |
| `ENCRYPTION_SECRET_KEY` | `MySecretKey12345` | AES de 16/24/32 bytes. En la demo es publica a proposito |
| `PORT` | *(la pone Cloud Run)* | El BFF escucha ahi |

### Opcionales

| Variable | Por defecto | Notas |
|---|---|---|
| `DEMO_CODIGOS` | `CUST001,...,CUST005` | Codigos que expone `/api/demo/customers` |
| `ENCRYPTION_ALGORITHM` | `AES/ECB/PKCS5Padding` | |
| `ENCRYPTION_IMPLEMENTATION` | `enhanced` | `basic` usa `AESEncryptionService` |
| `JAVA_OPTS_BFF` | `-Xms64m -Xmx280m -XX:+UseSerialGC` | Solo en `APP_MODULE=all` |
| `JAVA_OPTS_INTERNAL` | `-Xms48m -Xmx220m -XX:+UseSerialGC` | Solo en `APP_MODULE=all` |
| `JAVA_OPTS` | `-XX:MaxRAMPercentage=75 -XX:+UseSerialGC` | Solo en modo servicio unico |
| `R2DBC_POOL_INITIAL_SIZE` / `R2DBC_POOL_MAX_SIZE` | `1` / `5` | Cuidado con el limite de conexiones de Neon |
| `STARTUP_TIMEOUT` | `120` | Segundos que espera el entrypoint a los servicios internos |
| `ACTUATOR_EXPOSE` | `health,info` | |

### Perfil `prod` (OAuth2 real, **no** se usa en la demo)

Con `SPRING_PROFILES_ACTIVE=prod` el BFF exige un JWT valido en `/api/customer-info/**`. Hay que
dar el emisor por variable de entorno; no hay valor por defecto a proposito:

```
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=https://tu-emisor/realms/financial
# o, para no descargar metadata en el arranque:
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=https://tu-emisor/realms/financial/protocol/openid-connect/certs
```

---

## 5. Comprobar el despliegue

```bash
URL=$(gcloud run services describe financial-api-demo --region "$REGION" --format='value(status.url)')

curl -s -o /dev/null -w '%{http_code}\n' "$URL/"                  # 200  (pagina de demo)
curl -s -o /dev/null -w '%{http_code}\n' "$URL/actuator/health"   # 200
curl -s "$URL/api/demo/customers"                                 # codigos cifrados
ENC=$(curl -s "$URL/api/demo/customers" | python3 -c 'import sys,json;print(json.load(sys.stdin)[0]["codigoCifrado"])')
curl -s -i "$URL/api/customer-info/$ENC" | head -20                # 200 + cabecera Correlation-ID
curl -s -o /dev/null -w '%{http_code}\n' "$URL/api/no-existe"      # 404 (no devuelve el index)
curl -s -o /dev/null -w '%{http_code}\n' "$URL/api/customer-info/basura"  # 400
```

---

## 6. Probar la misma imagen en local

```bash
docker network create fam-net
docker run -d --name fam-pg --network fam-net \
  -e POSTGRES_PASSWORD=test -e POSTGRES_DB=t postgres:17-alpine

docker build -t financial-api:demo .
docker run --rm --name fam-app --network fam-net -p 127.0.0.1:8080:8080 \
  -e SPRING_PROFILES_ACTIVE=demo -e APP_MODULE=all \
  -e SPRING_R2DBC_URL='r2dbc:postgresql://fam-pg:5432/t' \
  -e SPRING_R2DBC_USERNAME=postgres -e SPRING_R2DBC_PASSWORD=test \
  financial-api:demo
```

Para el stack de desarrollo completo (tres contenedores + PostgreSQL) sigue estando
`docker compose up --build`, que ahora construye la imagen unica y no necesita un `mvn package`
previo.

---

## 7. Que NO cubre esta demo

- **No hay login.** La demo es publica y de solo lectura. El resource server OAuth2 existe pero
  solo se activa con el perfil `prod`, que necesita un emisor OIDC externo.
- **El cifrado del codigo de cliente es AES-128 en modo ECB** con una clave publica en el
  repositorio. Vale para ilustrar el reto, no para datos reales: ECB es determinista (el mismo
  codigo produce siempre el mismo texto cifrado).
- Los datos son inventados: nombres "Demo", documentos `1000000x`, cuentas `DEMO-xxxx-xxxx`.
- Sin persistencia en el contenedor: todo el estado vive en la base gestionada.
