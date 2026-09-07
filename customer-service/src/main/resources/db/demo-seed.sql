-- Datos de la DEMO PUBLICA. Personas, documentos, cuentas y saldos INVENTADOS.
-- Idempotente: ON CONFLICT DO NOTHING, se puede ejecutar en cada arranque.

INSERT INTO customers (codigo_unico, nombres, apellidos, tipo_documento, numero_documento) VALUES
('CUST001', 'Ana Demo',    'Perez Ficticio',   'DNI',       '10000001'),
('CUST002', 'Bruno Demo',  'Lopez Ejemplo',    'DNI',       '10000002'),
('CUST003', 'Carla Demo',  'Rojas Prueba',     'CE',        '10000003'),
('CUST004', 'Diego Demo',  'Marin Sandbox',    'DNI',       '10000004'),
('CUST005', 'Elena Demo',  'Torres Muestra',   'PASAPORTE', '10000005')
ON CONFLICT (codigo_unico) DO NOTHING;

INSERT INTO financial_products (codigo_unico, tipo_producto, nombre, saldo, numero_cuenta, estado) VALUES
('CUST001', 'CUENTA_AHORRO',       'Cuenta Ahorro Demo',        5500.50,    'DEMO-0001-0001', 'ACTIVO'),
('CUST001', 'TARJETA_CREDITO',     'Tarjeta Gold Demo',        -1200.00,    'DEMO-0001-0002', 'ACTIVO'),
('CUST001', 'DEPOSITO_PLAZO_FIJO', 'Deposito 12 meses Demo',   10000.00,    'DEMO-0001-0003', 'ACTIVO'),
('CUST002', 'CUENTA_AHORRO',       'Cuenta Ahorro Premium',    15000.75,    'DEMO-0002-0001', 'ACTIVO'),
('CUST002', 'CUENTA_CORRIENTE',    'Cuenta Corriente Demo',     2500.00,    'DEMO-0002-0002', 'ACTIVO'),
('CUST002', 'TARJETA_CREDITO',     'Tarjeta Platinum Demo',     -500.50,    'DEMO-0002-0003', 'ACTIVO'),
('CUST003', 'CUENTA_AHORRO',       'Cuenta Ahorro Joven Demo',   800.25,    'DEMO-0003-0001', 'ACTIVO'),
('CUST003', 'PRESTAMO',            'Prestamo Personal Demo',   -5000.00,    'DEMO-0003-0002', 'ACTIVO'),
('CUST004', 'CUENTA_CORRIENTE',    'Cuenta Corriente Demo',     3200.80,    'DEMO-0004-0001', 'ACTIVO'),
('CUST004', 'CREDITO_HIPOTECARIO', 'Credito Hipotecario Demo',-120000.00,   'DEMO-0004-0002', 'ACTIVO'),
('CUST005', 'CUENTA_AHORRO',       'Cuenta Ahorro Demo',         450.00,    'DEMO-0005-0001', 'ACTIVO'),
('CUST005', 'TARJETA_DEBITO',      'Tarjeta Debito Demo',          0.00,    'DEMO-0005-0002', 'ACTIVO')
ON CONFLICT (numero_cuenta) DO NOTHING;
