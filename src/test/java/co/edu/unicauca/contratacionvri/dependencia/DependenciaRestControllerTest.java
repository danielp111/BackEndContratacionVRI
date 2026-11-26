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

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;
import co.edu.unicauca.contratacionvri.dependencia.fachadaServicios.DTOPeticion.DependenciaDTOPeticion;

class DependenciaRestControllerTest extends PruebaIntegracionBase {

    private static final String URL = "/api/dependencias";

    @Test
    void creaEnMayusculasYRechazaNombreRepetidoSinImportarMayusculas() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new DependenciaDTOPeticion("  Oficina   jurídica "))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("OFICINA JURÍDICA"));

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new DependenciaDTOPeticion("oficina JURÍDICA"))))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizaInactivaYFiltra() throws Exception {
        long id = crear(URL, new DependenciaDTOPeticion("ZOOLOGÍA"));
        crear(URL, new DependenciaDTOPeticion("BIOLOGÍA"));

        mvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new DependenciaDTOPeticion("Agronomía"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("AGRONOMÍA"));

        mvc.perform(get(URL)).andExpect(jsonPath("$[*].nombre", contains("AGRONOMÍA", "BIOLOGÍA")));

        mvc.perform(patch(URL + "/" + id + "/estado").param("activo", "false"))
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(get(URL).param("activo", "true")).andExpect(jsonPath("$[*].nombre", contains("BIOLOGÍA")));
    }

    @Test
    void rechazaNombreVacio() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new DependenciaDTOPeticion("  "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists());
    }
}
