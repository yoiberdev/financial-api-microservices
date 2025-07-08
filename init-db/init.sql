-- Scripts de creación de tablas para PostgreSQL
-- Ejecutar estos scripts en tu base de datos PostgreSQL

-- =======================
-- TABLA CUSTOMERS
-- =======================
CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    codigo_unico VARCHAR(255) UNIQUE NOT NULL,
    nombres VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    tipo_documento VARCHAR(50) NOT NULL CHECK (tipo_documento IN ('DNI', 'CE', 'PASAPORTE', 'RUC')),
    numero_documento VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT uk_customers_numero_documento UNIQUE (numero_documento),
    CONSTRAINT chk_customers_nombres_length CHECK (LENGTH(nombres) >= 2),
    CONSTRAINT chk_customers_apellidos_length CHECK (LENGTH(apellidos) >= 2)
);

-- Índices para optimizar consultas
CREATE INDEX IF NOT EXISTS idx_customers_codigo_unico ON customers(codigo_unico);
CREATE INDEX IF NOT EXISTS idx_customers_numero_documento ON customers(numero_documento);
CREATE INDEX IF NOT EXISTS idx_customers_tipo_documento ON customers(tipo_documento);

-- =======================
-- TABLA FINANCIAL_PRODUCTS
-- =======================
CREATE TABLE IF NOT EXISTS financial_products (
    id BIGSERIAL PRIMARY KEY,
    codigo_unico VARCHAR(255) NOT NULL,
    tipo_producto VARCHAR(100) NOT NULL CHECK (tipo_producto IN (
        'CUENTA_AHORRO',
        'CUENTA_CORRIENTE',
        'TARJETA_CREDITO',
        'TARJETA_DEBITO',
        'PRESTAMO',
        'DEPOSITO_PLAZO_FIJO',
        'CREDITO_HIPOTECARIO'
    )),
    nombre VARCHAR(255) NOT NULL,
    saldo DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    numero_cuenta VARCHAR(50) UNIQUE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO', 'CERRADO')),
    fecha_apertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key (aunque R2DBC no la maneje automáticamente, es buena práctica definirla)
    CONSTRAINT fk_financial_products_customer
        FOREIGN KEY (codigo_unico) REFERENCES customers(codigo_unico)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Índices para optimizar consultas
CREATE INDEX IF NOT EXISTS idx_financial_products_codigo_unico ON financial_products(codigo_unico);
CREATE INDEX IF NOT EXISTS idx_financial_products_tipo_producto ON financial_products(tipo_producto);
CREATE INDEX IF NOT EXISTS idx_financial_products_estado ON financial_products(estado);
CREATE INDEX IF NOT EXISTS idx_financial_products_numero_cuenta ON financial_products(numero_cuenta);
CREATE INDEX IF NOT EXISTS idx_financial_products_fecha_apertura ON financial_products(fecha_apertura);

-- =======================
-- TRIGGERS PARA UPDATED_AT
-- =======================

-- Función para actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger para customers
DROP TRIGGER IF EXISTS update_customers_updated_at ON customers;
CREATE TRIGGER update_customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger para financial_products
DROP TRIGGER IF EXISTS update_financial_products_updated_at ON financial_products;
CREATE TRIGGER update_financial_products_updated_at
    BEFORE UPDATE ON financial_products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =======================
-- DATOS DE PRUEBA
-- =======================

-- Insertar clientes de prueba
INSERT INTO customers (codigo_unico, nombres, apellidos, tipo_documento, numero_documento) VALUES
('CUST001', 'Juan Carlos', 'Pérez García', 'DNI', '12345678'),
('CUST002', 'María Elena', 'López Fernández', 'DNI', '23456789'),
('CUST003', 'Carlos Alberto', 'Rodríguez Silva', 'CE', '34567890'),
('CUST004', 'Ana Sofía', 'Martínez Herrera', 'DNI', '45678901'),
('CUST005', 'Luis Fernando', 'González Vargas', 'DNI', '56789012')
ON CONFLICT (codigo_unico) DO NOTHING;

-- Insertar productos financieros de prueba
INSERT INTO financial_products (codigo_unico, tipo_producto, nombre, saldo, numero_cuenta, estado, fecha_apertura) VALUES
-- Productos para CUST001
('CUST001', 'CUENTA_AHORRO', 'Cuenta Ahorro Básica', 5500.50, '001-001-000000001', 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '2 years'),
('CUST001', 'TARJETA_CREDITO', 'Tarjeta Gold', -1200.00, '5555-1111-2222-3333', 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '1 year'),
('CUST001', 'DEPOSITO_PLAZO_FIJO', 'Depósito 12 meses', 10000.00, '001-002-000000001', 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '6 months'),

-- Productos para CUST002
('CUST002', 'CUENTA_AHORRO', 'Cuenta Ahorro Premium', 15000.75, '002-001-000000001', 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '3 years'),
('CUST002', 'CUENTA_CORRIENTE', 'Cuenta Corriente Empresarial', 2500.00, '002-002-000000001', 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '2 years'),
('CUST002', 'TARJETA_CREDITO', 'Tarjeta Platinum', -500.50, '5555-2222-3333-4444', 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '1 year'),

-- Productos para CUST003
('CUST003', 'CUENTA_AHORRO', 'Cuenta Ahorro Joven', 800.25, '003-001-000000001', 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '1 year'),
('CUST003', 'PRESTAMO', 'Préstamo Personal', -5000.00, '003-003-000000001', 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '8 months'),

-- Productos para CUST004
('CUST004', 'CUENTA_CORRIENTE', 'Cuenta Corriente Personal', 3200.80, '004-001-000000001', 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '1 year'),
('CUST004', 'CREDITO_HIPOTECARIO', 'Crédito Hipotecario', -120000.00, '004-004-000000001', 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '5 years'),

-- Productos para CUST005
('CUST005', 'CUENTA_AHORRO', 'Cuenta Ahorro Estudiantil', 450.00, '005-001-000000001', 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '6 months'),
('CUST005', 'TARJETA_DEBITO', 'Tarjeta Débito Básica', 0.00, '5555-5555-6666-7777', 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '6 months')
ON CONFLICT (numero_cuenta) DO NOTHING;