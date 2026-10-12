-- Completa identificadores de afiliación faltantes para la base de demostración.
-- Se conserva una copia recuperable de cada valor reemplazado.

CREATE TABLE IF NOT EXISTS respaldo_afiliados_20260826 (
    id_paciente INTEGER PRIMARY KEY,
    numeroafiliado_anterior VARCHAR(100),
    respaldado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO respaldo_afiliados_20260826 (id_paciente, numeroafiliado_anterior)
SELECT id_paciente, numeroafiliado
FROM paciente
WHERE id_obrasocial IS NOT NULL
  AND (NULLIF(TRIM(COALESCE(numeroafiliado, '')), '') IS NULL
       OR TRIM(numeroafiliado) = '-')
ON CONFLICT (id_paciente) DO NOTHING;

UPDATE paciente
SET numeroafiliado = LPAD((90000000 + id_paciente)::TEXT, 8, '0')
                     || '-'
                     || LPAD((((id_paciente * 37) % 97) + 1)::TEXT, 2, '0')
WHERE id_obrasocial IS NOT NULL
  AND (NULLIF(TRIM(COALESCE(numeroafiliado, '')), '') IS NULL
       OR TRIM(numeroafiliado) = '-');
