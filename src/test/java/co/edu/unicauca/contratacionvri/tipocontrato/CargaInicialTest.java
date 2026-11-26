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
