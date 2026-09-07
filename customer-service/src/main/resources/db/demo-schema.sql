-- Esquema de la demo publica (PostgreSQL). Idempotente: se ejecuta en cada arranque.
--
-- No usa funciones ni triggers con cuerpo $$ ... $$ a proposito: el inicializador de
-- spring.sql.init parte los scripts por ';' y romperia esos bloques. La demo es de solo
-- lectura, asi que no hace falta el trigger de updated_at de init-db/init.sql.

CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    codigo_unico VARCHAR(255) UNIQUE NOT NULL,
    nombres VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    tipo_documento VARCHAR(50) NOT NULL,
    numero_documento VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_customers_numero_documento UNIQUE (numero_documento)
);

CREATE INDEX IF NOT EXISTS idx_customers_codigo_unico ON customers(codigo_unico);
CREATE INDEX IF NOT EXISTS idx_customers_numero_documento ON customers(numero_documento);

CREATE TABLE IF NOT EXISTS financial_products (
    id BIGSERIAL PRIMARY KEY,
    codigo_unico VARCHAR(255) NOT NULL,
    tipo_producto VARCHAR(100) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    saldo DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    numero_cuenta VARCHAR(50) UNIQUE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    fecha_apertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_financial_products_customer
        FOREIGN KEY (codigo_unico) REFERENCES customers(codigo_unico)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_financial_products_codigo_unico ON financial_products(codigo_unico);
CREATE INDEX IF NOT EXISTS idx_financial_products_estado ON financial_products(estado);
