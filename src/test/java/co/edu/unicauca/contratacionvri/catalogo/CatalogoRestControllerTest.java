package co.edu.unicauca.contratacionvri.catalogo;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;

class CatalogoRestControllerTest extends PruebaIntegracionBase {

    private static final String URL = "/api/catalogos";

    private ResultActions enviar(String metodo, String url, String cuerpo) throws Exception {
        var peticion = "POST".equals(metodo) ? post(url) : put(url);
        return mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    void creaConElCodigoEnMayusculasYTextosNormalizados() throws Exception {
        enviar("POST", URL, "{\"codigo\": \" tipo_seguimiento \", \"nombre\": \"  Tipo   de seguimiento \", \"descripcion\": \"\"}")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/catalogos/TIPO_SEGUIMIENTO")))
                .andExpect(jsonPath("$.codigo").value("TIPO_SEGUIMIENTO"))
                .andExpect(jsonPath("$.nombre").value("Tipo de seguimiento"))
                .andExpect(jsonPath("$.descripcion").doesNotExist())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void codigoRepetidoResponde409() throws Exception {
        catalogo("NATURALEZA");
        enviar("POST", URL, "{\"codigo\": \"naturaleza\", \"nombre\": \"Otra\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigoError").value("GC-0002"))
                .andExpect(jsonPath("$.mensaje").value("Ya existe el catálogo NATURALEZA"));
    }

    @Test
    void validaElFormatoDelCodigoYLosCampos() throws Exception {
        for (String invalido : new String[]{"TIPO SEGUIMIENTO", "1TIPO", "TIPO-SEGUIMIENTO", "_TIPO", "TIPO_"}) {
            enviar("POST", URL, "{\"codigo\": \"" + invalido + "\", \"nombre\": \"X\"}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errores.codigo").exists());
        }
        enviar("POST", URL, "{\"codigo\": \"\", \"nombre\": \"\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.codigo").value("El código del catálogo es obligatorio"))
                .andExpect(jsonPath("$.errores.nombre").value("El nombre del catálogo es obligatorio"));
    }

    @Test
    void consultaPorCodigoSinImportarMayusculas() throws Exception {
        catalogo("ASEGURADORA");
        mvc.perform(get(URL + "/aseguradora")).andExpect(status().isOk()).andExpect(jsonPath("$.codigo").value("ASEGURADORA"));
        mvc.perform(get(URL + "/NO_EXISTE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el catálogo NO_EXISTE"));
    }

    @Test
    void actualizaNombreYDescripcionPeroNoElCodigo() throws Exception {
        catalogo("TIPO_ANTICIPO");
        enviar("PUT", URL + "/TIPO_ANTICIPO", "{\"nombre\": \"Anticipo\", \"descripcion\": \"Anticipo o pago anticipado\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Anticipo"))
                .andExpect(jsonPath("$.descripcion").value("Anticipo o pago anticipado"));

        enviar("PUT", URL + "/TIPO_ANTICIPO", "{\"nombre\": \"Anticipo\", \"codigo\": \"ANTICIPOS\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0004"))
                .andExpect(jsonPath("$.mensaje").value(containsString("no se puede cambiar")));
        // El mismo código (en cualquier mayúscula) se acepta.
        enviar("PUT", URL + "/TIPO_ANTICIPO", "{\"nombre\": \"Anticipo\", \"codigo\": \"tipo_anticipo\"}").andExpect(status().isOk());
        enviar("PUT", URL + "/NO_EXISTE", "{\"nombre\": \"X\"}").andExpect(status().isNotFound());
    }

    @Test
    void listaOrdenadoPorNombreInactivaYFiltra() throws Exception {
        enviar("POST", URL, "{\"codigo\": \"TIPO_ADICION\", \"nombre\": \"Tipo de adición\"}").andExpect(status().isCreated());
        enviar("POST", URL, "{\"codigo\": \"ASEGURADORA\", \"nombre\": \"Aseguradora\"}").andExpect(status().isCreated());

        mvc.perform(get(URL)).andExpect(jsonPath("$[*].codigo", contains("ASEGURADORA", "TIPO_ADICION")));
        mvc.perform(patch(URL + "/ASEGURADORA/estado").param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(get(URL).param("activo", "true")).andExpect(jsonPath("$[*].codigo", contains("TIPO_ADICION")));
        mvc.perform(patch(URL + "/ASEGURADORA/estado").param("activo", "true")).andExpect(jsonPath("$.activo").value(true));
        mvc.perform(patch(URL + "/NO_EXISTE/estado").param("activo", "false")).andExpect(status().isNotFound());
    }

    @Test
    void rechazaCuerpoQueNoEsJson() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("NATURALEZA"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigoError").value("GC-0010"));
    }
}
