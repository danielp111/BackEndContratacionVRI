package co.edu.unicauca.contratacionvri.tipocontrato;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.SerieDocumentalEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.TipoContratoEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTOPeticion.TipoContratoDTOPeticion;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios.UsoTipoContratoChecker;

class TipoContratoRestControllerTest extends PruebaIntegracionBase {

    private static final String URL = "/api/tipos-contrato";

    /**
     * Espía sobre el checker real: por defecto responde lo que hay en la base ("sin contratos" para tipos);
     * en las pruebas de la regla se le hace responder que el tipo ya tiene contratos.
     */
    @MockitoSpyBean
    private UsoTipoContratoChecker usoChecker;

    private SerieDocumentalEntity ops;
    private SerieDocumentalEntity formacion;

    @BeforeEach
    void series() {
        ops = serie("6.1-17.13");
        formacion = serie("6.1-17.3");
    }

    private ResultActions enviar(String metodo, String url, Object cuerpo) throws Exception {
        var peticion = "POST".equals(metodo) ? post(url) : put(url);
        String texto = cuerpo instanceof String s ? s : json.writeValueAsString(cuerpo);
        return mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(texto));
    }

    @Test
    void creaConElNombreTalComoSeEscribeConTildes() throws Exception {
        enviar("POST", URL, new TipoContratoDTOPeticion("  Pasantía  ", formacion.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Pasantía"))
                .andExpect(jsonPath("$.serieId").value(formacion.getId()))
                .andExpect(jsonPath("$.serieCodigo").value("6.1-17.3"))
                .andExpect(jsonPath("$.esConvenio").value(false))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.nombreYSerieEditables").value(true));
    }

    @Test
    void variosTiposPuedenCompartirSerie() throws Exception {
        crear(URL, new TipoContratoDTOPeticion("Aprendizaje", formacion.getId()));
        crear(URL, new TipoContratoDTOPeticion("Pasantía", formacion.getId()));
        crear(URL, new TipoContratoDTOPeticion("Judicatura", formacion.getId()));

        mvc.perform(get(URL).param("serieId", formacion.getId().toString()))
                .andExpect(jsonPath("$[*].nombre", contains("Aprendizaje", "Judicatura", "Pasantía")));
    }

    @Test
    void nombreRepetidoSinImportarMayusculasResponde409() throws Exception {
        tipoContrato("Pasantía", formacion);
        enviar("POST", URL, new TipoContratoDTOPeticion("PASANTÍA", ops.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigoError").value("GC-0002"));
    }

    @Test
    void validaLosCampos() throws Exception {
        enviar("POST", URL, "{\"nombre\": \"\", \"serieId\": null}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").value("El nombre del tipo de contrato es obligatorio"))
                .andExpect(jsonPath("$.errores.serieId").value("La serie documental es obligatoria"));
    }

    @Test
    void laSerieDebeExistirYEstarActiva() throws Exception {
        enviar("POST", URL, new TipoContratoDTOPeticion("OPS", 999_999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La serie documental con id 999999 no existe"));

        ops.setActivo(false);
        serieRepositorio.save(ops);
        enviar("POST", URL, new TipoContratoDTOPeticion("OPS", ops.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La serie documental 6.1-17.13 está inactiva"));
    }

    @Test
    void actualizaNombreYSerieSiNoHayContratos() throws Exception {
        TipoContratoEntity tipo = tipoContrato("Pasantia", ops);

        enviar("PUT", URL + "/" + tipo.getId(), new TipoContratoDTOPeticion("Pasantía", formacion.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pasantía"))
                .andExpect(jsonPath("$.serieCodigo").value("6.1-17.3"));
    }

    @Test
    void noPermiteCambiarNombreNiSerieSiYaHayContratosDelTipo() throws Exception {
        TipoContratoEntity tipo = tipoContrato("OPS", ops);
        doReturn(true).when(usoChecker).existenContratosConTipo(tipo.getId());

        mvc.perform(get(URL + "/" + tipo.getId())).andExpect(jsonPath("$.nombreYSerieEditables").value(false));

        enviar("PUT", URL + "/" + tipo.getId(), new TipoContratoDTOPeticion("Prestación de servicios", ops.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0004"))
                .andExpect(jsonPath("$.mensaje").value(containsString("no se pueden cambiar")));
        enviar("PUT", URL + "/" + tipo.getId(), new TipoContratoDTOPeticion("OPS", formacion.getId()))
                .andExpect(status().isBadRequest());

        // Sin cambios de nombre ni de serie se acepta (no hay nada que proteger).
        enviar("PUT", URL + "/" + tipo.getId(), new TipoContratoDTOPeticion("OPS", ops.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarAUnaSerieInactivaSeRechazaPeroConservarlaNo() throws Exception {
        TipoContratoEntity tipo = tipoContrato("OPS", ops);
        formacion.setActivo(false);
        serieRepositorio.save(formacion);

        enviar("PUT", URL + "/" + tipo.getId(), new TipoContratoDTOPeticion("OPS", formacion.getId()))
                .andExpect(status().isBadRequest());

        ops.setActivo(false);
        serieRepositorio.save(ops);
        enviar("PUT", URL + "/" + tipo.getId(), new TipoContratoDTOPeticion("OPS (servicios)", ops.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void activosSoloOfreceTiposActivosConSerieActiva() throws Exception {
        tipoContrato("OPS", ops);
        tipoContrato("Pasantía", formacion);
        TipoContratoEntity inactivo = tipoContrato("Judicatura", formacion);
        SerieDocumentalEntity cerrada = serie("6.1-17.99");
        tipoContrato("Tipo de serie cerrada", cerrada);

        mvc.perform(patch(URL + "/" + inactivo.getId() + "/estado").param("activo", "false")).andExpect(status().isOk());
        mvc.perform(patch("/api/series-documentales/" + cerrada.getId() + "/estado").param("activo", "false"))
                .andExpect(status().isOk());

        mvc.perform(get(URL + "/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre", contains("OPS", "Pasantía")))
                .andExpect(jsonPath("$[1].etiqueta").value("Pasantía (6.1-17.3)"))
                .andExpect(jsonPath("$[1].serieCodigo").value("6.1-17.3"));

        // El listado general sí los muestra todos y filtra por estado del tipo.
        mvc.perform(get(URL)).andExpect(jsonPath("$.length()").value(4));
        mvc.perform(get(URL).param("activo", "false")).andExpect(jsonPath("$[*].nombre", containsInAnyOrder("Judicatura")));
    }

    @Test
    void inactivaYReactiva() throws Exception {
        TipoContratoEntity tipo = tipoContrato("OPS", ops);
        mvc.perform(patch(URL + "/" + tipo.getId() + "/estado").param("activo", "false"))
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(patch(URL + "/" + tipo.getId() + "/estado").param("activo", "true"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void erroresHttp() throws Exception {
        mvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoError").value("GC-0003"));
        enviar("PUT", URL + "/999999", new TipoContratoDTOPeticion("X", ops.getId())).andExpect(status().isNotFound());
        mvc.perform(get(URL + "/0")).andExpect(status().isBadRequest());
        mvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("OPS"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
