package co.edu.unicauca.contratacionvri.tipocontrato;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.SerieDocumentalEntity;

class SerieDocumentalRestControllerTest extends PruebaIntegracionBase {

    private static final String URL = "/api/series-documentales";

    private ResultActions enviar(String metodo, String url, String cuerpo) throws Exception {
        var peticion = "POST".equals(metodo) ? post(url) : put(url);
        return mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    void creaYCalculaEsConvenioSegunElCodigoIgnorandoLoEnviado() throws Exception {
        enviar("POST", URL, "{\"codigo\": \" 6.1-17.13 \", \"esConvenio\": true}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("6.1-17.13"))
                .andExpect(jsonPath("$.esConvenio").value(false))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.codigoEditable").value(true));

        enviar("POST", URL, "{\"codigo\": \"6.1-18.4\", \"esConvenio\": false}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.esConvenio").value(true));
    }

    @Test
    void codigoRepetidoResponde409() throws Exception {
        serie("6.1-17.13");
        enviar("POST", URL, "{\"codigo\": \"6.1-17.13\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigoError").value("GC-0002"))
                .andExpect(jsonPath("$.mensaje").value("Ya existe la serie documental 6.1-17.13"));
    }

    @Test
    void codigoConFormatoInvalidoResponde400() throws Exception {
        for (String invalido : new String[]{"OPS", "6.1/17.13", "6.1-", "-17.13", "6.1-17.13a", ""}) {
            enviar("POST", URL, "{\"codigo\": \"" + invalido + "\"}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errores.codigo").exists());
        }
    }

    @Test
    void cambiarElCodigoSinNumerosEmitidosRecalculaEsConvenio() throws Exception {
        SerieDocumentalEntity s = serie("6.1-17.20");
        consecutivo(s, 2026, 0); // una fila en 0 no cuenta como uso

        enviar("PUT", URL + "/" + s.getId(), "{\"codigo\": \"6.1-18.5\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("6.1-18.5"))
                .andExpect(jsonPath("$.esConvenio").value(true));
    }

    @Test
    void noPermiteCambiarElCodigoSiYaHayContratosNumerados() throws Exception {
        SerieDocumentalEntity s = serie("6.1-17.13");
        consecutivo(s, 2025, 40);

        mvc.perform(get(URL + "/" + s.getId())).andExpect(jsonPath("$.codigoEditable").value(false));
        enviar("PUT", URL + "/" + s.getId(), "{\"codigo\": \"6.1-17.14\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0004"))
                .andExpect(jsonPath("$.mensaje").value(containsString("no se puede cambiar")));

        // Reenviar el mismo código no es un cambio: se acepta.
        enviar("PUT", URL + "/" + s.getId(), "{\"codigo\": \"6.1-17.13\"}").andExpect(status().isOk());
    }

    @Test
    void cambiarAlCodigoDeOtraSerieResponde409() throws Exception {
        serie("6.1-17.13");
        SerieDocumentalEntity otra = serie("6.1-17.15");
        enviar("PUT", URL + "/" + otra.getId(), "{\"codigo\": \"6.1-17.13\"}").andExpect(status().isConflict());
    }

    @Test
    void inactivaActivaYFiltra() throws Exception {
        SerieDocumentalEntity a = serie("6.1-17.13");
        serie("6.1-17.15");

        mvc.perform(patch(URL + "/" + a.getId() + "/estado").param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(get(URL).param("activo", "true")).andExpect(jsonPath("$[*].codigo", contains("6.1-17.15")));
        mvc.perform(get(URL).param("activo", "false")).andExpect(jsonPath("$[*].codigo", contains("6.1-17.13")));
        mvc.perform(get(URL)).andExpect(jsonPath("$.length()").value(2));

        mvc.perform(patch(URL + "/" + a.getId() + "/estado").param("activo", "true"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void erroresHttp() throws Exception {
        mvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoError").value("GC-0003"));
        enviar("PUT", URL + "/999999", "{\"codigo\": \"6.1-17.13\"}").andExpect(status().isNotFound());
        mvc.perform(patch(URL + "/999999/estado").param("activo", "false")).andExpect(status().isNotFound());
        mvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("6.1-17.13"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigoError").value("GC-0010"));
    }
}
