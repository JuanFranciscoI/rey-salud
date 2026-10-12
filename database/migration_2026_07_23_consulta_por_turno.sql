-- Permite relacionar cada historia/evolución clínica con el turno que la originó.
-- Ejecutar una sola vez en PostgreSQL.

ALTER TABLE consulta
ADD COLUMN IF NOT EXISTS id_turno INTEGER;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_consulta_turno'
    ) THEN
        ALTER TABLE consulta
        ADD CONSTRAINT fk_consulta_turno
        FOREIGN KEY (id_turno)
        REFERENCES turno(id_turno);
    END IF;
END $$;

-- Evita que un mismo turno tenga más de una historia clínica activa.
CREATE UNIQUE INDEX IF NOT EXISTS ux_consulta_turno_activa
ON consulta(id_turno)
WHERE id_turno IS NOT NULL AND LOWER(estado::text) = 'activo';
