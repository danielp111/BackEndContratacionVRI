-- =============================================================================================
-- Carga inicial de series documentales, tipos de contrato y consecutivos 2026.
-- Fuente: hoja CONTROL de VRI_2026_BASE_CONTRATACIÓN_DIRECTA_2026_VRI.xlsx
--   fila 1: tipos de contrato · fila 2: series · fila 3: CANTIDAD (último número usado en 2026).
--
-- Se ejecuta en CADA arranque (spring.sql.init.mode=always), por eso es idempotente:
--   * INSERT IGNORE: si la fila ya existe, no hace nada. NO se usa ON DUPLICATE KEY UPDATE porque
--     sobrescribiría lo que cambió después (el consecutivo volvería a 108 aunque OPS vaya en 140,
--     o se reactivaría un tipo que un ADMIN inactivó).
--   * ids fijos: el choque es por llave primaria, así que renombrar un tipo o una serie no hace que
--     el siguiente arranque la vuelva a crear con el nombre original.
--
-- Provisional: antes de producción pasar a Flyway (migraciones versionadas) y ddl-auto=validate.
-- =============================================================================================

-- es_convenio = TRUE solo para las series 6.1-18.x (regla de SerieDocumentalEntity).
INSERT IGNORE INTO serie_documental (id, codigo, es_convenio, activo, creado_en, actualizado_en) VALUES
    (1,  '6.1-17.13', FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2,  '6.1-17.15', FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3,  '6.1-17.3',  FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4,  '6.1-17.4',  FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5,  '6.1-17.12', FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6,  '6.1-17.11', FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7,  '6.1-17.9',  FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8,  '6.1-17.6',  FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9,  '6.1-17.7',  FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, '6.1-18.1',  TRUE,  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, '6.1-18.2',  TRUE,  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, '6.1-18.3',  TRUE,  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, '6.1-17.16', FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, '6.1-17.8',  FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, '6.1-17.17', FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Nombres con tildes y bien escritos: se muestran al usuario y en reportes.
-- (En el Excel: OPS, SUMINISTRO, APRENDIZAJE, PASANTIA, ..., CONVENIOS INTERADM, INTERADMINISTRATIVOS.)
INSERT IGNORE INTO tipo_contrato (id, nombre, serie_id, activo, creado_en, actualizado_en) VALUES
    (1,  'OPS',                          1,  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2,  'Suministro',                   2,  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3,  'Aprendizaje',                  3,  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4,  'Pasantía',                     3,  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5,  'Judicatura',                   3,  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6,  'Arrendamiento',                4,  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7,  'Obra',                         5,  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8,  'Mandato',                      6,  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9,  'Consultoría',                  7,  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 'Comodato',                     8,  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 'Compraventa',                  9,  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 'Convenio interadministrativo', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, 'Convenio interinstitucional',  11, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 'Convenio marco',               12, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 'Transporte',                   13, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, 'Consignación',                 14, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 'Contrato interadministrativo', 15, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Último número usado en 2026 (fila 3, CANTIDAD). El próximo contrato de OPS será 6.1-17.13/109.
-- La serie 6.1-17.3 (Aprendizaje, Pasantía, Judicatura) tiene una sola cantidad: el consecutivo es por serie.
INSERT IGNORE INTO consecutivo (serie_id, anio, ultimo_numero, creado_en, actualizado_en) VALUES
    (1,  2026, 108, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2,  2026, 15,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3,  2026, 1,   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4,  2026, 0,   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5,  2026, 0,   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6,  2026, 0,   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7,  2026, 1,   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8,  2026, 0,   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9,  2026, 3,   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 2026, 0,   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 2026, 0,   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 2026, 0,   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, 2026, 0,   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 2026, 0,   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 2026, 0,   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
