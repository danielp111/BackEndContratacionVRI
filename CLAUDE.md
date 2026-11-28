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
| ADMIN | Administra usuarios, maestros, catálogos y consecutivos. |
| CONSULTA | Solo lectura. |

Adiciones y observaciones las pueden editar abogados y financiero. Los campos calculados no los edita nadie. La matriz campo por campo está en `INFORMACIÓN VRI.xlsx` y debe aplicarse en el backend, no solo ocultarse en el frontend.

## Autenticación y autorización

- El inicio de sesión es con Google (OAuth2 / OpenID Connect) usando Spring Security OAuth2. **El sistema no guarda contraseñas.**
- Solo se aceptan correos del dominio de la universidad (`@unicauca.edu.co`); se valida el claim `hd` y el correo del token, y cualquier otro dominio se rechaza.
- Google solo confirma la identidad. El sistema busca el `usuario` por `google_id` (o por correo en el primer ingreso, y entonces guarda el `google_id`). Si no existe o está inactivo, se niega el acceso: los usuarios los crea un ADMIN, no se auto-registran.
- El rol del `usuario` decide los permisos, y para ABOGADO el filtro de contratos es `contrato.responsable_id`.
- Las APIs REST son sin estado: el frontend envía el token en `Authorization: Bearer …` y el backend lo valida en cada petición.

## Ciclo de vida del contrato

Estados: `EN_TRAMITE` → `SUSCRITO` → `EN_EJECUCION` → `TERMINADO` → `LIQUIDADO`, y `ANULADO` para un número asignado que no se usó.

- El estado se guarda en `contrato.estado`, pero nadie lo escribe: el sistema lo recalcula al guardar a partir de las fechas (`ContratoEntity.recalcularEstado()`). Sin suscripción = EN_TRAMITE; con suscripción = SUSCRITO; con inicio = EN_EJECUCION; con terminación = TERMINADO; con acta final = LIQUIDADO.
- ANULADO es la única acción explícita (`PATCH /api/contratos/{id}/anular` con motivo): solo desde EN_TRAMITE, irreversible, el contrato queda de solo lectura (no admite cambios ni observaciones nuevas) y su número no se reutiliza.
- Mientras está EN_TRAMITE el contrato se guarda incompleto. Registrar la fecha de suscripción exige: clase, contratista, ordenador, tipo de seguimiento, valor sin IVA, IVA, plazo, rubro, fuente de financiación, tipo de anticipo y dependencia solicitante (el 400 dice cuáles faltan).
- El abogado lo lleva desde el número hasta el acta de inicio (`PUT /api/contratos/{id}`); el financiero registra el cierre (`PATCH /{id}/cierre`: terminación, acta final y certificación de pago). La fecha de terminación la pueden registrar los dos.
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

Se calculan en el código (`CalculadoraResumen`, sin base de datos, probada con fechas fijas) a partir de los totales de modificaciones y pagos. La vista SQL `v_contrato_resumen` para los reportes se creará en la fase 4 con las mismas fórmulas. Porcentajes, valor diario y días ejecutados con 2 decimales.

- Valor inicial = valor sin IVA + IVA.
- Valor final = valor inicial + adiciones en valor. Plazo total = plazo + días de adición.
- Pagos acumulados = suma de los pagos.
- Saldo = valor inicial + adiciones − pagos acumulados − anticipo (= valor final − pagos − anticipo). Nunca negativo: un pago, una adición reducida o borrada o un valor del contrato más bajo que lo dejen negativo dan 400.
- Valor diario = valor final ÷ plazo total. Días ejecutados = pagos acumulados ÷ valor diario.
- Avance físico real = días ejecutados × 100 ÷ plazo total.
- Avance presupuestal real = (pagos acumulados + anticipo) × 100 ÷ valor final.
- Avance físico programado = días transcurridos desde el inicio, contando el de inicio y hasta hoy, × 100 ÷ plazo total, con tope de 100; 0 sin fecha de inicio y 100 con fecha de terminación. El avance presupuestal programado es el mismo porcentaje (el Excel usaba la fórmula del real para los dos).
- Tipo de adición (VALOR, TIEMPO, VALOR_TIEMPO o SIN_ADICION) = según las sumas de las modificaciones; "no se ha adicionado" no se registra como fila.
- Fecha de terminación estimada = inicio + plazo total − 1, mientras no haya fecha de terminación. Semestre en que finaliza ("2026-2") = de la fecha de terminación o, si no hay, de la estimada.
- Dependencia de afectación: es el nombre del rubro presupuestal (`rubro_presupuestal.nombre`), no una dependencia del catálogo. Nombre, cédula y cargo del ordenador: del periodo de ordenador del gasto (`ordenador_gasto` y su tercero). Datos del contratista y supervisor: del maestro de terceros. DV: algoritmo DIAN. Edad: desde la fecha de nacimiento.

