-- Retira dos diagnósticos de prueba sin uso clínico.
CREATE TABLE IF NOT EXISTS respaldo_catalogo_diagnostico_20260826
AS SELECT * FROM catalogo_diagnostico WITH NO DATA;
CREATE TABLE IF NOT EXISTS respaldo_catalogo_diag_especialidad_20260826
AS SELECT * FROM catalogo_diagnostico_especialidad WITH NO DATA;

INSERT INTO respaldo_catalogo_diagnostico_20260826
SELECT d.* FROM catalogo_diagnostico d WHERE d.id IN (18,19)
AND NOT EXISTS (SELECT 1 FROM respaldo_catalogo_diagnostico_20260826 r WHERE r.id=d.id);

INSERT INTO respaldo_catalogo_diag_especialidad_20260826
SELECT de.* FROM catalogo_diagnostico_especialidad de WHERE de.id_diagnostico IN (18,19)
AND NOT EXISTS (
    SELECT 1 FROM respaldo_catalogo_diag_especialidad_20260826 r
    WHERE r.id_diagnostico=de.id_diagnostico AND r.id_especialidad=de.id_especialidad
);

DELETE FROM catalogo_diagnostico_especialidad WHERE id_diagnostico IN (18,19);
DELETE FROM catalogo_diagnostico WHERE id IN (18,19);
