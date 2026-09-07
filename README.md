# Financial API Microservices

## Demo en vivo

**https://financial-api-demo-164532276262.us-central1.run.app**

Alojada en Google Cloud Run (region us-central1, escala a cero) con la base de
datos en Neon. La pagina de inicio explica el flujo y trae los codigos ya
cifrados para probar.

Sin autenticacion. Rutas utiles:

```
GET /health                              estado del BFF
GET /api/demo/customers                  codigos de cliente ya cifrados
GET /api/customer-info/{codigoCifrado}   el flujo completo
```

El endpoint principal descifra el codigo, llama en paralelo a los dos
microservicios internos y compone la respuesta con los datos del cliente y sus
productos financieros. La cabecera `Correlation-ID` de la respuesta coincide
con el `correlationId` del cuerpo, que es como se sigue una peticion por los
tres servicios.

Los tres microservicios corren dentro del mismo contenedor: el BFF escucha en
el puerto publico y los otros dos en local, sin exponerse. Los datos son
ficticios. La primera carga tras un rato tarda unos segundos por el arranque
en frio.



Reto técnico para crear una API segura de clientes y productos financieros usando Java 21, Spring Boot, WebFlux, OAuth2, AOP y Docker.

## 🌐 Demo pública (Cloud Run)

Hay una imagen única que arranca los tres microservicios en un solo contenedor y sirve, en el
mismo origen, una página de demo (`/`), la API (`/api/...`) y Swagger UI. Pasos, variables y
comprobaciones: **[docs/DESPLIEGUE-CLOUD-RUN.md](docs/DESPLIEGUE-CLOUD-RUN.md)**.

```bash
docker build -t financial-api:demo .
docker run --rm -p 127.0.0.1:8080:8080 \
  -e SPRING_PROFILES_ACTIVE=demo -e APP_MODULE=all \
  -e SPRING_R2DBC_URL='r2dbc:postgresql://TU_HOST:5432/financial_db?sslMode=require' \
  -e SPRING_R2DBC_USERNAME=usuario -e SPRING_R2DBC_PASSWORD=secreto \
  financial-api:demo
```

La demo es **de solo lectura, sin login y con datos inventados** (clientes "Demo", documentos
`1000000x`, cuentas `DEMO-xxxx-xxxx`).

## 🚀 Ejecución del Proyecto con Docker

### Prerrequisitos
- Docker y Docker Compose instalados
- Java 17+ (para compilación local)
- Maven 3.8+ (para compilación local)

### 1. Clonar
```bash
git clone https://github.com/yoiberdev/financial-api-microservices.git
cd financial-api-microservices
```

No hace falta compilar antes: el `Dockerfile` de la raíz es multietapa y hace el `mvn package`
dentro de la imagen. Para compilar en local de todas formas:

```bash
mvn clean package -DskipTests
```

### 2. Ejecutar con Docker Compose
```bash
# Levantar todos los servicios (construye la imagen única)
docker compose up -d --build

# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f bff-service
```

### 3. Verificar servicios
Los servicios estarán disponibles en:

| Servicio | Puerto | Health Check |
|----------|--------|-------------|
| **BFF Service** | 8080 (127.0.0.1) | http://localhost:8080/actuator/health |
| **Customer Service** | interno de la red `financial-network` | - |
| **Financial Products Service** | interno de la red `financial-network` | - |
| **PostgreSQL** | 5433 (127.0.0.1) | - |

Solo el BFF se publica al host, y únicamente en `127.0.0.1`. Los otros dos servicios se
alcanzan por el nombre de servicio dentro de la red de compose.

### 4. Detener servicios
```bash
# Detener servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v
```

## 📋 Colección de Postman

### Importar colección
1. Importar el archivo `Financial-API-BFF-Orchestration.postman_collection.json` en Postman
2. La colección incluye:
    - Variables de entorno preconfiguradas
    - Headers necesarios (Authorization, Correlation-ID)
    - Códigos únicos encriptados para testing

### Endpoints principales

#### BFF Service - Endpoint principal
```
GET http://localhost:8080/api/customer-info/{{encrypted_code}}
Headers:
- Authorization: Bearer {{jwt_token}}
- Correlation-ID: {{$randomUUID}}
```