### Obligatoriedad condicional (bloquean el guardado)
- Recursos de contrato o convenio interadministrativo = SI exige NIT y DV de la entidad; NO los deja vacíos.
- Persona natural no puede tener tipo de documento NIT. NIT exige número y DV; cédula o RUT exige número de cédula; cédula de extranjería exige su número. Igual para supervisor e interventor.
- Tipo de seguimiento "Supervisor" exige supervisor; "Interventor" exige interventor; "Interventor y Supervisor" exige ambos. Se aplica al suscribir (la lista de "faltan" incluye "supervisor" o "interventor"), al agregar un periodo (un rol que el tipo no admite da 400) y al cambiar el tipo del contrato (no puede dejar periodos que no cuadren).
- Clase "30 OTROS" exige describir la otra clase; cualquier otra clase la deja vacía. Se decide con `ClaseContratoEntity.esOtros()` (código 30 en el ámbito CONTRATO), no comparando textos.
- Anticipo "No pactados" exige valor 0; "Anticipo" o "Pago anticipado" exige valor.
- Adición en valor exige valor; en tiempo exige días; en valor y tiempo exige ambos.
- Fecha de terminación exige fecha de inicio; inicio exige suscripción; acta final exige terminación.
- Si el número de pagos es N, los pagos 1 a N son obligatorios: el acta final (liquidación) exige al menos N pagos registrados. Más de N se permite con aviso.

### Avisos (no bloquean; van en `advertencias`)
- El contratista tiene otro contrato no anulado en el año: posible fraccionamiento.
- La suma de los CDP con valor es menor que el valor final.
- Las adiciones en valor superan el 50 % del valor inicial (límite de la Ley 80).
- Se registra un pago más allá de los pactados.

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

**Vigencia**: no es una tabla. Es el año del contrato, guardado como número (`contrato.anio`, `consecutivo.anio`) y validado entre 2000 y 2100. Un contrato nuevo solo se crea en el año actual o en los `vri.contrato.anios-atras` anteriores (por defecto 1: en enero aún se puede numerar un trámite del año que terminó). Los contratos existentes de años anteriores se siguen editando y reportando.

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
| anio | SMALLINT | PK compuesta. Número entre 2000 y 2100 (no hay tabla de vigencias) |
| ultimo_numero | INT | Se incrementa con bloqueo de fila (`SELECT … FOR UPDATE`); ver "Numeración" |

**Uso por contratos.** "Ya hay contratos que usan la serie o el tipo" lo responde la interfaz `UsoTipoContratoChecker`, implementada en el dominio de contratos (`UsoTipoContratoCheckerContratos`): un tipo está usado si tiene contratos; una serie, si tiene contratos o si ya emitió algún número en cualquier año (consecutivo > 0), lo que cubre los números de la carga inicial, emitidos antes de que existiera la tabla `contrato`.

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

**`catalogo`** y **`catalogo_item`**: listas cortas (dominio `catalogo`).

| `catalogo` | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| codigo | VARCHAR(40) | Único, en mayúsculas (TIPO_IDENTIFICACION, ASEGURADORA). No cambia después de crear: el programa lo usa (`CodigoCatalogo`) |
| nombre | VARCHAR(120) | Obligatorio |
| descripcion | VARCHAR(300) | Opcional |
| activo | BOOLEAN | Inactivo: sus ítems no se ofrecen (el selector queda vacío) ni se aceptan en registros nuevos |

