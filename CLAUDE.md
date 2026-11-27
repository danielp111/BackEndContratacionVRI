# BackEnd Contratación VRI

Backend del sistema de contratación directa de la Vicerrectoría de Investigaciones (VRI) de la Universidad del Cauca. Este archivo describe el negocio para que cualquier cambio en el código respete las reglas del proceso. El frontend vive en el repositorio hermano `FrontEndContratacionVRI`.

## Stack

- **Lenguaje**: Java, JDK 17.
- **Framework**: Spring Boot 3.x (paquetes `jakarta.*`).
- **Build**: Maven (`pom.xml`).
- **Base de datos**: MySQL 8.4, `utf8mb4` con collation **`utf8mb4_0900_ai_ci`** (sin distinguir mayúsculas ni tildes) en el servidor, la base, todas las tablas y la conexión (`connectionCollation` en la URL JDBC). Todas las tablas deben usar la misma: mezclar collations produce "Illegal mix of collations". Ojo: esta collation es NO PAD (`'OPS'` y `'OPS '` son distintos); por eso los textos se recortan antes de guardar.
- **API**: servicios REST con JSON, consumidos por el frontend en Angular.

Comandos habituales:

```
mvn clean install        # compilar y correr pruebas
mvn spring-boot:run      # levantar el servicio
mvn test                 # solo pruebas

docker compose up -d mysql        # solo la base, para correr la app con mvn
docker compose up -d --build      # base + backend (imagen backendcontratacion, contenedor backEndContratacion)
docker compose logs -f backend    # ver logs del backend
```

El `Dockerfile` compila en dos etapas (Maven → JRE 17), no corre las pruebas (`-DskipTests`) y ejecuta la app con un usuario sin privilegios. Dentro de compose el backend se conecta a MySQL por el nombre de servicio `mysql`.

## Qué problema resuelve

Hoy la VRI registra la contratación directa en un libro de Google Sheets (`documentación/VRI_2026_BASE_CONTRATACIÓN_DIRECTA_2026_VRI.xlsx`): cada abogado tiene su propia hoja, una fórmula las une en "NO MODIFICAR" y de ahí salen el consolidado anual, la lista de chequeo SECOP y los reportes SIRECI. `documentación/INFORMACIÓN VRI.xlsx` dice quién llena cada uno de los 78 campos del contrato.

El sistema reemplaza ese libro por una sola base de datos con usuarios, permisos, validaciones y auditoría. Cubre el ciclo completo: asignación del número de contrato, trámite precontractual y contractual, ejecución y pagos, liquidación, publicación en SECOP II y reporte trimestral SIRECI a la Contraloría.

Escala de referencia (vigencia 2026, enero a marzo): 128 números asignados, 84% de prestación de servicios (OPS), unos 900 contratistas y 100 supervisores en los maestros.

## Glosario

- **VRI**: Vicerrectoría de Investigaciones.
- **OPS**: orden o contrato de prestación de servicios, el tipo más común.
- **TRD**: tabla de retención documental. Cada tipo de contrato pertenece a una serie documental de la TRD, y varios tipos pueden compartirla. Según la hoja CONTROL del libro hay **17 tipos y 15 series**: OPS = `6.1-17.13`, Suministro = `6.1-17.15`, Aprendizaje/Pasantía/Judicatura = `6.1-17.3` (comparten serie), Arrendamiento = `6.1-17.4`, Obra = `6.1-17.12`, Mandato = `6.1-17.11`, Consultoría = `6.1-17.9`, Comodato = `6.1-17.6`, Compraventa = `6.1-17.7`, Transporte = `6.1-17.16`, Consignación = `6.1-17.8`, Contrato interadministrativo = `6.1-17.17`, y los convenios interadministrativo, interinstitucional y marco = `6.1-18.1`, `6.1-18.2`, `6.1-18.3`. Solo las series `6.1-18.x` son de convenio.
- **CDP**: certificado de disponibilidad presupuestal. **RP**: registro presupuestal.
- **PAA**: plan anual de adquisiciones.
- **Ordenador del gasto**: quien firma el contrato (casi siempre el Vicerrector de Investigaciones).
- **Supervisor / interventor**: quien hace seguimiento a la ejecución.
- **SECOP II**: plataforma pública donde se publica el expediente del contrato.
- **SIRECI**: sistema de rendición de cuentas de la Contraloría. Se reporta por trimestre. Los catálogos guardan código y texto ("3 CÉDULA DE CIUDADANÍA"); el código numérico es el que pide el SIRECI.
- **UNSPSC**: códigos de clasificación de bienes y servicios.
- **DV**: dígito de verificación del NIT, se calcula con el algoritmo de la DIAN.

## Roles y permisos

| Rol | Qué hace |
|---|---|
| ABOGADO | Crea el contrato, obtiene el número y llena los datos precontractuales y contractuales hasta el acta de inicio. Ve y edita sus contratos. |
| FINANCIERO | Registra pagos, fecha de certificación de pago, acta final o liquidación y datos de contacto del contratista. |
| ADMIN | Administra usuarios, maestros, catálogos, vigencias y consecutivos. |
| CONSULTA | Solo lectura. |

Adiciones y observaciones las pueden editar abogados y financiero. Los campos calculados no los edita nadie. La matriz campo por campo está en `INFORMACIÓN VRI.xlsx` y debe aplicarse en el backend, no solo ocultarse en el frontend.

## Autenticación y autorización

