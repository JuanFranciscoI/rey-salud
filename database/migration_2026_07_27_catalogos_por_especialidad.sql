-- Relaciona diagnósticos y tratamientos con las especialidades médicas.
-- Después de correr esta migración, la pantalla de Nueva Evolución Clínica
-- solo muestra diagnósticos y tratamientos asociados a la especialidad del médico autenticado.

CREATE TABLE IF NOT EXISTS catalogo_diagnostico_especialidad (
    id_diagnostico INTEGER NOT NULL REFERENCES catalogo_diagnostico(id) ON DELETE CASCADE,
    id_especialidad INTEGER NOT NULL REFERENCES especialidad(id_especialidad) ON DELETE CASCADE,
    PRIMARY KEY (id_diagnostico, id_especialidad)
);

CREATE TABLE IF NOT EXISTS catalogo_medicamento_especialidad (
    id_medicamento INTEGER NOT NULL REFERENCES catalogo_medicamento(id) ON DELETE CASCADE,
    id_especialidad INTEGER NOT NULL REFERENCES especialidad(id_especialidad) ON DELETE CASCADE,
    PRIMARY KEY (id_medicamento, id_especialidad)
);

CREATE INDEX IF NOT EXISTS idx_catalogo_diag_esp_especialidad
    ON catalogo_diagnostico_especialidad(id_especialidad);

CREATE INDEX IF NOT EXISTS idx_catalogo_med_esp_especialidad
    ON catalogo_medicamento_especialidad(id_especialidad);

-- Diagnósticos por especialidad.
WITH mapa(especialidad, diagnostico) AS (
    VALUES
    ('Oftalmología General', 'H00 - Chalazión'),
    ('Oftalmología General', 'H00 - Orzuelo'),
    ('Oftalmología General', 'H10 - Conjuntivitis Alérgica'),
    ('Oftalmología General', 'H10 - Conjuntivitis Bacteriana'),
    ('Oftalmología General', 'H10 - Conjuntivitis Viral'),
    ('Oftalmología General', 'H25 - Catarata Congénita'),
    ('Oftalmología General', 'H25 - CATARATA SENIL'),
    ('Oftalmología General', 'H40 - Glaucoma Agudo'),
    ('Oftalmología General', 'H40 - Glaucoma Primario de Ángulo Abierto'),
    ('Oftalmología General', 'H52 - Astigmatismo'),
    ('Oftalmología General', 'H52 - Hipermetropía'),
    ('Oftalmología General', 'H52 - Miopía'),
    ('Oftalmología General', 'H52 - Presbicia'),

    ('Retinologo', 'H35 - Degeneración Macular (DMAE)'),
    ('Retinologo', 'H36 - Retinopatía Diabética'),
    ('Retinologo', 'H40 - Glaucoma Agudo'),
    ('Retinologo', 'H40 - Glaucoma Primario de Ángulo Abierto'),

    ('Cirugía Ocular', 'H00 - Chalazión'),
    ('Cirugía Ocular', 'H00 - Orzuelo'),
    ('Cirugía Ocular', 'H18 - Queratocono'),
    ('Cirugía Ocular', 'H25 - Catarata Congénita'),
    ('Cirugía Ocular', 'H25 - CATARATA SENIL'),

    ('Oftalmopediatría', 'H00 - Chalazión'),
    ('Oftalmopediatría', 'H00 - Orzuelo'),
    ('Oftalmopediatría', 'H10 - Conjuntivitis Alérgica'),
    ('Oftalmopediatría', 'H10 - Conjuntivitis Bacteriana'),
    ('Oftalmopediatría', 'H10 - Conjuntivitis Viral'),
    ('Oftalmopediatría', 'H52 - Astigmatismo'),
    ('Oftalmopediatría', 'H52 - Hipermetropía'),
    ('Oftalmopediatría', 'H52 - Miopía'),

    ('Coronato', 'H00 - Chalazión'),
    ('Coronato', 'H00 - Orzuelo'),
    ('Coronato', 'H10 - Conjuntivitis Alérgica'),
    ('Coronato', 'H10 - Conjuntivitis Bacteriana'),
    ('Coronato', 'H10 - Conjuntivitis Viral'),
    ('Coronato', 'H18 - Queratocono')
)
INSERT INTO catalogo_diagnostico_especialidad (id_diagnostico, id_especialidad)
SELECT d.id, e.id_especialidad
FROM mapa m
JOIN catalogo_diagnostico d ON d.descripcion = m.diagnostico
JOIN especialidad e ON e.nombre = m.especialidad
ON CONFLICT DO NOTHING;

