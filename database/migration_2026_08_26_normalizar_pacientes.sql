-- Normaliza datos de demostración de pacientes conservando un respaldo reversible.
CREATE TABLE IF NOT EXISTS respaldo_pacientes_normalizacion_20260826 (
    id_paciente INTEGER PRIMARY KEY,
    fechanacimiento_anterior DATE,
    direccion_anterior VARCHAR(255),
    numeroafiliado_anterior VARCHAR(100),
    respaldado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO respaldo_pacientes_normalizacion_20260826
    (id_paciente, fechanacimiento_anterior, direccion_anterior, numeroafiliado_anterior)
SELECT id_paciente, fechanacimiento, direccion, numeroafiliado
FROM paciente
ON CONFLICT (id_paciente) DO NOTHING;

-- Los particulares no poseen número de afiliado.
UPDATE paciente
SET numeroafiliado = NULL
WHERE id_obrasocial IS NULL;

-- Reemplaza una afiliación de prueba alfanumérica por un número plausible.
UPDATE paciente
SET numeroafiliado = '115874263'
WHERE id_paciente = 22;

-- Direcciones de demostración completas y legibles.
UPDATE paciente SET direccion = 'Vélez Sarsfield 3254' WHERE id_paciente = 22;
UPDATE paciente SET direccion = 'Tucumán 2860' WHERE id_paciente = 24;
UPDATE paciente SET direccion = 'Milenio de Polonia 3330' WHERE id_paciente = 25;
UPDATE paciente SET direccion = 'San Martín 1365' WHERE id_paciente = 27;
UPDATE paciente SET direccion = 'Belgrano 2148' WHERE id_paciente = 28;
UPDATE paciente SET direccion = 'Sarmiento 1842' WHERE id_paciente = 29;
UPDATE paciente SET direccion = 'General Paz 2450' WHERE id_paciente = 31;
UPDATE paciente SET direccion = 'Juan de Garay 1735' WHERE id_paciente = 32;
UPDATE paciente SET direccion = 'Iturraspe 2206' WHERE id_paciente = 33;
UPDATE paciente SET direccion = 'Mitre 1560' WHERE id_paciente = 34;
UPDATE paciente SET direccion = 'San Martín 2541' WHERE id_paciente = 35;
UPDATE paciente SET direccion = 'Junín 2600' WHERE id_paciente = 54;
UPDATE paciente SET direccion = 'Rivadavia 3180' WHERE id_paciente = 55;
UPDATE paciente SET direccion = 'Belgrano 2275' WHERE id_paciente = 56;
UPDATE paciente SET direccion = 'Sarmiento 3480' WHERE id_paciente = 57;
UPDATE paciente SET direccion = 'Urquiza 2850' WHERE id_paciente = 59;
UPDATE paciente SET direccion = 'San Lorenzo 1945' WHERE id_paciente = 61;
UPDATE paciente SET direccion = 'General López 2310' WHERE id_paciente = 62;
UPDATE paciente SET direccion = 'Juan de Garay 2640' WHERE id_paciente = 64;
UPDATE paciente SET direccion = 'Facundo Zuviría 3150' WHERE id_paciente = 65;
UPDATE paciente SET direccion = 'Avenida Freyre 2230' WHERE id_paciente = 66;
UPDATE paciente SET direccion = 'Lisandro de la Torre 2875' WHERE id_paciente = 67;

-- Fechas plausibles para registros que tenían valores recientes de prueba.
UPDATE paciente SET fechanacimiento = DATE '1997-05-10' WHERE id_paciente = 28;
UPDATE paciente SET fechanacimiento = DATE '1995-05-13' WHERE id_paciente = 29;
UPDATE paciente SET fechanacimiento = DATE '1994-05-04' WHERE id_paciente = 34;
UPDATE paciente SET fechanacimiento = DATE '1988-05-14' WHERE id_paciente = 55;
UPDATE paciente SET fechanacimiento = DATE '1992-05-14' WHERE id_paciente = 56;
UPDATE paciente SET fechanacimiento = DATE '1990-05-21' WHERE id_paciente = 57;
UPDATE paciente SET fechanacimiento = DATE '1986-06-18' WHERE id_paciente = 59;
UPDATE paciente SET fechanacimiento = DATE '1991-06-02' WHERE id_paciente = 61;
UPDATE paciente SET fechanacimiento = DATE '1989-06-02' WHERE id_paciente = 62;
UPDATE paciente SET fechanacimiento = DATE '1993-06-04' WHERE id_paciente = 64;
UPDATE paciente SET fechanacimiento = DATE '1984-06-01' WHERE id_paciente = 65;
UPDATE paciente SET fechanacimiento = DATE '1996-06-03' WHERE id_paciente = 66;
UPDATE paciente SET fechanacimiento = DATE '1998-08-01' WHERE id_paciente = 71;

