-- =======================
-- DATOS DE PRUEBA ADICIONALES
-- =======================

-- Verificar si ya existen datos para evitar duplicados
DO $$
BEGIN
    -- Solo insertar si no hay datos previos
    IF NOT EXISTS (SELECT 1 FROM customers LIMIT 1) THEN
        -- Insertar clientes adicionales de prueba
        INSERT INTO customers (codigo_unico, nombres, apellidos, tipo_documento, numero_documento) VALUES
        ('CUST006', 'Roberto Carlos', 'Mendoza López', 'DNI', '67890123'),
        ('CUST007', 'Patricia Elena', 'Vásquez Torres', 'CE', '78901234'),
        ('CUST008', 'Diego Fernando', 'Castro Morales', 'DNI', '89012345');

        RAISE NOTICE 'Datos de prueba adicionales insertados exitosamente.';
    ELSE
        RAISE NOTICE 'Los datos ya existen, omitiendo inserción.';
    END IF;
END
$$;