| `catalogo_item` | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| catalogo_id | BIGINT | FK `catalogo`. No cambia: un ítem no se mueve de catálogo (el catálogo va en la URL) |
| codigo | VARCHAR(40) | Mnemónico que usa el programa (CC, NIT, SUPERVISOR), en mayúsculas. No cambia después de crear. Único por catálogo |
| codigo_sireci | VARCHAR(10) | Opcional: el número que pide el SIRECI ("3" de "3 CÉDULA DE CIUDADANÍA"). Fuente de financiación y aseguradora no lo tienen |
| nombre | VARCHAR(150) | Tal como se escribe (textos oficiales del SIRECI en mayúsculas, con tildes). Único por catálogo (sin mayúsculas ni tildes) |
| orden | INT | Posición en los selectores, que se ordenan por orden y luego por nombre |
| activo | BOOLEAN | Inactivo: no se ofrece en formularios nuevos; los registros existentes lo conservan |

- La etiqueta es "código SIRECI + nombre" ("3 CÉDULA DE CIUDADANÍA") o solo el nombre si no hay código SIRECI.
- **Validador reutilizable** `ICatalogoItemServicio.validarItem(itemId, codigoCatalogo)`: el ítem debe existir, pertenecer a ese catálogo, estar activo y su catálogo también; si no, 400. Las entidades que referencian ítems lo llaman **solo cuando el ítem se asigna o cambia** (tercero y abogado para `tipo_identificacion_id`; luego el contrato para seguimiento, anticipo, adición, fuente y aseguradora).
- Carga inicial (`data.sql`, sin ids fijos: clave natural catálogo + código): desde la hoja CONTROL, TIPO_IDENTIFICACION (A348:A351: NIT, RUT, CC, CE), NATURALEZA (A341:A343: NATURAL, JURIDICA, CONSORCIO_UT), TIPO_SEGUIMIENTO (A356:A358: INTERVENTOR, SUPERVISOR, INTERVENTOR_SUPERVISOR), TIPO_ANTICIPO (A363:A365: ANTICIPO, PAGO_ANTICIPADO, NO_PACTADO), TIPO_ADICION (A369:A372: VALOR, TIEMPO, VALOR_TIEMPO, SIN_ADICION) y FUENTE_FINANCIACION (A455:A459: Funcionamiento, Inversión, SGR, Externos, Otro; sin código SIRECI). Se excluyen las opciones "no se diligencia información para este formulario en este período de reporte" (pertenecen al reporte SIRECI); "4 NO SE HA ADICIONADO..." sí es un valor real. ASEGURADORA no está en CONTROL: sale de la columna COMPAÑIA ASEGURADORA de los contratos, unificada en 4 (SEGUROS MUNDIAL, SEGUROS DEL ESTADO, SURA, ASEGURADORA SOLIDARIA DE COLOMBIA; las variantes "S.A", "SA" y sin "DE COLOMBIA" eran la misma); "N/A" (105 contratos) significa sin póliza y no se carga.
- **Migración manual** `db/migracion-manual/2026-11-27-catalogo.sql`: pasa una base existente del modelo anterior (catálogo como texto en `catalogo_item.catalogo`) a este, conservando los ids. Una base nueva no la necesita. Ya se aplicó a la base de desarrollo.

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

El NIT se reconoce por el **código mnemónico** del tipo de identificación, configurado en `vri.tipo-identificacion.codigo-nit` (`NIT`; su código SIRECI es 1, según la lista "TIPO DE DOCUMENTO" de la hoja CONTROL). Persona natural no admite NIT.

**`persona_natural`**: nombres VARCHAR(100) y apellidos VARCHAR(100) obligatorios; fecha_nacimiento DATE (pasada; la edad se calcula), ciudad_nacimiento VARCHAR(80), fecha_expedicion DATE (no anterior al nacimiento), lugar_expedicion VARCHAR(80), profesion VARCHAR(120), tarjeta_profesional VARCHAR(40), entidad_tarjeta VARCHAR(120), correo_institucional VARCHAR(120).

