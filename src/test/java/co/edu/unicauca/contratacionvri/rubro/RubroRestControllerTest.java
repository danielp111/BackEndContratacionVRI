package co.edu.unicauca.contratacionvri.rubro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import co.edu.unicauca.contratacionvri.rubro.accesoADatos.modelos.RubroEntity;
import co.edu.unicauca.contratacionvri.rubro.fachadaServicios.servicios.IRubroServicio;
import co.edu.unicauca.contratacionvri.rubro.fachadaServicios.servicios.UsoRubroChecker;

class RubroRestControllerTest extends PruebaIntegracionBase {

    private static final String URL = "/api/rubros";

    /** Espía sobre el checker real: por defecto "sin contratos" (la prueba no crea contratos); en la prueba de la regla, "con contratos". */
    @MockitoSpyBean
    private UsoRubroChecker usoChecker;

    @Autowired
    private IRubroServicio servicio;

    private ResultActions enviar(String metodo, String url, String cuerpo) throws Exception {
        var peticion = "POST".equals(metodo) ? post(url) : put(url);
        return mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    // ------------------------------------------------------------------ crear

    @Test
    void creaConTextosNormalizadosCodigoComoTextoYEtiqueta() throws Exception {
        enviar("POST", URL, "{\"codigo\": \"  0012.3-04 \", \"nombre\": \"  Museo   de Historia  Natural \"}")
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.codigo").value("0012.3-04"))
                .andExpect(jsonPath("$.nombre").value("Museo de Historia Natural"))
                .andExpect(jsonPath("$.etiqueta").value("0012.3-04 - Museo de Historia Natural"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.codigoEditable").value(true));
    }

    @Test
    void elCodigoEsUnicoPeroElNombrePuedeRepetirse() throws Exception {
        rubro("9011141", "Servicios prestados a Empresas y");

        enviar("POST", URL, "{\"codigo\": \"9011141\", \"nombre\": \"Otro nombre\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigoError").value("GC-0002"))
                .andExpect(jsonPath("$.mensaje").value("Ya existe el rubro con código 9011141"));

        // El Excel tiene dos rubros con el mismo nombre: se permite.
        enviar("POST", URL, "{\"codigo\": \"9011044001\", \"nombre\": \"Servicios prestados a Empresas y\"}")
                .andExpect(status().isCreated());
    }

    @Test
    void validaElCodigoYElNombre() throws Exception {
        enviar("POST", URL, "{\"codigo\": \"\", \"nombre\": \"   \"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0005"))
                .andExpect(jsonPath("$.errores.codigo").value("El código del rubro es obligatorio"))
                .andExpect(jsonPath("$.errores.nombre").value("El nombre del rubro es obligatorio"));

        for (String invalido : new String[]{"10 37", "1037/008", ".1037", "1037-", "1037..8"}) {
            enviar("POST", URL, "{\"codigo\": \"" + invalido + "\", \"nombre\": \"X\"}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errores.codigo").value(containsString("solo admite números y letras")));
        }

        String largo = "x".repeat(301);
        enviar("POST", URL, "{\"codigo\": \"1\", \"nombre\": \"" + largo + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").value("El nombre admite máximo 300 caracteres"));
        assertThat(rubroRepositorio.count()).isZero();
    }

    // ------------------------------------------------------------------ actualizar

    @Test
    void actualizaCodigoYNombreSiNoHayContratos() throws Exception {
        RubroEntity r = rubro("1037008", "Fresa");
        enviar("PUT", URL + "/" + r.getId(), "{\"codigo\": \"1037009\", \"nombre\": \"Cadena productiva de la fresa\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("1037009"))
                .andExpect(jsonPath("$.etiqueta").value("1037009 - Cadena productiva de la fresa"));
    }

    @Test
    void noPermiteCambiarElCodigoSiYaHayContratos() throws Exception {
        RubroEntity r = rubro("1037008", "Fresa");
        doReturn(true).when(usoChecker).existenContratosConRubro(r.getId());

        mvc.perform(get(URL + "/" + r.getId())).andExpect(jsonPath("$.codigoEditable").value(false));
        enviar("PUT", URL + "/" + r.getId(), "{\"codigo\": \"1037009\", \"nombre\": \"Fresa\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0004"))
                .andExpect(jsonPath("$.mensaje").value("El código del rubro 1037008 no se puede cambiar: ya hay contratos que lo usan"));

        // El nombre sí se puede cambiar.
        enviar("PUT", URL + "/" + r.getId(), "{\"codigo\": \"1037008\", \"nombre\": \"Cadena de la fresa\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cadena de la fresa"));
    }

