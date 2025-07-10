# Financial API Microservices

Reto técnico para crear una API segura de clientes y productos financieros usando Java 17, Spring Boot, WebFlux, OAuth2, AOP y Docker.

## 🚀 Ejecución del Proyecto con Docker

### Prerrequisitos
- Docker y Docker Compose instalados
- Java 17+ (para compilación local)
- Maven 3.8+ (para compilación local)

### 1. Clonar y compilar
```bash
git clone [repository-url]
cd financial-api-microservices

# Compilar todos los servicios
mvn clean package -DskipTests

# O compilar individualmente si es necesario
cd bff-service && mvn clean package -DskipTests && cd ..
cd customer-service && mvn clean package -DskipTests && cd ..
cd financial-products-service && mvn clean package -DskipTests && cd ..
```

### 2. Ejecutar con Docker Compose
```bash
# Levantar todos los servicios
docker-compose up -d

# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f bff-service
```

### 3. Verificar servicios
Los servicios estarán disponibles en:

| Servicio | Puerto | Health Check |
|----------|--------|-------------|
| **BFF Service** | 8080 | http://localhost:8080/actuator/health |
| **Customer Service** | 8081 | http://localhost:8081/actuator/health |
| **Financial Products Service** | 8082 | http://localhost:8082/actuator/health |
| **PostgreSQL** | 5433 | - |

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
| **Customer Service** | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| **Financial Products Service** | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| **BFF Service** | ❌ No disponible | ❌ No disponible |

### ¿Por qué el BFF no tiene Swagger público?

El BFF Service requiere **autenticación OAuth2** y maneja **códigos únicos encriptados**, por lo que:
- Swagger UI no puede funcionar sin token válido
- Los endpoints requieren códigos encriptados específicos
- Se debe usar **Postman** para testing del BFF

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
├── init-db/                  # Scripts SQL de inicialización
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
- **Correlation-ID**: Header `X-Correlation-ID` para trazabilidad
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
SPRING_R2DBC_PASSWORD=perupcs123

# Microservicios URLs
SERVICES_CUSTOMER_BASE_URL=http://localhost:8081
SERVICES_FINANCIAL_PRODUCTS_BASE_URL=http://localhost:8082
```

### Profiles disponibles
- **dev**: Desarrollo local
- **docker**: Contenedores Docker
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