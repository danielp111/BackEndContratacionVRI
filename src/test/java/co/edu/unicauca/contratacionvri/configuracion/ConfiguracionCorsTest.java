package co.edu.unicauca.contratacionvri.configuracion;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;

class ConfiguracionCorsTest extends PruebaIntegracionBase {

    private static final String FRONTEND = "http://localhost:4200";

    /** 1. El navegador pregunta (preflight) antes de un PUT desde el frontend: se permite. */
    @Test
    void permitePreflightDelFrontend() throws Exception {
        mvc.perform(options("/api/dependencias/1")
                        .header(HttpHeaders.ORIGIN, FRONTEND)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONTEND))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    /** 2. Una petición simple desde el frontend recibe el encabezado y puede leer Location. */
    @Test
    void respondeConElOrigenDelFrontend() throws Exception {
        mvc.perform(get("/api/dependencias").header(HttpHeaders.ORIGIN, FRONTEND))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONTEND))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION));
    }

    /** 3. Otro origen no está permitido. */
    @Test
    void rechazaOtroOrigen() throws Exception {
        mvc.perform(options("/api/dependencias")
                        .header(HttpHeaders.ORIGIN, "http://sitio-ajeno.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
