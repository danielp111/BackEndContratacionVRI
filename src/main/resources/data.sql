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

-- =============================================================================================
-- Dependencias de la universidad. Fuente: lista entregada por la VRI (55 dependencias, en seis
-- grupos). Los nombres van tal como vienen en esa lista, con tildes y sin anteponer el tipo
-- ("de Admisiones" es una división; "Jurídica", una oficina).
--   * id 1 = Vicerrectoría de Investigaciones: coincide con la fila que ya existía en desarrollo,
--     a la que apuntan abogados; INSERT IGNORE la salta sin cambiarla.
--   * NO se usó la lista Control_Dependencias del Excel (CONTROL!C19:C100): mezcla códigos
--     numéricos y nombres.
--   * Posibles duplicados POR CONFIRMAR con la VRI (se cargan todos activos): Facultad de Ciencias
--     Agrarias / Agropecuarias; de Admisiones / Admisiones y Registro; de Salud Ocupacional /
--     de Seguridad y Salud en el Trabajo; de Gestión del Talento Humano / Recursos Humanos - Pensiones.
-- =============================================================================================
INSERT IGNORE INTO dependencia (id, nombre, activo, creado_en, actualizado_en) VALUES
    (1 , 'Vicerrectoría de Investigaciones',                                TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- Facultades y posgrados
    (2 , 'Facultad de Artes',                                               TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3 , 'Facultad de Ciencias Agrarias',                                   TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4 , 'Facultad de Ciencias Agropecuarias',                              TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5 , 'Facultad de Ciencias Contables, Económicas y Administrativas',    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6 , 'Facultad de Ciencias de la Salud',                                TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7 , 'Facultad de Ciencias Humanas y Sociales',                         TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8 , 'Facultad de Ciencias Naturales, Exactas y de la Educación',       TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9 , 'Facultad de Derecho, Ciencias Políticas y Sociales',              TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 'Facultad de Ingeniería Civil',                                    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 'Facultad de Ingeniería Electrónica y Telecomunicaciones',         TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 'Posgrados',                                                       TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, 'Programa de Especialización, Maestría y Doctorado FIET',          TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 'Doctorado en Ciencias Ambientales',                               TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 'Doctorado en Ingeniería Telemática',                              TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, 'Doctorado en Ciencias de la Educación',                           TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 'Maestría en Educación Becas Excelencia MEN',                      TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, 'Maestría en Educación Popular Santander de Quilichao',            TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- Rectoría y vicerrectorías
    (19, 'Rectoría',                                                        TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (20, 'Secretaría General',                                              TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21, 'Vicerrectoría Académica',                                         TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (22, 'Vicerrectoría Administrativa',                                    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, 'Vicerrectoría de Cultura y Bienestar',                            TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- Divisiones
    (24, 'Administrativa y de Servicios',                                   TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (25, 'de Admisiones',                                                   TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (26, 'de Gestión de la Investigación',                                  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (27, 'de Gestión de la Recreación y el Deporte',                        TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (28, 'de Gestión de Medios y Recursos Bibliográficos',                  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (29, 'de Gestión de Salud Integral',                                    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (30, 'de Gestión del Talento Humano',                                   TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (31, 'de Gestión Financiera',                                           TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (32, 'de Innovación, Emprendimiento y Articulación con el Entorno',     TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (33, 'de Tecnologías de la Información y las Telecomunicaciones',       TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- Oficinas
    (34, 'de Control Interno',                                              TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (35, 'de Planeación y Desarrollo Institucional',                        TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (36, 'de Relaciones Interinstitucionales e Internacionales',            TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (37, 'Jurídica',                                                        TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- Áreas
    (38, 'de Adquisiciones e Inventarios',                                  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (39, 'de Desarrollo Editorial',                                         TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (40, 'de Salud Ocupacional',                                            TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (41, 'de Seguridad, Control y Movilidad',                               TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (42, 'de Seguridad y Salud en el Trabajo',                              TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (43, 'de Servicio y Mantenimiento',                                     TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- Centros y otras unidades
    (44, 'Admisiones y Registro',                                           TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (45, 'CECAV',                                                           TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (46, 'Centro de Regionalización',                                       TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (47, 'Centro de Educación Continua',                                    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (48, 'Centro de Gestión de la Calidad y la Acreditación Institucional', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (49, 'Centro de Gestión de las Comunicaciones',                         TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (50, 'Centro Universitario en Salud Alfonso López',                     TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (51, 'Consultorio Jurídico',                                            TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (52, 'Egresados',                                                       TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (53, 'Recursos Humanos - Pensiones',                                    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (54, 'Unidad de Salud',                                                 TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (55, 'Unilingua',                                                       TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =============================================================================================
-- Catálogos de listas cortas y sus ítems. Fuente: hoja CONTROL del libro de contratación, salvo las
-- aseguradoras, que no están en CONTROL y salen de la columna COMPAÑIA ASEGURADORA de los contratos.
--   * Sin ids fijos: la clave natural es catalogo.codigo y (catalogo_id, codigo) para los ítems, así
--     no choca con registros que ya existan con otros ids.
--   * codigo = mnemónico que usa el programa (CC, NIT...); codigo_sireci = número del SIRECI.
--   * orden = código SIRECI (o el orden del Excel si no tiene). Los selectores ordenan por orden y nombre.
--   * Se excluyen las opciones "NO SE DILIGENCIA INFORMACIÓN PARA ESTE FORMULARIO EN ESTE PERÍODO DE
--     REPORTE": pertenecen al reporte SIRECI, no son valores de un registro. "4 NO SE HA ADICIONADO..."
--     sí es un valor real del tipo de adición.
--   * Aseguradoras unificadas: SEGUROS MUNDIAL (y "S.A"), SEGUROS DEL ESTADO (y "SA"), SURA,
--     ASEGURADORA SOLIDARIA DE COLOMBIA (y "ASEGURADORA SOLIDARIA"). "N/A" (105 contratos) significa
--     sin póliza y no se carga.
-- =============================================================================================
INSERT IGNORE INTO catalogo (codigo, nombre, descripcion, activo, creado_en, actualizado_en) VALUES
    ('TIPO_IDENTIFICACION', 'Tipo de identificación', 'Tipos de documento del SIRECI (hoja CONTROL, A348:A351).', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('NATURALEZA', 'Naturaleza', 'Naturaleza del contratista en el SIRECI (hoja CONTROL, A341:A343).', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TIPO_SEGUIMIENTO', 'Tipo de seguimiento', 'Supervisión o interventoría del contrato (hoja CONTROL, A356:A358).', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TIPO_ANTICIPO', 'Anticipo', 'Anticipo o pago anticipado (hoja CONTROL, A363:A365).', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TIPO_ADICION', 'Tipo de adición', 'Adiciones en valor o en tiempo (hoja CONTROL, A369:A372).', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FUENTE_FINANCIACION', 'Fuente de financiación', 'Fuente de los recursos del contrato (hoja CONTROL, A455:A459). Sin código SIRECI.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ASEGURADORA', 'Aseguradora', 'Compañías de seguros de las pólizas (de la columna COMPAÑIA ASEGURADORA de los contratos). Sin código SIRECI.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT IGNORE INTO catalogo_item (catalogo_id, codigo, codigo_sireci, nombre, orden, activo, creado_en, actualizado_en)
    SELECT k.id, v.codigo, v.codigo_sireci, v.nombre, v.orden, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM catalogo k JOIN (
    SELECT 'NIT' AS codigo, '1' AS codigo_sireci, 'NIT' AS nombre, 1 AS orden
    UNION ALL SELECT 'RUT' AS codigo, '2' AS codigo_sireci, 'RUT - REGISTRO ÚNICO TRIBUTARIO' AS nombre, 2 AS orden
    UNION ALL SELECT 'CC' AS codigo, '3' AS codigo_sireci, 'CÉDULA DE CIUDADANÍA' AS nombre, 3 AS orden
    UNION ALL SELECT 'CE' AS codigo, '4' AS codigo_sireci, 'CÉDULA DE EXTRANJERÍA' AS nombre, 4 AS orden
    ) v ON 1 = 1
    WHERE k.codigo = 'TIPO_IDENTIFICACION';
INSERT IGNORE INTO catalogo_item (catalogo_id, codigo, codigo_sireci, nombre, orden, activo, creado_en, actualizado_en)
    SELECT k.id, v.codigo, v.codigo_sireci, v.nombre, v.orden, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM catalogo k JOIN (
    SELECT 'NATURAL' AS codigo, '1' AS codigo_sireci, 'PERSONA NATURAL' AS nombre, 1 AS orden
    UNION ALL SELECT 'JURIDICA' AS codigo, '2' AS codigo_sireci, 'PERSONA JURÍDICA' AS nombre, 2 AS orden
    UNION ALL SELECT 'CONSORCIO_UT' AS codigo, '3' AS codigo_sireci, 'P JURÍDICA - UNIÓN TEMPORAL o CONSORCIO' AS nombre, 3 AS orden
    ) v ON 1 = 1
    WHERE k.codigo = 'NATURALEZA';
INSERT IGNORE INTO catalogo_item (catalogo_id, codigo, codigo_sireci, nombre, orden, activo, creado_en, actualizado_en)
    SELECT k.id, v.codigo, v.codigo_sireci, v.nombre, v.orden, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM catalogo k JOIN (
    SELECT 'INTERVENTOR' AS codigo, '1' AS codigo_sireci, 'INTERVENTOR' AS nombre, 1 AS orden
    UNION ALL SELECT 'SUPERVISOR' AS codigo, '2' AS codigo_sireci, 'SUPERVISOR' AS nombre, 2 AS orden
    UNION ALL SELECT 'INTERVENTOR_SUPERVISOR' AS codigo, '3' AS codigo_sireci, 'INTERVENTOR y SUPERVISOR' AS nombre, 3 AS orden
    ) v ON 1 = 1
    WHERE k.codigo = 'TIPO_SEGUIMIENTO';
INSERT IGNORE INTO catalogo_item (catalogo_id, codigo, codigo_sireci, nombre, orden, activo, creado_en, actualizado_en)
    SELECT k.id, v.codigo, v.codigo_sireci, v.nombre, v.orden, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM catalogo k JOIN (
    SELECT 'ANTICIPO' AS codigo, '1' AS codigo_sireci, 'ANTICIPOS' AS nombre, 1 AS orden
    UNION ALL SELECT 'PAGO_ANTICIPADO' AS codigo, '2' AS codigo_sireci, 'PAGO ANTICIPADO' AS nombre, 2 AS orden
    UNION ALL SELECT 'NO_PACTADO' AS codigo, '3' AS codigo_sireci, 'NO PACTADOS' AS nombre, 3 AS orden
    ) v ON 1 = 1
    WHERE k.codigo = 'TIPO_ANTICIPO';
INSERT IGNORE INTO catalogo_item (catalogo_id, codigo, codigo_sireci, nombre, orden, activo, creado_en, actualizado_en)
    SELECT k.id, v.codigo, v.codigo_sireci, v.nombre, v.orden, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM catalogo k JOIN (
    SELECT 'VALOR' AS codigo, '1' AS codigo_sireci, 'ADICIÓN EN VALOR (DIFERENTE A PRÓRROGAS)' AS nombre, 1 AS orden
    UNION ALL SELECT 'TIEMPO' AS codigo, '2' AS codigo_sireci, 'ADICIÓN EN TIEMPO (PRÓRROGAS)' AS nombre, 2 AS orden
    UNION ALL SELECT 'VALOR_TIEMPO' AS codigo, '3' AS codigo_sireci, 'ADICIÓN EN VALOR y EN TIEMPO' AS nombre, 3 AS orden
    UNION ALL SELECT 'SIN_ADICION' AS codigo, '4' AS codigo_sireci, 'NO SE HA ADICIONADO NI EN VALOR y EN TIEMPO' AS nombre, 4 AS orden
    ) v ON 1 = 1
    WHERE k.codigo = 'TIPO_ADICION';
INSERT IGNORE INTO catalogo_item (catalogo_id, codigo, codigo_sireci, nombre, orden, activo, creado_en, actualizado_en)
    SELECT k.id, v.codigo, v.codigo_sireci, v.nombre, v.orden, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM catalogo k JOIN (
    SELECT 'FUNCIONAMIENTO' AS codigo, NULL AS codigo_sireci, 'Funcionamiento' AS nombre, 1 AS orden
    UNION ALL SELECT 'INVERSION' AS codigo, NULL AS codigo_sireci, 'Inversión' AS nombre, 2 AS orden
    UNION ALL SELECT 'SGR' AS codigo, NULL AS codigo_sireci, 'SGR' AS nombre, 3 AS orden
    UNION ALL SELECT 'EXTERNOS' AS codigo, NULL AS codigo_sireci, 'Externos' AS nombre, 4 AS orden
    UNION ALL SELECT 'OTRO' AS codigo, NULL AS codigo_sireci, 'Otro' AS nombre, 5 AS orden
    ) v ON 1 = 1
    WHERE k.codigo = 'FUENTE_FINANCIACION';
INSERT IGNORE INTO catalogo_item (catalogo_id, codigo, codigo_sireci, nombre, orden, activo, creado_en, actualizado_en)
    SELECT k.id, v.codigo, v.codigo_sireci, v.nombre, v.orden, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM catalogo k JOIN (
    SELECT 'SEGUROS_MUNDIAL' AS codigo, NULL AS codigo_sireci, 'SEGUROS MUNDIAL' AS nombre, 1 AS orden
    UNION ALL SELECT 'SEGUROS_ESTADO' AS codigo, NULL AS codigo_sireci, 'SEGUROS DEL ESTADO' AS nombre, 2 AS orden
    UNION ALL SELECT 'SURA' AS codigo, NULL AS codigo_sireci, 'SURA' AS nombre, 3 AS orden
    UNION ALL SELECT 'SOLIDARIA' AS codigo, NULL AS codigo_sireci, 'ASEGURADORA SOLIDARIA DE COLOMBIA' AS nombre, 4 AS orden
    ) v ON 1 = 1
    WHERE k.codigo = 'ASEGURADORA';

-- =============================================================================================
-- Ordenadores del gasto. Fuente: hoja CONSOLIDADO ANUAL (columnas N, O y P: 127 contratos, 2
-- combinaciones) y lista ORDENADOR DEL GASTO de la hoja CONTROL (A428:C436, 9 filas). En total
-- 8 personas y 9 combinaciones de nombre, cédula y cargo.
--   * Terceros: se crean como persona natural con cédula SOLO si no existe ya un tercero con esa
--     cédula (clave natural: tipo + número, sin ids fijos). Nombres tal como vienen en el Excel
--     (mayúsculas, sin tildes); nombres y apellidos separados tomando las dos últimas palabras como
--     apellidos.
--   * Periodos: solo para las 2 combinaciones con contratos en 2026, desde 2026-01-01, abiertos y
--     POR CONFIRMAR con la VRI (el Excel no trae fechas). Las otras 7 combinaciones quedan sin
--     periodo hasta que la VRI entregue sus fechas. es_delegado = FALSE por confirmar.
--   * Cargo corregido: "VICERECTOR DE INVESTIGACIONES" (sic) → "Vicerrector de Investigaciones".
-- =============================================================================================
INSERT IGNORE INTO tercero (naturaleza, tipo_identificacion_id, numero_documento, nombre_completo, activo, creado_en, actualizado_en)
    SELECT 'NATURAL', c.id, '76314448', 'FRANCISCO JOSE PINO CORREA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM catalogo_item c WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC';
INSERT IGNORE INTO persona_natural (tercero_id, nombres, apellidos)
    SELECT t.id, 'FRANCISCO JOSE', 'PINO CORREA'
    FROM tercero t JOIN catalogo_item c ON c.id = t.tipo_identificacion_id
    WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC' AND t.numero_documento = '76314448' AND t.naturaleza = 'NATURAL';
INSERT IGNORE INTO tercero (naturaleza, tipo_identificacion_id, numero_documento, nombre_completo, activo, creado_en, actualizado_en)
    SELECT 'NATURAL', c.id, '76311561', 'DEIBAR RENE HURTADO HERRERA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM catalogo_item c WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC';
INSERT IGNORE INTO persona_natural (tercero_id, nombres, apellidos)
    SELECT t.id, 'DEIBAR RENE', 'HURTADO HERRERA'
    FROM tercero t JOIN catalogo_item c ON c.id = t.tipo_identificacion_id
    WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC' AND t.numero_documento = '76311561' AND t.naturaleza = 'NATURAL';
INSERT IGNORE INTO tercero (naturaleza, tipo_identificacion_id, numero_documento, nombre_completo, activo, creado_en, actualizado_en)
    SELECT 'NATURAL', c.id, '34570235', 'CIELO PEREZ SOLANO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM catalogo_item c WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC';
INSERT IGNORE INTO persona_natural (tercero_id, nombres, apellidos)
    SELECT t.id, 'CIELO', 'PEREZ SOLANO'
    FROM tercero t JOIN catalogo_item c ON c.id = t.tipo_identificacion_id
    WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC' AND t.numero_documento = '34570235' AND t.naturaleza = 'NATURAL';
INSERT IGNORE INTO tercero (naturaleza, tipo_identificacion_id, numero_documento, nombre_completo, activo, creado_en, actualizado_en)
    SELECT 'NATURAL', c.id, '10535844', 'JOSE LUIS DIAGO FRANCO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM catalogo_item c WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC';
INSERT IGNORE INTO persona_natural (tercero_id, nombres, apellidos)
    SELECT t.id, 'JOSE LUIS', 'DIAGO FRANCO'
    FROM tercero t JOIN catalogo_item c ON c.id = t.tipo_identificacion_id
    WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC' AND t.numero_documento = '10535844' AND t.naturaleza = 'NATURAL';
INSERT IGNORE INTO tercero (naturaleza, tipo_identificacion_id, numero_documento, nombre_completo, activo, creado_en, actualizado_en)
    SELECT 'NATURAL', c.id, '34552816', 'JANETH NOGUERA RAMOS', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM catalogo_item c WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC';
INSERT IGNORE INTO persona_natural (tercero_id, nombres, apellidos)
    SELECT t.id, 'JANETH', 'NOGUERA RAMOS'
    FROM tercero t JOIN catalogo_item c ON c.id = t.tipo_identificacion_id
    WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC' AND t.numero_documento = '34552816' AND t.naturaleza = 'NATURAL';
INSERT IGNORE INTO tercero (naturaleza, tipo_identificacion_id, numero_documento, nombre_completo, activo, creado_en, actualizado_en)
    SELECT 'NATURAL', c.id, '7551810', 'HECTOR SAMUEL VILLADA CASTILLO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM catalogo_item c WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC';
INSERT IGNORE INTO persona_natural (tercero_id, nombres, apellidos)
    SELECT t.id, 'HECTOR SAMUEL', 'VILLADA CASTILLO'
    FROM tercero t JOIN catalogo_item c ON c.id = t.tipo_identificacion_id
    WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC' AND t.numero_documento = '7551810' AND t.naturaleza = 'NATURAL';
INSERT IGNORE INTO tercero (naturaleza, tipo_identificacion_id, numero_documento, nombre_completo, activo, creado_en, actualizado_en)
    SELECT 'NATURAL', c.id, '76321704', 'ALEJANDRO TOLEDO TOVAR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM catalogo_item c WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC';
INSERT IGNORE INTO persona_natural (tercero_id, nombres, apellidos)
    SELECT t.id, 'ALEJANDRO', 'TOLEDO TOVAR'
    FROM tercero t JOIN catalogo_item c ON c.id = t.tipo_identificacion_id
    WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC' AND t.numero_documento = '76321704' AND t.naturaleza = 'NATURAL';
INSERT IGNORE INTO tercero (naturaleza, tipo_identificacion_id, numero_documento, nombre_completo, activo, creado_en, actualizado_en)
    SELECT 'NATURAL', c.id, '15505403', 'JUAN CARLOS CASAS ZAPATA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM catalogo_item c WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC';
INSERT IGNORE INTO persona_natural (tercero_id, nombres, apellidos)
    SELECT t.id, 'JUAN CARLOS', 'CASAS ZAPATA'
    FROM tercero t JOIN catalogo_item c ON c.id = t.tipo_identificacion_id
    WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC' AND t.numero_documento = '15505403' AND t.naturaleza = 'NATURAL';
-- Periodos (uk_ordenador_gasto_periodo: tercero + cargo + fecha_desde hace idempotente la carga).
INSERT IGNORE INTO ordenador_gasto (tercero_id, cargo, es_delegado, acto_delegacion, fecha_desde, fecha_hasta, activo, creado_en, actualizado_en)
    SELECT pn.tercero_id, 'Vicerrector de Investigaciones', FALSE, NULL, DATE '2026-01-01', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM persona_natural pn JOIN tercero t ON t.id = pn.tercero_id JOIN catalogo_item c ON c.id = t.tipo_identificacion_id
    WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC' AND t.numero_documento = '76314448';
INSERT IGNORE INTO ordenador_gasto (tercero_id, cargo, es_delegado, acto_delegacion, fecha_desde, fecha_hasta, activo, creado_en, actualizado_en)
    SELECT pn.tercero_id, 'Rector', FALSE, NULL, DATE '2026-01-01', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM persona_natural pn JOIN tercero t ON t.id = pn.tercero_id JOIN catalogo_item c ON c.id = t.tipo_identificacion_id
    WHERE c.catalogo_id = (SELECT k.id FROM catalogo k WHERE k.codigo = 'TIPO_IDENTIFICACION') AND c.codigo = 'CC' AND t.numero_documento = '76311561';
