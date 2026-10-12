-- Sustituye cinco valores de prueba detectados durante la auditoría final.
-- Los registros originales quedan respaldados íntegramente.

CREATE TABLE IF NOT EXISTS respaldo_consultas_limpieza_20260826
AS SELECT * FROM consulta WITH NO DATA;

INSERT INTO respaldo_consultas_limpieza_20260826
SELECT c.* FROM consulta c
WHERE c.id_consulta IN (60,61,63,70,75)
  AND NOT EXISTS (
      SELECT 1 FROM respaldo_consultas_limpieza_20260826 r
      WHERE r.id_consulta=c.id_consulta
  );

UPDATE consulta SET
    enfermedad='H40 - Glaucoma agudo | Inicio: 02/06/2026 | Fin: 12/06/2026 | Estado: Agudo',
    tratamiento='ALIVIAPRES (Dorzolamida / Timolol) | Dosis: 1 gota por ojo | Frec: Cada 8 horas | Inicio: 02/06/2026 | Fin: 12/06/2026 | Estado: Actual'
WHERE id_consulta=60;

UPDATE consulta SET
    enfermedad='H04 - Síndrome de ojo seco | Inicio: 02/06/2026 | Fin: 10/06/2026 | Estado: Crónico',
    tratamiento='Lágrimas artificiales | Dosis: 1 gota por ojo | Frec: Cada 6 horas | Inicio: 02/06/2026 | Fin: 06/06/2026 | Estado: Actual'
WHERE id_consulta=61;

UPDATE consulta SET
    enfermedad='[H52 - Hipermetropía | Inicio: 03/06/2026 | Fin: 17/06/2026 | Est: Crónico]',
    tratamiento='[Corrección óptica con anteojos | Dosis: Según refracción | Frec: Uso diario | Inicio: 03/06/2026 | Fin: 03/06/2027 | Est: Actual]'
WHERE id_consulta=63;

UPDATE consulta SET
    tratamiento='[Control metabólico y de retina | Dosis: No aplica | Frec: Cada 90 días | Inicio: 18/06/2026 | Fin: 16/09/2026 | Est: Actual]'
WHERE id_consulta=70;

UPDATE consulta SET
    tratamiento='[Compresas tibias | Dosis: 10 minutos | Frec: Cada 8 horas | Inicio: 24/06/2026 | Fin: 01/07/2026 | Est: Finalizado]'
WHERE id_consulta=75;
