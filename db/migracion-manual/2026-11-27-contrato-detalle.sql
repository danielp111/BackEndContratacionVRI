-- =============================================================================================
-- MIGRACIÓN MANUAL (MySQL 8): detalle del contrato (fase 2).
--
-- Las tablas nuevas (contrato_cdp, contrato_rp, contrato_unspsc, contrato_seguimiento, modificacion,
-- pago, poliza) las crea Hibernate con ddl-auto=update: no necesitan este script.
--
-- Lo que sí necesita: Hibernate crea un CHECK con los valores de cada enum al crear la tabla, y con
-- ddl-auto=update no lo actualiza cuando el enum crece. En la fase 2 CampoContrato ganó 8 campos
-- (CDP, RP, UNSPSC, SUPERVISOR, INTERVENTOR, MODIFICACIONES, PAGOS, POLIZAS), así que una base creada
-- en la fase 1 rechaza observaciones sobre ellos ("Check constraint 'contrato_observacion_chk_1' is
-- violated"). Se quita el CHECK: el servicio ya valida el campo contra CampoContrato, y así los campos
-- de las fases siguientes no exigen otra migración. Una base NUEVA no necesita este script.
--
-- Cómo usarlo:  mysql -uroot -p contratacion_vri < db/migracion-manual/2026-11-27-contrato-detalle.sql
-- Si el CHECK tiene otro nombre en su base, ver:
--   SELECT CONSTRAINT_NAME, CHECK_CLAUSE FROM information_schema.CHECK_CONSTRAINTS
--   WHERE CONSTRAINT_SCHEMA = DATABASE() AND CHECK_CLAUSE LIKE '%TIPO_CONTRATO%';
-- =============================================================================================

ALTER TABLE contrato_observacion DROP CHECK contrato_observacion_chk_1;

-- Verificación: no debe quedar ningún CHECK sobre contrato_observacion.campo.
SELECT tc.CONSTRAINT_NAME
FROM information_schema.TABLE_CONSTRAINTS tc
JOIN information_schema.CHECK_CONSTRAINTS cc USING (CONSTRAINT_SCHEMA, CONSTRAINT_NAME)
WHERE tc.CONSTRAINT_SCHEMA = DATABASE() AND tc.TABLE_NAME = 'contrato_observacion' AND cc.CHECK_CLAUSE LIKE '%campo%';