**`persona_juridica`**: razon_social VARCHAR(200) obligatoria; representante_legal_id BIGINT opcional, FK a `persona_natural` (la base impide que sea jurídica). Al asignarlo o cambiarlo debe existir y estar activo. es_consorcio_union_temporal BOOLEAN (falso por defecto).

Naturaleza para el SIRECI (catálogo `NATURALEZA`): sale del tercero, sin guardarla aparte. Persona natural → 1 PERSONA NATURAL; persona jurídica → 2 PERSONA JURÍDICA, o 3 P JURÍDICA - UNIÓN TEMPORAL o CONSORCIO si `es_consorcio_union_temporal`. La columna discriminadora `naturaleza` (NATURAL/JURIDICA) no cambia.

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
- No hay borrado: se inactiva o se cierra el periodo (`PATCH /{id}/cerrar`). Si ya hay contratos (`UsoOrdenadorChecker`, que consulta `contrato.ordenador_gasto_id`), el tercero, el cargo y la fecha desde no se pueden cambiar; cerrar sí.
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
- "Ya hay contratos que lo usan" lo responde `UsoRubroChecker`, que consulta `contrato_presupuesto.rubro_id`. El dominio de contratos valida el rubro con `IRubroServicio.obtenerRubroValido(id, actual)`.
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

El dominio de contratos se construye por fases: **1** `contrato`, `contrato_presupuesto` y `contrato_observacion` (hecha); **2** detalle (CDP, RP, UNSPSC, seguimiento, modificaciones, pagos, pólizas) y los campos calculados (hecha); **3** `contrato_sireci`, `publicacion_secop` y `documento_expediente`; **4** reporte SIRECI.

**`contrato`**: centro del modelo (dominio `contrato`).

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK autoincremental |
| serie_id | BIGINT | FK `serie_documental`. Se copia de la serie del tipo al crear y no cambia |
| anio | SMALLINT | Año de la vigencia, entre 2000 y 2100. Al crear: año actual o hasta `vri.contrato.anios-atras` anteriores (por defecto 1). No cambia después de crear |
| consecutivo | INT | Lo asigna `generarNumero` al crear. No cambia. **Único junto con serie y año** (`uk_contrato_numero`) |
| tipo_contrato_id | BIGINT | FK `tipo_contrato`, activo al crear. No cambia (el número depende de su serie) |
| estado | VARCHAR(20) | EN_TRAMITE, SUSCRITO, EN_EJECUCION, TERMINADO, LIQUIDADO, ANULADO. Lo calcula el sistema (ver "Ciclo de vida") |
| responsable_id | BIGINT | FK `abogado`, obligatorio. `IAbogadoServicio.obtenerAbogadoValido(id, actual)` |
| clase_contrato_id | BIGINT | FK `clase_contrato` |
| otra_clase | VARCHAR(150) | Obligatoria solo si la clase es 30 OTROS (`esOtros()`); vacía en cualquier otra |
| objeto | VARCHAR(390) | Obligatorio |
| contratista_id | BIGINT | FK `tercero` (natural o jurídica). `ITerceroServicio.obtenerTerceroValido(id, actual, rol)` |
| ordenador_gasto_id | BIGINT | FK `ordenador_gasto` (el periodo). La fecha de suscripción debe caer dentro del periodo |
| tipo_seguimiento_id | BIGINT | FK `catalogo_item` (TIPO_SEGUIMIENTO) |
| valor_sin_iva | DECIMAL(18,2) | ≥ 0 |
| iva | DECIMAL(18,2) | ≥ 0 |
| plazo_dias | INT | > 0 |
| numero_pagos | INT | 0 a 999. El acta final exige al menos N pagos registrados |
| fecha_suscripcion | DATE | Dentro del año del contrato. Exige el contrato completo |
| fecha_inicio | DATE | Exige fecha de suscripción y no es anterior a ella |
| fecha_terminacion | DATE | Exige fecha de inicio y no es anterior a ella |
| fecha_acta_final | DATE | Exige fecha de terminación y no es anterior a ella |
| motivo_anulacion | VARCHAR(390) | Solo en ANULADO |
| fecha_anulacion | DATE | Solo en ANULADO |

