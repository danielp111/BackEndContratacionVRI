package co.edu.unicauca.contratacionvri.tercero;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CodigoCatalogo;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.PersonaNaturalEntity;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTOPeticion.PersonaJuridicaDTOPeticion;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTOPeticion.PersonaNaturalDTOPeticion;

class TerceroRestControllerTest extends PruebaIntegracionBase {

    private static final String URL = "/api/terceros";
    private static final String NATURALES = URL + "/naturales";
    private static final String JURIDICAS = URL + "/juridicas";

    /** NIT de la DIAN, DV conocido 4. */
    private static final String NIT_DIAN = "800197268";

    private CatalogoItemEntity nit;
    private CatalogoItemEntity cedula;
    private CatalogoItemEntity extranjeria;

    @BeforeEach
    void catalogos() {
        // El código "1" es el configurado como NIT (vri.tipo-identificacion.codigo-nit).
        nit = item(CodigoCatalogo.TIPO_IDENTIFICACION, "NIT", "1", "NIT");
        cedula = item(CodigoCatalogo.TIPO_IDENTIFICACION, "CC", "3", "CÉDULA DE CIUDADANÍA");
        extranjeria = item(CodigoCatalogo.TIPO_IDENTIFICACION, "CE", "4", "CÉDULA DE EXTRANJERÍA");
    }

    // ------------------------------------------------------------------ utilidades

    private PersonaNaturalDTOPeticion natural(String documento, String nombres, String apellidos) {
        return PersonaNaturalDTOPeticion.builder()
                .tipoIdentificacionId(cedula.getId())
                .numeroDocumento(documento)
                .nombres(nombres)
                .apellidos(apellidos)
                .correo("persona@correo.com")
                .fechaNacimiento(LocalDate.of(1990, 5, 10))
                .fechaExpedicion(LocalDate.of(2008, 6, 1))
                .build();
    }

    private PersonaJuridicaDTOPeticion juridica(String nit, String razonSocial) {
        return PersonaJuridicaDTOPeticion.builder()
                .tipoIdentificacionId(this.nit.getId())
                .numeroDocumento(nit)
                .razonSocial(razonSocial)
                .build();
    }

