package co.edu.unicauca.contratacionvri.contrato;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContratoObservacionTest extends ContratoPruebaBase {

    private long contrato;
    private String url;

    @BeforeEach
    void contrato() throws Exception {
        contrato = crearContratoId(minimo());
        url = URL + "/" + contrato + "/observaciones";
    }

    private long observar(String campo, String texto) throws Exception {
        return json.readTree(enviar("POST", url, Map.of("campo", campo, "texto", texto))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void listaLosCamposObservablesConSuEtiqueta() throws Exception {
        mvc.perform(get(URL + "/campos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("TIPO_CONTRATO"))
                .andExpect(jsonPath("$[*].codigo", hasItem("VALOR_SIN_IVA")))
                .andExpect(jsonPath("$[?(@.codigo == 'VALOR_SIN_IVA')].etiqueta", contains("Valor sin IVA")));
    }

    @Test
    void agregaUnaObservacionPendienteAUnCampo() throws Exception {
        enviar("POST", url, Map.of("campo", "valor_sin_iva", "texto", "  No coincide   con el CDP 1234 "))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contratoId").value(contrato))
                .andExpect(jsonPath("$.campo").value("VALOR_SIN_IVA"))
                .andExpect(jsonPath("$.campoEtiqueta").value("Valor sin IVA"))
                .andExpect(jsonPath("$.texto").value("No coincide con el CDP 1234"))
                .andExpect(jsonPath("$.resuelta").value(false))
                .andExpect(jsonPath("$.creadoEn").value(notNullValue()))
                .andExpect(jsonPath("$.resueltaEn").value(nullValue()));
        mvc.perform(get(URL + "/" + contrato)).andExpect(jsonPath("$.observacionesPendientes").value(1));
    }

    @Test
    void validaElCampoElTextoYElContrato() throws Exception {
        enviar("POST", url, Map.of("campo", "COLOR", "texto", "x"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "El campo 'COLOR' no existe en el contrato; los campos válidos los da GET /api/contratos/campos"));
        enviar("POST", url, Map.of("campo", "OBJETO", "texto", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.texto").value("El texto de la observación es obligatorio"));
        enviar("POST", url, Map.of("campo", "OBJETO", "texto", "x".repeat(391)))
                .andExpect(jsonPath("$.errores.texto").value("La observación admite máximo 390 caracteres"));
        enviar("POST", URL + "/999999/observaciones", Map.of("campo", "OBJETO", "texto", "x"))
                .andExpect(status().isNotFound());
        mvc.perform(get(URL + "/999999/observaciones")).andExpect(status().isNotFound());
    }

    @Test
    void listaPorCampoYEstadoLaMasRecientePrimero() throws Exception {
        long primera = observar("VALOR_SIN_IVA", "Primera");
        long segunda = observar("VALOR_SIN_IVA", "Segunda");
        long objeto = observar("OBJETO", "Del objeto");
        enviar("PATCH", url + "/" + primera + "/resolver", Map.of()).andExpect(status().isOk());

        mvc.perform(get(url).param("campo", "VALOR_SIN_IVA"))
                .andExpect(jsonPath("$[*].id", contains((int) segunda, (int) primera)));
        mvc.perform(get(url).param("resuelta", "false"))
                .andExpect(jsonPath("$[*].id", contains((int) objeto, (int) segunda)));
        mvc.perform(get(url)).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get(url).param("campo", "COLOR")).andExpect(status().isBadRequest());
    }

    @Test
    void resuelveUnaSolaVez() throws Exception {
        long id = observar("IVA", "Confirmar IVA");
        enviar("PATCH", url + "/" + id + "/resolver", Map.of("respuesta", "Se confirmó con el financiero"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resuelta").value(true))
                .andExpect(jsonPath("$.respuesta").value("Se confirmó con el financiero"))
                .andExpect(jsonPath("$.resueltaEn").value(notNullValue()));
        enviar("PATCH", url + "/" + id + "/resolver", Map.of())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La observación " + id + " ya está resuelta"));

        long otro = crearContratoId(minimo());
        enviar("PATCH", URL + "/" + otro + "/observaciones/" + id + "/resolver", Map.of())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la observación " + id + " en el contrato con id " + otro));
    }

    @Test
    void cuentaPorCampoEnElOrdenDelFormulario() throws Exception {
        mvc.perform(get(url + "/conteo"))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.pendientes").value(0));

        long resuelta = observar("VALOR_SIN_IVA", "Primera");
        observar("VALOR_SIN_IVA", "Segunda");
        observar("VALOR_SIN_IVA", "Tercera");
        observar("OBJETO", "Del objeto");
        enviar("PATCH", url + "/" + resuelta + "/resolver", Map.of()).andExpect(status().isOk());

        mvc.perform(get(url + "/conteo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.pendientes").value(3))
                .andExpect(jsonPath("$.porCampo.OBJETO.total").value(1))
                .andExpect(jsonPath("$.porCampo.OBJETO.pendientes").value(1))
                .andExpect(jsonPath("$.porCampo.VALOR_SIN_IVA.total").value(3))
                .andExpect(jsonPath("$.porCampo.VALOR_SIN_IVA.pendientes").value(2))
                .andExpect(jsonPath("$.porCampo.IVA").doesNotExist());
        // OBJETO va antes que VALOR_SIN_IVA en el formulario.
        String cuerpo = mvc.perform(get(url + "/conteo")).andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(cuerpo.indexOf("OBJETO")).isLessThan(cuerpo.indexOf("VALOR_SIN_IVA"));
    }

    @Test
    void unContratoAnuladoNoAdmiteObservacionesNuevasPeroSeLeenYResuelven() throws Exception {
        long id = observar("OBJETO", "Antes de anular");
        enviar("PATCH", URL + "/" + contrato + "/anular", Map.of("motivo", "No se usó")).andExpect(status().isOk());
        enviar("POST", url, Map.of("campo", "OBJETO", "texto", "Después"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El contrato 6.1-17.13/001 está anulado y no admite observaciones nuevas"));
        mvc.perform(get(url)).andExpect(jsonPath("$.length()").value(1));
        enviar("PATCH", url + "/" + id + "/resolver", Map.of()).andExpect(status().isOk());
    }
}