- El inicio de sesión es con Google (OAuth2 / OpenID Connect) usando Spring Security OAuth2. **El sistema no guarda contraseñas.**
- Solo se aceptan correos del dominio de la universidad (`@unicauca.edu.co`); se valida el claim `hd` y el correo del token, y cualquier otro dominio se rechaza.
- Google solo confirma la identidad. El sistema busca el `usuario` por `google_id` (o por correo en el primer ingreso, y entonces guarda el `google_id`). Si no existe o está inactivo, se niega el acceso: los usuarios los crea un ADMIN, no se auto-registran.
- El rol del `usuario` decide los permisos, y para ABOGADO el filtro de contratos es `contrato.responsable_id`.
- Las APIs REST son sin estado: el frontend envía el token en `Authorization: Bearer …` y el backend lo valida en cada petición.

## Ciclo de vida del contrato

Estados (guardarlos explícitamente; hoy se deducen de las fechas):

`EN_TRAMITE` → `SUSCRITO` → `EN_EJECUCION` → `TERMINADO` → `LIQUIDADO`, y `ANULADO` para un número asignado que no se usó.

- El abogado lo lleva desde el número hasta el acta de inicio; el financiero, desde los pagos hasta el cierre.
- Desde la suscripción, el contrato entra al reporte trimestral SIRECI y se reporta cada trimestre mientras esté vivo, incluso en años siguientes.

## Reglas de negocio

### Numeración
- Formato `serie/NNN`, por ejemplo `6.1-17.13/023`. Consecutivo de 3 dígitos con ceros a la izquierda (desde 1000 crece a 4 dígitos en lugar de fallar).
- Un consecutivo por **serie documental** y por año, no por tipo: los tipos que comparten serie (Aprendizaje, Pasantía y Judicatura) no repiten números entre sí.
- El número lo asigna `INumeracionServicio.generarNumero(tipoContratoId, anio)` en la misma transacción que crea el contrato: asegura la fila (serie, año) con `INSERT … ON DUPLICATE KEY UPDATE`, la bloquea con `SELECT … FOR UPDATE`, incrementa `ultimo_numero` y devuelve `serie/NNN`. Si la transacción del contrato falla, el número no se consume. El tipo y su serie deben estar activos. Probado con llamadas concurrentes en H2 y en MySQL.
- No usar una transacción aparte (`REQUIRES_NEW`) para crear la fila (cada petición necesitaría dos conexiones y el pool se agota con muchas peticiones simultáneas), ni `INSERT IGNORE` (toma un bloqueo compartido y produce deadlocks al pasar a `FOR UPDATE`), ni un `FOR UPDATE` sobre una fila que aún no existe (bloquea el hueco del índice en InnoDB).
- El número es único. Un número asignado y no usado queda en estado `ANULADO`: nunca se borra ni se reutiliza.
- Un contrato de un año anterior que sigue vivo se reporta en SIRECI con prefijo `AN25-`, `AN24-`… delante del número.

### Campos calculados (no se guardan, se calculan en vistas o en el dominio)
- Valor inicial = valor sin IVA + IVA.
- Valor final = valor inicial + adiciones en valor. Plazo total = plazo + días de adición.
- Pagos acumulados = suma de los pagos.
- Saldo = valor inicial + adiciones − pagos acumulados − anticipo.
- Valor diario = valor final ÷ plazo total. Días ejecutados = pagos acumulados ÷ valor diario.
- Avance físico = días ejecutados × 100 ÷ plazo total.
- Avance presupuestal = (pagos acumulados + anticipo) × 100 ÷ valor final.
- Dependencia de afectación: es el nombre del rubro presupuestal (`rubro_presupuestal.nombre`), no una dependencia del catálogo. Nombre, cédula y cargo del ordenador: del periodo de ordenador del gasto (`ordenador_gasto` y su tercero). Datos del contratista y supervisor: del maestro de terceros. DV: algoritmo DIAN. Edad: desde la fecha de nacimiento.

### Obligatoriedad condicional (bloquean el guardado)
- Recursos de contrato o convenio interadministrativo = SI exige NIT y DV de la entidad; NO los deja vacíos.
- Persona natural no puede tener tipo de documento NIT. NIT exige número y DV; cédula o RUT exige número de cédula; cédula de extranjería exige su número. Igual para supervisor e interventor.
- Tipo de seguimiento "Supervisor" exige supervisor; "Interventor" exige interventor; "Interventor y Supervisor" exige ambos.
- Clase "30 OTROS" exige describir la otra clase; cualquier otra clase la deja vacía. Se decide con `ClaseContratoEntity.esOtros()` (código 30 en el ámbito CONTRATO), no comparando textos.
- Anticipo "No pactados" exige valor 0; "Anticipo" o "Pago anticipado" exige valor.
- Adición en valor exige valor; en tiempo exige días; en valor y tiempo exige ambos.
- Fecha de terminación exige fecha de inicio; inicio exige suscripción; acta final exige terminación.
- Si el número de pagos es N, los pagos 1 a N son obligatorios.

### Límites
- Objeto y observaciones: máximo 390 caracteres.
- Saldo y valor inicial no negativos. Porcentajes entre 0 y 100.
- Fecha de suscripción dentro del año de la vigencia.
- Los campos de lista solo aceptan valores activos del catálogo.
- Avisar (sin bloquear) si un contratista tiene más de un contrato en el año: posible fraccionamiento.

## Entidades que gestiona el sistema (27 tablas, en MySQL)

Convenciones del modelo:
- Llaves primarias `id BIGINT AUTO_INCREMENT`, salvo donde se indica otra.
- Dinero en `DECIMAL(18,2)` (pesos colombianos), porcentajes en `DECIMAL(5,2)`, fechas en `DATE`, marcas de tiempo en `DATETIME`.
- Los enumerados se guardan como `VARCHAR` con los valores indicados (en JPA, `@Enumerated(EnumType.STRING)`).
- Los campos que apuntan a `catalogo_item` deben pertenecer al catálogo indicado entre paréntesis.
- Todas las tablas de negocio llevan además `creado_en`, `creado_por`, `actualizado_en` y `actualizado_por` (no se repiten abajo).
- Nada calculado se guarda: valor inicial, valor final, plazo total, pagos acumulados, saldo, días ejecutados y avances salen de la vista `v_contrato_resumen` (fórmulas en "Campos calculados").

