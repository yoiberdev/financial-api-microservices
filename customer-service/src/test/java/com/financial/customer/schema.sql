CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    codigo_unico VARCHAR(50),
    nombres VARCHAR(100),
    apellidos VARCHAR(100),
    tipo_documento VARCHAR(20),
    numero_documento VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