#### Customer Service - Directo
```
GET http://localhost:8081/api/customers/CUST001
Headers:
- Correlation-ID: {{$randomUUID}}
```

#### Financial Products Service - Directo
```
GET http://localhost:8082/api/financial-products/customer/CUST001
Headers:
- Correlation-ID: {{$randomUUID}}
```

### Códigos encriptados para testing
Los siguientes códigos están preconfigurados en la colección:

| Código Original | Código Encriptado (variable) |
|----------------|------------------------------|
| CUST001 | {{encrypted_code}} |
| CUST002 | {{encrypted_code_2}} |
| CUST003 | {{encrypted_code_3}} |

## 📖 Documentación Swagger/OpenAPI

### URLs de documentación

| Servicio | Swagger UI | OpenAPI JSON |
|----------|------------|--------------|
| **BFF Service** | http://localhost:8080/swagger-ui.html | http://localhost:8080/v3/api-docs |
| **Customer Service** | `/swagger-ui.html` del contenedor | `/v3/api-docs` |
| **Financial Products Service** | `/swagger-ui.html` del contenedor | `/v3/api-docs` |

El Swagger del BFF sí funciona. Antes `/v3/api-docs` se quedaba colgado: springdoc 2.3.0 apunta a
Spring Boot 3.2 y con Spring Framework 6.2 lanza `NoSuchMethodError` en cuanto la aplicación
tiene un `@RestControllerAdvice`. Se ha subido a la línea 2.8.x.

En los perfiles `dev`, `docker` y `demo` el BFF no exige token, así que se puede probar desde
Swagger UI. Los códigos cifrados de ejemplo están en `GET /api/demo/customers` (con
`DEMO_ENABLED=true`).

## 🧪 Pruebas Unitarias

### Ejecutar tests
```bash
# Ejecutar todos los tests
mvn test

# Tests por servicio
cd customer-service && mvn test
cd financial-products-service && mvn test
cd bff-service && mvn test
```

### Cobertura de tests
- **Customer Service**: Tests unitarios con WebTestClient y StepVerifier
- **Financial Products Service**: Tests de repository y service layer
- **BFF Service**: Tests de integración con WebClient mocks
- **Common**: Tests de encriptación/desencriptación

### Tecnologías de testing
- **JUnit 5**: Framework de testing
- **Mockito**: Mocks y stubs
- **WebTestClient**: Testing reactivo para WebFlux
- **StepVerifier**: Testing de Mono/Flux
- **Testcontainers**: Tests de integración con BD

## 🏗️ Arquitectura del Proyecto

```
financial-microservices/
├── bff-service/              # Backend for Frontend
├── customer-service/         # Microservicio de clientes
├── financial-products-service/ # Microservicio de productos
├── common/                   # Utilidades compartidas
├── init-db/                  # Scripts SQL de inicialización (docker compose)
├── docker/entrypoint.sh      # Selector de módulo de la imagen única
├── docs/                     # Guía de despliegue en Cloud Run
├── Dockerfile                # Imagen única multietapa (los 3 servicios)
├── docker-compose.yml        # Orquestación de servicios
└── Financial-API-BFF-Orchestration.postman_collection.json
```

### Flujo de datos
```
[Postman] → [BFF:8080] → [Customer:8081 + Financial:8082] → [PostgreSQL:5433]
```

## 🔐 Seguridad y Encriptación

### Algoritmo de encriptación
- **Algoritmo**: AES/ECB/PKCS5Padding
- **Clave secreta**: MySecretKey12345 (configurable)
- **Charset**: UTF-8

### OAuth2 (Preparado para integración)
- Headers `Authorization: Bearer {token}` implementados
- Configuración OAuth2 en BFF Service
- Resource Server configurado para validación JWT

### Tracking distribuido
- **Correlation-ID**: Header `Correlation-ID` para trazabilidad (petición y respuesta)
- Propagación automática entre microservicios
- Logging estructurado con ID de correlación

## 🗄️ Base de Datos