### 1. Maestros y catálogos

**`vigencia`**: año de trabajo.

| Campo | Tipo | Llave / regla |
|---|---|---|
| anio | SMALLINT | PK |
| abierta | BOOLEAN | Solo en vigencias abiertas se crean contratos |

**`serie_documental`**: serie de la TRD; es el prefijo del número del contrato y la unidad del consecutivo.

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| codigo | VARCHAR(20) | Único, formato TRD, ej. `6.1-17.13`. No se puede cambiar si ya hay contratos con la serie |
| es_convenio | BOOLEAN | Lo calcula el sistema: verdadero si el código empieza por `6.1-18.`. No se recibe ni se edita |
| activo | BOOLEAN | Inactiva: sus tipos no se ofrecen ni generan números |

**`tipo_contrato`**: tipo de contrato (OPS, Suministro, Pasantía…).

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| nombre | VARCHAR(80) | Único (sin distinguir mayúsculas). Con tildes y bien escrito, se muestra al usuario y en reportes. No se puede cambiar si ya hay contratos del tipo |
| serie_id | BIGINT | FK `serie_documental`. Varios tipos pueden compartir serie. No se puede cambiar si ya hay contratos del tipo; al asignarla o cambiarla debe estar activa |
| activo | BOOLEAN | Inactivo: no se ofrece para contratos nuevos ni genera números |

**`consecutivo`**: último número emitido por serie y año.

| Campo | Tipo | Llave / regla |
|---|---|---|
| serie_id | BIGINT | PK compuesta, FK `serie_documental` |
| anio | SMALLINT | PK compuesta. Hoy se valida entre 2000 y 2100; cuando exista `vigencia`, pasa a ser FK a ella |
| ultimo_numero | INT | Se incrementa con bloqueo de fila (`SELECT … FOR UPDATE`); ver "Numeración" |

**Uso por contratos (provisional).** "Ya hay contratos que usan la serie o el tipo" lo responde la interfaz `UsoTipoContratoChecker`. Mientras no exista la tabla `contrato`, su implementación `UsoTipoContratoCheckerProvisional` considera usada una serie si ya emitió algún número en cualquier año (consecutivo > 0, incluye anulados) y responde que ningún tipo tiene contratos. **Al crear el dominio de contratos hay que reemplazarla** por una que consulte `contrato`.

**Carga inicial.** `src/main/resources/data.sql` carga las 15 series, los 17 tipos y el consecutivo 2026 de cada serie con el último número usado según la hoja CONTROL (fila 3, CANTIDAD): OPS 108, Suministro 15, `6.1-17.3` 1, Consultoría 1, Compraventa 3 y el resto 0. Corre en cada arranque (`spring.sql.init.mode=always`, `spring.jpa.defer-datasource-initialization=true`) y es idempotente: `INSERT IGNORE` con ids fijos, así nunca sobrescribe un consecutivo que avanzó ni recrea un tipo renombrado o reactiva uno inactivado. **Antes de producción** pasar a Flyway y dejar `ddl-auto=validate`.

**`clase_contrato`**: clases oficiales SIRECI de contratos y de convenios (dominio `clasecontrato`).

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| codigo_sireci | INT | Código oficial de la Contraloría, positivo. No se puede cambiar después de crear |
| nombre | VARCHAR(120) | Texto oficial con tildes (mayúsculas como en el SIRECI), tal como se escribe |
| ambito | VARCHAR(10) | CONTRATO o CONVENIO. No se puede cambiar después de crear |
| activo | BOOLEAN | Inactiva: no se ofrece para contratos nuevos; los que ya la tienen la conservan |

- Único por (ambito, codigo_sireci) (`uk_clase_contrato_ambito_codigo`): las clases de convenio reinician su numeración en 1.
- `ClaseContratoEntity.esOtros()`: verdadero solo para el código 30 en el ámbito CONTRATO ("30 OTROS"). La API lo devuelve como `esOtros`, junto con `codigoSireci`, `nombre` y `etiqueta` ("23 PRESTACIÓN DE SERVICIOS").
- El código **99999998** ("no se diligencia información para este formulario en este período") **no es una clase**: pertenece al reporte SIRECI. Crearlo responde 400 y no está en la carga inicial.
- Carga inicial (`data.sql`, ids 1 a 32): las 30 clases de contrato de la hoja CONTROL (columna A, filas 290 a 319) y las 2 de convenio (filas 322 y 323: "1 CONTRATO / CONVENIO INTERADMINISTRATIVO" y "2 CONVENIO DE COOPERACIÓN (NACIONAL / INTERNACIONAL)"), con número y texto separados. Única corrección al texto del Excel: "COMISION" → "COMISIÓN".
- El dominio de contratos valida la clase con `IClaseContratoServicio.obtenerClaseValida(id, actual)` (debe estar activa si cambia).

**`catalogo_item`**: listas cortas en una sola tabla.

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| catalogo | VARCHAR(40) | TIPO_IDENTIFICACION, NATURALEZA, TIPO_SEGUIMIENTO, TIPO_ANTICIPO, TIPO_ADICION, FUENTE_FINANCIACION, ASEGURADORA |
| codigo_sireci | VARCHAR(10) | Único por catálogo |
| nombre | VARCHAR(150) | Se muestra como "código nombre" |
| activo | BOOLEAN | |