- **El número no se guarda**: `ContratoEntity.getNumero()` lo arma con el código de la serie y el consecutivo ("6.1-17.13/023"). Se repite entre años (cada año el consecutivo vuelve a 1), por eso la llave única es (serie, año, consecutivo) y el SIRECI antepone `AN25-` a los de años anteriores.
- **Creación**: llegan `tipoContratoId`, `anio`, `responsableId` y `objeto`; el resto es opcional. El servicio valida todo y pide el número al final (`generarNumero`, en la misma transacción) para bloquear el consecutivo el menor tiempo posible; si algo falla, el número no se consume. Probado con creaciones simultáneas en H2 y MySQL.
- **Actualización** (`PUT`): formulario completo (lo que no se envía queda vacío). `tipoContratoId` y `anio` son opcionales y, si vienen distintos, 400. Una relación que no cambia no se vuelve a validar; una que cambia debe estar activa.
- **No cambian después de crear**: serie, año, consecutivo y tipo.
- **Aviso de posible fraccionamiento**: la respuesta trae `advertencias` si el contratista tiene otro contrato no anulado en el mismo año. No bloquea.
- El valor inicial (valor sin IVA + IVA) se calcula en la entidad y la API lo devuelve.

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
| valor_anticipo | DECIMAL(18,2) | 0 si "No pactados" (código `NO_PACTADO`); en otro caso mayor que 0 y no mayor que el valor inicial. Sin tipo de anticipo no admite valor |
| fecha_certificacion_pago | DATE | La llena el FINANCIERO (cierre) |

- Se crea vacía junto con el contrato (PK = `contrato_id`, `@MapsId`) y viaja dentro del contrato en la API (`presupuesto`).
- **El porcentaje de anticipo no se guarda**: valor del anticipo × 100 ÷ valor inicial, 2 decimales.
- `recursos_de_convenio` verdadero exige `entidad_financiadora_id` y que el tercero tenga NIT (tiene DV); falso o nulo la deja vacía.

**`contrato_observacion`**: observaciones sobre un campo del contrato, escritas por abogados y financiero.

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| contrato_id | BIGINT | FK `contrato` |
| campo | VARCHAR(40) | Código de `CampoContrato` (OBJETO, VALOR_SIN_IVA, RUBRO...). La lista con etiquetas la da `GET /api/contratos/campos` |
| texto | VARCHAR(390) | Obligatorio |
| autor_id | BIGINT | FK `usuario`. Nulo hasta que exista el login |
| resuelta | BOOLEAN | Falso al crear |
| resuelta_en | DATETIME | |
| respuesta | VARCHAR(390) | Opcional: cómo se atendió |
| resuelta_por_id | BIGINT | FK `usuario`. Nulo hasta que exista el login |

