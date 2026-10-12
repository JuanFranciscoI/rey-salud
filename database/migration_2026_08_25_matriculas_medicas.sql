-- Asigna matrículas de demostración únicas y con formato uniforme a todos
-- los médicos, reemplazando marcadores temporales y valores inconsistentes.
UPDATE personal
SET matricula = 'MP-' || LPAD((50000 + id_personal)::text, 5, '0')
WHERE tipo_personal = 'Médico';