    private ResultActions enviar(String metodo, String url, Object cuerpo) throws Exception {
        var peticion = switch (metodo) {
            case "POST" -> post(url);
            case "PUT" -> put(url);
            default -> throw new IllegalArgumentException(metodo);
        };
        String json = cuerpo instanceof String texto ? texto : this.json.writeValueAsString(cuerpo);
        return mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(json));
    }

    // ------------------------------------------------------------------ creación y normalización

    @Nested
    class Crear {

        @Test
        void personaNaturalNormalizaTextosTrataVacioComoNullYNoTieneDv() throws Exception {
            String cuerpo = """
                    {"tipoIdentificacionId": %d, "numeroDocumento": "  1061abc ",
                     "nombres": "  Ana    María ", "apellidos": " Pérez\\tGómez ",
                     "correo": " Ana.Perez@Correo.COM ", "correoInstitucional": "APerez@Unicauca.edu.co",
                     "profesion": "", "ciudadNacimiento": "   ", "telefono": ""}
                    """.formatted(cedula.getId());

            enviar("POST", NATURALES, cuerpo)
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", endsWith("/api/terceros/" + terceroRepositorio.findAll().get(0).getId())))
                    .andExpect(jsonPath("$.naturaleza").value("NATURAL"))
                    .andExpect(jsonPath("$.numeroDocumento").value("1061ABC"))
                    .andExpect(jsonPath("$.nombres").value("Ana María"))
                    .andExpect(jsonPath("$.apellidos").value("Pérez Gómez"))
                    .andExpect(jsonPath("$.nombreCompleto").value("Ana María Pérez Gómez"))
                    .andExpect(jsonPath("$.correo").value("ana.perez@correo.com"))
                    .andExpect(jsonPath("$.correoInstitucional").value("aperez@unicauca.edu.co"))
                    .andExpect(jsonPath("$.profesion").value(nullValue()))
                    .andExpect(jsonPath("$.ciudadNacimiento").value(nullValue()))
                    .andExpect(jsonPath("$.telefono").value(nullValue()))
                    .andExpect(jsonPath("$.dv").value(nullValue()))
                    .andExpect(jsonPath("$.tipoIdentificacionEtiqueta").value("3 CÉDULA DE CIUDADANÍA"))
                    .andExpect(jsonPath("$.activo").value(true));
        }

        @Test
        void marcaDeConsorcioOUnionTemporalSoloEnPersonaJuridica() throws Exception {
            enviar("POST", JURIDICAS, juridica(NIT_DIAN, "DIAN"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.esConsorcioUnionTemporal").value(false));

            PersonaJuridicaDTOPeticion consorcio = juridica("890903938", "Consorcio Vías del Cauca");
            consorcio.setEsConsorcioUnionTemporal(true);
            long id = crear(JURIDICAS, consorcio);
            mvc.perform(get(URL + "/" + id)).andExpect(jsonPath("$.esConsorcioUnionTemporal").value(true));

            // Al actualizar sin enviarla, queda en falso (PUT reemplaza).
            enviar("PUT", JURIDICAS + "/" + id, juridica("890903938", "Vías del Cauca SAS"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.esConsorcioUnionTemporal").value(false));
            // La persona natural no tiene esa marca.
            long natural = crear(NATURALES, natural("1061000", "Ana", "Pérez"));
            mvc.perform(get(URL + "/" + natural)).andExpect(jsonPath("$.esConsorcioUnionTemporal").doesNotExist());
        }

        @Test
        void elNitSeReconocePorSuCodigoMnemonicoNoPorElNumeroSireci() throws Exception {
            // Un NIT con otro código SIRECI sigue siendo NIT: el programa usa el código "NIT".
            nit.setCodigoSireci("99");
            catalogoRepositorio.save(nit);
            enviar("POST", JURIDICAS, juridica(NIT_DIAN, "DIAN")).andExpect(status().isCreated())
                    .andExpect(jsonPath("$.dv").value("4"));
            // Y un ítem con código SIRECI "1" pero otro mnemónico no se trata como NIT.
            CatalogoItemEntity otro = item(CodigoCatalogo.TIPO_IDENTIFICACION, "OTRO", "1", "OTRO DOCUMENTO");
            PersonaNaturalDTOPeticion p = natural("123456", "Ana", "Pérez");
            p.setTipoIdentificacionId(otro.getId());
            enviar("POST", NATURALES, p).andExpect(status().isCreated()).andExpect(jsonPath("$.dv").value(nullValue()));
        }

        @Test
        void personaJuridicaConNitCalculaElDvEIgnoraDvYNaturalezaEnviados() throws Exception {
            String cuerpo = """
                    {"tipoIdentificacionId": %d, "numeroDocumento": "%s", "razonSocial": "Dirección de Impuestos",
                     "dv": "9", "naturaleza": "NATURAL", "nombreCompleto": "otro", "activo": false}
                    """.formatted(nit.getId(), NIT_DIAN);

            enviar("POST", JURIDICAS, cuerpo)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.naturaleza").value("JURIDICA"))
                    .andExpect(jsonPath("$.dv").value("4"))
                    .andExpect(jsonPath("$.razonSocial").value("Dirección de Impuestos"))
                    .andExpect(jsonPath("$.nombreCompleto").value("Dirección de Impuestos"))
                    .andExpect(jsonPath("$.activo").value(true))
                    .andExpect(jsonPath("$.representanteLegalId").value(nullValue()));
        }

        @Test
        void camposObligatoriosVaciosSeRechazanConDetallePorCampo() throws Exception {
            String cuerpo = """
                    {"tipoIdentificacionId": %d, "numeroDocumento": "  ", "nombres": "", "apellidos": "X",
                     "correo": "no-es-correo"}
                    """.formatted(cedula.getId());

            enviar("POST", NATURALES, cuerpo)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("GC-0005"))
                    .andExpect(jsonPath("$.errores.numeroDocumento").exists())
                    .andExpect(jsonPath("$.errores.nombres").exists())
                    .andExpect(jsonPath("$.errores.correo").exists());
            assertThat(terceroRepositorio.count()).isZero();
        }

        @Test
        void tipoDeIdentificacionDebeSerDelCatalogoCorrectoYActivo() throws Exception {
            CatalogoItemEntity naturaleza = item(CodigoCatalogo.NATURALEZA, "NATURAL", "1", "PERSONA NATURAL");
            PersonaNaturalDTOPeticion p = natural("1001", "Ana", "Pérez");
            p.setTipoIdentificacionId(naturaleza.getId());
            enviar("POST", NATURALES, p).andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigoError").value("GC-0004"));

            extranjeria.setActivo(false);
            catalogoRepositorio.save(extranjeria);
            p.setTipoIdentificacionId(extranjeria.getId());
            enviar("POST", NATURALES, p).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El ítem '4 CÉDULA DE EXTRANJERÍA' del catálogo TIPO_IDENTIFICACION está inactivo"));
        }
    }

    // ------------------------------------------------------------------ reglas de documento

    @Nested
    class Documento {

        @Test
        void personaNaturalNoPuedeTenerNitNiAlCrearNiAlActualizar() throws Exception {
            PersonaNaturalDTOPeticion p = natural(NIT_DIAN, "Ana", "Pérez");
            p.setTipoIdentificacionId(nit.getId());
            enviar("POST", NATURALES, p)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("GC-0004"))
                    .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("no puede tener tipo de documento NIT")));

            long id = crear(NATURALES, natural("1002", "Ana", "Pérez"));
            enviar("PUT", NATURALES + "/" + id, p).andExpect(status().isBadRequest());
        }

        @Test
        void nitConDvIncluidoOConLetrasSeRechaza() throws Exception {
            enviar("POST", JURIDICAS, juridica(NIT_DIAN + "-4", "DIAN"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("sin el dígito de verificación")));
            enviar("POST", JURIDICAS, juridica("80019726A", "DIAN")).andExpect(status().isBadRequest());
        }

        @Test
        void elDocumentoEsUnicoPorTipoYNumero() throws Exception {
            crear(NATURALES, natural("123456", "Ana", "Pérez"));

            // Mismo tipo y número (aunque llegue con espacios alrededor): 409.
            enviar("POST", NATURALES, natural(" 123456 ", "Otra", "Persona"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.codigoError").value("GC-0002"));

            // Mismo número con otro tipo: permitido.
            PersonaNaturalDTOPeticion otroTipo = natural("123456", "Otra", "Persona");
            otroTipo.setTipoIdentificacionId(extranjeria.getId());
            enviar("POST", NATURALES, otroTipo).andExpect(status().isCreated());

            // La unicidad cruza naturalezas: un NIT repetido entre jurídicas también choca.
            crear(JURIDICAS, juridica(NIT_DIAN, "DIAN"));
            enviar("POST", JURIDICAS, juridica(NIT_DIAN, "Otra empresa")).andExpect(status().isConflict());
        }

        @Test
        void alActualizarNoChocaConsigoMismoPeroSiConOtro() throws Exception {
            long id = crear(NATURALES, natural("111", "Ana", "Pérez"));
            crear(NATURALES, natural("222", "Luis", "Gómez"));

            enviar("PUT", NATURALES + "/" + id, natural("111", "Ana", "Pérez Ruiz")).andExpect(status().isOk());
            enviar("PUT", NATURALES + "/" + id, natural("222", "Ana", "Pérez")).andExpect(status().isConflict());
        }

        @Test
        void cambiarElNitRecalculaElDv() throws Exception {
            long id = crear(JURIDICAS, juridica(NIT_DIAN, "DIAN"));

            enviar("PUT", JURIDICAS + "/" + id, juridica("890903938", "Bancolombia"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dv").value("8"))
                    .andExpect(jsonPath("$.nombreCompleto").value("Bancolombia"));

            // Pasar a un tipo que no es NIT borra el DV.
            PersonaJuridicaDTOPeticion sinNit = juridica("E-12345", "Bancolombia");
            sinNit.setTipoIdentificacionId(extranjeria.getId());
            enviar("PUT", JURIDICAS + "/" + id, sinNit)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dv").value(nullValue()));
        }
    }

    // ------------------------------------------------------------------ representante legal

    @Nested
    class RepresentanteLegal {

        @Test
        void asignaUnaPersonaNaturalActivaYMuestraSusDatos() throws Exception {
            long representante = crear(NATURALES, natural("1061000", "Carlos", "Ruiz"));
            PersonaJuridicaDTOPeticion p = juridica(NIT_DIAN, "DIAN");
            p.setRepresentanteLegalId(representante);

            enviar("POST", JURIDICAS, p)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.representanteLegalId").value(representante))
                    .andExpect(jsonPath("$.representanteLegalNombre").value("Carlos Ruiz"))
                    .andExpect(jsonPath("$.representanteLegalDocumento").value("1061000"));
        }

        @Test
        void rechazaRepresentanteInexistente() throws Exception {
            PersonaJuridicaDTOPeticion p = juridica(NIT_DIAN, "DIAN");
            p.setRepresentanteLegalId(999_999L);
            enviar("POST", JURIDICAS, p)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El representante legal con id 999999 no existe"));
        }

        @Test
        void rechazaRepresentanteQueEsPersonaJuridica() throws Exception {
            long empresa = crear(JURIDICAS, juridica("890903938", "Bancolombia"));
            PersonaJuridicaDTOPeticion p = juridica(NIT_DIAN, "DIAN");
            p.setRepresentanteLegalId(empresa);
            enviar("POST", JURIDICAS, p)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("debe ser una persona natural")));
        }

        @Test
        void rechazaRepresentanteInactivoSoloSiCambia() throws Exception {
            long representante = crear(NATURALES, natural("1061000", "Carlos", "Ruiz"));
            PersonaJuridicaDTOPeticion p = juridica(NIT_DIAN, "DIAN");
            p.setRepresentanteLegalId(representante);
            long empresa = crear(JURIDICAS, p);

            mvc.perform(patch(URL + "/" + representante + "/estado").param("activo", "false")).andExpect(status().isOk());

            // El mismo representante (ya asignado) no bloquea editar la empresa.
            p.setRazonSocial("DIAN Colombia");
            enviar("PUT", JURIDICAS + "/" + empresa, p).andExpect(status().isOk());

            // Asignarlo a una empresa nueva sí se rechaza.
            PersonaJuridicaDTOPeticion otra = juridica("890903938", "Bancolombia");
            otra.setRepresentanteLegalId(representante);
            enviar("POST", JURIDICAS, otra)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El representante legal Carlos Ruiz está inactivo"));
        }

        @Test
        void sePuedeQuitarElRepresentante() throws Exception {
            long representante = crear(NATURALES, natural("1061000", "Carlos", "Ruiz"));
            PersonaJuridicaDTOPeticion p = juridica(NIT_DIAN, "DIAN");
            p.setRepresentanteLegalId(representante);
            long empresa = crear(JURIDICAS, p);

            p.setRepresentanteLegalId(null);
            enviar("PUT", JURIDICAS + "/" + empresa, p)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.representanteLegalId").value(nullValue()));
        }
    }

    // ------------------------------------------------------------------ naturaleza, fechas, consulta

    @Test
    void laNaturalezaNoSePuedeCambiar() throws Exception {
        long persona = crear(NATURALES, natural("1061000", "Carlos", "Ruiz"));
        long empresa = crear(JURIDICAS, juridica(NIT_DIAN, "DIAN"));

        enviar("PUT", JURIDICAS + "/" + persona, juridica("890903938", "Carlos SAS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El tercero " + persona
                        + " es persona NATURAL; la naturaleza no se puede cambiar a JURIDICA"));
        enviar("PUT", NATURALES + "/" + empresa, natural("999", "Dian", "Persona"))
                .andExpect(status().isBadRequest());

        assertThat(terceroRepositorio.findById(persona).orElseThrow()).isInstanceOf(PersonaNaturalEntity.class);
        mvc.perform(get(URL + "/" + empresa)).andExpect(jsonPath("$.naturaleza").value("JURIDICA"));
    }

    @Test
    void validaLasFechasDeLaPersonaNatural() throws Exception {
        PersonaNaturalDTOPeticion p = natural("1061000", "Carlos", "Ruiz");
        p.setFechaExpedicion(LocalDate.of(1985, 1, 1));
        enviar("POST", NATURALES, p)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La fecha de expedición del documento no puede ser anterior a la fecha de nacimiento"));

        p = natural("1061000", "Carlos", "Ruiz");
        p.setFechaNacimiento(LocalDate.now().plusDays(1));
        enviar("POST", NATURALES, p)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.fechaNacimiento").exists());
    }

    @Test
    void consultarDevuelveLaFormaSegunLaNaturaleza() throws Exception {
        long persona = crear(NATURALES, natural("1061000", "Carlos", "Ruiz"));
        long empresa = crear(JURIDICAS, juridica(NIT_DIAN, "DIAN"));

        mvc.perform(get(URL + "/" + persona))
                .andExpect(jsonPath("$.naturaleza").value("NATURAL"))
                .andExpect(jsonPath("$.nombres").value("Carlos"))
                .andExpect(jsonPath("$.razonSocial").doesNotExist());
        mvc.perform(get(URL + "/" + empresa))
                .andExpect(jsonPath("$.naturaleza").value("JURIDICA"))
                .andExpect(jsonPath("$.razonSocial").value("DIAN"))
                .andExpect(jsonPath("$.nombres").doesNotExist());
        mvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoError").value("GC-0003"));
    }

    @Test
    void inactivaYFiltraPorEstado() throws Exception {
        long id = crear(NATURALES, natural("1061000", "Carlos", "Ruiz"));
        crear(NATURALES, natural("1062000", "Diana", "López"));

        mvc.perform(patch(URL + "/" + id + "/estado").param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false))
                .andExpect(jsonPath("$.nombres").value("Carlos"));

        mvc.perform(get(URL).param("activo", "false")).andExpect(jsonPath("$.contenido[*].id", contains((int) id)));
        mvc.perform(get(URL).param("activo", "true")).andExpect(jsonPath("$.totalElementos").value(1));
        mvc.perform(patch(URL + "/999999/estado").param("activo", "false")).andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ búsqueda

    @Nested
    class Buscar {

        @BeforeEach
        void datos() throws Exception {
            crear(NATURALES, natural("1061000111", "Carlos", "Ruiz"));
            crear(NATURALES, natural("1061000222", "Ana", "Zapata"));
            crear(NATURALES, natural("94000333", "Beatriz", "Ruiz"));
            crear(JURIDICAS, juridica(NIT_DIAN, "Dirección de Impuestos"));
        }

        @Test
        void porPrefijoDeDocumento() throws Exception {
            mvc.perform(get(URL).param("documento", "1061"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(2))
                    .andExpect(jsonPath("$.contenido[*].nombreCompleto", contains("Ana Zapata", "Carlos Ruiz")));
            // Prefijo, no contenido: "000" está en medio de varios documentos pero no al inicio.
            mvc.perform(get(URL).param("documento", "000")).andExpect(jsonPath("$.totalElementos").value(0));
        }

        @Test
        void porNombreSinDistinguirMayusculasYOrdenadoPorNombre() throws Exception {
            mvc.perform(get(URL).param("nombre", "RUIZ"))
                    .andExpect(jsonPath("$.contenido[*].nombreCompleto", contains("Beatriz Ruiz", "Carlos Ruiz")));
            mvc.perform(get(URL).param("nombre", "impuestos"))
                    .andExpect(jsonPath("$.contenido[0].naturaleza").value("JURIDICA"))
                    .andExpect(jsonPath("$.contenido[0].dv").value("4"))
                    .andExpect(jsonPath("$.contenido[0].tipoIdentificacionEtiqueta").value("1 NIT"));
        }

        @Test
        void porNaturalezaYCombinandoFiltros() throws Exception {
            mvc.perform(get(URL).param("naturaleza", "JURIDICA")).andExpect(jsonPath("$.totalElementos").value(1));
            mvc.perform(get(URL).param("naturaleza", "NATURAL").param("nombre", "ruiz").param("documento", "94"))
                    .andExpect(jsonPath("$.contenido[*].nombreCompleto", contains("Beatriz Ruiz")));
        }

        @Test
        void paginaLosResultados() throws Exception {
            mvc.perform(get(URL).param("tamanio", "3").param("pagina", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contenido.length()").value(1))
                    .andExpect(jsonPath("$.pagina").value(1))
                    .andExpect(jsonPath("$.tamanio").value(3))
                    .andExpect(jsonPath("$.totalElementos").value(4))
                    .andExpect(jsonPath("$.totalPaginas").value(2))
                    .andExpect(jsonPath("$.primera").value(false))
                    .andExpect(jsonPath("$.ultima").value(true));
        }

        @Test
        void losComodinesDelUsuarioSeBuscanLiteralmente() throws Exception {
            mvc.perform(get(URL).param("nombre", "%")).andExpect(jsonPath("$.totalElementos").value(0));
            mvc.perform(get(URL).param("documento", "_")).andExpect(jsonPath("$.totalElementos").value(0));
            mvc.perform(get(URL).param("nombre", "  ")).andExpect(jsonPath("$.totalElementos").value(4));
        }

        @Test
        void rechazaParametrosDePaginacionInvalidos() throws Exception {
            mvc.perform(get(URL).param("tamanio", "101"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errores.tamanio").value("El tamaño máximo de página es 100"));
            mvc.perform(get(URL).param("pagina", "-1")).andExpect(status().isBadRequest());
            mvc.perform(get(URL).param("naturaleza", "MIXTA")).andExpect(status().isBadRequest());
        }
    }

    // ------------------------------------------------------------------ tipo de contenido

    @Test
    void rechazaCuerposQueNoSonJsonCon415() throws Exception {
        mvc.perform(post(NATURALES).contentType(MediaType.TEXT_PLAIN).content("nombres=Ana"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigoError").value("GC-0010"))
                .andExpect(jsonPath("$.codigoHttp").value(415));
        mvc.perform(put(JURIDICAS + "/1").contentType(MediaType.APPLICATION_FORM_URLENCODED).content("a=b"))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(post(JURIDICAS).content("{}"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