Carga inicial (`data.sql`): los tipos de identificación del SIRECI (CONTROL, lista TIPO DE DOCUMENTO): 1 NIT, 2 RUT - REGISTRO ÚNICO TRIBUTARIO, 3 CÉDULA DE CIUDADANÍA, 4 CÉDULA DE EXTRANJERÍA, sin ids fijos (clave natural catálogo + código). El 5 ("no se diligencia información...") no se carga: pertenece al reporte SIRECI.

**`tercero`**: contratistas, supervisores, interventores, ordenadores, directores de proyecto y entidades financiadoras. Herencia JPA `JOINED`: los datos comunes en `tercero` y los propios en `persona_natural` o `persona_juridica` (PK = FK `tercero_id`).

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| naturaleza | VARCHAR(10) | NATURAL o JURIDICA. Columna discriminadora: no cambia después de crear el tercero |
| tipo_identificacion_id | BIGINT | FK `catalogo_item` (TIPO_IDENTIFICACION) |
| numero_documento | VARCHAR(20) | Único junto con el tipo (`uk_tercero_documento`). Si es NIT: solo dígitos, sin DV |
| dv | CHAR(1) | Calculado con el algoritmo DIAN al guardar, solo para NIT; la API no lo recibe |
| nombre_completo | VARCHAR(200) | Lo arma el sistema: nombres + apellidos o razón social. Para buscar y mostrar |
| correo | VARCHAR(120) | |
| direccion | VARCHAR(200) | |
| telefono | VARCHAR(20) | |
| celular | VARCHAR(20) | |
| activo | BOOLEAN | |

El NIT se reconoce por el código SIRECI del tipo de identificación, configurado en `vri.tipo-identificacion.codigo-nit` (`1`, confirmado por la lista "TIPO DE DOCUMENTO" de la hoja CONTROL: "1 NIT"). Persona natural no admite NIT.

**`persona_natural`**: nombres VARCHAR(100) y apellidos VARCHAR(100) obligatorios; fecha_nacimiento DATE (pasada; la edad se calcula), ciudad_nacimiento VARCHAR(80), fecha_expedicion DATE (no anterior al nacimiento), lugar_expedicion VARCHAR(80), profesion VARCHAR(120), tarjeta_profesional VARCHAR(40), entidad_tarjeta VARCHAR(120), correo_institucional VARCHAR(120).

**`persona_juridica`**: razon_social VARCHAR(200) obligatoria; representante_legal_id BIGINT opcional, FK a `persona_natural` (la base impide que sea jurídica). Al asignarlo o cambiarlo debe existir y estar activo.

El catálogo `NATURALEZA` de `catalogo_item` ya no se usa para terceros.

**`ordenador_gasto`**: periodo en que una persona natural firma contratos como ordenador del gasto (o su delegado) con un cargo (dominio `ordenador`). No repite datos personales: nombre y cédula vienen del tercero.

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| tercero_id | BIGINT | FK a `persona_natural` (la base impide que sea jurídica). Debe existir y estar activo al asignarlo o cambiarlo |
| cargo | VARCHAR(120) | Obligatorio, tal como se escribe (ej. "Vicerrector de Investigaciones", "Rector") |
| es_delegado | BOOLEAN | Verdadero si firma por delegación |
| acto_delegacion | VARCHAR(200) | Número y fecha de la resolución. Obligatorio si es_delegado; vacío si no |
| fecha_desde | DATE | Obligatoria |
| fecha_hasta | DATE | Opcional (nula = periodo abierto). Si viene, ≥ fecha_desde |
| activo | BOOLEAN | |

- Único por (tercero_id, cargo, fecha_desde) (`uk_ordenador_gasto_periodo`), que además hace idempotente la carga inicial.
- Un mismo tercero no puede tener dos periodos **activos** con el mismo cargo que se crucen (409); el cargo se compara sin mayúsculas ni tildes. Un periodo inactivo no bloquea, y reactivarlo vuelve a validar el cruce.
- No hay borrado: se inactiva o se cierra el periodo (`PATCH /{id}/cerrar`). Si ya hay contratos (`UsoOrdenadorChecker`; su implementación provisional responde "sin contratos" hasta que exista la tabla contrato), el tercero, el cargo y la fecha desde no se pueden cambiar; cerrar sí.
- El contrato guardará `ordenador_gasto_id` (el periodo, no la persona) y lo validará con `IOrdenadorGastoServicio.obtenerOrdenadorValido(id, actual, fechaSuscripcion)`: activo si cambia, y la fecha de suscripción siempre dentro del periodo. Así se corrige un error del Excel: allí el cargo se busca con `BUSCARV` por el nombre y, si una persona tiene dos cargos, devuelve siempre el primero.
- La API devuelve `nombre`, `cedula` y `tipoIdentificacion` del tercero junto con el `cargo`, listos para el SIRECI ("nombre, cédula y cargo del ordenador del gasto o su delegado").
- Carga inicial (`data.sql`): valores distintos de la hoja CONSOLIDADO ANUAL (columnas N, O y P; 127 contratos, 2 combinaciones) y de la lista ORDENADOR DEL GASTO de CONTROL (A428:C436): 8 personas y 9 combinaciones de nombre, cédula y cargo. Las 8 personas se crean como persona natural con cédula si no existe ya un tercero con esa cédula (clave natural, sin ids fijos); nombres tal como vienen (mayúsculas, sin tildes), separados tomando las dos últimas palabras como apellidos. Solo las 2 combinaciones con contratos en 2026 tienen periodo: Francisco José Pino Correa, Vicerrector de Investigaciones, y Deibar René Hurtado Herrera, Rector, desde 2026-01-01, abiertos, no delegados, **por confirmar**. Las otras 7 combinaciones quedan sin periodo hasta que la VRI entregue sus fechas. Cargo corregido: "VICERECTOR" (sic) → "Vicerrector".

