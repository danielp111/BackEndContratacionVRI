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

-- =============================================================================================
-- Clases de contrato SIRECI. Fuente: hoja CONTROL, columna A: filas 290 a 319 (30 clases de
-- contrato) y filas 322 y 323 (clases de convenio, que reinician su numeración en 1).
-- Número y texto separados al cargar. Única corrección al texto del Excel: "COMISION" → "COMISIÓN".
-- El código 99999998 ("no se diligencia información en este período", fila 320) NO es una clase:
-- pertenece al reporte SIRECI y no se carga aquí.
-- =============================================================================================
INSERT IGNORE INTO clase_contrato (id, codigo_sireci, nombre, ambito, activo, creado_en, actualizado_en) VALUES
    (1 , 1 , 'AGENCIA',                                            'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2 , 2 , 'ARRENDAMIENTO y/o ADQUISICIÓN DE INMUEBLES',         'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3 , 3 , 'CESIÓN DE CRÉDITOS',                                 'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4 , 4 , 'COMISIÓN',                                           'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5 , 5 , 'COMODATO',                                           'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6 , 6 , 'COMPRAVENTA MERCANTIL',                              'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7 , 7 , 'COMPRAVENTA y/o SUMINISTRO',                         'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8 , 8 , 'CONCESIÓN',                                          'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9 , 9 , 'CONSULTORÍA',                                        'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 10, 'CONTRATOS DE ACTIVIDAD CIENTÍFICA Y TECNOLÓGICA',    'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 11, 'CONTRATOS DE ESTABILIDAD JURÍDICA',                  'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 12, 'DEPÓSITO',                                           'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, 13, 'FACTORING',                                          'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 14, 'FIDUCIA y/o ENCARGO FIDUCIARIO',                     'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 15, 'FLETAMENTO',                                         'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, 16, 'FRANQUICIA',                                         'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 17, 'INTERVENTORÍA',                                      'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, 18, 'LEASING',                                            'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (19, 19, 'MANTENIMIENTO y/o REPARACIÓN',                       'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (20, 20, 'MEDIACIÓN o MANDATO',                                'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21, 21, 'OBRA PÚBLICA',                                       'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (22, 22, 'PERMUTA',                                            'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, 23, 'PRESTACIÓN DE SERVICIOS',                            'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (24, 24, 'PRESTACIÓN DE SERVICIOS DE SALUD',                   'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (25, 25, 'PRÉSTAMO o MUTUO',                                   'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (26, 26, 'PUBLICIDAD',                                         'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (27, 27, 'RENTING',                                            'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (28, 28, 'SEGUROS',                                            'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (29, 29, 'TRANSPORTE',                                         'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (30, 30, 'OTROS',                                              'CONTRATO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (31, 1 , 'CONTRATO / CONVENIO INTERADMINISTRATIVO',            'CONVENIO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (32, 2 , 'CONVENIO DE COOPERACIÓN (NACIONAL / INTERNACIONAL)', 'CONVENIO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =============================================================================================
-- Rubros presupuestales (códigos de afectación presupuestal). Fuente: hoja CONTROL, bloque
-- "AFECTACIÓN PRESUPUESTAL": código en la columna A y nombre en la B, filas 102 a 152.
--   * El rango con nombre del Excel (Control_AfectacionPresupuestal_Depen = A102:B283) reserva
--     182 filas, pero solo 51 tienen datos; las vacías no se cargan.
--   * Los códigos venían guardados como números ("1052002.0", "9.011044001E9"): se cargan como
--     texto exacto. Ninguno trae puntos, guiones ni ceros a la izquierda.
--   * El nombre es lo que el Excel llama "dependencia de afectación" (Control_Dependencia_Afectacion).
--   * Limpieza: espacios sobrantes y dobles; comillas que envolvían todo el texto (fila 111) o
--     quedaban sueltas al final (filas 109 y 115). Las tildes faltantes NO se corrigen: son nombres
--     oficiales de proyectos del sistema financiero.
--   * "Servicios prestados a Empresas y" aparece en dos rubros (9011141 y 9011044001) y parece
--     truncado en el Excel: se carga tal cual, POR CONFIRMAR con la VRI.
-- =============================================================================================
INSERT IGNORE INTO rubro_presupuestal (id, codigo, nombre, activo, creado_en, actualizado_en) VALUES
    (1 , '1052002', 'Museo de Historia Natural', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2 , '9011141', 'Servicios prestados a Empresas y', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3 , '9011044001', 'Servicios prestados a Empresas y', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4 , '4902182', 'Convenio de Monto fijo No.JI-G-040', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5 , '1048111', 'Plan de Mantenimiento', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6 , '1027008', 'Desarrollo de consult relacionadas con proyectos', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7 , '1048107', 'Estampilla Nal-consultorias', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8 , '1037008', 'Investigación y desarrollo para el fortalecimiento de la cadena productiva de la fresa en el departamento del Cauca', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9 , '1037014', 'FORTALECIMIENTO DE LABORATORIOS DE ENSAYO DE LA UNIVERSIDAD DEL CAUCA, COMO ESTRATEGIA PARA FOMENTAR ACTIVIDADES DE PRESTACIÓN DE SERVICIOS, INVESTIGACIÓN, DESARROLLO TECNOLOGICO E INNOVACIÓN EN EL DEPARTAMENTO CAUCA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, '1037007', 'CONSOLIDACION DE PROCESOS DE TRANSFERENCIA DE CONOCIMIENTO Y TECNOLOGIAS PARA LA PRODUCCION DE MATERIALES BIODEGRADABLES DESARROLLADOS EN EL DEPARTAMENTO DEL CAUCA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, '1037006', 'Implementación de un programa para la participación y articulación del ecosistema Regional de Ciencia, Tecnología e Innovación del Cauca', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, '4901501', 'Propuesta de implementación del programa ondas en los mpios de Popayán, Silvia y Morales', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, '4901481', 'Fortalecimiento de la red de valor de la yuca en Colombia mediante coinnovación en producción primaria, transformación y acceso a mercados con criterios de sostenibilidad, competitividad circularidad.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, '4902184', 'Identificación de las necesidades en rehabilitación de la población residente en 4 municipios con el fin de establecer recomendaciones orientadas hacia el ordenamiento de los servicios de rehabilitación', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, '1034001', 'Jovenes Investigadores', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, '1034002', 'Convocatorias VRI', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, '1036002', 'Sello Editorial VRI', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, '1036003', 'División de Innovación, Emprendimiento y Articulación con el Entorno - DAE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (19, '1031', 'Dirección Vice-rectoría Investigaciones', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (20, '1126001', 'Serv. para la comunidad, sociales', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21, '1048103', 'Modernización del portal web Unicauca', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (22, '9011051', 'Serv. prestados a las Empresas y Servicios de producción', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, '1034004', 'Gestión del conocimiento en ciencia, tecnología, innovación- Creación', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (24, '1034005', 'Fortalecimiento de la oficina de apropiacion social del conocimiento desde la ciencia, tecnología, innovación- Creación', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (25, '1037032', 'Implementacion de una estrategia para mejorar el transito a la educacion superior de jovenes de grados decimo y once del departamento del cauca', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (26, '1034006', 'Formalización de mineros artesanales en la región del NAYA de la universidad el cauca', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (27, '1037020', 'DESARROLLO DE ESTRATEGIAS DE SEGURIDAD ALIMENTARIA E HIDRICA PARA LA REACTIVACIÓN ECONOMICA DE COMUNIDADES RURALES, MEDIANTE TRANSFERENCIA DE TECNOLOGIAS Y CONOCIMIENTOS PARA LA INNOVACIÓN COMO MEDIDA DE ATENCIÓN A LA EMERGENCIA COVID -19 EN EL CAUCA.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (28, '1037031', 'FORTALECIMIENTO DE UNA CAFICULTURA AGROECOLOGICA COMO ESTRATEGIA DE ADAPTACION AL CAMBIO CLIMATICO EN EL DEPARTAMENTO DEL CAUCA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (29, '1037027', 'FORTALECIMIENTO BIOECONOMICO PARA LA REACTIVACION SOCIAL Y PRODUCTIVA A PARTIR DE LA OFERTA DE SERVICIOS ECOSISTEMICOS HIDRICOS EN EL CONTEXTO DEL CAMBIO CLIMÁTICO Y LOS RETOS DEL COVID18 EN MUNICIPIOS PRIORIZADOS DEL DEPARTAMENTO DEL CAUCA BPIN 2021000100086 1D 5797', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (30, '4901500', 'Contrato de financiamiento de recuperación contingente No. 776 de 2023 Minciencias.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (31, '4901506', 'Contrato 2023-0871 ICETEX, MCTEL, POLTEC', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (32, '4901504', 'Contrato 2022-0795 ICETEX, MCTEL, UNAD', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (33, '4901494', 'Conv. cooperación Univalle', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (34, '1037019', 'Fortalecimiento de Capacidades de CTEI para la Innovación Educativa en Educación Basica y Media mediante uso de la Plataforma de recomendación de contenidos de video...', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (35, '1037018', 'Implementación del Py de Jovenes Investigadores e Innovadores en el Dpto del Cauca...', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (36, '4901477', 'Convenio específico Unal Sede Medellín y la Universidad del Cauca proyecto titulado "econavipesca del pacífico: ecosistema para la navegación pesquera sustentable en el municipio de Guapi, Cauca.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (37, '9911031', 'PFC- Implementación del Programa de excelencia en Investigacion', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (38, '4901488', 'Contrato # 80740-131', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (39, '4901508', 'CONV. MIN CULTURA SONIDOS PARA LA CONSTRUCCIÓN DE PAZ', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (40, '4901514', 'Conv. Interadm. 738 - Min Justicia y Derecho', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (41, '1037010', 'Conformación de un Centro de Desarrollo Tecnológico', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (42, '1037028', 'Fortalecimiento Bioeconómico', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (43, '4901528', 'CONT. DE FINANC. DE RECUP.CONT.567 DE 2025', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (44, '1037029', 'FORTALECIMIENTO BIOECONÓMICO PARA LA REACTIVACIÓN SOCIAL Y PRODUCTIVA A PARTIR DE LA OFERTA DE SERVICIOS ECOSISTEMICOS HÍDRICOS EN EL CONTEXTO DEL CAMBIO CLIMÁTICO Y LOS RETOS DEL COVID19 EN MUNICIPIOS PRIORIZADOS DEL DEPARTAMENTO DEL CAUCA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (45, '4901517', 'CONVENIO USURCOLOMBIANA Y UNICAUCA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (46, '4901521', 'CONTRATO 112721-210-2024 FIDUCOLDEZ', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (47, '1034003', 'FORTALECIMIENTO DE LA TRANSFERENCIA DE RESULTADOS', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (48, '1037003', 'IMPLEMENTACION DE UNA RED DE INFORMACION DEL TALENTO HUMANO PARA LA INNOVACION SOCIAL Y PRODUCTIVA DEL DEPARTAMENTO DEL CAUCA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (49, '4902189', 'ACUERDO PY SUCCES MALARDALEN UNIVERSITY', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (50, '4901505', 'CON.2023-0870 ICETEX-MCTel-UNIVERSIDADES', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (51, '4901523', 'CONVENIO INTERADM. 395-2025 ICANH', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