-- Tratamientos / medicamentos por especialidad.
WITH mapa(especialidad, medicamento) AS (
    VALUES
    ('Oftalmología General', 'CIPROFLOXACINA DF (Antibiótico)'),
    ('Oftalmología General', 'Derivación Quirúrgica'),
    ('Oftalmología General', 'Lágrimas Artificiales Genéricas'),
    ('Oftalmología General', 'LOTEOFTAL (Loteprednol)'),
    ('Oftalmología General', 'MOXIDROP (Moxifloxacina)'),
    ('Oftalmología General', 'OFTALFENAC (Bromfenac)'),
    ('Oftalmología General', 'OFTALOOK PLUS (Lágrimas Artificiales)'),
    ('Oftalmología General', 'OFTALPRES (Latanoprost)'),
    ('Oftalmología General', 'Receta de Anteojos'),
    ('Oftalmología General', 'TIMOLOL DENVER FARMA'),
    ('Oftalmología General', 'TOBRABIOTIC (Tobramicina)'),
    ('Oftalmología General', 'TOBRABIOTIC D (Tobramicina / Dexametasona)'),

    ('Retinologo', 'Derivación Quirúrgica'),
    ('Retinologo', 'Lágrimas Artificiales Genéricas'),
    ('Retinologo', 'MACUVITE (Suplemento Vitamínico)'),
    ('Retinologo', 'OFTALOOK PLUS (Lágrimas Artificiales)'),
    ('Retinologo', 'OFTALPRES (Latanoprost)'),
    ('Retinologo', 'TIMOLOL DENVER FARMA'),

    ('Cirugía Ocular', 'CIPROFLOXACINA DF (Antibiótico)'),
    ('Cirugía Ocular', 'Derivación Quirúrgica'),
    ('Cirugía Ocular', 'LOTEOFTAL (Loteprednol)'),
    ('Cirugía Ocular', 'MOXIDROP (Moxifloxacina)'),
    ('Cirugía Ocular', 'OFTALFENAC (Bromfenac)'),
    ('Cirugía Ocular', 'TOBRABIOTIC (Tobramicina)'),
    ('Cirugía Ocular', 'TOBRABIOTIC D (Tobramicina / Dexametasona)'),

    ('Oftalmopediatría', 'CIPROFLOXACINA DF (Antibiótico)'),
    ('Oftalmopediatría', 'Lágrimas Artificiales Genéricas'),
    ('Oftalmopediatría', 'MOXIDROP (Moxifloxacina)'),
    ('Oftalmopediatría', 'OFTALOOK PLUS (Lágrimas Artificiales)'),
    ('Oftalmopediatría', 'Receta de Anteojos'),
    ('Oftalmopediatría', 'TOBRABIOTIC (Tobramicina)'),
    ('Oftalmopediatría', 'TOBRABIOTIC D (Tobramicina / Dexametasona)'),

    ('Coronato', 'CIPROFLOXACINA DF (Antibiótico)'),
    ('Coronato', 'Lágrimas Artificiales Genéricas'),
    ('Coronato', 'LOTEOFTAL (Loteprednol)'),
    ('Coronato', 'MOXIDROP (Moxifloxacina)'),
    ('Coronato', 'OFTALFENAC (Bromfenac)'),
    ('Coronato', 'OFTALOOK PLUS (Lágrimas Artificiales)')
)
INSERT INTO catalogo_medicamento_especialidad (id_medicamento, id_especialidad)
SELECT m.id, e.id_especialidad
FROM mapa mp
JOIN catalogo_medicamento m ON m.descripcion = mp.medicamento
JOIN especialidad e ON e.nombre = mp.especialidad
ON CONFLICT DO NOTHING;
