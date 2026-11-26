package co.edu.unicauca.contratacionvri.proyecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.Catalogo;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import co.edu.unicauca.contratacionvri.proyecto.accesoADatos.modelos.ProyectoEntity;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTOPeticion.ProyectoActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTOPeticion.ProyectoCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.servicios.IProyectoServicio;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.PersonaJuridicaEntity;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.PersonaNaturalEntity;

class ProyectoRestControllerTest extends PruebaIntegracionBase {

    private static final String URL = "/api/proyectos";

    @Autowired
    private IProyectoServicio proyectoServicio;

    private CatalogoItemEntity cedula;
    private PersonaNaturalEntity carlos;
    private PersonaNaturalEntity ana;

    @BeforeEach
    void datosBase() {
        cedula = item(Catalogo.TIPO_IDENTIFICACION, "3", "CÉDULA DE CIUDADANÍA");
        carlos = personaNatural("1061000111", "Carlos", "Ruiz");
        ana = personaNatural("94000222", "Ana", "Zapata");
    }

    // ------------------------------------------------------------------ utilidades

    private PersonaNaturalEntity personaNatural(String documento, String nombres, String apellidos) {
        PersonaNaturalEntity p = new PersonaNaturalEntity();
        p.setTipoIdentificacion(cedula);
        p.setNumeroDocumento(documento);
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setActivo(true);
        return terceroRepositorio.save(p);
    }

    private PersonaJuridicaEntity personaJuridica(String documento, String razonSocial) {
        PersonaJuridicaEntity p = new PersonaJuridicaEntity();
        p.setTipoIdentificacion(cedula);
        p.setNumeroDocumento(documento);
        p.setRazonSocial(razonSocial);
        p.setActivo(true);
        return terceroRepositorio.save(p);
    }

    private void inactivar(PersonaNaturalEntity persona) {
        persona.setActivo(false);
        terceroRepositorio.save(persona);
    }

    private ProyectoCrearDTOPeticion nuevo(int id, String nombre, PersonaNaturalEntity director) {
        return ProyectoCrearDTOPeticion.builder().idProyecto(id).nombre(nombre).directorTerceroId(director.getId()).build();
    }

    private ProyectoActualizarDTOPeticion cambio(String nombre, PersonaNaturalEntity director) {
        return ProyectoActualizarDTOPeticion.builder().nombre(nombre).directorTerceroId(director.getId()).build();
    }

