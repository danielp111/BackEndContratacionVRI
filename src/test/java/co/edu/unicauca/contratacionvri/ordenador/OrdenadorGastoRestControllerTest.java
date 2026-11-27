package co.edu.unicauca.contratacionvri.ordenador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.Catalogo;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import co.edu.unicauca.contratacionvri.ordenador.accesoADatos.modelos.OrdenadorGastoEntity;
import co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.DTOPeticion.OrdenadorGastoDTOPeticion;
import co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.servicios.IOrdenadorGastoServicio;
import co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.servicios.UsoOrdenadorChecker;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.PersonaJuridicaEntity;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.PersonaNaturalEntity;

class OrdenadorGastoRestControllerTest extends PruebaIntegracionBase {

    private static final String URL = "/api/ordenadores-gasto";
    private static final String VRI = "Vicerrector de Investigaciones";

    /** Espía sobre el checker provisional: por defecto "sin contratos"; en la prueba de la regla, "con contratos". */
    @MockitoSpyBean
    private UsoOrdenadorChecker usoChecker;

    @Autowired
    private IOrdenadorGastoServicio servicio;

    @Autowired
    private TransactionTemplate transaccion;

    private CatalogoItemEntity cedula;
    private PersonaNaturalEntity pino;
    private PersonaNaturalEntity hurtado;

    @BeforeEach
    void personas() {
        cedula = item(Catalogo.TIPO_IDENTIFICACION, "3", "CÉDULA DE CIUDADANÍA");
        pino = personaNatural("76314448", "FRANCISCO JOSE", "PINO CORREA");
        hurtado = personaNatural("76311561", "DEIBAR RENE", "HURTADO HERRERA");
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

    private OrdenadorGastoDTOPeticion periodo(PersonaNaturalEntity t, String cargo, String desde, String hasta) {
        return OrdenadorGastoDTOPeticion.builder().terceroId(t.getId()).cargo(cargo)
                .fechaDesde(LocalDate.parse(desde)).fechaHasta(hasta == null ? null : LocalDate.parse(hasta)).build();
    }

    private ResultActions enviar(String metodo, String url, Object cuerpo) throws Exception {
        var peticion = switch (metodo) {
            case "POST" -> post(url);
            case "PUT" -> put(url);
            default -> patch(url);
        };
        String texto = cuerpo instanceof String s ? s : json.writeValueAsString(cuerpo);
        return mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(texto));
    }

    // ------------------------------------------------------------------ crear

    @Nested
    class Crear {

        @Test
        void devuelveLosDatosDelTerceroListosParaElSireci() throws Exception {
            enviar("POST", URL, "{\"terceroId\": %d, \"cargo\": \"  Vicerrector   de Investigaciones \", \"fechaDesde\": \"2026-01-01\"}"
                    .formatted(pino.getId()))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.terceroId").value(pino.getId()))
                    .andExpect(jsonPath("$.nombre").value("FRANCISCO JOSE PINO CORREA"))
                    .andExpect(jsonPath("$.cedula").value("76314448"))
                    .andExpect(jsonPath("$.tipoIdentificacion").value("3 CÉDULA DE CIUDADANÍA"))
                    .andExpect(jsonPath("$.cargo").value(VRI))
                    .andExpect(jsonPath("$.esDelegado").value(false))
                    .andExpect(jsonPath("$.actoDelegacion").value(nullValue()))
                    .andExpect(jsonPath("$.fechaDesde").value("2026-01-01"))
                    .andExpect(jsonPath("$.fechaHasta").value(nullValue()))
                    .andExpect(jsonPath("$.vigente").value(true))
                    .andExpect(jsonPath("$.activo").value(true))
                    .andExpect(jsonPath("$.etiqueta").value("FRANCISCO JOSE PINO CORREA - " + VRI))
                    .andExpect(jsonPath("$.datosEditables").value(true));
        }

        @Test
        void elTerceroDebeExistirSerPersonaNaturalYEstarActivo() throws Exception {
            OrdenadorGastoDTOPeticion p = periodo(pino, VRI, "2026-01-01", null);
            p.setTerceroId(999_999L);
            enviar("POST", URL, p).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El ordenador del gasto con id 999999 no existe"));

            PersonaJuridicaEntity empresa = new PersonaJuridicaEntity();
            empresa.setTipoIdentificacion(cedula);
            empresa.setNumeroDocumento("800197268");
            empresa.setRazonSocial("DIAN");
            empresa.setActivo(true);
            p.setTerceroId(terceroRepositorio.save(empresa).getId());
            enviar("POST", URL, p).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("debe ser una persona natural")));

