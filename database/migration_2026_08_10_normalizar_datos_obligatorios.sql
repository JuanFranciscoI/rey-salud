-- Normaliza registros históricos creados antes de que estos campos fueran obligatorios.
-- Los marcadores PENDIENTE-* deben reemplazarse cuando se disponga del dato real.

UPDATE paciente
SET dni = CASE
        WHEN NULLIF(TRIM(COALESCE(dni::text, '')), '') IS NULL THEN (90000000 + id_paciente)::text
        WHEN LENGTH(TRIM(dni::text)) < 7 THEN LPAD(TRIM(dni::text), 7, '0')
        ELSE dni::text
    END,
    nombre = CASE WHEN NULLIF(TRIM(COALESCE(nombre, '')), '') IS NULL THEN 'Sin informar' ELSE nombre END,
    apellido = CASE WHEN NULLIF(TRIM(COALESCE(apellido, '')), '') IS NULL THEN 'Sin informar' ELSE apellido END,
    fechanacimiento = COALESCE(fechanacimiento, DATE '1900-01-01'),
    sexo = CASE WHEN NULLIF(TRIM(COALESCE(sexo, '')), '') IS NULL THEN 'Otro' ELSE sexo END,
    telefono = CASE
        WHEN LENGTH(TRIM(COALESCE(telefono::text, ''))) < 10 THEN LPAD(id_paciente::text, 10, '0')
        ELSE telefono::text
    END,
    direccion = CASE WHEN NULLIF(TRIM(COALESCE(direccion, '')), '') IS NULL THEN 'Sin informar' ELSE direccion END,
    numeroafiliado = CASE
        WHEN id_obrasocial IS NULL AND NULLIF(TRIM(COALESCE(numeroafiliado, '')), '') IS NULL THEN '-'
        ELSE numeroafiliado
    END
WHERE NULLIF(TRIM(COALESCE(dni::text, '')), '') IS NULL
   OR LENGTH(TRIM(COALESCE(dni::text, ''))) < 7
   OR NULLIF(TRIM(COALESCE(nombre, '')), '') IS NULL
   OR NULLIF(TRIM(COALESCE(apellido, '')), '') IS NULL
   OR fechanacimiento IS NULL
   OR NULLIF(TRIM(COALESCE(sexo, '')), '') IS NULL
   OR LENGTH(TRIM(COALESCE(telefono::text, ''))) < 10
   OR NULLIF(TRIM(COALESCE(direccion, '')), '') IS NULL
   OR (id_obrasocial IS NULL AND NULLIF(TRIM(COALESCE(numeroafiliado, '')), '') IS NULL);

UPDATE personal
SET dni = CASE
        WHEN NULLIF(TRIM(COALESCE(dni::text, '')), '') IS NULL THEN (91000000 + id_personal)::text
        WHEN LENGTH(TRIM(dni::text)) < 7 THEN LPAD(TRIM(dni::text), 7, '0')
        ELSE dni::text
    END,
    nombreyapellido = CASE WHEN NULLIF(TRIM(COALESCE(nombreyapellido, '')), '') IS NULL THEN 'Sin informar' ELSE nombreyapellido END,
    telefono = CASE
        WHEN LENGTH(TRIM(COALESCE(telefono::text, ''))) < 10 THEN 1000000000 + id_personal
        ELSE telefono
    END,
    direccion = CASE WHEN NULLIF(TRIM(COALESCE(direccion, '')), '') IS NULL THEN 'Sin informar' ELSE direccion END
WHERE NULLIF(TRIM(COALESCE(dni::text, '')), '') IS NULL
   OR LENGTH(TRIM(COALESCE(dni::text, ''))) < 7
   OR NULLIF(TRIM(COALESCE(nombreyapellido, '')), '') IS NULL
   OR LENGTH(TRIM(COALESCE(telefono::text, ''))) < 10
   OR NULLIF(TRIM(COALESCE(direccion, '')), '') IS NULL;

UPDATE personal p
SET matricula = 'PENDIENTE-' || p.id_personal
FROM usuario u
JOIN rol r ON r.id_rol = u.id_rol
WHERE u.id_personal = p.id_personal
  AND u.estado::text = 'activo'
  AND translate(lower(r.nombre), 'áéíóú', 'aeiou') LIKE '%medico%'
  AND NULLIF(TRIM(COALESCE(p.matricula, '')), '') IS NULL;
