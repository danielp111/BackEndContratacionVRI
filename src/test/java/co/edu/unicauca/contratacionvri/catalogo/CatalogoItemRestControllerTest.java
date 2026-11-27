package co.edu.unicauca.contratacionvri.catalogo;

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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoEntity;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CodigoCatalogo;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.servicios.ICatalogoItemServicio;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;

class CatalogoItemRestControllerTest extends PruebaIntegracionBase {

    private static final String TIPOS = "/api/catalogos/TIPO_IDENTIFICACION/items";

    @Autowired
    private ICatalogoItemServicio servicio;

    private ResultActions enviar(String metodo, String url, String cuerpo) throws Exception {
        var peticion = "POST".equals(metodo) ? post(url) : put(url);
        return mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    // ------------------------------------------------------------------ crear

    @Test
    void creaConCodigoEnMayusculasNombreTalComoSeEscribeYEtiquetaSireci() throws Exception {
        catalogo(CodigoCatalogo.TIPO_IDENTIFICACION);
        enviar("POST", TIPOS, "{\"codigo\": \" cc \", \"codigoSireci\": \" 3 \", \"nombre\": \"  Cédula   de Ciudadanía \", \"orden\": 3}")
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.catalogo").value("TIPO_IDENTIFICACION"))
                .andExpect(jsonPath("$.codigo").value("CC"))
                .andExpect(jsonPath("$.codigoSireci").value("3"))
                .andExpect(jsonPath("$.nombre").value("Cédula de Ciudadanía"))
                .andExpect(jsonPath("$.etiqueta").value("3 Cédula de Ciudadanía"))
                .andExpect(jsonPath("$.orden").value(3))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void sinCodigoSireciLaEtiquetaEsElNombreYElOrdenPorDefectoEs0() throws Exception {
        catalogo(CodigoCatalogo.FUENTE_FINANCIACION);
        enviar("POST", "/api/catalogos/FUENTE_FINANCIACION/items", "{\"codigo\": \"INVERSION\", \"nombre\": \"Inversión\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoSireci").doesNotExist())
                .andExpect(jsonPath("$.etiqueta").value("Inversión"))
                .andExpect(jsonPath("$.orden").value(0));
    }

    @Test
    void codigoYNombreSonUnicosEnElCatalogoPeroSePuedenRepetirEnOtro() throws Exception {
        item(CodigoCatalogo.TIPO_IDENTIFICACION, "CC", "3", "CÉDULA DE CIUDADANÍA");

        enviar("POST", TIPOS, "{\"codigo\": \"cc\", \"nombre\": \"Otra cédula\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigoError").value("GC-0002"))
                .andExpect(jsonPath("$.mensaje").value("Ya existe el código CC en el catálogo TIPO_IDENTIFICACION"));
        enviar("POST", TIPOS, "{\"codigo\": \"CC2\", \"nombre\": \"cédula de ciudadanía\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe el ítem 'cédula de ciudadanía' en el catálogo TIPO_IDENTIFICACION"));

