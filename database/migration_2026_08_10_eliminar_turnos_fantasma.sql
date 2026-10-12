-- Elimina cancelaciones duplicadas generadas al reutilizar incorrectamente
-- un horario que ya poseía un turno atendido/finalizado.
DELETE FROM turno fantasma
WHERE LOWER(fantasma.estado) = 'cancelado'
  AND EXISTS (
      SELECT 1 FROM turno valido
      WHERE valido.id_turno <> fantasma.id_turno
        AND valido.id_paciente = fantasma.id_paciente
        AND valido.id_personal = fantasma.id_personal
        AND valido.fechahora = fantasma.fechahora
        AND LOWER(valido.estado) IN ('atendido', 'finalizado')
  );

CREATE OR REPLACE FUNCTION impedir_turno_sobre_atencion_existente()
RETURNS trigger AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM turno existente
        WHERE existente.id_personal = NEW.id_personal
          AND existente.fechahora = NEW.fechahora
          AND (
              LOWER(existente.estado) IN ('atendido', 'finalizado')
              OR EXISTS (
                  SELECT 1 FROM consulta c
                  WHERE c.id_turno = existente.id_turno
                    AND LOWER(c.estado::text) = 'activo'
              )
          )
    ) THEN
        RAISE EXCEPTION 'El horario ya posee una atención clínica definitiva y no puede reutilizarse.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_impedir_turno_sobre_atencion ON turno;
CREATE TRIGGER trg_impedir_turno_sobre_atencion
BEFORE INSERT ON turno
FOR EACH ROW EXECUTE FUNCTION impedir_turno_sobre_atencion_existente();
