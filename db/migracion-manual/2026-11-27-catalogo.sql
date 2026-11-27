-- =============================================================================================
-- MIGRACIÓN MANUAL (MySQL 8): catálogos genéricos.
--
-- Antes: catalogo_item tenía el catálogo como texto (columna `catalogo` con un CHECK de valores
--        fijos) y era único por (catalogo, codigo_sireci).
-- Ahora: tabla `catalogo` y catalogo_item.catalogo_id (FK), con código mnemónico `codigo`,
--        `codigo_sireci` opcional y `orden`; único por (catalogo_id, codigo) y (catalogo_id, nombre).
--        Además persona_juridica.es_consorcio_union_temporal.
--
-- Por qué a mano: con ddl-auto=update Hibernate agregaría catalogo_id como NOT NULL con valor 0, la
-- FK fallaría y la columna vieja quedaría. Una base NUEVA no necesita este script (la crea Hibernate).
--
-- Cómo usarlo:
--   1. Detener el backend.
--   2. Respaldar:  mysqldump -uroot -p --single-transaction --databases contratacion_vri > respaldo.sql
--   3. Ejecutar:   mysql -uroot -p contratacion_vri < db/migracion-manual/2026-11-27-catalogo.sql
--   4. Revisar la consulta de verificación del final (debe devolver 0 ítems con código provisional).
--   5. Arrancar la versión nueva del backend (data.sql agrega los ítems de los catálogos nuevos).
--
-- Conserva los ids de catalogo_item: tercero.tipo_identificacion_id y abogado.tipo_identificacion_id
-- siguen apuntando al mismo ítem. Si el CHECK tiene otro nombre en su base, ver:
--   SELECT CONSTRAINT_NAME FROM information_schema.CHECK_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE();
-- Antes de producción: pasar a migraciones versionadas con Flyway.
-- =============================================================================================

-- 1. Tabla catalogo (igual a la que crearía Hibernate) y los 7 catálogos (mismos textos que data.sql).
CREATE TABLE IF NOT EXISTS catalogo (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    actualizado_en  DATETIME(6)  NULL,
    actualizado_por BIGINT       NULL,
    creado_en       DATETIME(6)  NOT NULL,
    creado_por      BIGINT       NULL,
    activo          BIT(1)       NOT NULL,
    codigo          VARCHAR(40)  NOT NULL,
    descripcion     VARCHAR(300) NULL,
    nombre          VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_catalogo_codigo UNIQUE (codigo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

INSERT IGNORE INTO catalogo (codigo, nombre, descripcion, activo, creado_en, actualizado_en) VALUES
    ('TIPO_IDENTIFICACION', 'Tipo de identificación', 'Tipos de documento del SIRECI (hoja CONTROL, A348:A351).', TRUE, NOW(6), NOW(6)),
    ('NATURALEZA', 'Naturaleza', 'Naturaleza del contratista en el SIRECI (hoja CONTROL, A341:A343).', TRUE, NOW(6), NOW(6)),
    ('TIPO_SEGUIMIENTO', 'Tipo de seguimiento', 'Supervisión o interventoría del contrato (hoja CONTROL, A356:A358).', TRUE, NOW(6), NOW(6)),
    ('TIPO_ANTICIPO', 'Anticipo', 'Anticipo o pago anticipado (hoja CONTROL, A363:A365).', TRUE, NOW(6), NOW(6)),
    ('TIPO_ADICION', 'Tipo de adición', 'Adiciones en valor o en tiempo (hoja CONTROL, A369:A372).', TRUE, NOW(6), NOW(6)),
    ('FUENTE_FINANCIACION', 'Fuente de financiación', 'Fuente de los recursos del contrato (hoja CONTROL, A455:A459). Sin código SIRECI.', TRUE, NOW(6), NOW(6)),
    ('ASEGURADORA', 'Aseguradora', 'Compañías de seguros de las pólizas (de la columna COMPAÑIA ASEGURADORA de los contratos). Sin código SIRECI.', TRUE, NOW(6), NOW(6));

-- 2. Columnas nuevas (primero nulas, para poder llenarlas).
ALTER TABLE catalogo_item
    ADD COLUMN catalogo_id BIGINT      NULL,
    ADD COLUMN codigo      VARCHAR(40) NULL,
    ADD COLUMN orden       INT         NULL;

-- 3. Llenarlas desde la columna vieja, conservando los ids.
UPDATE catalogo_item ci JOIN catalogo c ON c.codigo = ci.catalogo
SET ci.catalogo_id = c.id;

UPDATE catalogo_item
SET codigo = CASE
        WHEN catalogo = 'TIPO_IDENTIFICACION' AND codigo_sireci = '1' THEN 'NIT'
        WHEN catalogo = 'TIPO_IDENTIFICACION' AND codigo_sireci = '2' THEN 'RUT'
        WHEN catalogo = 'TIPO_IDENTIFICACION' AND codigo_sireci = '3' THEN 'CC'
        WHEN catalogo = 'TIPO_IDENTIFICACION' AND codigo_sireci = '4' THEN 'CE'
        ELSE CONCAT('PROVISIONAL_', id)     -- ítems no previstos: revisar y renombrar (ver verificación)
    END,
    orden = CASE WHEN codigo_sireci REGEXP '^[0-9]+$' THEN CAST(codigo_sireci AS UNSIGNED) ELSE 0 END;

-- 4. Quitar lo viejo: el CHECK (usa la columna), los índices y la columna `catalogo`.
ALTER TABLE catalogo_item DROP CHECK catalogo_item_chk_1;
ALTER TABLE catalogo_item
    DROP INDEX uk_catalogo_item_codigo,
    DROP INDEX ix_catalogo_item_catalogo_activo;
ALTER TABLE catalogo_item
    DROP COLUMN catalogo,
    MODIFY catalogo_id   BIGINT      NOT NULL,
    MODIFY codigo        VARCHAR(40) NOT NULL,
    MODIFY orden         INT         NOT NULL,
    MODIFY codigo_sireci VARCHAR(10) NULL;

-- 5. Restricciones nuevas (mismos nombres que la entidad JPA).
ALTER TABLE catalogo_item
    ADD CONSTRAINT uk_catalogo_item_codigo UNIQUE (catalogo_id, codigo),
    ADD CONSTRAINT uk_catalogo_item_nombre UNIQUE (catalogo_id, nombre),
    ADD CONSTRAINT fk_catalogo_item_catalogo FOREIGN KEY (catalogo_id) REFERENCES catalogo (id);

-- 6. Unión temporal o consorcio en personas jurídicas (falso para las existentes).
ALTER TABLE persona_juridica ADD COLUMN es_consorcio_union_temporal BIT(1) NOT NULL DEFAULT b'0';

-- 7. Verificación: debe devolver 0 filas. Si hay ítems con código PROVISIONAL_, asignarles su código.
SELECT ci.id, c.codigo AS catalogo, ci.codigo, ci.codigo_sireci, ci.nombre
FROM catalogo_item ci JOIN catalogo c ON c.id = ci.catalogo_id
WHERE ci.codigo LIKE 'PROVISIONAL\_%';
