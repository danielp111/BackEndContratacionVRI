package co.edu.unicauca.contratacionvri.tipocontrato;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.AmbitoClase;
import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.ClaseContratoEntity;
import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.repositorios.ClaseContratoRepositorio;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoEntity;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CodigoCatalogo;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.repositorios.CatalogoRepositorio;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.repositorios.CatalogoItemRepositorio;
import co.edu.unicauca.contratacionvri.ordenador.accesoADatos.modelos.OrdenadorGastoEntity;
import co.edu.unicauca.contratacionvri.ordenador.accesoADatos.repositorios.OrdenadorGastoRepositorio;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.PersonaNaturalEntity;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.repositorios.TerceroRepositorio;
import co.edu.unicauca.contratacionvri.dependencia.accesoADatos.modelos.DependenciaEntity;
import co.edu.unicauca.contratacionvri.dependencia.accesoADatos.repositorios.DependenciaRepositorio;
import co.edu.unicauca.contratacionvri.rubro.accesoADatos.modelos.RubroEntity;
import co.edu.unicauca.contratacionvri.rubro.accesoADatos.repositorios.RubroRepositorio;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.ConsecutivoEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.ConsecutivoId;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.SerieDocumentalEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.TipoContratoEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios.ConsecutivoRepositorio;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios.SerieDocumentalRepositorio;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios.TipoContratoRepositorio;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTOPeticion.SerieDocumentalDTOPeticion;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios.INumeracionServicio;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios.ISerieDocumentalServicio;

