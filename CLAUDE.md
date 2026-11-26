# BackEnd Contratación VRI

Backend del sistema de contratación directa de la Vicerrectoría de Investigaciones (VRI) de la Universidad del Cauca. Este archivo describe el negocio para que cualquier cambio en el código respete las reglas del proceso. El frontend vive en el repositorio hermano `FrontEndContratacionVRI`.

## Stack

- **Lenguaje**: Java, JDK 17.
- **Framework**: Spring Boot 3.x (paquetes `jakarta.*`).
- **Build**: Maven (`pom.xml`).
- **Base de datos**: MySQL.
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
- **TRD**: tabla de retención documental. Cada tipo de contrato tiene una serie TRD (OPS = `6.1-17.13`, SUMINISTRO = `6.1-17.15`, COMPRAVENTA = `6.1-17.7`, CONSULTORÍA = `6.1-17.9`, APRENDIZAJE/PASANTÍA/JUDICATURA = `6.1-17.3`, convenios = `6.1-18.x`; 16 en total).
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
- Formato `serie_trd/NNN`, por ejemplo `6.1-17.13/023`. Consecutivo de 3 dígitos con ceros a la izquierda.
- Un consecutivo por tipo de contrato y por año. El siguiente se asigna dentro de una transacción (bloqueo de fila sobre `consecutivo`) para que dos abogados nunca reciban el mismo número.
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
- Dependencia de afectación: se deduce del código de rubro. Cédula y cargo del ordenador: del ordenador. Datos del contratista y supervisor: del maestro de terceros. DV: algoritmo DIAN. Edad: desde la fecha de nacimiento.

### Obligatoriedad condicional (bloquean el guardado)
- Recursos de contrato o convenio interadministrativo = SI exige NIT y DV de la entidad; NO los deja vacíos.
- Persona natural no puede tener tipo de documento NIT. NIT exige número y DV; cédula o RUT exige número de cédula; cédula de extranjería exige su número. Igual para supervisor e interventor.
- Tipo de seguimiento "Supervisor" exige supervisor; "Interventor" exige interventor; "Interventor y Supervisor" exige ambos.
- Clase "30 OTROS" exige describir la otra clase; cualquier otra clase la deja vacía.
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

**`tipo_contrato`**: tipo y serie documental (OPS, SUMINISTRO…).

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| nombre | VARCHAR(80) | Obligatorio |
| serie_trd | VARCHAR(20) | Única, ej. `6.1-17.13` |
| es_convenio | BOOLEAN | |
| activo | BOOLEAN | |

**`consecutivo`**: contador de números por tipo y año.

| Campo | Tipo | Llave / regla |
|---|---|---|
| tipo_contrato_id | BIGINT | PK compuesta, FK `tipo_contrato` |
| anio | SMALLINT | PK compuesta, FK `vigencia` |
| ultimo_numero | INT | Se incrementa con bloqueo de fila (`SELECT … FOR UPDATE`) |

**`clase_contrato`**: clases oficiales SIRECI.

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| codigo_sireci | SMALLINT | Único (1 a 30, más las de convenio) |
| nombre | VARCHAR(120) | |
| activo | BOOLEAN | |

**`catalogo_item`**: listas cortas en una sola tabla.

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| catalogo | VARCHAR(40) | TIPO_IDENTIFICACION, NATURALEZA, TIPO_SEGUIMIENTO, TIPO_ANTICIPO, TIPO_ADICION, FUENTE_FINANCIACION, ASEGURADORA |
| codigo_sireci | VARCHAR(10) | Único por catálogo |
| nombre | VARCHAR(150) | Se muestra como "código nombre" |
| activo | BOOLEAN | |

