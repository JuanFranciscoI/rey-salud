-- Corrige tres DNI de demostración que excedían el máximo de ocho dígitos.
CREATE TABLE IF NOT EXISTS respaldo_dni_personal_20260826 (
    id_personal INTEGER PRIMARY KEY,
    dni_anterior VARCHAR(50),
    respaldado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO respaldo_dni_personal_20260826 (id_personal,dni_anterior)
SELECT id_personal,dni::text FROM personal WHERE id_personal IN (33,35,36)
ON CONFLICT (id_personal) DO NOTHING;

UPDATE personal SET dni=32423424 WHERE id_personal=33;
UPDATE personal SET dni=31312312 WHERE id_personal=35;
UPDATE personal SET dni=33423423 WHERE id_personal=36;