**`proyecto`**

| Campo | Tipo | Llave / regla |
|---|---|---|
| id_proyecto | INT | PK (código institucional que digita el usuario, ej. 5019). No autoincremental, positivo, no cambia después de crear |
| nombre | VARCHAR(400) | Obligatorio |
| director_tercero_id | BIGINT | Obligatorio. FK `persona_natural`: debe existir, ser persona natural y estar activo al asignarlo o cambiarlo |
| bpin | VARCHAR(20) | Opcional, solo dígitos (se quitan los espacios). No es único |
| activo | BOOLEAN | `true` al crear. Inactivo: no se ofrece para contratos nuevos; los existentes lo conservan |

`ProyectoEntity` implementa `Persistable`: con la llave asignada a mano, `save()` haría merge y crear un código repetido sobrescribiría el proyecto existente. Así siempre inserta, y el servicio responde 409 si el código ya existe.

**`rubro_presupuestal`**: código de afectación presupuestal (dominio `rubro`).

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| codigo | VARCHAR(40) | Único (`uk_rubro_presupuestal_codigo`). Texto, no número: conserva ceros a la izquierda, puntos y guiones; números y letras separados por `.` o `-`, sin espacios. No se puede cambiar si ya hay contratos que lo usan |
| nombre | VARCHAR(300) | Obligatorio. Es la **dependencia de afectación** del contrato. Puede repetirse entre rubros |
| activo | BOOLEAN | Inactivo: no se ofrece para contratos nuevos; los existentes lo conservan |

- **El nombre del rubro es la "dependencia de afectación".** En el libro de Excel, la columna "DEPENDENCIA AFECTACIÓN PRESUPUESTAL" de cada contrato es `BUSCARV(código; CONTROL!A102:B283; 2)`, es decir, el texto del rubro (rango con nombre `Control_Dependencia_Afectacion`). No es una fila de la tabla `dependencia` (esas están en `Control_Dependencias`, CONTROL!C19:C100), por eso el rubro no tiene `dependencia_id`. Tampoco se guarda `codigo_ordenado`: la columna C del Excel es solo la lista ordenada, y el orden lo da la API.
- Orden de los listados: por longitud del código y luego por valor (orden numérico para códigos de solo dígitos), igual que la lista ordenada del Excel: 1031 antes que 1027008.
- "Ya hay contratos que lo usan" lo responde `UsoRubroChecker`; su implementación `UsoRubroCheckerProvisional` responde "sin contratos" hasta que exista la tabla contrato (**reemplazarla** entonces). El dominio de contratos valida el rubro con `IRubroServicio.obtenerRubroValido(id, actual)`.
- Carga inicial (`data.sql`, ids 1 a 51): los 51 rubros de la hoja CONTROL (filas 102 a 152). El rango con nombre `Control_AfectacionPresupuestal_Depen` (A102:B283) reserva 182 filas, pero las 131 restantes están vacías y no se cargan. Los códigos venían guardados como números en el Excel (`1052002.0`, `9.011044001E9`) y se cargan como texto exacto; ninguno trae puntos, guiones ni ceros a la izquierda. Limpieza: espacios sobrantes y comillas envolventes o sueltas al final (filas 109, 111 y 115); las tildes faltantes en nombres de proyectos no se corrigen porque son nombres oficiales del sistema financiero.

**`dependencia`**

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| nombre | VARCHAR(200) | Único (sin distinguir mayúsculas ni tildes, por la collation). Se guarda tal como se escribe, con tildes: ya no se pasa a mayúsculas |
| activo | BOOLEAN | |

- Carga inicial (`data.sql`, ids 1 a 55): la lista de 55 dependencias entregada por la VRI (facultades y posgrados, rectoría y vicerrectorías, divisiones, oficinas, áreas, centros y otras unidades), con los nombres tal como vienen en esa lista. Las divisiones, oficinas y áreas van sin anteponer el tipo ("de Admisiones", "Jurídica", "de Desarrollo Editorial"). El id 1 es "Vicerrectoría de Investigaciones".
- No se usó la lista `Control_Dependencias` del Excel (CONTROL!C19:C100): mezcla códigos numéricos y nombres.

### 2. Usuarios y abogados

**`usuario`**: cuenta de acceso (Google, sin contraseña).

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| google_id | VARCHAR(64) | Único, se llena en el primer ingreso |
| correo | VARCHAR(120) | Único, solo `@unicauca.edu.co` |
| nombre | VARCHAR(150) | |
| rol | VARCHAR(20) | ABOGADO, FINANCIERO, ADMIN, CONSULTA |
| activo | BOOLEAN | |

**`abogado`**: datos profesionales del abogado.

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| usuario_id | BIGINT | FK única a `usuario` |
| tipo_identificacion_id | BIGINT | FK `catalogo_item` (TIPO_IDENTIFICACION) |
| numero_documento | VARCHAR(20) | Único |
| nombres | VARCHAR(100) | |
| apellidos | VARCHAR(100) | |
| correo_institucional | VARCHAR(120) | |
| telefono | VARCHAR(20) | |
| tarjeta_profesional | VARCHAR(40) | |
| cargo | VARCHAR(120) | |
| tipo_vinculacion | VARCHAR(20) | PLANTA, CONTRATISTA |
| dependencia_id | BIGINT | FK `dependencia` |
| fecha_ingreso | DATE | |
| activo | BOOLEAN | |

### 3. Contrato