- No se editan ni se borran: se resuelven una sola vez (400 la segunda) y, si el problema sigue, se agrega otra. Así queda el historial.
- Un contrato anulado no admite observaciones nuevas; las suyas se pueden leer y resolver.
- "Con observaciones" en los filtros significa **con observaciones pendientes**.

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
| `contrato_cdp` | numero_cdp VARCHAR(40), valor DECIMAL(18,2), fecha DATE | Número obligatorio, en mayúsculas y único por contrato. Valor (> 0) y fecha opcionales: los contratos migrados del Excel solo traen el número |
| `contrato_rp` | numero_rp VARCHAR(40), valor DECIMAL(18,2), fecha DATE | Igual que el CDP. CDP y RP comparten la clase `DocumentoPresupuestalEntity` (`@MappedSuperclass`) |
| `contrato_unspsc` | codigo_unspsc VARCHAR(10) | 8 dígitos (se quitan los espacios). Único por contrato |
| `contrato_seguimiento` | rol VARCHAR(15) (SUPERVISOR, INTERVENTOR), tercero_id BIGINT FK `tercero`, desde DATE, hasta DATE | Debe cuadrar con el tipo de seguimiento; `hasta` nulo = vigente. Supervisor: persona natural; interventor: natural o jurídica. Dos periodos del mismo rol no se cruzan (409): para cambiar de supervisor se cierra el actual (`PATCH /cerrar`) y se agrega el nuevo. Con el contrato suscrito, `desde` no es anterior a la suscripción |
| `modificacion` | tipo_adicion_id BIGINT FK `catalogo_item` (TIPO_ADICION), valor DECIMAL(18,2), dias INT, fecha DATE, documento VARCHAR(200), observacion VARCHAR(390) | Solo en contratos suscritos; fecha no anterior a la suscripción. VALOR exige valor y no lleva días; TIEMPO exige días y no lleva valor; VALOR_TIEMPO exige los dos; SIN_ADICION no se registra |
| `pago` | numero SMALLINT, valor DECIMAL(18,2), fecha DATE, registrado_por BIGINT FK `usuario` | El número lo asigna el sistema, consecutivo desde 1 y único por contrato; sin límite. Solo con fecha de inicio y no antes de ella. Solo el **último** pago se corrige o se borra; con acta final, un pago pactado no se borra. `registrado_por` nulo hasta que exista el login |
| `poliza` | numero VARCHAR(40), aseguradora_id BIGINT FK `catalogo_item` (ASEGURADORA), vigencia_hasta DATE | Número en mayúsculas y único por contrato |

- Las filas se pueden borrar para corregir errores (CDP, RP, UNSPSC, pólizas, modificaciones y el último pago); la auditoría (fase 3) dejará la traza. Los periodos de seguimiento no se borran: se cierran.
- Un contrato anulado no admite cambios en su detalle (400); se puede leer.
- **Bloqueo**: pagos, modificaciones y los cambios del contrato (`PUT`, cierre) bloquean la fila del contrato (`ContratoRepositorio.bloquear`, `SELECT … FOR UPDATE` sobre una fila que existe). Así dos pagos simultáneos no reciben el mismo número ni dejan el saldo negativo. Probado con pagos simultáneos en H2 y en MySQL.
- **CHECK de los enums**: Hibernate crea un CHECK con los valores de cada enum al crear la tabla y, con `ddl-auto=update`, no lo actualiza si el enum crece. Agregar un valor a un enum guardado exige actualizar o quitar su CHECK en las bases existentes. `db/migracion-manual/2026-11-27-contrato-detalle.sql` quita el de `contrato_observacion.campo` (`CampoContrato` ganó los campos del detalle); ya se aplicó a la base de desarrollo.

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

