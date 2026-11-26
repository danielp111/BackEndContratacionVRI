package co.edu.unicauca.contratacionvri.catalogo;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.Catalogo;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoItemDTOPeticion;

class CatalogoItemRestControllerTest extends PruebaIntegracionBase {

    private static final String URL = "/api/catalogo-items";

    private CatalogoItemDTOPeticion peticion(Catalogo catalogo, String codigo, String nombre) {
        return new CatalogoItemDTOPeticion(catalogo, codigo, nombre);
    }

    @Test
    void creaItemEnMayusculasConEtiqueta() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(peticion(Catalogo.TIPO_IDENTIFICACION, " 3 ", " cédula de  ciudadanía "))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.catalogo").value("TIPO_IDENTIFICACION"))
                .andExpect(jsonPath("$.codigoSireci").value("3"))
                .andExpect(jsonPath("$.nombre").value("CÉDULA DE CIUDADANÍA"))
                .andExpect(jsonPath("$.etiqueta").value("3 CÉDULA DE CIUDADANÍA"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void elCodigoEsUnicoPorCatalogoNoGlobal() throws Exception {
        crear(URL, peticion(Catalogo.TIPO_IDENTIFICACION, "1", "NIT"));
        crear(URL, peticion(Catalogo.NATURALEZA, "1", "PERSONA NATURAL"));

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(peticion(Catalogo.TIPO_IDENTIFICACION, "1", "OTRO"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigoError").value("GC-0002"));
    }

    @Test
    void filtraPorCatalogoYEstadoYOrdenaPorCodigoNumerico() throws Exception {
        crear(URL, peticion(Catalogo.TIPO_IDENTIFICACION, "10", "DIEZ"));
        crear(URL, peticion(Catalogo.TIPO_IDENTIFICACION, "2", "DOS"));
        long inactivo = crear(URL, peticion(Catalogo.TIPO_IDENTIFICACION, "1", "UNO"));
        crear(URL, peticion(Catalogo.ASEGURADORA, "1", "SURA"));
        mvc.perform(patch(URL + "/" + inactivo + "/estado").param("activo", "false")).andExpect(status().isOk());

        mvc.perform(get(URL).param("catalogo", "TIPO_IDENTIFICACION").param("activo", "true"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].codigoSireci", contains("2", "10")));
        mvc.perform(get(URL)).andExpect(jsonPath("$", hasSize(4)));
        mvc.perform(get(URL).param("catalogo", "NO_EXISTE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void noPermiteCambiarElCatalogoDeUnItem() throws Exception {
        long id = crear(URL, peticion(Catalogo.TIPO_ANTICIPO, "1", "NO PACTADOS"));

        mvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(peticion(Catalogo.TIPO_ADICION, "1", "NO PACTADOS"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0004"));

        mvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(peticion(Catalogo.TIPO_ANTICIPO, "1", "No pactados"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etiqueta").value("1 NO PACTADOS"));
    }
}