**`contrato`**: centro del modelo.

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| numero | VARCHAR(30) | Único, `serie_trd/NNN`, lo genera el sistema |
| tipo_contrato_id | BIGINT | FK `tipo_contrato` |
| anio | SMALLINT | FK `vigencia` |
| consecutivo | INT | Único junto con la serie del tipo y el año; lo asigna `generarNumero` |
| estado | VARCHAR(20) | EN_TRAMITE, SUSCRITO, EN_EJECUCION, TERMINADO, LIQUIDADO, ANULADO |
| responsable_id | BIGINT | FK `abogado` |
| fecha_registro | DATETIME | |
| clase_contrato_id | BIGINT | FK `clase_contrato` |
| otra_clase | VARCHAR(150) | Obligatoria solo si la clase es 30 OTROS (`esOtros()`); vacía en cualquier otra |
| objeto | VARCHAR(390) | Obligatorio |
| contratista_id | BIGINT | FK `tercero` |
| ordenador_gasto_id | BIGINT | FK `ordenador_gasto` (el periodo). La fecha de suscripción debe caer dentro del periodo |
| tipo_seguimiento_id | BIGINT | FK `catalogo_item` (TIPO_SEGUIMIENTO) |
| valor_sin_iva | DECIMAL(18,2) | ≥ 0 |
| iva | DECIMAL(18,2) | ≥ 0 |
| plazo_dias | INT | > 0 |
| fecha_suscripcion | DATE | Dentro del año de la vigencia |
| fecha_inicio | DATE | Exige fecha de suscripción |
| fecha_terminacion | DATE | Exige fecha de inicio |
| fecha_acta_final | DATE | Exige fecha de terminación |

**`contrato_presupuesto`**: 1:1 con contrato.

| Campo | Tipo | Llave / regla |
|---|---|---|
| contrato_id | BIGINT | PK y FK `contrato` |
| rubro_id | BIGINT | FK `rubro_presupuestal` (el código y la dependencia de afectación salen del rubro) |
| proyecto_id | INT | FK `proyecto` |
| paa | VARCHAR(40) | Código del plan anual de adquisiciones |
| fuente_financiacion_id | BIGINT | FK `catalogo_item` (FUENTE_FINANCIACION) |
| recursos_de_convenio | BOOLEAN | Si es verdadero exige entidad financiadora |
| entidad_financiadora_id | BIGINT | FK `tercero` (con NIT y DV) |
| dependencia_solicitante_id | BIGINT | FK `dependencia` |
| naturaleza_gasto | CHAR(3) | I o SPI |
| tipo_anticipo_id | BIGINT | FK `catalogo_item` (TIPO_ANTICIPO) |
| porcentaje_anticipo | DECIMAL(5,2) | 0 a 100 |
| valor_anticipo | DECIMAL(18,2) | 0 si "No pactados"; obligatorio en otro caso |
| fecha_certificacion_pago | DATE | La llena el FINANCIERO |

**`contrato_sireci`**: 1:1 con contrato, campos que solo pide el SIRECI.

| Campo | Tipo | Llave / regla |
|---|---|---|
| contrato_id | BIGINT | PK y FK `contrato` |
| regimen | VARCHAR(40) | Valores por confirmar |
| multiples_contratistas | BOOLEAN | |
| genera_derivada | BOOLEAN | |
| numero_derivado | VARCHAR(30) | Obligatorio si genera derivada |
| contratista_derivado_id | BIGINT | FK `tercero` |
| requiere_poliza | BOOLEAN | |
| vigencia_poliza | DATE | |
| tipo_gasto | VARCHAR(40) | |
| origen_presupuesto | VARCHAR(40) | |
| origen_recursos | VARCHAR(40) | |
| valor_recursos_propios | DECIMAL(18,2) | |
| valor_otros_recursos | DECIMAL(18,2) | |
| moneda | CHAR(3) | Por defecto COP |
| observaciones | VARCHAR(390) | |
| aclaraciones | VARCHAR(390) | |

### 4. Detalle del contrato (1 contrato : N filas)

Todas tienen `id BIGINT` PK y `contrato_id BIGINT` FK `contrato`.

| Tabla | Campos propios | Reglas |
|---|---|---|
| `contrato_cdp` | numero_cdp VARCHAR(40), valor DECIMAL(18,2), fecha DATE | Número único por contrato |
| `contrato_rp` | numero_rp VARCHAR(40), valor DECIMAL(18,2), fecha DATE | Número único por contrato |
| `contrato_unspsc` | codigo_unspsc VARCHAR(10) | Código único por contrato |
| `contrato_seguimiento` | rol VARCHAR(15) (SUPERVISOR, INTERVENTOR), tercero_id BIGINT FK `tercero`, desde DATE, hasta DATE | Debe cuadrar con el tipo de seguimiento; `hasta` nulo = vigente |
| `modificacion` | tipo_adicion_id BIGINT FK `catalogo_item` (TIPO_ADICION), valor DECIMAL(18,2), dias INT, fecha DATE, documento VARCHAR(200), observacion VARCHAR(390) | En valor exige valor; en tiempo exige días; ambos exige los dos |
| `pago` | numero SMALLINT, valor DECIMAL(18,2), fecha DATE, registrado_por BIGINT FK `usuario` | Número único por contrato, consecutivo desde 1; sin límite |
| `poliza` | numero VARCHAR(40), aseguradora_id BIGINT FK `catalogo_item` (ASEGURADORA), vigencia_hasta DATE | |

### 5. Expediente, reportes y auditoría

**`documento_expediente`**: lista de chequeo para SECOP.

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| contrato_id | BIGINT | FK `contrato` |
| tipo | VARCHAR(20) | FOR38_45, CDP, MATRIZ_RIESGO, PROPUESTA, CONTRATO, RDP, GARANTIA, AG, DS, AI, AL. Único junto con contrato |
| entregado | BOOLEAN | |
| escaneado | BOOLEAN | |
| fecha | DATE | |
| archivo_url | VARCHAR(500) | |

