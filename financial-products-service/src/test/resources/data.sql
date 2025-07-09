-- Datos de prueba para Financial Products Service Tests
INSERT INTO financial_products (codigo_unico, tipo_producto, nombre, saldo, numero_cuenta, estado, fecha_apertura) VALUES
-- Productos para CUST001
('CUST001', 'CUENTA_AHORRO', 'Cuenta Ahorro Básica', 5500.50, '001-001-000000001', 'ACTIVO', CURRENT_TIMESTAMP),
('CUST001', 'TARJETA_CREDITO', 'Tarjeta Gold', -1200.00, '5555-1111-2222-3333', 'ACTIVO', CURRENT_TIMESTAMP),
('CUST001', 'DEPOSITO_PLAZO_FIJO', 'Depósito 12 meses', 10000.00, '001-002-000000001', 'ACTIVO', CURRENT_TIMESTAMP),

-- Productos para CUST002
('CUST002', 'CUENTA_AHORRO', 'Cuenta Ahorro Premium', 15000.75, '002-001-000000001', 'ACTIVO', CURRENT_TIMESTAMP),
('CUST002', 'CUENTA_CORRIENTE', 'Cuenta Corriente Empresarial', 2500.00, '002-002-000000001', 'ACTIVO', CURRENT_TIMESTAMP),
('CUST002', 'TARJETA_CREDITO', 'Tarjeta Platinum', -500.50, '5555-2222-3333-4444', 'ACTIVO', CURRENT_TIMESTAMP),

-- Productos para TEST001
('TEST001', 'CUENTA_AHORRO', 'Cuenta Test', 1000.00, '999-999-999999999', 'ACTIVO', CURRENT_TIMESTAMP),

-- Productos para CUSTOMER001
('CUSTOMER001', 'TARJETA_DEBITO', 'Tarjeta Test', 0.00, '9999-9999-9999-9999', 'ACTIVO', CURRENT_TIMESTAMP);