            hurtado.setActivo(false);
            terceroRepositorio.save(hurtado);
            enviar("POST", URL, periodo(hurtado, "Rector", "2026-01-01", null)).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El ordenador del gasto DEIBAR RENE HURTADO HERRERA está inactivo"));
            assertThat(ordenadorGastoRepositorio.count()).isZero();
        }

        @Test
        void fechaHastaNoPuedeSerAnteriorAFechaDesde() throws Exception {
            enviar("POST", URL, periodo(pino, VRI, "2026-06-30", "2026-06-29"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("GC-0004"))
                    .andExpect(jsonPath("$.mensaje").value("La fecha hasta (2026-06-29) no puede ser anterior a la fecha desde (2026-06-30)"));
            // El mismo día sí: un periodo de un solo día.
            enviar("POST", URL, periodo(pino, VRI, "2026-06-30", "2026-06-30")).andExpect(status().isCreated());
        }

        @Test
        void elActoDeDelegacionEsObligatorioSoloSiEsDelegado() throws Exception {
            OrdenadorGastoDTOPeticion delegadoSinActo = periodo(pino, "Rector Delegatario", "2026-01-01", null);
            delegadoSinActo.setEsDelegado(true);
            enviar("POST", URL, delegadoSinActo).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("el acto de delegación")));

            OrdenadorGastoDTOPeticion actoSinDelegacion = periodo(pino, VRI, "2026-01-01", null);
            actoSinDelegacion.setActoDelegacion("Resolución 0123 de 2026");
            enviar("POST", URL, actoSinDelegacion).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El acto de delegación solo aplica cuando el ordenador firma por delegación"));

            delegadoSinActo.setActoDelegacion("  Resolución   0123 del 1 de febrero de 2026 ");
            enviar("POST", URL, delegadoSinActo).andExpect(status().isCreated())
                    .andExpect(jsonPath("$.esDelegado").value(true))
                    .andExpect(jsonPath("$.actoDelegacion").value("Resolución 0123 del 1 de febrero de 2026"));
        }

        @Test
        void validaLosCamposObligatorios() throws Exception {
            enviar("POST", URL, "{\"terceroId\": null, \"cargo\": \"\", \"fechaDesde\": null}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("GC-0005"))
                    .andExpect(jsonPath("$.errores.terceroId").exists())
                    .andExpect(jsonPath("$.errores.cargo").value("El cargo es obligatorio"))
                    .andExpect(jsonPath("$.errores.fechaDesde").value("La fecha desde es obligatoria"));
            enviar("POST", URL, "{\"terceroId\": %d, \"cargo\": \"X\", \"fechaDesde\": \"2026-02-30\"}".formatted(pino.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("GC-0006"));
        }
    }

    // ------------------------------------------------------------------ periodos que se cruzan

    @Nested
    class Cruces {

        private long abierto;

        @BeforeEach
        void periodoAbierto() throws Exception {
            abierto = crear(URL, periodo(pino, VRI, "2026-01-01", null));
        }

        @Test
        void elMismoTerceroYCargoNoPuedenTenerPeriodosQueSeCrucen() throws Exception {
            enviar("POST", URL, periodo(pino, VRI, "2026-06-01", null))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.codigoError").value("GC-0002"))
                    .andExpect(jsonPath("$.mensaje").value(containsString("ya tiene un periodo activo como " + VRI)));
            // Un periodo anterior que termina justo antes del abierto sí se permite (no se cruzan).
            enviar("POST", URL, periodo(pino, VRI, "2025-01-01", "2025-12-31")).andExpect(status().isCreated());
            // Termina el mismo día en que empieza el abierto: se cruzan en ese día.
            enviar("POST", URL, periodo(pino, VRI, "2024-01-01", "2026-01-01")).andExpect(status().isConflict());
        }

        @Test
        void elCargoSeComparaSinMayusculas() throws Exception {
            enviar("POST", URL, periodo(pino, "VICERRECTOR DE INVESTIGACIONES", "2026-03-01", null)).andExpect(status().isConflict());
        }

        @Test
        void otroCargoUOtroTerceroNoSeCruzan() throws Exception {
            enviar("POST", URL, periodo(pino, "Rector Delegatario", "2026-02-01", "2026-02-28")).andExpect(status().isCreated());
            enviar("POST", URL, periodo(hurtado, VRI, "2026-02-01", null)).andExpect(status().isCreated());
        }

        @Test
        void unPeriodoInactivoNoBloqueaPeroAlReactivarloSiSeValida() throws Exception {
            long viejo = crear(URL, periodo(pino, VRI, "2025-01-01", "2025-12-31"));
            mvc.perform(patch(URL + "/" + viejo + "/estado").param("activo", "false")).andExpect(status().isOk());

            // El inactivo no cuenta: se puede crear uno que se cruce con él.
            enviar("POST", URL, periodo(pino, VRI, "2025-06-01", "2025-06-30")).andExpect(status().isCreated());
            // Reactivarlo ahora lo haría cruzarse con el de junio de 2025.
            mvc.perform(patch(URL + "/" + viejo + "/estado").param("activo", "true"))
                    .andExpect(status().isConflict());
        }

        @Test
        void actualizarNoChocaConsigoMismoPeroSiConOtro() throws Exception {
            OrdenadorGastoDTOPeticion cambio = periodo(pino, VRI, "2026-01-01", "2026-12-31");
            enviar("PUT", URL + "/" + abierto, cambio).andExpect(status().isOk())
                    .andExpect(jsonPath("$.fechaHasta").value("2026-12-31"));

            long otro = crear(URL, periodo(pino, VRI, "2027-01-01", null));
            enviar("PUT", URL + "/" + otro, periodo(pino, VRI, "2026-12-01", null)).andExpect(status().isConflict());
        }
    }

    // ------------------------------------------------------------------ actualizar con contratos y cerrar

    @Test
    void conContratosNoCambianTerceroCargoNiFechaDesdePeroSiSeCierra() throws Exception {
        long id = crear(URL, periodo(pino, VRI, "2026-01-01", null));
        doReturn(true).when(usoChecker).existenContratosConOrdenador(id);

        mvc.perform(get(URL + "/" + id)).andExpect(jsonPath("$.datosEditables").value(false));
        enviar("PUT", URL + "/" + id, periodo(hurtado, VRI, "2026-01-01", null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("ya tiene contratos")));
        enviar("PUT", URL + "/" + id, periodo(pino, "Rector", "2026-01-01", null)).andExpect(status().isBadRequest());
        enviar("PUT", URL + "/" + id, periodo(pino, VRI, "2026-02-01", null)).andExpect(status().isBadRequest());

        // Lo demás sí: delegación y fecha hasta, y cerrar el periodo.
        OrdenadorGastoDTOPeticion delegado = periodo(pino, VRI, "2026-01-01", null);
        delegado.setEsDelegado(true);
        delegado.setActoDelegacion("Resolución 0123 de 2026");
        enviar("PUT", URL + "/" + id, delegado).andExpect(status().isOk()).andExpect(jsonPath("$.esDelegado").value(true));
        enviar("PATCH", URL + "/" + id + "/cerrar", "{\"fechaHasta\": \"2026-06-30\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaHasta").value("2026-06-30"));
    }

    @Test
    void cerrarElPeriodo() throws Exception {
        long id = crear(URL, periodo(pino, VRI, "2026-01-01", null));

        enviar("PATCH", URL + "/" + id + "/cerrar", "{\"fechaHasta\": \"2025-12-31\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("no puede ser anterior a la fecha desde")));
        enviar("PATCH", URL + "/" + id + "/cerrar", "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.fechaHasta").value("La fecha hasta es obligatoria para cerrar el periodo"));

        // Cerrado antes de hoy (2026-11-26 o después): ya no está vigente.
        enviar("PATCH", URL + "/" + id + "/cerrar", "{\"fechaHasta\": \"2026-06-30\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaHasta").value("2026-06-30"))
                .andExpect(jsonPath("$.vigente").value(false))
                .andExpect(jsonPath("$.activo").value(true));
    }

    // ------------------------------------------------------------------ listar

    @Nested
    class Listar {

        @BeforeEach
        void periodos() throws Exception {
            crear(URL, periodo(pino, VRI, "2026-01-01", "2026-06-30"));
            crear(URL, periodo(hurtado, "Rector", "2026-01-01", null));
            PersonaNaturalEntity villada = personaNatural("7551810", "HECTOR SAMUEL", "VILLADA CASTILLO");
            crear(URL, periodo(villada, VRI, "2020-01-01", "2022-12-31"));
            long delegatario = crear(URL, periodo(pino, "Rector Delegatario", "2026-03-01", "2026-03-15"));
            mvc.perform(patch(URL + "/" + delegatario + "/estado").param("activo", "false")).andExpect(status().isOk());
        }

        @Test
        void vigentesEnUnaFechaIncluyeLosBordesYExcluyeInactivos() throws Exception {
            mvc.perform(get(URL).param("vigenteEn", "2026-01-01"))
                    .andExpect(jsonPath("$[*].etiqueta", containsInAnyOrder(
                            "DEIBAR RENE HURTADO HERRERA - Rector", "FRANCISCO JOSE PINO CORREA - " + VRI)));
            mvc.perform(get(URL).param("vigenteEn", "2026-06-30")).andExpect(jsonPath("$.length()").value(2));
            mvc.perform(get(URL).param("vigenteEn", "2026-07-01"))
                    .andExpect(jsonPath("$[*].nombre", containsInAnyOrder("DEIBAR RENE HURTADO HERRERA")));
            // El de Rector Delegatario cubre el 10 de marzo pero está inactivo.
            mvc.perform(get(URL).param("vigenteEn", "2026-03-10"))
                    .andExpect(jsonPath("$[*].cargo", containsInAnyOrder("Rector", VRI)));
            mvc.perform(get(URL).param("vigenteEn", "2021-05-01"))
                    .andExpect(jsonPath("$[*].nombre", containsInAnyOrder("HECTOR SAMUEL VILLADA CASTILLO")));
            mvc.perform(get(URL).param("vigenteEn", "2026-13-01")).andExpect(status().isBadRequest());
        }

        @Test
        void buscaPorNombreCedulaOCargoYFiltraPorEstado() throws Exception {
            mvc.perform(get(URL).param("texto", "pino")).andExpect(jsonPath("$.length()").value(2));
            mvc.perform(get(URL).param("texto", "7631"))
                    .andExpect(jsonPath("$[*].cedula", containsInAnyOrder("76314448", "76314448", "76311561")));
            mvc.perform(get(URL).param("texto", "delegatario")).andExpect(jsonPath("$[*].cargo", containsInAnyOrder("Rector Delegatario")));
            mvc.perform(get(URL).param("activo", "false")).andExpect(jsonPath("$[*].cargo", containsInAnyOrder("Rector Delegatario")));
            mvc.perform(get(URL)).andExpect(jsonPath("$.length()").value(4));
        }
    }

    // ------------------------------------------------------------------ uso desde el contrato

    @Test
    void laFechaDeSuscripcionDebeCaerDentroDelPeriodo() throws Exception {
        long id = crear(URL, periodo(pino, VRI, "2026-01-01", "2026-06-30"));

        assertThat(servicio.obtenerOrdenadorValido(id, null, LocalDate.parse("2026-06-30")).getId()).isEqualTo(id);
        assertThatThrownBy(() -> servicio.obtenerOrdenadorValido(id, null, LocalDate.parse("2026-07-01")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no está dentro del periodo");

        // Aunque sea el mismo que ya tenía el contrato, la fecha se valida. El contrato llamará este método
        // dentro de su transacción, con el ordenador ya cargado en ella: aquí se imita con una transacción.
        transaccion.executeWithoutResult(estado -> {
            OrdenadorGastoEntity actual = ordenadorGastoRepositorio.findById(id).orElseThrow();
            assertThatThrownBy(() -> servicio.obtenerOrdenadorValido(id, actual, LocalDate.parse("2025-12-31")))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("2026-01-01 a 2026-06-30");
            // La excepción del servicio marcó la transacción para revertir; solo se leyó, así que se revierte.
            estado.setRollbackOnly();
        });

        // Inactivo: no sirve para contratos nuevos, pero el que ya lo tenía lo conserva.
        servicio.cambiarEstado(id, false);
        assertThatThrownBy(() -> servicio.obtenerOrdenadorValido(id, null, LocalDate.parse("2026-03-01")))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("está inactivo");
        transaccion.executeWithoutResult(estado -> {
            OrdenadorGastoEntity actual = ordenadorGastoRepositorio.findById(id).orElseThrow();
            assertThat(servicio.obtenerOrdenadorValido(id, actual, LocalDate.parse("2026-03-01"))).isSameAs(actual);
        });
    }

    // ------------------------------------------------------------------ errores HTTP

    @Test
    void erroresHttp() throws Exception {
        mvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoError").value("GC-0003"));
        enviar("PUT", URL + "/999999", periodo(pino, VRI, "2026-01-01", null)).andExpect(status().isNotFound());
        enviar("PATCH", URL + "/999999/cerrar", "{\"fechaHasta\": \"2026-06-30\"}").andExpect(status().isNotFound());
        mvc.perform(patch(URL + "/999999/estado").param("activo", "false")).andExpect(status().isNotFound());
        mvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("Pino"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigoError").value("GC-0010"));
    }
}
