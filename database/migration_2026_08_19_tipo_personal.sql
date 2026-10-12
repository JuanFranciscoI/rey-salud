-- El tipo de personal existe aunque el administrador no le otorgue acceso al sistema.
ALTER TABLE personal ADD COLUMN IF NOT EXISTS tipo_personal VARCHAR(30);

UPDATE personal p
SET tipo_personal = r.nombre
FROM usuario u
JOIN rol r ON r.id_rol = u.id_rol
WHERE u.id_personal = p.id_personal
  AND p.tipo_personal IS NULL;

UPDATE personal
SET tipo_personal = CASE
    WHEN id_especialidad IS NOT NULL OR NULLIF(TRIM(COALESCE(matricula, '')), '') IS NOT NULL THEN 'Médico'
    ELSE 'Secretaria'
END
WHERE tipo_personal IS NULL;

ALTER TABLE personal ALTER COLUMN tipo_personal SET NOT NULL;
