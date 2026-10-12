BEGIN;

ALTER TABLE usuario ALTER COLUMN contrasena TYPE VARCHAR(255);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='turno' AND column_name='estadoturno') THEN
        EXECUTE 'UPDATE turno SET estado=CASE WHEN LOWER(TRIM(COALESCE(estado,estadoturno::text,''reservado'')))=''pendiente'' THEN ''reservado'' ELSE LOWER(TRIM(COALESCE(estado,estadoturno::text,''reservado''))) END';
    ELSE
        UPDATE turno SET estado=CASE WHEN LOWER(TRIM(COALESCE(estado,'reservado')))='pendiente' THEN 'reservado' ELSE LOWER(TRIM(COALESCE(estado,'reservado'))) END;
    END IF;
END $$;

ALTER TABLE turno ALTER COLUMN estado SET DEFAULT 'reservado';
ALTER TABLE turno ALTER COLUMN estado SET NOT NULL;
ALTER TABLE turno DROP COLUMN IF EXISTS estadoturno;
ALTER TABLE turno DROP CONSTRAINT IF EXISTS turno_estado_valido;
ALTER TABLE turno ADD CONSTRAINT turno_estado_valido
    CHECK (estado IN ('reservado','esperando','en consulta','atendido','cancelado','ausente','finalizado'));

UPDATE horario SET estado='inactivo'
WHERE estado::text='activo' AND (id_personal IS NULL OR diasemana IS NULL);

WITH repetidos AS (
    SELECT id_horario, ROW_NUMBER() OVER (PARTITION BY id_personal,diasemana ORDER BY id_horario DESC) AS posicion
    FROM horario
    WHERE estado::text='activo' AND id_personal IS NOT NULL AND diasemana IS NOT NULL
)
UPDATE horario h SET estado='inactivo'
FROM repetidos r WHERE h.id_horario=r.id_horario AND r.posicion>1;

CREATE UNIQUE INDEX IF NOT EXISTS uq_horario_activo_profesional_dia
    ON horario(id_personal,diasemana)
    WHERE estado='activo'::estado_enum AND id_personal IS NOT NULL AND diasemana IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_turno_activo_profesional_hora
    ON turno(id_personal,fechahora)
    WHERE estado NOT IN ('cancelado','finalizado','ausente');

CREATE UNIQUE INDEX IF NOT EXISTS uq_turno_activo_paciente_hora
    ON turno(id_paciente,fechahora)
    WHERE estado NOT IN ('cancelado','finalizado','ausente');

CREATE UNIQUE INDEX IF NOT EXISTS uq_usuario_activo_personal
    ON usuario(id_personal)
    WHERE estado='activo'::estado_enum AND id_personal IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_usuario_username_ci ON usuario(LOWER(username));

DO $$
DECLARE tiene_datos BOOLEAN;
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='paciente' AND column_name='id_obra_social') THEN
        EXECUTE 'SELECT EXISTS(SELECT 1 FROM paciente WHERE id_obra_social IS NOT NULL)' INTO tiene_datos;
        IF tiene_datos THEN
            RAISE EXCEPTION 'No se puede quitar paciente.id_obra_social porque contiene datos';
        END IF;
    END IF;
END $$;
ALTER TABLE paciente DROP CONSTRAINT IF EXISTS fk_paciente_obra_social;
ALTER TABLE paciente DROP COLUMN IF EXISTS id_obra_social;

ALTER TABLE consulta ALTER COLUMN enfermedad TYPE TEXT;
UPDATE consulta SET estado=LOWER(TRIM(estado)) WHERE estado IS NOT NULL;
ALTER TABLE consulta DROP CONSTRAINT IF EXISTS consulta_observaciones_max_300;
ALTER TABLE consulta ADD CONSTRAINT consulta_observaciones_max_300
    CHECK (observaciones IS NULL OR CHAR_LENGTH(observaciones)<=300);

COMMIT;
