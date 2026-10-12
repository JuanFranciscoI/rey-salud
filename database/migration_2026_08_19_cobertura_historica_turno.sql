-- Conserva la cobertura utilizada al reservar cada turno.
ALTER TABLE turno ADD COLUMN IF NOT EXISTS id_obrasocial_atencion INTEGER
    REFERENCES obrasocial(id_obrasocial);
ALTER TABLE turno ADD COLUMN IF NOT EXISTS numeroafiliado_atencion VARCHAR(100);
ALTER TABLE turno ADD COLUMN IF NOT EXISTS cobertura_registrada BOOLEAN NOT NULL DEFAULT FALSE;

-- Para registros anteriores solo es posible fijar la cobertura actualmente conocida.
UPDATE turno t
SET id_obrasocial_atencion = p.id_obrasocial,
    numeroafiliado_atencion = p.numeroafiliado,
    cobertura_registrada = TRUE
FROM paciente p
WHERE p.id_paciente = t.id_paciente
  AND NOT t.cobertura_registrada;