**`publicacion_secop`**: 1:1 con contrato.

| Campo | Tipo | Llave / regla |
|---|---|---|
| contrato_id | BIGINT | PK y FK `contrato` |
| proceso_creado | BOOLEAN | |
| fecha_creacion | DATE | |
| publicado | BOOLEAN | |
| fecha_publicacion | DATE | |
| url | VARCHAR(500) | Enlace al proceso en SECOP II |

**`reporte_sireci`**

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| anio | SMALLINT | Único junto con trimestre |
| trimestre | TINYINT | 1 a 4 |
| fecha_corte | DATE | |
| estado | VARCHAR(15) | BORRADOR, GENERADO, ENVIADO |

**`reporte_sireci_contrato`**

| Campo | Tipo | Llave / regla |
|---|---|---|
| reporte_id | BIGINT | PK compuesta, FK `reporte_sireci` |
| contrato_id | BIGINT | PK compuesta, FK `contrato` |
| numero_reportado | VARCHAR(40) | Con prefijo `AN25-`… si el contrato es de otro año |
| veces_reportado | INT | |

**`auditoria`**

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| tabla | VARCHAR(60) | |
| registro_id | VARCHAR(40) | |
| accion | VARCHAR(10) | CREAR, EDITAR, ANULAR, ELIMINAR |
| usuario_id | BIGINT | FK `usuario` |
| fecha | DATETIME | |
| cambios | JSON | Valores antes y después |

El consolidado anual, la plantilla SECOP y el archivo SIRECI se generan con consultas sobre este modelo.

## Migración y calidad de datos

La carga inicial desde el Excel debe limpiar: CDP, RP y UNSPSC con varios valores y separadores en una celda; textos inconsistentes (aseguradoras, "VICERECTOR"/"VICERRECTOR"); fechas inválidas en CONTRATISTAS; fórmulas `#REF!` en FIN I–IV TRIM; datos de TRIMESTRES y CONTRATOS 2025 que vienen de hojas externas no disponibles; y la hoja CONVENIOS, que hoy no entra al consolidado.

## Preguntas abiertas con el equipo VRI

- Significado de AG, DS, AI, AL (se supone aprobación de garantía, designación de supervisor, acta de inicio, acta de liquidación) y de FOR 38 / FOR 45.
- Valores válidos de RÉGIMEN y ESTADO del SIRECI.
- Si los convenios usan el mismo sistema y flujo.
- Si el sistema asigna el número automáticamente.
- Cómo distinguir avance programado del real (hoy usan la misma fórmula).
- Confirmar que el SIRECI es trimestral (el diccionario dice mensual).
- Nombre completo de los rubros 9011141 y 9011044001: en el Excel ambos dicen "Servicios prestados a Empresas y", que parece truncado.
- Dependencias que podrían ser la misma (se cargaron todas activas): "Facultad de Ciencias Agrarias" y "Facultad de Ciencias Agropecuarias"; "de Admisiones" y "Admisiones y Registro"; "de Salud Ocupacional" y "de Seguridad y Salud en el Trabajo"; "de Gestión del Talento Humano" y "Recursos Humanos - Pensiones". Si alguno es un nombre anterior, conviene inactivarlo (sirve para migrar contratos viejos sin ofrecerse en selectores).
- Ordenadores del gasto: fechas reales de cada periodo (el Excel no las trae; los 2 de 2026 se cargaron desde 2026-01-01 por confirmar, y los otros 7 sin periodo); si el Vicerrector de Investigaciones firma por delegación del Rector y con qué resolución; de qué vicerrectoría eran encargados los dos "Vicerrector (E)" (Alejandro Toledo Tovar y Juan Carlos Casas Zapata); y quién firmó el contrato 6.1-17.13/093, que en el consolidado no tiene ordenador.

## Estructura del código

Paquete raíz `co.edu.unicauca.contratacionvri`. Dominios actuales: `abogado`, `usuario`, `catalogo` (tabla `catalogo_item`), `dependencia`, `tercero`, `proyecto`, `tipocontrato` (series documentales, tipos de contrato y consecutivos), `clasecontrato` (clases SIRECI), `rubro` (rubros presupuestales) y `ordenador` (ordenadores del gasto). Cada dominio tiene tres capas:

```
<dominio>/
  controladores/            @RestController en /api/<dominio-plural>
  fachadaServicios/
    DTOPeticion/            lo que envía el cliente, con validaciones jakarta
    DTORespuesta/           lo que devuelve la API
    servicios/              interfaz I<Dominio>Servicio + <Dominio>ServicioImpl
  accesoADatos/
    modelos/                entidades JPA (<Dominio>Entity) y enumerados
    repositorios/           interfaces JpaRepository
```