Paquete raíz `co.edu.unicauca.contratacionvri`. Dominios actuales: `abogado`, `usuario`, `catalogo` (tablas `catalogo` y `catalogo_item`), `dependencia`, `tercero`, `proyecto`, `tipocontrato` (series documentales, tipos de contrato y consecutivos), `clasecontrato` (clases SIRECI), `rubro` (rubros presupuestales), `ordenador` (ordenadores del gasto) y `contrato` (contrato, presupuesto y observaciones). Cada dominio tiene tres capas:

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
- Textos: Jackson normaliza todo texto al leer el JSON, antes de `@Valid` (`configuracion/ConfiguracionJackson`): recorta, colapsa espacios horizontales repetidos y convierte `""` en `null`. Cada servicio termina la limpieza con `comun/utilidades/Textos` (correos en minúsculas; documentos y códigos en mayúsculas). Los nombres que se muestran al usuario y en reportes (dependencias, tipos de contrato, clases SIRECI, rubros, terceros, proyectos) se guardan tal como se escriben, con tildes; la collation evita duplicados por mayúsculas o tildes.
- Búsquedas paginadas: devuelven `comun/fachadaServicios/PaginaRespuesta` (contenido, pagina, tamanio, totalElementos, totalPaginas, primera, ultima), con parámetros `pagina` (desde 0) y `tamanio` (1 a 100) y orden fijo definido por el servicio. Los comodines de LIKE del usuario se escapan con `!`.
- Proyectos: `POST /api/proyectos` (con `idProyecto`), `GET/PUT /api/proyectos/{idProyecto}` (un `idProyecto` distinto en el cuerpo da 400), `PATCH /{idProyecto}/estado`, `GET /api/proyectos?id=&nombre=&director=&activo=&pagina=&tamanio=` y `GET /api/proyectos/activos?texto=` para selectores. Los demás dominios validan un proyecto con `IProyectoServicio.obtenerProyectoValido(id, exigirActivo)`.
- Personas naturales asignadas (representante legal, director, y luego supervisor o contratista): validar con `ITerceroServicio.obtenerPersonaNaturalValida(id, actual, rol)`.
- Terceros: `POST/PUT /api/terceros/naturales[/{id}]` y `/api/terceros/juridicas[/{id}]` con DTOs distintos; `GET /api/terceros/{id}` devuelve la forma según la naturaleza; `GET /api/terceros?documento=&nombre=&naturaleza=&activo=&pagina=&tamanio=`.
- Tipos de contrato (dominio `tipocontrato`): `GET/POST /api/series-documentales`, `GET/PUT /api/series-documentales/{id}`, `PATCH /{id}/estado`, `GET /api/series-documentales/{id}/consecutivos/{anio}` (último número y siguiente; 0 si no hay números, no 404); `GET/POST /api/tipos-contrato?activo=&serieId=`, `GET/PUT /api/tipos-contrato/{id}`, `PATCH /{id}/estado` y `GET /api/tipos-contrato/activos` para selectores (tipo activo con serie activa). `generarNumero` no tiene endpoint: lo usa el dominio de contratos.
- Clases de contrato SIRECI: `GET /api/clases-contrato?ambito=&activo=` (con `activo=true` para selectores; orden por ámbito y código), `GET/PUT /api/clases-contrato/{id}` (PUT solo cambia el nombre; un código o ámbito distinto en el cuerpo da 400), `POST /api/clases-contrato` y `PATCH /{id}/estado`.
- Rubros presupuestales: `GET /api/rubros?texto=&activo=&pagina=&tamanio=` (`texto` = prefijo del código o contenido del nombre; con `activo=true` para selectores; orden numérico por código), `GET/PUT /api/rubros/{id}` (el código no cambia si hay contratos: 400; si choca con otro: 409), `POST /api/rubros` y `PATCH /{id}/estado`. Respuesta con `codigo`, `nombre`, `etiqueta` ("1052002 - Museo de Historia Natural") y `codigoEditable`.
- Contratos: `POST /api/contratos`, `GET/PUT /api/contratos/{id}`, `PATCH /{id}/cierre`, `PATCH /{id}/anular` con `{"motivo": ...}`, `GET /api/contratos?numero=&anio=&estado=&tipoContratoId=&claseContratoId=&responsableId=&contratista=&texto=&pagina=&tamanio=` (`numero` completo, solo el consecutivo o solo la serie; `contratista` = prefijo del documento o parte del nombre; `texto` = parte del objeto; orden: año y consecutivo descendentes) y `GET /api/abogados/{abogadoId}/contratos?inicioDesde=&inicioHasta=&terminacionDesde=&terminacionHasta=&conObservaciones=&estado=&anio=&pagina=&tamanio=` (vive en el dominio contrato; 404 si el abogado no existe; un rango invertido da 400). Las filas traen `observacionesPendientes`.
- Detalle del contrato (`ContratoDetalleRestController`, bajo `/api/contratos/{id}`): `/cdp` y `/rp` (GET, POST, PUT y DELETE `/{filaId}`), `/unspsc` (GET, POST, DELETE), `/polizas` (GET, POST, PUT, DELETE), `/seguimientos` (GET, POST, PUT `/{filaId}`, PATCH `/{filaId}/cerrar` con `{"hasta"}`), `/modificaciones` (GET, POST, PUT, DELETE), `/pagos` (GET, POST sin número, PUT y DELETE solo del último) y `GET /resumen` (solo los calculados). `GET /api/contratos/{id}` trae el bloque `resumen`; las filas de los listados traen `valorFinal`, `saldo`, `avanceFisicoReal` y `avancePresupuestalReal`. Borrar responde 204.
- Observaciones: `GET /api/contratos/campos`, `POST /api/contratos/{id}/observaciones` con `{"campo", "texto"}`, `GET /api/contratos/{id}/observaciones?campo=&resuelta=` (la más reciente primero), `PATCH /{id}/observaciones/{obsId}/resolver` con `{"respuesta"}` opcional y `GET /{id}/observaciones/conteo` (`total`, `pendientes` y `porCampo` en el orden del formulario).
- Ordenadores del gasto: `GET /api/ordenadores-gasto?vigenteEn=AAAA-MM-DD&texto=&activo=` (`vigenteEn` para los selectores del contrato: periodos activos que cubren esa fecha; `texto` = nombre, cédula o cargo), `GET/PUT /api/ordenadores-gasto/{id}`, `POST /api/ordenadores-gasto`, `PATCH /{id}/cerrar` con `{"fechaHasta": ...}` y `PATCH /{id}/estado`.
- Catálogos: `GET /api/catalogos?activo=`, `GET/PUT /api/catalogos/{codigo}` (un código distinto en el cuerpo da 400), `POST /api/catalogos` y `PATCH /{codigo}/estado`. Ítems: `GET /api/catalogos/{codigo}/items?activo=true` (selectores), `GET/PUT /api/catalogos/{codigo}/items/{id}` (un ítem de otro catálogo da 404; un código distinto en el cuerpo da 400), `POST /api/catalogos/{codigo}/items` y `PATCH /{id}/estado`. Ya no existe `/api/catalogo-items`.
- Errores HTTP: 400 (validación o regla de negocio), 404 (no existe), 409 (duplicado), 415 (cuerpo que no es `application/json`).
- CORS (`configuracion/ConfiguracionCors`): `/api/**` acepta peticiones de los orígenes de `vri.cors.origenes-permitidos` (variable `CORS_ORIGENES`, por defecto `http://localhost:4200`, el `ng serve` del frontend; varios separados por coma). Métodos GET, POST, PUT, PATCH, DELETE y OPTIONS; encabezados `Content-Type`, `Authorization` y `Accept`; expone `Location`; sin credenciales (el token irá en `Authorization`, no en cookies). Otro origen recibe 403 en el preflight. La propiedad se repite en `src/test/resources/application.properties`.
- No hay borrado físico de maestros: se usa `PATCH /{id}/estado?activo=`.
- MySQL local: `docker compose up -d` (MySQL 8.4 en el puerto 3306, base `contratacion_vri`). Las pruebas usan H2 en memoria y no necesitan Docker.
- `src/test/resources/application.properties` reemplaza por completo al de `src/main`: toda propiedad que las pruebas necesiten (p. ej. `spring.jpa.defer-datasource-initialization`) hay que repetirla ahí. Las pruebas no cargan `data.sql` (`spring.sql.init.mode=never`); `CargaInicialTest` lo prueba en su propia base H2.
- Llaves asignadas a mano (`proyecto`, `consecutivo`): la entidad implementa `Persistable` para que crear haga INSERT y no merge (un merge sobrescribiría el registro existente).
- Lo que dependa de bloqueos o concurrencia se prueba también contra MySQL real: H2 no reproduce los bloqueos de InnoDB. Se puede correr una prueba apuntando a una base temporal con `-Dspring.datasource.url=jdbc:mysql://…/contratacion_vri_prueba` (y usuario/clave/driver de MySQL).

## Convenciones

- Código, comentarios y mensajes de error en español, con los nombres de tablas y campos de este documento.
- Las reglas de negocio viven en el backend; el frontend puede repetirlas para la experiencia de usuario, pero el backend es la fuente de verdad.
