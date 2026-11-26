package co.edu.unicauca.contratacionvri.clasecontrato;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;
import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.AmbitoClase;
import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.ClaseContratoEntity;
import co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.servicios.IClaseContratoServicio;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;

class ClaseContratoRestControllerTest extends PruebaIntegracionBase {

    private static final String URL = "/api/clases-contrato";

    @Autowired
    private IClaseContratoServicio servicio;

    private ResultActions enviar(String metodo, String url, String cuerpo) throws Exception {
        var peticion = "POST".equals(metodo) ? post(url) : put(url);
        return mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    // ------------------------------------------------------------------ crear

    @Test
    void creaConCodigoYNombreSeparadosYEtiquetaParaSelectores() throws Exception {
        enviar("POST", URL, "{\"codigoSireci\": 23, \"nombre\": \"  PRESTACIÓN   DE SERVICIOS \", \"ambito\": \"CONTRATO\"}")
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.codigoSireci").value(23))
                .andExpect(jsonPath("$.nombre").value("PRESTACIÓN DE SERVICIOS"))
                .andExpect(jsonPath("$.ambito").value("CONTRATO"))
                .andExpect(jsonPath("$.etiqueta").value("23 PRESTACIÓN DE SERVICIOS"))
                .andExpect(jsonPath("$.esOtros").value(false))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void elCodigoEsUnicoPorAmbitoYLosConveniosReinicianEn1() throws Exception {
        clase(AmbitoClase.CONTRATO, 1, "AGENCIA");

        enviar("POST", URL, "{\"codigoSireci\": 1, \"nombre\": \"OTRA AGENCIA\", \"ambito\": \"CONTRATO\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigoError").value("GC-0002"))
                .andExpect(jsonPath("$.mensaje").value("Ya existe la clase 1 en el ámbito CONTRATO"));

        enviar("POST", URL, "{\"codigoSireci\": 1, \"nombre\": \"CONTRATO / CONVENIO INTERADMINISTRATIVO\", \"ambito\": \"CONVENIO\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.etiqueta").value("1 CONTRATO / CONVENIO INTERADMINISTRATIVO"));
    }