- Mapeo entidad ↔ DTO con ModelMapper (`configuracion/ConfiguracionModelMapper`, coincidencia estricta).
- Errores: `excepciones/ControladorExcepciones` devuelve siempre `ErrorRespuesta` (codigoError, mensaje, codigoHttp, url, metodo, fecha, errores por campo). Lanzar `EntidadNoExisteException` (404), `EntidadYaExisteException` (409) o `ReglaNegocioException` (400) desde los servicios.
- Las entidades de negocio extienden `comun/accesoADatos/EntidadAuditable`, que trae `creado_en/por` y `actualizado_en/por` llenados por JPA Auditing (`configuracion/ConfiguracionAuditoria`); `*_por` queda nulo hasta que exista el login.
- Relaciones entre dominios: el DTOPeticion recibe el id (`usuarioId`, `dependenciaId`…); el servicio la valida con el método `obtener…Valido` del servicio del otro dominio (existe, está activa, pertenece al catálogo correcto) y la asigna. ModelMapper no copia relaciones: en el TypeMap de la petición se hacen `skip` de los setters de relación. Al actualizar, solo se exige que esté activo un valor que cambia.
- Textos: Jackson normaliza todo texto al leer el JSON, antes de `@Valid` (`configuracion/ConfiguracionJackson`): recorta, colapsa espacios horizontales repetidos y convierte `""` en `null`. Cada servicio termina la limpieza con `comun/utilidades/Textos` (correos en minúsculas; documentos y nombres de `catalogo_item` en mayúsculas). Los nombres que se muestran al usuario y en reportes (dependencias, tipos de contrato, clases SIRECI, rubros, terceros, proyectos) se guardan tal como se escriben, con tildes; la collation evita duplicados por mayúsculas o tildes.
- Búsquedas paginadas: devuelven `comun/fachadaServicios/PaginaRespuesta` (contenido, pagina, tamanio, totalElementos, totalPaginas, primera, ultima), con parámetros `pagina` (desde 0) y `tamanio` (1 a 100) y orden fijo definido por el servicio. Los comodines de LIKE del usuario se escapan con `!`.
- Proyectos: `POST /api/proyectos` (con `idProyecto`), `GET/PUT /api/proyectos/{idProyecto}` (un `idProyecto` distinto en el cuerpo da 400), `PATCH /{idProyecto}/estado`, `GET /api/proyectos?id=&nombre=&director=&activo=&pagina=&tamanio=` y `GET /api/proyectos/activos?texto=` para selectores. Los demás dominios validan un proyecto con `IProyectoServicio.obtenerProyectoValido(id, exigirActivo)`.
- Personas naturales asignadas (representante legal, director, y luego supervisor o contratista): validar con `ITerceroServicio.obtenerPersonaNaturalValida(id, actual, rol)`.
- Terceros: `POST/PUT /api/terceros/naturales[/{id}]` y `/api/terceros/juridicas[/{id}]` con DTOs distintos; `GET /api/terceros/{id}` devuelve la forma según la naturaleza; `GET /api/terceros?documento=&nombre=&naturaleza=&activo=&pagina=&tamanio=`.
- Tipos de contrato (dominio `tipocontrato`): `GET/POST /api/series-documentales`, `GET/PUT /api/series-documentales/{id}`, `PATCH /{id}/estado`, `GET /api/series-documentales/{id}/consecutivos/{anio}` (último número y siguiente; 0 si no hay números, no 404); `GET/POST /api/tipos-contrato?activo=&serieId=`, `GET/PUT /api/tipos-contrato/{id}`, `PATCH /{id}/estado` y `GET /api/tipos-contrato/activos` para selectores (tipo activo con serie activa). `generarNumero` no tiene endpoint: lo usa el dominio de contratos.
- Clases de contrato SIRECI: `GET /api/clases-contrato?ambito=&activo=` (con `activo=true` para selectores; orden por ámbito y código), `GET/PUT /api/clases-contrato/{id}` (PUT solo cambia el nombre; un código o ámbito distinto en el cuerpo da 400), `POST /api/clases-contrato` y `PATCH /{id}/estado`.
- Rubros presupuestales: `GET /api/rubros?texto=&activo=&pagina=&tamanio=` (`texto` = prefijo del código o contenido del nombre; con `activo=true` para selectores; orden numérico por código), `GET/PUT /api/rubros/{id}` (el código no cambia si hay contratos: 400; si choca con otro: 409), `POST /api/rubros` y `PATCH /{id}/estado`. Respuesta con `codigo`, `nombre`, `etiqueta` ("1052002 - Museo de Historia Natural") y `codigoEditable`.
- Ordenadores del gasto: `GET /api/ordenadores-gasto?vigenteEn=AAAA-MM-DD&texto=&activo=` (`vigenteEn` para los selectores del contrato: periodos activos que cubren esa fecha; `texto` = nombre, cédula o cargo), `GET/PUT /api/ordenadores-gasto/{id}`, `POST /api/ordenadores-gasto`, `PATCH /{id}/cerrar` con `{"fechaHasta": ...}` y `PATCH /{id}/estado`.
- Errores HTTP: 400 (validación o regla de negocio), 404 (no existe), 409 (duplicado), 415 (cuerpo que no es `application/json`).
- No hay borrado físico de maestros: se usa `PATCH /{id}/estado?activo=`.
- MySQL local: `docker compose up -d` (MySQL 8.4 en el puerto 3306, base `contratacion_vri`). Las pruebas usan H2 en memoria y no necesitan Docker.
- `src/test/resources/application.properties` reemplaza por completo al de `src/main`: toda propiedad que las pruebas necesiten (p. ej. `spring.jpa.defer-datasource-initialization`) hay que repetirla ahí. Las pruebas no cargan `data.sql` (`spring.sql.init.mode=never`); `CargaInicialTest` lo prueba en su propia base H2.
- Llaves asignadas a mano (`proyecto`, `consecutivo`): la entidad implementa `Persistable` para que crear haga INSERT y no merge (un merge sobrescribiría el registro existente).
- Lo que dependa de bloqueos o concurrencia se prueba también contra MySQL real: H2 no reproduce los bloqueos de InnoDB. Se puede correr una prueba apuntando a una base temporal con `-Dspring.datasource.url=jdbc:mysql://…/contratacion_vri_prueba` (y usuario/clave/driver de MySQL).

## Convenciones

- Código, comentarios y mensajes de error en español, con los nombres de tablas y campos de este documento.
- Las reglas de negocio viven en el backend; el frontend puede repetirlas para la experiencia de usuario, pero el backend es la fuente de verdad.
