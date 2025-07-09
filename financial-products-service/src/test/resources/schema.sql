DROP TABLE IF EXISTS financial_products;

CREATE TABLE financial_products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_unico VARCHAR(255) NOT NULL,
    tipo_producto VARCHAR(50) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    saldo DECIMAL(15,2) NOT NULL,
    numero_cuenta VARCHAR(255),
    estado VARCHAR(50) NOT NULL,
    fecha_apertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_financial_products_codigo_unico ON financial_products(codigo_unico);