### Estructura PostgreSQL
```sql
-- Tabla de clientes
CREATE TABLE customers (
    codigo_unico VARCHAR(255) PRIMARY KEY,
    nombres VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    tipo_documento VARCHAR(50) NOT NULL,
    numero_documento VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de productos financieros
CREATE TABLE financial_products (
    id BIGSERIAL PRIMARY KEY,
    codigo_unico VARCHAR(255) NOT NULL,
    tipo_producto VARCHAR(100) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    saldo DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (codigo_unico) REFERENCES customers(codigo_unico)
);
```

### Datos de prueba
Al iniciar el proyecto, se cargan automáticamente datos de prueba:
- 5 clientes (CUST001 - CUST005)
- Productos financieros asociados
- Cuentas de ahorro y tarjetas de crédito

## 📊 Tecnologías Implementadas

### Core Framework
- **Java 17**: Features modernas (Records, Pattern Matching, etc.)
- **Spring Boot 3.x**: Framework principal
- **Spring WebFlux**: Programación reactiva
- **Spring Security**: OAuth2 Resource Server
- **Spring Data R2DBC**: Acceso reactivo a base de datos

### Librerías utilizadas
- **Lombok**: Reducción de boilerplate code
- **MapStruct**: Mapping entre entidades y DTOs
- **Jackson**: Serialización JSON
- **Logback**: Logging estructurado
- **JUnit 5**: Testing framework
- **Mockito**: Mocking para tests

### Patrones implementados
- **SOLID principles**: Single Responsibility, Open/Closed, etc.
- **Microservices patterns**: Service Discovery, API Gateway
- **Reactive patterns**: Non-blocking I/O, backpressure
- **AOP**: Logging y monitoreo con aspectos

## 🔧 Configuración de Desarrollo

### Variables de entorno principales
```bash
# Encriptación
ENCRYPTION_SECRET_KEY=MySecretKey12345
ENCRYPTION_ALGORITHM=AES/ECB/PKCS5Padding

# Base de datos
SPRING_R2DBC_URL=r2dbc:postgresql://localhost:5433/financial_db
SPRING_R2DBC_USERNAME=admin
SPRING_R2DBC_PASSWORD=Demo1234!   # solo entorno local, ver docker-compose.yml

# Microservicios URLs
SERVICES_CUSTOMER_BASE_URL=http://localhost:8081
SERVICES_FINANCIAL_PRODUCTS_BASE_URL=http://localhost:8082
```

### Profiles disponibles
- **dev**: Desarrollo local (sin autenticación)
- **docker**: Contenedores Docker con `docker compose` (sin autenticación)
- **demo**: Demo pública: seguridad permisiva, seed idempotente y `/api/demo/customers`
- **prod**: OAuth2 resource server real (requiere emisor OIDC, ver `application-prod.yml`)
- **test**: Ejecución de tests

## 📈 Monitoreo y Observabilidad

### Actuator endpoints
Todos los servicios exponen:
- `/actuator/health`: Estado del servicio
- `/actuator/info`: Información del servicio
- `/actuator/metrics`: Métricas de rendimiento

### Logging con Logback
- Logs estructurados en JSON
- Rotación automática de archivos
- Niveles configurables por paquete
- Correlation ID en todos los logs

## 🚨 Troubleshooting

### Problemas comunes

**Error de conexión a base de datos:**
```bash
# Verificar que PostgreSQL esté corriendo
docker-compose logs postgres

# Recrear volumen si es necesario
docker-compose down -v && docker-compose up -d
```

**Error de compilación:**
```bash
# Limpiar y recompilar
mvn clean package -DskipTests

# Verificar versión de Java
java -version
```

**Error en tests:**
```bash
# Ejecutar tests con más detalle
mvn test -X

# Ejecutar test específico
mvn test -Dtest=CustomerServiceTest
```

## 📝 Entrega del Proyecto

El proyecto incluye:
- ✅ **3 microservicios** (BFF, Customer, Financial Products)
- ✅ **Dockerización completa** con docker-compose
- ✅ **OAuth2** y encriptación implementados
- ✅ **Correlation ID** para tracking distribuido
- ✅ **Tests unitarios** con JUnit 5
- ✅ **Swagger/OpenAPI** en microservicios
- ✅ **Colección Postman** lista para usar
- ✅ **Base de datos PostgreSQL** con datos de prueba
- ✅ **Logback** configurado
- ✅ **Patrones SOLID** y clean code
- ✅ **Common starter** con utilidades compartidas