**`tercero`**: contratistas, supervisores, interventores, ordenadores, directores de proyecto y entidades financiadoras.

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| tipo_identificacion_id | BIGINT | FK `catalogo_item` (TIPO_IDENTIFICACION) |
| numero_documento | VARCHAR(20) | Único junto con el tipo |
| dv | CHAR(1) | Calculado con el algoritmo DIAN al guardar, solo para NIT |
| naturaleza_id | BIGINT | FK `catalogo_item` (NATURALEZA). Persona natural no admite NIT |
| nombre_completo | VARCHAR(200) | Obligatorio |
| correo | VARCHAR(120) | |
| correo_institucional | VARCHAR(120) | |
| direccion | VARCHAR(200) | |
| telefono | VARCHAR(20) | |
| celular_1 | VARCHAR(20) | |
| celular_2 | VARCHAR(20) | |
| profesion | VARCHAR(120) | |
| tarjeta_profesional | VARCHAR(40) | |
| entidad_tarjeta | VARCHAR(120) | |
| fecha_nacimiento | DATE | La edad se calcula |
| fecha_expedicion | DATE | |
| lugar_expedicion | VARCHAR(80) | |
| ciudad_nacimiento | VARCHAR(80) | |

**`ordenador_gasto`**

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| tercero_id | BIGINT | FK `tercero` |
| cargo | VARCHAR(120) | Ej. VICERRECTOR DE INVESTIGACIONES, RECTOR DELEGATARIO |
| vigente_desde | DATE | |
| vigente_hasta | DATE | Nulo si sigue vigente |

**`proyecto`**

| Campo | Tipo | Llave / regla |
|---|---|---|
| id_proyecto | INT | PK (código institucional, ej. 5019) |
| nombre | VARCHAR(400) | |
| director_tercero_id | BIGINT | FK `tercero` |
| bpin | VARCHAR(20) | |
| activo | BOOLEAN | |

**`rubro_presupuestal`**

| Campo | Tipo | Llave / regla |
|---|---|---|
| codigo | VARCHAR(40) | PK (código de afectación presupuestal) |
| nombre | VARCHAR(250) | |
| codigo_ordenado | VARCHAR(40) | Para ordenar |
| dependencia_id | BIGINT | FK `dependencia` (dependencia de afectación) |
| activo | BOOLEAN | |

**`dependencia`**

| Campo | Tipo | Llave / regla |
|---|---|---|
| id | BIGINT | PK |
| nombre | VARCHAR(200) | Único |
| activo | BOOLEAN | |

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
| consecutivo | INT | Único junto con tipo y año |
| estado | VARCHAR(20) | EN_TRAMITE, SUSCRITO, EN_EJECUCION, TERMINADO, LIQUIDADO, ANULADO |
| responsable_id | BIGINT | FK `abogado` |
| fecha_registro | DATETIME | |
| clase_contrato_id | BIGINT | FK `clase_contrato` |
| otra_clase | VARCHAR(150) | Obligatoria solo si la clase es 30 OTROS |
| objeto | VARCHAR(390) | Obligatorio |
| contratista_id | BIGINT | FK `tercero` |
| ordenador_id | BIGINT | FK `ordenador_gasto` |
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
| rubro_codigo | VARCHAR(40) | FK `rubro_presupuestal` |
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

## Estructura del código

Paquete raíz `co.edu.unicauca.contratacionvri`. Dominios actuales: `abogado`, `usuario`, `catalogo` (tabla `catalogo_item`) y `dependencia`. Cada dominio tiene tres capas:

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
- Textos: Jackson recorta espacios de todo texto al leer el JSON (`configuracion/ConfiguracionJackson`); cada servicio termina la limpieza con `comun/utilidades/Textos` (correos en minúsculas; nombres de catálogo y dependencia en mayúsculas).
- No hay borrado físico de maestros: se usa `PATCH /{id}/estado?activo=`.
- MySQL local: `docker compose up -d` (MySQL 8.4 en el puerto 3306, base `contratacion_vri`). Las pruebas usan H2 en memoria y no necesitan Docker.

## Convenciones

- Código, comentarios y mensajes de error en español, con los nombres de tablas y campos de este documento.
- Las reglas de negocio viven en el backend; el frontend puede repetirlas para la experiencia de usuario, pero el backend es la fuente de verdad.
