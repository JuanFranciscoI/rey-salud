-- Las especialidades clínicas pertenecen exclusivamente al personal médico.
-- Corrige datos existentes de usuarios con rol administrativo o de secretaría.
UPDATE personal p
SET id_especialidad = NULL
WHERE p.id_especialidad IS NOT NULL
  AND EXISTS (
      SELECT 1
      FROM usuario u
      JOIN rol r ON r.id_rol = u.id_rol
      WHERE u.id_personal = p.id_personal
        AND LOWER(r.nombre) IN ('administrador', 'secretaria', 'secretaría')
  );