    @Test
    void cambiarAlCodigoDeOtroRubroResponde409() throws Exception {
        rubro("1031", "Dirección VRI");
        RubroEntity otro = rubro("1034001", "Jóvenes investigadores");
        enviar("PUT", URL + "/" + otro.getId(), "{\"codigo\": \"1031\", \"nombre\": \"X\"}").andExpect(status().isConflict());
    }

    // ------------------------------------------------------------------ buscar

    @Nested
    class Buscar {

        @BeforeEach
        void datos() {
            rubro("1052002", "Museo de Historia Natural");
            rubro("1031", "Dirección Vice-rectoría Investigaciones");
            rubro("9011044001", "Servicios prestados a Empresas y");
            rubro("1027008", "Desarrollo de consultorías");
            RubroEntity inactivo = rubro("1037008", "Investigación de la fresa");
            inactivo.setActivo(false);
            rubroRepositorio.save(inactivo);
        }

        @Test
        void ordenaPorCodigoNumericoComoLaListaDelExcel() throws Exception {
            mvc.perform(get(URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contenido[*].codigo", contains("1031", "1027008", "1037008", "1052002", "9011044001")))
                    .andExpect(jsonPath("$.totalElementos").value(5));
        }

        @Test
        void buscaPorPrefijoDelCodigoOPorNombre() throws Exception {
            mvc.perform(get(URL).param("texto", "10"))
                    .andExpect(jsonPath("$.contenido[*].codigo", contains("1031", "1027008", "1037008", "1052002")));
            // "052" está dentro de 1052002 pero no al inicio: no coincide por código.
            mvc.perform(get(URL).param("texto", "052")).andExpect(jsonPath("$.totalElementos").value(0));
            mvc.perform(get(URL).param("texto", "MUSEO"))
                    .andExpect(jsonPath("$.contenido[*].codigo", contains("1052002")));
            mvc.perform(get(URL).param("texto", "%")).andExpect(jsonPath("$.totalElementos").value(0));
        }

        @Test
        void filtraSoloActivosYPagina() throws Exception {
            mvc.perform(get(URL).param("activo", "true"))
                    .andExpect(jsonPath("$.contenido[*].codigo", contains("1031", "1027008", "1052002", "9011044001")));
            mvc.perform(get(URL).param("activo", "false"))
                    .andExpect(jsonPath("$.contenido[*].codigo", contains("1037008")));
            mvc.perform(get(URL).param("tamanio", "2").param("pagina", "1"))
                    .andExpect(jsonPath("$.contenido[*].codigo", contains("1037008", "1052002")))
                    .andExpect(jsonPath("$.totalPaginas").value(3))
                    .andExpect(jsonPath("$.primera").value(false));
            mvc.perform(get(URL).param("tamanio", "101")).andExpect(status().isBadRequest());
        }
    }

    // ------------------------------------------------------------------ estado

    @Test
    void inactivaYReactiva() throws Exception {
        RubroEntity r = rubro("1031", "Dirección VRI");
        mvc.perform(patch(URL + "/" + r.getId() + "/estado").param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(patch(URL + "/" + r.getId() + "/estado").param("activo", "true"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void unRubroInactivoNoSirveParaContratosNuevosPeroSiParaLosQueYaLoTienen() {
        RubroEntity r = rubro("1031", "Dirección VRI");
        servicio.cambiarEstado(r.getId(), false);

        assertThatThrownBy(() -> servicio.obtenerRubroValido(r.getId(), null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("1031 - Dirección VRI está inactivo");
        RubroEntity actual = rubroRepositorio.findById(r.getId()).orElseThrow();
        assertThat(servicio.obtenerRubroValido(r.getId(), actual)).isSameAs(actual);
        assertThatThrownBy(() -> servicio.obtenerRubroValido(999_999L, null))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("no existe");
    }

    // ------------------------------------------------------------------ errores HTTP

    @Test
    void erroresHttp() throws Exception {
        mvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoError").value("GC-0003"));
        enviar("PUT", URL + "/999999", "{\"codigo\": \"1\", \"nombre\": \"X\"}").andExpect(status().isNotFound());
        mvc.perform(patch(URL + "/999999/estado").param("activo", "false")).andExpect(status().isNotFound());
        mvc.perform(get(URL + "/0")).andExpect(status().isBadRequest());
        mvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("1031"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigoError").value("GC-0010"));
    }
}