        catalogo(CodigoCatalogo.NATURALEZA);
        enviar("POST", "/api/catalogos/NATURALEZA/items", "{\"codigo\": \"CC\", \"nombre\": \"CÉDULA DE CIUDADANÍA\"}")
                .andExpect(status().isCreated());
    }

    @Test
    void validaLosCampos() throws Exception {
        catalogo(CodigoCatalogo.TIPO_IDENTIFICACION);
        enviar("POST", TIPOS, "{\"codigo\": \"\", \"nombre\": \"\", \"orden\": -1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0005"))
                .andExpect(jsonPath("$.errores.codigo").value("El código del ítem es obligatorio"))
                .andExpect(jsonPath("$.errores.nombre").value("El nombre del ítem es obligatorio"))
                .andExpect(jsonPath("$.errores.orden").value("El orden no puede ser negativo"));
        enviar("POST", TIPOS, "{\"codigo\": \"PAGO ANTICIPADO\", \"nombre\": \"X\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.codigo").value(containsString("solo admite letras, números y guion bajo")));
        enviar("POST", "/api/catalogos/NO_EXISTE/items", "{\"codigo\": \"X\", \"nombre\": \"X\"}").andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ actualizar

    @Test
    void actualizaNombreCodigoSireciYOrdenPeroNoElCodigo() throws Exception {
        CatalogoItemEntity cc = item(CodigoCatalogo.TIPO_IDENTIFICACION, "CC", "3", "CEDULA DE CIUDADANIA");
        String url = TIPOS + "/" + cc.getId();

        enviar("PUT", url, "{\"nombre\": \"CÉDULA DE CIUDADANÍA\", \"codigoSireci\": \"3\", \"orden\": 3}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("CÉDULA DE CIUDADANÍA"))
                .andExpect(jsonPath("$.codigo").value("CC"))
                .andExpect(jsonPath("$.orden").value(3));

        enviar("PUT", url, "{\"nombre\": \"CÉDULA DE CIUDADANÍA\", \"codigo\": \"CEDULA\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("El código del ítem no se puede cambiar")));
        enviar("PUT", url, "{\"nombre\": \"CÉDULA DE CIUDADANÍA\", \"codigo\": \"cc\"}").andExpect(status().isOk());

        item(CodigoCatalogo.TIPO_IDENTIFICACION, "NIT", "1", "NIT");
        enviar("PUT", url, "{\"nombre\": \"nit\"}").andExpect(status().isConflict());
    }

    @Test
    void unItemDeOtroCatalogoNoSeEncuentraPorLaUrlDeEsteCatalogo() throws Exception {
        CatalogoItemEntity natural = item(CodigoCatalogo.NATURALEZA, "NATURAL", "1", "PERSONA NATURAL");
        catalogo(CodigoCatalogo.TIPO_IDENTIFICACION);
        mvc.perform(get(TIPOS + "/" + natural.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe un ítem con id " + natural.getId() + " en el catálogo TIPO_IDENTIFICACION"));
        enviar("PUT", TIPOS + "/" + natural.getId(), "{\"nombre\": \"X\"}").andExpect(status().isNotFound());
        mvc.perform(patch(TIPOS + "/" + natural.getId() + "/estado").param("activo", "false")).andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ listar y estado

    @Test
    void elSelectorOrdenaPorOrdenYLuegoPorNombreYSoloMuestraActivos() throws Exception {
        CatalogoItemEntity ce = item(CodigoCatalogo.TIPO_IDENTIFICACION, "CE", "4", "CÉDULA DE EXTRANJERÍA");
        ce.setOrden(4);
        catalogoRepositorio.save(ce);
        CatalogoItemEntity nit = item(CodigoCatalogo.TIPO_IDENTIFICACION, "NIT", "1", "NIT");
        nit.setOrden(1);
        catalogoRepositorio.save(nit);
        // Mismo orden: desempata el nombre.
        item(CodigoCatalogo.TIPO_IDENTIFICACION, "RUT", "2", "RUT");
        item(CodigoCatalogo.TIPO_IDENTIFICACION, "CC", "3", "CÉDULA DE CIUDADANÍA");

        mvc.perform(get(TIPOS)).andExpect(jsonPath("$[*].codigo", contains("CC", "RUT", "NIT", "CE")));

        mvc.perform(patch(TIPOS + "/" + nit.getId() + "/estado").param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(get(TIPOS).param("activo", "true")).andExpect(jsonPath("$[*].codigo", contains("CC", "RUT", "CE")));
        mvc.perform(get(TIPOS).param("activo", "false")).andExpect(jsonPath("$[*].codigo", contains("NIT")));
    }

    @Test
    void conElCatalogoInactivoElSelectorQuedaVacio() throws Exception {
        item(CodigoCatalogo.TIPO_IDENTIFICACION, "CC", "3", "CÉDULA DE CIUDADANÍA");
        mvc.perform(patch("/api/catalogos/TIPO_IDENTIFICACION/estado").param("activo", "false")).andExpect(status().isOk());

        mvc.perform(get(TIPOS).param("activo", "true")).andExpect(jsonPath("$.length()").value(0));
        // La administración sí los sigue viendo.
        mvc.perform(get(TIPOS)).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void erroresHttp() throws Exception {
        catalogo(CodigoCatalogo.TIPO_IDENTIFICACION);
        mvc.perform(get(TIPOS + "/999999")).andExpect(status().isNotFound()).andExpect(jsonPath("$.codigoError").value("GC-0003"));
        mvc.perform(get(TIPOS + "/0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/catalogos/NO_EXISTE/items")).andExpect(status().isNotFound());
        mvc.perform(post(TIPOS).contentType(MediaType.TEXT_PLAIN).content("CC"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigoError").value("GC-0010"));
    }

    // ------------------------------------------------------------------ validarItem

    @Nested
    class ValidarItem {

        @Test
        void aceptaUnItemActivoDelCatalogoEsperado() {
            CatalogoItemEntity cc = item(CodigoCatalogo.TIPO_IDENTIFICACION, "CC", "3", "CÉDULA DE CIUDADANÍA");
            assertThat(servicio.validarItem(cc.getId(), CodigoCatalogo.TIPO_IDENTIFICACION).getId()).isEqualTo(cc.getId());
        }

        @Test
        void rechazaInexistenteDeOtroCatalogoInactivoOConCatalogoInactivo() {
            assertThatThrownBy(() -> servicio.validarItem(999_999L, CodigoCatalogo.TIPO_IDENTIFICACION))
                    .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("no existe");

            CatalogoItemEntity natural = item(CodigoCatalogo.NATURALEZA, "NATURAL", "1", "PERSONA NATURAL");
            assertThatThrownBy(() -> servicio.validarItem(natural.getId(), CodigoCatalogo.TIPO_IDENTIFICACION))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("El ítem '1 PERSONA NATURAL' es del catálogo NATURALEZA y se esperaba uno del catálogo TIPO_IDENTIFICACION");

            CatalogoItemEntity ce = item(CodigoCatalogo.TIPO_IDENTIFICACION, "CE", "4", "CÉDULA DE EXTRANJERÍA");
            ce.setActivo(false);
            catalogoRepositorio.save(ce);
            assertThatThrownBy(() -> servicio.validarItem(ce.getId(), CodigoCatalogo.TIPO_IDENTIFICACION))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("El ítem '4 CÉDULA DE EXTRANJERÍA' del catálogo TIPO_IDENTIFICACION está inactivo");

            CatalogoItemEntity cc = item(CodigoCatalogo.TIPO_IDENTIFICACION, "CC", "3", "CÉDULA DE CIUDADANÍA");
            CatalogoEntity tipos = catalogo(CodigoCatalogo.TIPO_IDENTIFICACION);
            tipos.setActivo(false);
            catalogosRepositorio.save(tipos);
            assertThatThrownBy(() -> servicio.validarItem(cc.getId(), CodigoCatalogo.TIPO_IDENTIFICACION))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("El catálogo TIPO_IDENTIFICACION está inactivo");
        }

        @Test
        void unRegistroExistenteConservaSuItemAunqueLuegoSeInactive() throws Exception {
            // Tercero con cédula; luego la cédula se inactiva: editar el tercero sin cambiar el tipo sigue funcionando.
            CatalogoItemEntity cc = item(CodigoCatalogo.TIPO_IDENTIFICACION, "CC", "3", "CÉDULA DE CIUDADANÍA");
            String persona = "{\"tipoIdentificacionId\": %d, \"numeroDocumento\": \"1061\", \"nombres\": \"Ana\", \"apellidos\": \"%s\"}";
            long id = crear("/api/terceros/naturales", json.readTree(persona.formatted(cc.getId(), "Pérez")));
            cc.setActivo(false);
            catalogoRepositorio.save(cc);

            enviar("PUT", "/api/terceros/naturales/" + id, persona.formatted(cc.getId(), "Pérez Ruiz"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.apellidos").value("Pérez Ruiz"));
            // Un tercero nuevo con la cédula inactiva sí se rechaza.
            enviar("POST", "/api/terceros/naturales", persona.formatted(cc.getId(), "Gómez").replace("1061", "1062"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("está inactivo")));
        }
    }
}