/**
 * Carga inicial (data.sql) de la hoja CONTROL. Usa su propia base H2 ("carga_inicial") con la carga
 * activada, para no chocar con la limpieza que hacen las demás pruebas. Las pruebas van en orden
 * porque comparten esa base.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=always",
        "spring.datasource.url=jdbc:h2:mem:carga_inicial;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CargaInicialTest {

    @Autowired
    private SerieDocumentalRepositorio series;

    @Autowired
    private TipoContratoRepositorio tipos;

    @Autowired
    private ConsecutivoRepositorio consecutivos;

    @Autowired
    private ClaseContratoRepositorio clases;

    @Autowired
    private RubroRepositorio rubros;

    @Autowired
    private DependenciaRepositorio dependencias;

    @Autowired
    private CatalogoItemRepositorio catalogo;

    @Autowired
    private CatalogoRepositorio catalogos;

    @Autowired
    private TerceroRepositorio terceros;

    @Autowired
    private OrdenadorGastoRepositorio ordenadores;

    @Autowired
    private INumeracionServicio numeracion;

    @Autowired
    private ISerieDocumentalServicio serieServicio;

    @Autowired
    private DataSource dataSource;

    private void volverACargarDataSql() {
        ResourceDatabasePopulator carga = new ResourceDatabasePopulator(new ClassPathResource("data.sql"));
        carga.setSqlScriptEncoding("UTF-8");
        carga.execute(dataSource);
    }

    private int ultimo2026(String codigoSerie) {
        Long serieId = series.findAll().stream().filter(s -> s.getCodigo().equals(codigoSerie)).findFirst().orElseThrow().getId();
        return consecutivos.findById(new ConsecutivoId(serieId, 2026)).map(ConsecutivoEntity::getUltimoNumero).orElseThrow();
    }

    private TipoContratoEntity tipo(String nombre) {
        return tipos.findAll().stream().filter(t -> t.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    @Test
    @Order(1)
    void cargaLas15SeriesY17TiposDeLaHojaControl() {
        assertThat(series.count()).isEqualTo(15);
        assertThat(tipos.count()).isEqualTo(17);
        assertThat(consecutivos.count()).isEqualTo(15);

        assertThat(tipos.findAll()).extracting(TipoContratoEntity::getNombre).containsExactlyInAnyOrder(
                "OPS", "Suministro", "Aprendizaje", "Pasantía", "Judicatura", "Arrendamiento", "Obra", "Mandato",
                "Consultoría", "Comodato", "Compraventa", "Convenio interadministrativo", "Convenio interinstitucional",
                "Convenio marco", "Transporte", "Consignación", "Contrato interadministrativo");
    }

    @Test
    @Order(2)
    void asociaCadaTipoASuSerieYLosDeFormacionCompartenLa17Punto3() {
        Map<String, String> serieDeTipo = tipos.buscar(null, null).stream()
                .collect(Collectors.toMap(TipoContratoEntity::getNombre, t -> t.getSerie().getCodigo()));
        assertThat(serieDeTipo).containsEntry("OPS", "6.1-17.13")
                .containsEntry("Suministro", "6.1-17.15")
                .containsEntry("Aprendizaje", "6.1-17.3")
                .containsEntry("Pasantía", "6.1-17.3")
                .containsEntry("Judicatura", "6.1-17.3")
                .containsEntry("Consultoría", "6.1-17.9")
                .containsEntry("Compraventa", "6.1-17.7")
                .containsEntry("Convenio marco", "6.1-18.3")
                .containsEntry("Consignación", "6.1-17.8")
                .containsEntry("Contrato interadministrativo", "6.1-17.17");
    }

    @Test
    @Order(3)
    void esConvenioCoincideConLaReglaDelCodigo() {
        for (SerieDocumentalEntity s : series.findAll()) {
            assertThat(s.getEsConvenio())
                    .as("es_convenio de %s", s.getCodigo())
                    .isEqualTo(SerieDocumentalEntity.esCodigoDeConvenio(s.getCodigo()));
        }
        assertThat(series.findAll().stream().filter(SerieDocumentalEntity::getEsConvenio).map(SerieDocumentalEntity::getCodigo))
                .containsExactlyInAnyOrder("6.1-18.1", "6.1-18.2", "6.1-18.3");
    }

    @Test
    @Order(4)
    void losConsecutivos2026TienenElUltimoNumeroUsado() {
        assertThat(ultimo2026("6.1-17.13")).isEqualTo(108);
        assertThat(ultimo2026("6.1-17.15")).isEqualTo(15);
        assertThat(ultimo2026("6.1-17.3")).isEqualTo(1);
        assertThat(ultimo2026("6.1-17.9")).isEqualTo(1);
        assertThat(ultimo2026("6.1-17.7")).isEqualTo(3);
        assertThat(ultimo2026("6.1-18.1")).isZero();
        assertThat(ultimo2026("6.1-17.17")).isZero();

        // Los siguientes números continúan desde el Excel.
        assertThat(numeracion.generarNumero(tipo("OPS").getId(), 2026).numero()).isEqualTo("6.1-17.13/109");
        assertThat(numeracion.generarNumero(tipo("Pasantía").getId(), 2026).numero()).isEqualTo("6.1-17.3/002");
        assertThat(numeracion.generarNumero(tipo("Convenio marco").getId(), 2026).numero()).isEqualTo("6.1-18.3/001");
    }

    @Test
    @Order(5)
    void volverACargarNoDuplicaNiSobrescribeLoQueCambio() {
        // Cambios hechos después de la carga: OPS ya va en 109 (prueba anterior), se renombra un tipo
        // y se inactiva otro.
        TipoContratoEntity ops = tipo("OPS");
        ops.setNombre("Prestación de servicios");
        tipos.save(ops);
        TipoContratoEntity consignacion = tipo("Consignación");
        consignacion.setActivo(false);
        tipos.save(consignacion);

        volverACargarDataSql();

        assertThat(series.count()).isEqualTo(15);
        assertThat(tipos.count()).isEqualTo(17);
        assertThat(consecutivos.count()).isEqualTo(15);
        assertThat(ultimo2026("6.1-17.13")).as("no vuelve a 108").isEqualTo(109);
        assertThat(tipos.findAll()).extracting(TipoContratoEntity::getNombre)
                .contains("Prestación de servicios").doesNotContain("OPS");
        assertThat(tipo("Consignación").getActivo()).as("no se reactiva").isFalse();
    }

    @Test
    @Order(6)
    void despuesDeLaCargaSePuedenCrearSeriesNuevas() {
        // Los ids fijos de la carga no deben chocar con los que genera la base para registros nuevos.
        var nueva = serieServicio.crear(new SerieDocumentalDTOPeticion("6.1-17.99"));
        assertThat(nueva.getId()).isGreaterThan(15);
    }

    // ------------------------------------------------------------------ clases de contrato SIRECI

    private ClaseContratoEntity clase(AmbitoClase ambito, int codigo) {
        return clases.findAll().stream()
                .filter(c -> c.getAmbito() == ambito && c.getCodigoSireci() == codigo).findFirst().orElseThrow();
    }

    @Test
    @Order(7)
    void cargaLas30ClasesDeContratoY2DeConvenio() {
        assertThat(clases.count()).isEqualTo(32);
        assertThat(clases.buscar(AmbitoClase.CONTRATO, null)).extracting(ClaseContratoEntity::getCodigoSireci)
                .containsExactlyElementsOf(java.util.stream.IntStream.rangeClosed(1, 30).boxed().toList());
        assertThat(clases.buscar(AmbitoClase.CONVENIO, null)).extracting(ClaseContratoEntity::getEtiqueta)
                .containsExactly("1 CONTRATO / CONVENIO INTERADMINISTRATIVO",
                        "2 CONVENIO DE COOPERACIÓN (NACIONAL / INTERNACIONAL)");

        // Número y texto separados, con tildes; "COMISION" del Excel corregida.
        assertThat(clase(AmbitoClase.CONTRATO, 23).getNombre()).isEqualTo("PRESTACIÓN DE SERVICIOS");
        assertThat(clase(AmbitoClase.CONTRATO, 23).getEtiqueta()).isEqualTo("23 PRESTACIÓN DE SERVICIOS");
        assertThat(clase(AmbitoClase.CONTRATO, 4).getNombre()).isEqualTo("COMISIÓN");
        assertThat(clase(AmbitoClase.CONTRATO, 2).getNombre()).isEqualTo("ARRENDAMIENTO y/o ADQUISICIÓN DE INMUEBLES");
        assertThat(clases.findAll()).allSatisfy(c -> assertThat(c.getNombre()).doesNotMatch("^\\d.*"));

        // Solo "30 OTROS" de contratos es esOtros.
        assertThat(clases.findAll().stream().filter(ClaseContratoEntity::esOtros).map(ClaseContratoEntity::getEtiqueta))
                .containsExactly("30 OTROS");

        // 99999998 no es una clase.
        assertThat(clases.findAll()).noneMatch(c -> c.getCodigoSireci() == 99_999_998);
    }

    // ------------------------------------------------------------------ rubros presupuestales

    private RubroEntity rubro(String codigo) {
        return rubros.findAll().stream().filter(r -> r.getCodigo().equals(codigo)).findFirst().orElseThrow();
    }

    @Test
    @Order(9)
    void cargaLos51RubrosConElCodigoComoTextoExacto() {
        assertThat(rubros.count()).isEqualTo(51);
        // Venían como número en el Excel ("9.011044001E9", "1052002.0").
        assertThat(rubro("9011044001").getNombre()).isEqualTo("Servicios prestados a Empresas y");
        assertThat(rubro("1052002").getEtiqueta()).isEqualTo("1052002 - Museo de Historia Natural");
        assertThat(rubros.findAll()).allSatisfy(r -> assertThat(r.getCodigo()).matches("\\d+"));

        // El primero en el orden es 1031, como la lista ordenada del Excel.
        assertThat(rubros.buscar(null, null, org.springframework.data.domain.PageRequest.of(0, 3)).getContent())
                .extracting(RubroEntity::getCodigo).containsExactly("1031", "1027008", "1034001");

        // Nombre repetido del Excel, cargado tal cual en dos rubros.
        assertThat(rubros.findAll().stream().filter(r -> r.getNombre().equals("Servicios prestados a Empresas y")))
                .hasSize(2);

        // Limpieza: sin espacios sobrantes ni comillas envolventes o sueltas al final.
        assertThat(rubros.findAll()).allSatisfy(r -> {
            assertThat(r.getNombre()).isEqualTo(r.getNombre().strip()).doesNotContain("  ");
            assertThat(r.getNombre()).doesNotStartWith("\"").doesNotEndWith("\"");
        });
        assertThat(rubro("4901501").getNombre()).startsWith("Propuesta de implementación");
        assertThat(rubro("1037007").getNombre()).startsWith("CONSOLIDACION DE PROCESOS");
        assertThat(rubro("1037008").getNombre()).endsWith("en el departamento del Cauca");
        // La comilla de un título real se conserva (fila 137).
        assertThat(rubro("4901477").getNombre()).contains("titulado \"econavipesca");
    }

    @Test
    @Order(10)
    void volverACargarNoDuplicaNiReactivaRubros() {
        RubroEntity museo = rubro("1052002");
        museo.setActivo(false);
        rubros.save(museo);

        volverACargarDataSql();

        assertThat(rubros.count()).isEqualTo(51);
        assertThat(rubro("1052002").getActivo()).as("no se reactiva").isFalse();
    }

    // ------------------------------------------------------------------ dependencias

    private DependenciaEntity dependencia(String nombre) {
        return dependencias.findAll().stream().filter(d -> d.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    /** 10 a 14 del plan: cantidad, id 1, textos exactos, posibles duplicados y sin repetidos. */
    @Test
    @Order(11)
    void cargaLas55DependenciasDeLaListaDeLaVri() {
        assertThat(dependencias.count()).isEqualTo(55);
        assertThat(dependencias.findAll()).allMatch(DependenciaEntity::getActivo);

        assertThat(dependencias.findById(1L).orElseThrow().getNombre()).isEqualTo("Vicerrectoría de Investigaciones");

        // Textos exactos: tildes, comas y sin prefijo en divisiones, oficinas y áreas.
        assertThat(dependencias.findAll()).extracting(DependenciaEntity::getNombre).contains(
                "Facultad de Ciencias Contables, Económicas y Administrativas",
                "Centro Universitario en Salud Alfonso López",
                "Programa de Especialización, Maestría y Doctorado FIET",
                "de Admisiones", "Jurídica", "de Desarrollo Editorial", "Administrativa y de Servicios",
                "Recursos Humanos - Pensiones", "CECAV", "Unilingua");

        // Los cuatro pares de posibles duplicados se cargan, activos.
        for (String nombre : new String[]{"Facultad de Ciencias Agrarias", "Facultad de Ciencias Agropecuarias",
                "de Admisiones", "Admisiones y Registro", "de Salud Ocupacional", "de Seguridad y Salud en el Trabajo",
                "de Gestión del Talento Humano", "Recursos Humanos - Pensiones"}) {
            assertThat(dependencia(nombre).getActivo()).as(nombre).isTrue();
        }

        // Ningún nombre repetido, ni siquiera ignorando mayúsculas y tildes.
        assertThat(dependencias.findAll().stream()
                .map(d -> java.text.Normalizer.normalize(d.getNombre(), java.text.Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "").toLowerCase(java.util.Locale.ROOT)))
                .doesNotHaveDuplicates();
    }

    /** 15 del plan: volver a cargar no duplica ni reactiva. */
    @Test
    @Order(12)
    void volverACargarNoDuplicaNiReactivaDependencias() {
        DependenciaEntity cecav = dependencia("CECAV");
        cecav.setActivo(false);
        dependencias.save(cecav);

        volverACargarDataSql();

        assertThat(dependencias.count()).isEqualTo(55);
        assertThat(dependencia("CECAV").getActivo()).as("no se reactiva").isFalse();
    }

    // ------------------------------------------------------------------ tipos de identificación y ordenadores

    private PersonaNaturalEntity personaPorCedula(String cedula) {
        return terceros.findAll().stream()
                .filter(t -> t instanceof PersonaNaturalEntity && t.getNumeroDocumento().equals(cedula))
                .map(PersonaNaturalEntity.class::cast).findFirst().orElseThrow();
    }

    private java.util.List<CatalogoItemEntity> itemsDe(String codigoCatalogo) {
        return catalogo.buscar(catalogos.findByCodigo(codigoCatalogo).orElseThrow().getId(), null);
    }

    /** Los 7 catálogos con sus ítems exactos, sin las opciones de "no se diligencia". */
    @Test
    @Order(16)
    @org.springframework.transaction.annotation.Transactional
    void cargaLos7CatalogosConSusItems() {
        assertThat(catalogos.findAll()).extracting(CatalogoEntity::getCodigo).containsExactlyInAnyOrder(
                "TIPO_IDENTIFICACION", "NATURALEZA", "TIPO_SEGUIMIENTO", "TIPO_ANTICIPO", "TIPO_ADICION",
                "FUENTE_FINANCIACION", "ASEGURADORA");
        assertThat(itemsDe(CodigoCatalogo.TIPO_IDENTIFICACION)).extracting(CatalogoItemEntity::getCodigo)
                .containsExactly("NIT", "RUT", "CC", "CE");
        assertThat(itemsDe(CodigoCatalogo.NATURALEZA)).extracting(CatalogoItemEntity::getEtiqueta)
                .containsExactly("1 PERSONA NATURAL", "2 PERSONA JURÍDICA", "3 P JURÍDICA - UNIÓN TEMPORAL o CONSORCIO");
        assertThat(itemsDe(CodigoCatalogo.TIPO_SEGUIMIENTO)).extracting(CatalogoItemEntity::getCodigo)
                .containsExactly("INTERVENTOR", "SUPERVISOR", "INTERVENTOR_SUPERVISOR");
        assertThat(itemsDe(CodigoCatalogo.TIPO_ANTICIPO)).extracting(CatalogoItemEntity::getEtiqueta)
                .containsExactly("1 ANTICIPOS", "2 PAGO ANTICIPADO", "3 NO PACTADOS");
        assertThat(itemsDe(CodigoCatalogo.TIPO_ADICION)).extracting(CatalogoItemEntity::getCodigo)
                .containsExactly("VALOR", "TIEMPO", "VALOR_TIEMPO", "SIN_ADICION");
        assertThat(itemsDe(CodigoCatalogo.FUENTE_FINANCIACION)).extracting(CatalogoItemEntity::getEtiqueta)
                .containsExactly("Funcionamiento", "Inversión", "SGR", "Externos", "Otro");
        assertThat(itemsDe(CodigoCatalogo.FUENTE_FINANCIACION)).allMatch(i -> i.getCodigoSireci() == null);
        assertThat(itemsDe(CodigoCatalogo.ASEGURADORA)).extracting(CatalogoItemEntity::getNombre).containsExactly(
                "SEGUROS MUNDIAL", "SEGUROS DEL ESTADO", "SURA", "ASEGURADORA SOLIDARIA DE COLOMBIA");
        // Ninguna opción de "no se diligencia", ni "N/A" como aseguradora.
        assertThat(catalogo.findAll()).noneMatch(i -> i.getNombre().contains("NO SE DILIGENCIA") || i.getNombre().equals("N/A"));
    }

    @Test
    @Order(17)
    void volverACargarNoDuplicaCatalogosNiItemsNiReactiva() {
        CatalogoItemEntity sura = itemsDe(CodigoCatalogo.ASEGURADORA).stream()
                .filter(i -> i.getCodigo().equals("SURA")).findFirst().orElseThrow();
        sura.setActivo(false);
        catalogo.save(sura);

        volverACargarDataSql();

        assertThat(catalogos.count()).isEqualTo(7);
        assertThat(catalogo.count()).isEqualTo(4 + 3 + 3 + 3 + 4 + 5 + 4);
        assertThat(catalogo.findById(sura.getId()).orElseThrow().getActivo()).as("no se reactiva").isFalse();
    }

    @Test
    @Order(13)
    void cargaLosTiposDeIdentificacionSinElDeNoDiligencia() {
        assertThat(itemsDe(CodigoCatalogo.TIPO_IDENTIFICACION)).extracting(CatalogoItemEntity::getEtiqueta)
                .containsExactly("1 NIT", "2 RUT - REGISTRO ÚNICO TRIBUTARIO", "3 CÉDULA DE CIUDADANÍA", "4 CÉDULA DE EXTRANJERÍA");
    }

    /** Solo lee; transaccional para poder recorrer las relaciones perezosas (tipo de identificación, tercero). */
    @Test
    @Order(14)
    @org.springframework.transaction.annotation.Transactional
    void creaLos8OrdenadoresComoPersonasNaturalesYSoloLos2PeriodosCon2026() {
        String[][] esperados = {
                {"76314448", "FRANCISCO JOSE", "PINO CORREA"}, {"76311561", "DEIBAR RENE", "HURTADO HERRERA"},
                {"34570235", "CIELO", "PEREZ SOLANO"}, {"10535844", "JOSE LUIS", "DIAGO FRANCO"},
                {"34552816", "JANETH", "NOGUERA RAMOS"}, {"7551810", "HECTOR SAMUEL", "VILLADA CASTILLO"},
                {"76321704", "ALEJANDRO", "TOLEDO TOVAR"}, {"15505403", "JUAN CARLOS", "CASAS ZAPATA"}};
        for (String[] e : esperados) {
            PersonaNaturalEntity p = personaPorCedula(e[0]);
            assertThat(p.getNombres()).as(e[0]).isEqualTo(e[1]);
            assertThat(p.getApellidos()).as(e[0]).isEqualTo(e[2]);
            assertThat(p.getNombreCompleto()).isEqualTo(e[1] + " " + e[2]);
            assertThat(p.getTipoIdentificacion().getEtiqueta()).isEqualTo("3 CÉDULA DE CIUDADANÍA");
            assertThat(p.getActivo()).isTrue();
        }
        assertThat(terceros.count()).isEqualTo(8);

        assertThat(ordenadores.findAll()).hasSize(2).allSatisfy(o -> {
            assertThat(o.getFechaDesde()).isEqualTo(java.time.LocalDate.of(2026, 1, 1));
            assertThat(o.getFechaHasta()).isNull();
            assertThat(o.getEsDelegado()).isFalse();
            assertThat(o.getActoDelegacion()).isNull();
            assertThat(o.getActivo()).isTrue();
        });
        assertThat(ordenadores.findAll()).extracting(OrdenadorGastoEntity::getEtiqueta).containsExactlyInAnyOrder(
                "FRANCISCO JOSE PINO CORREA - Vicerrector de Investigaciones",
                "DEIBAR RENE HURTADO HERRERA - Rector");
    }

    @Test
    @Order(15)
    void volverACargarNoDuplicaTercerosNiPeriodosNiRevierteCambios() {
        PersonaNaturalEntity pino = personaPorCedula("76314448");
        pino.setNombres("FRANCISCO JOSÉ");   // la VRI corrige la tilde en el sistema
        terceros.save(pino);
        OrdenadorGastoEntity rector = ordenadores.findAll().stream()
                .filter(o -> o.getCargo().equals("Rector")).findFirst().orElseThrow();
        rector.setFechaHasta(java.time.LocalDate.of(2026, 12, 31));
        ordenadores.save(rector);

        volverACargarDataSql();

        assertThat(terceros.count()).isEqualTo(8);
        assertThat(ordenadores.count()).isEqualTo(2);
        assertThat(itemsDe(CodigoCatalogo.TIPO_IDENTIFICACION)).hasSize(4);
        assertThat(personaPorCedula("76314448").getNombres()).as("no revierte la corrección").isEqualTo("FRANCISCO JOSÉ");
        assertThat(ordenadores.findById(rector.getId()).orElseThrow().getFechaHasta()).isEqualTo(java.time.LocalDate.of(2026, 12, 31));
    }

    @Test
    @Order(8)
    void volverACargarNoDuplicaNiReactivaClases() {
        ClaseContratoEntity factoring = clase(AmbitoClase.CONTRATO, 13);
        factoring.setActivo(false);
        clases.save(factoring);

        volverACargarDataSql();

        assertThat(clases.count()).isEqualTo(32);
        assertThat(clase(AmbitoClase.CONTRATO, 13).getActivo()).as("no se reactiva").isFalse();
    }
}
