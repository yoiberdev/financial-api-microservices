-- =======================
-- DATOS DE PRUEBA ADICIONALES (solo entorno local con docker compose)
-- =======================
--
-- Antes esto iba dentro de un bloque DO $$ ... $$ con la guarda
-- "IF NOT EXISTS (SELECT 1 FROM customers LIMIT 1)": como init.sql ya inserta CUST001..CUST005
-- antes de este script, la condicion nunca se cumplia y CUST006..CUST008 no se creaban jamas.
-- ON CONFLICT DO NOTHING es idempotente de verdad y ademas no rompe a los parsers que trocean
-- los scripts por ';'.

INSERT INTO customers (codigo_unico, nombres, apellidos, tipo_documento, numero_documento) VALUES
('CUST006', 'Fabio Demo',  'Mendoza Ejemplo', 'DNI', '10000006'),
('CUST007', 'Gala Demo',   'Vasquez Prueba',  'CE',  '10000007'),
('CUST008', 'Hugo Demo',   'Castro Muestra',  'DNI', '10000008')
ON CONFLICT (codigo_unico) DO NOTHING;

INSERT INTO financial_products (codigo_unico, tipo_producto, nombre, saldo, numero_cuenta, estado) VALUES
('CUST006', 'CUENTA_AHORRO',    'Cuenta Ahorro Demo',  1250.00, 'DEMO-0006-0001', 'ACTIVO'),
('CUST007', 'CUENTA_CORRIENTE', 'Cuenta Corriente Demo', 980.40, 'DEMO-0007-0001', 'ACTIVO'),
('CUST008', 'TARJETA_DEBITO',   'Tarjeta Debito Demo',     0.00, 'DEMO-0008-0001', 'ACTIVO')
ON CONFLICT (numero_cuenta) DO NOTHING;