    @Test
    void elCodigoDeNoDiligenciaNoEsUnaClase() throws Exception {
        enviar("POST", URL, "{\"codigoSireci\": 99999998, \"nombre\": \"NO SE DILIGENCIA INFORMACIÓN\", \"ambito\": \"CONTRATO\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0004"))
                .andExpect(jsonPath("$.mensaje").value(containsString("pertenece al reporte SIRECI")));
        assertThat(claseContratoRepositorio.count()).isZero();
    }

    @Test
    void validaLosCampos() throws Exception {
        enviar("POST", URL, "{\"codigoSireci\": 0, \"nombre\": \"\", \"ambito\": null}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0005"))
                .andExpect(jsonPath("$.errores.codigoSireci").value("El código SIRECI debe ser un número positivo"))
                .andExpect(jsonPath("$.errores.nombre").value("El nombre de la clase es obligatorio"))
                .andExpect(jsonPath("$.errores.ambito").value("El ámbito es obligatorio (CONTRATO o CONVENIO)"));

        enviar("POST", URL, "{\"nombre\": \"X\", \"ambito\": \"CONTRATO\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.codigoSireci").value("El código SIRECI es obligatorio"));

        // Ámbito que no existe: el JSON no se puede leer.
        enviar("POST", URL, "{\"codigoSireci\": 5, \"nombre\": \"X\", \"ambito\": \"ACUERDO\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0006"));
    }

    // ------------------------------------------------------------------ esOtros

    @Test
    void esOtrosSoloParaEl30DeContratos() throws Exception {
        ClaseContratoEntity otros = clase(AmbitoClase.CONTRATO, 30, "OTROS");
        ClaseContratoEntity treintaConvenio = clase(AmbitoClase.CONVENIO, 30, "CONVENIO 30");
        ClaseContratoEntity prestacion = clase(AmbitoClase.CONTRATO, 23, "PRESTACIÓN DE SERVICIOS");

        assertThat(otros.esOtros()).isTrue();
        assertThat(treintaConvenio.esOtros()).isFalse();
        assertThat(prestacion.esOtros()).isFalse();

        mvc.perform(get(URL + "/" + otros.getId())).andExpect(jsonPath("$.esOtros").value(true));
        mvc.perform(get(URL + "/" + treintaConvenio.getId())).andExpect(jsonPath("$.esOtros").value(false));
    }

    // ------------------------------------------------------------------ actualizar

    @Test
    void actualizaSoloElNombre() throws Exception {
        ClaseContratoEntity c = clase(AmbitoClase.CONTRATO, 4, "COMISION");

        enviar("PUT", URL + "/" + c.getId(), "{\"nombre\": \"COMISIÓN\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("COMISIÓN"))
                .andExpect(jsonPath("$.codigoSireci").value(4))
                .andExpect(jsonPath("$.ambito").value("CONTRATO"))
                .andExpect(jsonPath("$.etiqueta").value("4 COMISIÓN"));
    }

    @Test
    void elCodigoYElAmbitoNoSePuedenCambiar() throws Exception {
        ClaseContratoEntity c = clase(AmbitoClase.CONTRATO, 4, "COMISIÓN");
        String url = URL + "/" + c.getId();

        enviar("PUT", url, "{\"nombre\": \"COMISIÓN\", \"codigoSireci\": 5}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("El código SIRECI de una clase no se puede cambiar")));
        enviar("PUT", url, "{\"nombre\": \"COMISIÓN\", \"ambito\": \"CONVENIO\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("El ámbito de una clase no se puede cambiar")));

        // Los mismos valores se aceptan (clientes que reenvían el objeto completo).
        enviar("PUT", url, "{\"nombre\": \"COMISIÓN\", \"codigoSireci\": 4, \"ambito\": \"CONTRATO\"}")
                .andExpect(status().isOk());

        ClaseContratoEntity guardada = claseContratoRepositorio.findById(c.getId()).orElseThrow();
        assertThat(guardada.getCodigoSireci()).isEqualTo(4);
        assertThat(guardada.getAmbito()).isEqualTo(AmbitoClase.CONTRATO);
    }

    // ------------------------------------------------------------------ listar y estado

    @Test
    void listaFiltrandoPorAmbitoYEstadoOrdenadoPorCodigo() throws Exception {
        clase(AmbitoClase.CONTRATO, 23, "PRESTACIÓN DE SERVICIOS");
        clase(AmbitoClase.CONTRATO, 9, "CONSULTORÍA");
        ClaseContratoEntity inactiva = clase(AmbitoClase.CONTRATO, 13, "FACTORING");
        clase(AmbitoClase.CONVENIO, 2, "CONVENIO DE COOPERACIÓN (NACIONAL / INTERNACIONAL)");
        clase(AmbitoClase.CONVENIO, 1, "CONTRATO / CONVENIO INTERADMINISTRATIVO");
        mvc.perform(patch(URL + "/" + inactiva.getId() + "/estado").param("activo", "false")).andExpect(status().isOk());

        mvc.perform(get(URL))
                .andExpect(jsonPath("$[*].etiqueta", contains("9 CONSULTORÍA", "13 FACTORING", "23 PRESTACIÓN DE SERVICIOS",
                        "1 CONTRATO / CONVENIO INTERADMINISTRATIVO", "2 CONVENIO DE COOPERACIÓN (NACIONAL / INTERNACIONAL)")));
        mvc.perform(get(URL).param("ambito", "CONTRATO").param("activo", "true"))
                .andExpect(jsonPath("$[*].codigoSireci", contains(9, 23)));
        mvc.perform(get(URL).param("ambito", "CONVENIO"))
                .andExpect(jsonPath("$[*].codigoSireci", contains(1, 2)));
        mvc.perform(get(URL).param("activo", "false"))
                .andExpect(jsonPath("$[*].codigoSireci", contains(13)));
        mvc.perform(get(URL).param("ambito", "ACUERDO")).andExpect(status().isBadRequest());
    }

    @Test
    void inactivaYReactiva() throws Exception {
        ClaseContratoEntity c = clase(AmbitoClase.CONTRATO, 23, "PRESTACIÓN DE SERVICIOS");
        mvc.perform(patch(URL + "/" + c.getId() + "/estado").param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(patch(URL + "/" + c.getId() + "/estado").param("activo", "true"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void unaClaseInactivaNoSirveParaContratosNuevosPeroSiParaLosQueYaLaTienen() {
        ClaseContratoEntity c = clase(AmbitoClase.CONTRATO, 23, "PRESTACIÓN DE SERVICIOS");
        servicio.cambiarEstado(c.getId(), false);

        assertThatThrownBy(() -> servicio.obtenerClaseValida(c.getId(), null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("'23 PRESTACIÓN DE SERVICIOS' está inactiva");
        // Un contrato que ya la tenía la conserva.
        ClaseContratoEntity actual = claseContratoRepositorio.findById(c.getId()).orElseThrow();
        assertThat(servicio.obtenerClaseValida(c.getId(), actual)).isSameAs(actual);
        assertThatThrownBy(() -> servicio.obtenerClaseValida(999_999L, null))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("no existe");
    }

    // ------------------------------------------------------------------ errores HTTP

    @Test
    void erroresHttp() throws Exception {
        mvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoError").value("GC-0003"));
        enviar("PUT", URL + "/999999", "{\"nombre\": \"X\"}").andExpect(status().isNotFound());
        mvc.perform(patch(URL + "/999999/estado").param("activo", "false")).andExpect(status().isNotFound());
        mvc.perform(get(URL + "/0")).andExpect(status().isBadRequest());
        mvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("23 PRESTACIÓN DE SERVICIOS"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigoError").value("GC-0010"));
    }
}