    private ResultActions enviar(String metodo, String url, Object cuerpo) throws Exception {
        var peticion = "POST".equals(metodo) ? post(url) : put(url);
        String json = cuerpo instanceof String texto ? texto : this.json.writeValueAsString(cuerpo);
        return mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(json));
    }

    // ------------------------------------------------------------------ crear

    @Nested
    class Crear {

        @Test
        void creaConElCodigoDigitadoYTextosNormalizados() throws Exception {
            String cuerpo = """
                    {"idProyecto": 5019, "nombre": "  Agua   potable  rural ",
                     "directorTerceroId": %d, "bpin": " 2023 0001 0012 "}
                    """.formatted(carlos.getId());

            enviar("POST", URL, cuerpo)
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", endsWith("/api/proyectos/5019")))
                    .andExpect(jsonPath("$.idProyecto").value(5019))
                    .andExpect(jsonPath("$.nombre").value("Agua potable rural"))
                    .andExpect(jsonPath("$.bpin").value("202300010012"))
                    .andExpect(jsonPath("$.directorTerceroId").value(carlos.getId()))
                    .andExpect(jsonPath("$.directorNombre").value("Carlos Ruiz"))
                    .andExpect(jsonPath("$.directorDocumento").value("1061000111"))
                    .andExpect(jsonPath("$.activo").value(true))
                    .andExpect(jsonPath("$.creadoEn").isNotEmpty());
        }

        @Test
        void bpinVacioQuedaEnNullYActivoEnviadoSeIgnora() throws Exception {
            String cuerpo = """
                    {"idProyecto": 5020, "nombre": "Energía", "directorTerceroId": %d, "bpin": "  ", "activo": false}
                    """.formatted(ana.getId());
            enviar("POST", URL, cuerpo)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.bpin").value(nullValue()))
                    .andExpect(jsonPath("$.activo").value(true));
        }

        @Test
        void codigoRepetidoResponde409YNoSobrescribeElOriginal() throws Exception {
            crear(URL, nuevo(5019, "Original", carlos));

            enviar("POST", URL, nuevo(5019, "Impostor", ana))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.codigoError").value("GC-0002"))
                    .andExpect(jsonPath("$.mensaje").value("Ya existe el proyecto con código 5019"));

            mvc.perform(get(URL + "/5019"))
                    .andExpect(jsonPath("$.nombre").value("Original"))
                    .andExpect(jsonPath("$.directorNombre").value("Carlos Ruiz"));
        }

        @Test
        void guardarUnCodigoExistenteSinPasarPorElServicioTampocoSobrescribe() throws Exception {
            // Simula dos peticiones simultáneas que pasan la revisión del servicio: la entidad nueva
            // se inserta (persist) y la llave primaria la rechaza, en lugar de un merge silencioso.
            crear(URL, nuevo(5019, "Original", carlos));
            ProyectoEntity duplicado = new ProyectoEntity(5019);
            duplicado.setNombre("Impostor");
            duplicado.setDirector(ana);

            assertThatThrownBy(() -> proyectoRepositorio.saveAndFlush(duplicado))
                    .isInstanceOf(DataIntegrityViolationException.class);
            assertThat(proyectoRepositorio.findById(5019).orElseThrow().getNombre()).isEqualTo("Original");
        }

        @Test
        void validaLosCamposConDetallePorCampo() throws Exception {
            String cuerpo = """
                    {"idProyecto": 0, "nombre": "", "directorTerceroId": null, "bpin": "12A"}
                    """;
            enviar("POST", URL, cuerpo)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("GC-0005"))
                    .andExpect(jsonPath("$.errores.idProyecto").value("El código del proyecto debe ser un número positivo"))
                    .andExpect(jsonPath("$.errores.nombre").value("El nombre del proyecto es obligatorio"))
                    .andExpect(jsonPath("$.errores.directorTerceroId").value("El director del proyecto es obligatorio"))
                    .andExpect(jsonPath("$.errores.bpin").value("El BPIN solo admite dígitos"));

            enviar("POST", URL, "{\"nombre\": \"Sin código\", \"directorTerceroId\": %d}".formatted(carlos.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errores.idProyecto").value("El código del proyecto es obligatorio"));

            enviar("POST", URL, "{\"idProyecto\": -5, \"nombre\": \"X\", \"directorTerceroId\": %d, \"bpin\": \"123456789012345678901\"}"
                    .formatted(carlos.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errores.idProyecto").exists())
                    .andExpect(jsonPath("$.errores.bpin").value("El BPIN admite máximo 20 dígitos"));

            assertThat(proyectoRepositorio.count()).isZero();
        }

        @Test
        void codigoQueNoEsNumeroEnteroResponde400() throws Exception {
            enviar("POST", URL, "{\"idProyecto\": \"50A9\", \"nombre\": \"X\", \"directorTerceroId\": 1}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("GC-0006"));
        }
    }

    // ------------------------------------------------------------------ director

    @Nested
    class Director {

        @Test
        void rechazaDirectorInexistente() throws Exception {
            ProyectoCrearDTOPeticion p = nuevo(5019, "X", carlos);
            p.setDirectorTerceroId(999_999L);
            enviar("POST", URL, p)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("GC-0004"))
                    .andExpect(jsonPath("$.mensaje").value("El director del proyecto con id 999999 no existe"));
        }

        @Test
        void rechazaDirectorQueEsPersonaJuridica() throws Exception {
            PersonaJuridicaEntity empresa = personaJuridica("800197268", "DIAN");
            ProyectoCrearDTOPeticion p = nuevo(5019, "X", carlos);
            p.setDirectorTerceroId(empresa.getId());
            enviar("POST", URL, p)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("debe ser una persona natural")));
        }

        @Test
        void rechazaDirectorInactivoAlAsignarlo() throws Exception {
            inactivar(ana);
            enviar("POST", URL, nuevo(5019, "X", ana))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El director del proyecto Ana Zapata está inactivo"));
        }

        @Test
        void unDirectorQueSeInactivaDespuesNoBloqueaEditarElProyecto() throws Exception {
            crear(URL, nuevo(5019, "Agua", carlos));
            inactivar(carlos);

            enviar("PUT", URL + "/5019", cambio("Agua potable", carlos))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Agua potable"))
                    .andExpect(jsonPath("$.directorNombre").value("Carlos Ruiz"));

            // Cambiar a otro director inactivo sí se rechaza.
            inactivar(ana);
            enviar("PUT", URL + "/5019", cambio("Agua potable", ana)).andExpect(status().isBadRequest());
        }
    }

    // ------------------------------------------------------------------ actualizar

    @Nested
    class Actualizar {

        @Test
        void actualizaDatosYConservaCodigoEstadoYAuditoria() throws Exception {
            ProyectoCrearDTOPeticion original = nuevo(5019, "Agua", carlos);
            original.setBpin("2023000100012");
            crear(URL, original);
            var creadoEn = proyectoRepositorio.findById(5019).orElseThrow().getCreadoEn();

            enviar("PUT", URL + "/5019", cambio("Agua potable rural", ana))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idProyecto").value(5019))
                    .andExpect(jsonPath("$.nombre").value("Agua potable rural"))
                    .andExpect(jsonPath("$.directorNombre").value("Ana Zapata"))
                    .andExpect(jsonPath("$.bpin").value(nullValue()))
                    .andExpect(jsonPath("$.activo").value(true));

            ProyectoEntity guardado = proyectoRepositorio.findById(5019).orElseThrow();
            assertThat(guardado.getCreadoEn()).isEqualTo(creadoEn);
            assertThat(guardado.getDirector().getId()).isEqualTo(ana.getId());
        }

        @Test
        void elCodigoNoSePuedeCambiar() throws Exception {
            crear(URL, nuevo(5019, "Agua", carlos));

            ProyectoActualizarDTOPeticion otroCodigo = cambio("Agua", carlos);
            otroCodigo.setIdProyecto(6000);
            enviar("PUT", URL + "/5019", otroCodigo)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("GC-0004"))
                    .andExpect(jsonPath("$.mensaje").value(containsString("no se puede cambiar")));

            // El mismo código en el cuerpo se acepta (clientes que reenvían el objeto completo).
            ProyectoActualizarDTOPeticion mismoCodigo = cambio("Agua 2", carlos);
            mismoCodigo.setIdProyecto(5019);
            enviar("PUT", URL + "/5019", mismoCodigo).andExpect(status().isOk());

            assertThat(proyectoRepositorio.findAll()).extracting(ProyectoEntity::getIdProyecto).containsExactly(5019);
        }

        @Test
        void actualizarUnProyectoInexistenteResponde404() throws Exception {
            enviar("PUT", URL + "/7777", cambio("X", carlos))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.mensaje").value("No existe el proyecto con código 7777"));
        }
    }

    // ------------------------------------------------------------------ buscar y activos

    @Nested
    class BuscarYActivos {

        @BeforeEach
        void datos() throws Exception {
            crear(URL, nuevo(5019, "Agua potable", carlos));
            crear(URL, nuevo(5020, "Energía solar", ana));
            crear(URL, nuevo(6001, "Agua subterránea", ana));
            mvc.perform(patch(URL + "/6001/estado").param("activo", "false")).andExpect(status().isOk());
        }

        @Test
        void porPrefijoDelCodigo() throws Exception {
            mvc.perform(get(URL).param("id", "50"))
                    .andExpect(jsonPath("$.contenido[*].idProyecto", contains(5019, 5020)));
            mvc.perform(get(URL).param("id", "6"))
                    .andExpect(jsonPath("$.contenido[*].idProyecto", contains(6001)));
            mvc.perform(get(URL).param("id", "abc")).andExpect(status().isBadRequest());
        }

        @Test
        void porNombre() throws Exception {
            mvc.perform(get(URL).param("nombre", "AGUA"))
                    .andExpect(jsonPath("$.contenido[*].idProyecto", contains(5019, 6001)));
            mvc.perform(get(URL).param("nombre", "%")).andExpect(jsonPath("$.totalElementos").value(0));
        }

        @Test
        void porNombreODocumentoDelDirector() throws Exception {
            mvc.perform(get(URL).param("director", "zapata"))
                    .andExpect(jsonPath("$.contenido[*].idProyecto", contains(5020, 6001)));
            mvc.perform(get(URL).param("director", "1061"))
                    .andExpect(jsonPath("$.contenido[*].idProyecto", contains(5019)));
        }

        @Test
        void porEstadoYCombinandoFiltros() throws Exception {
            mvc.perform(get(URL).param("activo", "false"))
                    .andExpect(jsonPath("$.contenido[*].idProyecto", contains(6001)));
            mvc.perform(get(URL).param("nombre", "agua").param("director", "ana"))
                    .andExpect(jsonPath("$.contenido[*].idProyecto", contains(6001)));
        }

        @Test
        void paginaLosResultados() throws Exception {
            mvc.perform(get(URL).param("tamanio", "2"))
                    .andExpect(jsonPath("$.contenido[*].idProyecto", contains(5019, 5020)))
                    .andExpect(jsonPath("$.totalElementos").value(3))
                    .andExpect(jsonPath("$.totalPaginas").value(2))
                    .andExpect(jsonPath("$.ultima").value(false));
            mvc.perform(get(URL).param("tamanio", "2").param("pagina", "1"))
                    .andExpect(jsonPath("$.contenido[*].idProyecto", contains(6001)))
                    .andExpect(jsonPath("$.ultima").value(true));
            mvc.perform(get(URL).param("tamanio", "101")).andExpect(status().isBadRequest());
        }

        @Test
        void activosExcluyeInactivosYArmaLaEtiqueta() throws Exception {
            mvc.perform(get(URL + "/activos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].idProyecto", contains(5019, 5020)))
                    .andExpect(jsonPath("$[0].etiqueta").value("5019 - Agua potable"))
                    .andExpect(jsonPath("$[0].directorNombre").value("Carlos Ruiz"));
            // "agua" también está en 6001, pero está inactivo.
            mvc.perform(get(URL + "/activos").param("texto", "agua"))
                    .andExpect(jsonPath("$[*].idProyecto", contains(5019)));
            mvc.perform(get(URL + "/activos").param("texto", "502"))
                    .andExpect(jsonPath("$[*].idProyecto", contains(5020)));
        }
    }

    // ------------------------------------------------------------------ estado

    @Test
    void inactivaYReactiva() throws Exception {
        crear(URL, nuevo(5019, "Agua", carlos));

        mvc.perform(patch(URL + "/5019/estado").param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(get(URL + "/activos")).andExpect(jsonPath("$.length()").value(0));

        mvc.perform(patch(URL + "/5019/estado").param("activo", "true"))
                .andExpect(jsonPath("$.activo").value(true));
        mvc.perform(get(URL + "/activos")).andExpect(jsonPath("$.length()").value(1));

        mvc.perform(patch(URL + "/7777/estado").param("activo", "false")).andExpect(status().isNotFound());
    }

    @Test
    void unProyectoInactivoNoSirveParaContratosNuevosPeroSigueExistiendo() throws Exception {
        crear(URL, nuevo(5019, "Agua", carlos));
        mvc.perform(patch(URL + "/5019/estado").param("activo", "false")).andExpect(status().isOk());

        assertThatThrownBy(() -> proyectoServicio.obtenerProyectoValido(5019, true))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("inactivo");
        // Un contrato existente lo sigue resolviendo (sin exigir que esté activo).
        assertThat(proyectoServicio.obtenerProyectoValido(5019, false).getNombre()).isEqualTo("Agua");
        assertThatThrownBy(() -> proyectoServicio.obtenerProyectoValido(7777, false))
                .isInstanceOf(ReglaNegocioException.class);
    }

    // ------------------------------------------------------------------ errores HTTP

    @Test
    void consultarYErroresHttp() throws Exception {
        crear(URL, nuevo(5019, "Agua", carlos));
        mvc.perform(get(URL + "/5019")).andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Agua"));
        mvc.perform(get(URL + "/7777"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoError").value("GC-0003"));
        mvc.perform(get(URL + "/0")).andExpect(status().isBadRequest());

        mvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("idProyecto=1"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigoError").value("GC-0010"));
        mvc.perform(put(URL + "/5019").contentType(MediaType.APPLICATION_FORM_URLENCODED).content("nombre=X"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
