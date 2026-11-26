package co.edu.unicauca.contratacionvri.dependencia;

import static org.hamcrest.Matchers.contains;
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
import co.edu.unicauca.contratacionvri.dependencia.fachadaServicios.DTOPeticion.DependenciaDTOPeticion;

class DependenciaRestControllerTest extends PruebaIntegracionBase {

    private static final String URL = "/api/dependencias";

    private ResultActions enviar(String metodo, String url, Object cuerpo) throws Exception {
        var peticion = "POST".equals(metodo) ? post(url) : put(url);
        String texto = cuerpo instanceof String s ? s : json.writeValueAsString(cuerpo);
        return mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(texto));
    }

    /** 1. El nombre se guarda tal como se escribe (antes se pasaba a mayúsculas); solo se limpian espacios. */
    @Test
    void creaConservandoMayusculasMinusculasYTildes() throws Exception {
        enviar("POST", URL, new DependenciaDTOPeticion("  Vicerrectoría   de Investigaciones "))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Vicerrectoría de Investigaciones"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    /** 2. Un nombre repetido con otras mayúsculas choca igual (en MySQL además sin importar tildes). */
    @Test
    void rechazaNombreRepetidoSinImportarMayusculas() throws Exception {
        crear(URL, new DependenciaDTOPeticion("Vicerrectoría de Investigaciones"));
        enviar("POST", URL, new DependenciaDTOPeticion("VICERRECTORÍA DE INVESTIGACIONES"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigoError").value("GC-0002"))
                .andExpect(jsonPath("$.mensaje").value("Ya existe la dependencia VICERRECTORÍA DE INVESTIGACIONES"));
    }

    /** 3. Los nombres sin prefijo de la lista de la VRI ("de Admisiones", "Jurídica") se aceptan tal cual. */
    @Test
    void aceptaNombresSinPrefijo() throws Exception {
        enviar("POST", URL, new DependenciaDTOPeticion("de Admisiones"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("de Admisiones"));
        enviar("POST", URL, new DependenciaDTOPeticion("Jurídica"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Jurídica"));
    }

    /** 4. Nombre vacío o solo espacios. */
    @Test
    void rechazaNombreVacio() throws Exception {
        enviar("POST", URL, new DependenciaDTOPeticion("  "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0005"))
                .andExpect(jsonPath("$.errores.nombre").exists());
    }

    /** 5. Actualizar conserva el texto tal como se escribe. */
    @Test
    void actualizaConservandoElTexto() throws Exception {
        long id = crear(URL, new DependenciaDTOPeticion("de Planeación y Desarrollo Institucional"));
        enviar("PUT", URL + "/" + id, new DependenciaDTOPeticion("  Oficina   de Planeación "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Oficina de Planeación"));
    }

    /** 6. Actualizar al nombre de otra dependencia. */
    @Test
    void actualizarAlNombreDeOtraResponde409() throws Exception {
        crear(URL, new DependenciaDTOPeticion("Rectoría"));
        long id = crear(URL, new DependenciaDTOPeticion("Secretaría General"));
        enviar("PUT", URL + "/" + id, new DependenciaDTOPeticion("rectoría"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe otra dependencia llamada rectoría"));
        // Reenviar su propio nombre no es un choque.
        enviar("PUT", URL + "/" + id, new DependenciaDTOPeticion("Secretaría General")).andExpect(status().isOk());
    }

    /** 7. Inactivar, activar y filtrar; el listado se ordena por nombre. */
    @Test
    void inactivaActivaYFiltra() throws Exception {
        long id = crear(URL, new DependenciaDTOPeticion("Unilingua"));
        crear(URL, new DependenciaDTOPeticion("Egresados"));

        mvc.perform(get(URL)).andExpect(jsonPath("$[*].nombre", contains("Egresados", "Unilingua")));

        mvc.perform(patch(URL + "/" + id + "/estado").param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(get(URL).param("activo", "true")).andExpect(jsonPath("$[*].nombre", contains("Egresados")));
        mvc.perform(get(URL).param("activo", "false")).andExpect(jsonPath("$[*].nombre", contains("Unilingua")));

        mvc.perform(patch(URL + "/" + id + "/estado").param("activo", "true"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    /** 8. Errores HTTP. */
    @Test
    void erroresHttp() throws Exception {
        mvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoError").value("GC-0003"));
        enviar("PUT", URL + "/999999", new DependenciaDTOPeticion("X")).andExpect(status().isNotFound());
        mvc.perform(patch(URL + "/999999/estado").param("activo", "false")).andExpect(status().isNotFound());
        mvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("Rectoría"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigoError").value("GC-0010"));
    }
}
