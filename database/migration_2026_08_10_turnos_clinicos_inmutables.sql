-- Repara estados históricos y vuelve inmutables los turnos con atención clínica.

-- Si se reutilizó el horario después de una cancelación incorrecta, conserva
-- el turno con historia y cancela el duplicado que no posee atención clínica.
UPDATE turno duplicado
SET estado = 'cancelado', fechacancelacion = CURRENT_DATE
WHERE LOWER(duplicado.estado) NOT IN ('cancelado', 'atendido', 'finalizado')
  AND EXISTS (
      SELECT 1
      FROM turno clinico
      JOIN consulta c ON c.id_turno = clinico.id_turno AND LOWER(c.estado::text) = 'activo'
      WHERE clinico.id_turno <> duplicado.id_turno
        AND clinico.id_personal = duplicado.id_personal
        AND clinico.fechahora = duplicado.fechahora
        AND NOT EXISTS (
            SELECT 1 FROM consulta propia
            WHERE propia.id_turno = duplicado.id_turno AND LOWER(propia.estado::text) = 'activo'
        )
  );

UPDATE turno t
SET estado = 'atendido', fechacancelacion = NULL
WHERE LOWER(t.estado) NOT IN ('atendido', 'finalizado')
  AND EXISTS (
      SELECT 1 FROM consulta c
      WHERE c.id_turno = t.id_turno AND LOWER(c.estado::text) = 'activo'
  );

CREATE OR REPLACE FUNCTION impedir_modificacion_turno_clinico()
RETURNS trigger AS $$
BEGIN
    IF LOWER(OLD.estado) IN ('atendido', 'finalizado')
       AND LOWER(NEW.estado) IS DISTINCT FROM LOWER(OLD.estado) THEN
        RAISE EXCEPTION 'El turno % está % y su estado es definitivo.', OLD.id_turno, OLD.estado;
    END IF;

    IF LOWER(NEW.estado) NOT IN ('atendido', 'finalizado')
       AND EXISTS (
           SELECT 1 FROM consulta c
           WHERE c.id_turno = OLD.id_turno AND LOWER(c.estado::text) = 'activo'
       ) THEN
        RAISE EXCEPTION 'El turno % tiene una historia clínica y no puede cancelarse ni retroceder.', OLD.id_turno;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_turno_clinico_inmutable ON turno;
CREATE TRIGGER trg_turno_clinico_inmutable
BEFORE UPDATE OF estado ON turno
FOR EACH ROW EXECUTE FUNCTION impedir_modificacion_turno_clinico();
