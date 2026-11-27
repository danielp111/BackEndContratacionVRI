package co.edu.unicauca.contratacionvri.contrato;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import co.edu.unicauca.contratacionvri.ordenador.accesoADatos.modelos.OrdenadorGastoEntity;
import co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.servicios.UsoOrdenadorChecker;
import co.edu.unicauca.contratacionvri.rubro.fachadaServicios.servicios.UsoRubroChecker;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios.UsoTipoContratoChecker;

class ContratoRestControllerTest extends ContratoPruebaBase {

    // ------------------------------------------------------------------ crear

    @Nested
    class Crear {

        @Test
        void creaEnTramiteConElSiguienteNumeroDeLaSerie() throws Exception {
            consecutivo(serieOps, ANIO, 108);
            enviar("POST", URL, minimo())
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/contratos/")))
                    .andExpect(jsonPath("$.numero").value("6.1-17.13/109"))
                    .andExpect(jsonPath("$.serieCodigo").value("6.1-17.13"))
                    .andExpect(jsonPath("$.consecutivo").value(109))
                    .andExpect(jsonPath("$.anio").value(ANIO))
                    .andExpect(jsonPath("$.estado").value("EN_TRAMITE"))
                    .andExpect(jsonPath("$.tipoContratoNombre").value("Prestación de servicios"))
                    .andExpect(jsonPath("$.responsableNombre").value("Laura Muñoz"))
                    .andExpect(jsonPath("$.objeto").value("Prestar servicios profesionales de apoyo al proyecto 5019"))
                    .andExpect(jsonPath("$.contratistaId").value(nullValue()))
                    .andExpect(jsonPath("$.valorInicial").value(nullValue()))
                    .andExpect(jsonPath("$.presupuesto.rubroId").value(nullValue()))
                    .andExpect(jsonPath("$.observacionesPendientes").value(0))
                    .andExpect(jsonPath("$.advertencias", empty()));
        }

        @Test
        void losTiposQueCompartenSerieNoRepitenNumero() throws Exception {
            Map<String, Object> p = minimo();
            p.put("tipoContratoId", pasantia.getId());
            assertThat(crearContrato(p).get("numero").asText()).isEqualTo("6.1-17.3/001");
            p.put("tipoContratoId", judicatura.getId());
            assertThat(crearContrato(p).get("numero").asText()).isEqualTo("6.1-17.3/002");
        }

        @Test
        void siLaValidacionFallaElNumeroNoSeConsume() throws Exception {
            Map<String, Object> p = minimo();
            p.put("claseContratoId", servicios.getId());
            p.put("otraClase", "No aplica");
            enviar("POST", URL, p).andExpect(status().isBadRequest());
            assertThat(crearContrato(minimo()).get("numero").asText()).isEqualTo("6.1-17.13/001");
        }

        @Test
        void soloAceptaElAnioActualYElAnterior() throws Exception {
            Map<String, Object> p = minimo();
            p.put("anio", ANIO - 1);
            assertThat(crearContrato(p).get("numero").asText()).isEqualTo("6.1-17.13/001");
            for (int anio : new int[]{ANIO - 2, ANIO + 1}) {
                p.put("anio", anio);
                enviar("POST", URL, p)
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.mensaje").value("Solo se pueden crear contratos de los años " + (ANIO - 1) + " a " + ANIO));
            }
        }

        @Test
        void exigeTipoAnioResponsableYObjeto() throws Exception {
            enviar("POST", URL, "{\"objeto\": \"   \"}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errores.tipoContratoId").value("El tipo de contrato es obligatorio"))
                    .andExpect(jsonPath("$.errores.anio").value("El año del contrato es obligatorio"))
                    .andExpect(jsonPath("$.errores.responsableId").value("El abogado responsable es obligatorio"))
                    .andExpect(jsonPath("$.errores.objeto").value("El objeto del contrato es obligatorio"));
            Map<String, Object> p = minimo();
            p.put("objeto", "x".repeat(391));
            enviar("POST", URL, p).andExpect(jsonPath("$.errores.objeto").value("El objeto admite máximo 390 caracteres"));
        }

        @Test
        void rechazaTipoInactivoOInexistenteYResponsableInactivo() throws Exception {
            ops.setActivo(false);
            tipoContratoRepositorio.save(ops);
            enviar("POST", URL, minimo()).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("está inactivo")));

            Map<String, Object> p = minimo();
            p.put("tipoContratoId", 999999);
            enviar("POST", URL, p).andExpect(jsonPath("$.mensaje").value("El tipo de contrato con id 999999 no existe"));

            p.put("tipoContratoId", pasantia.getId());
            laura.setActivo(false);
            abogadoRepositorio.save(laura);
            enviar("POST", URL, p).andExpect(jsonPath("$.mensaje").value("El abogado Laura Muñoz está inactivo"));
        }

        @Test
        void conTodosLosDatosYLaSuscripcionQuedaSuscritoConSusCalculados() throws Exception {
            Map<String, Object> p = completo();
            p.put("iva", 1900000);
            p.put("fechaSuscripcion", fecha(3, 10));
            presupuestoDe(p).put("tipoAnticipoId", anticipo.getId());
            presupuestoDe(p).put("valorAnticipo", 1190000);
            enviar("POST", URL, p)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.estado").value("SUSCRITO"))
                    .andExpect(jsonPath("$.valorInicial").value(11900000.0))
                    .andExpect(jsonPath("$.claseContratoEtiqueta").value("23 PRESTACIÓN DE SERVICIOS"))
                    .andExpect(jsonPath("$.contratistaNombre").value("Ana María Gómez López"))
                    .andExpect(jsonPath("$.contratistaDocumento").value("1061111111"))
                    .andExpect(jsonPath("$.ordenadorNombre").value("FRANCISCO JOSE PINO CORREA"))
                    .andExpect(jsonPath("$.ordenadorCedula").value("76314448"))
                    .andExpect(jsonPath("$.ordenadorCargo").value("Vicerrector de Investigaciones"))
                    .andExpect(jsonPath("$.tipoSeguimientoEtiqueta").value("2 SUPERVISOR"))
                    .andExpect(jsonPath("$.numeroPagos").value(4))
                    .andExpect(jsonPath("$.presupuesto.rubroCodigo").value("1052002"))
                    .andExpect(jsonPath("$.presupuesto.dependenciaAfectacion").value("Museo de Historia Natural"))
                    .andExpect(jsonPath("$.presupuesto.proyectoId").value(5019))
                    .andExpect(jsonPath("$.presupuesto.fuenteFinanciacionEtiqueta").value("Inversión"))
                    .andExpect(jsonPath("$.presupuesto.dependenciaSolicitanteNombre").value("Vicerrectoría de Investigaciones"))
                    .andExpect(jsonPath("$.presupuesto.naturalezaGasto").value("SPI"))
                    .andExpect(jsonPath("$.presupuesto.valorAnticipo").value(1190000.0))
                    .andExpect(jsonPath("$.presupuesto.porcentajeAnticipo").value(10.0));
        }

        @Test
        void rechazaCuerpoQueNoEsJson() throws Exception {
            mvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("contrato"))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    // ------------------------------------------------------------------ reglas al guardar

    @Nested
    class Reglas {

        @Test
        void laClase30OtrosExigeDescribirlaYLasDemasNoLaAdmiten() throws Exception {
            long id = crearContratoId(minimo());
            Map<String, Object> p = minimo();
            p.put("claseContratoId", otros.getId());
            enviar("PUT", URL + "/" + id, p)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("La clase 30 OTROS exige describir la otra clase de contrato"));
            p.put("otraClase", "  Convenio   de uso ");
            enviar("PUT", URL + "/" + id, p).andExpect(status().isOk()).andExpect(jsonPath("$.otraClase").value("Convenio de uso"));

            p.put("claseContratoId", servicios.getId());
            enviar("PUT", URL + "/" + id, p)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("La descripción de otra clase de contrato solo aplica cuando la clase es 30 OTROS"));
        }

        @Test
        void laSuscripcionExigeElContratoCompleto() throws Exception {
            long id = crearContratoId(minimo());
            Map<String, Object> p = minimo();
            p.put("fechaSuscripcion", fecha(3, 10));
            enviar("PUT", URL + "/" + id, p)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("Para registrar la suscripción faltan: clase de contrato, contratista, "
                            + "ordenador del gasto, tipo de seguimiento, valor sin IVA, IVA, plazo, código de afectación presupuestal, "
                            + "fuente de financiación, tipo de anticipo, dependencia que solicita"));

            // En trámite sí se guarda incompleto.
            p.remove("fechaSuscripcion");
            p.put("contratistaId", ana.getId());
            enviar("PUT", URL + "/" + id, p).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("EN_TRAMITE"));
        }

        @Test
        void lasFechasSeEncadenanYLaSuscripcionCaeEnElAnio() throws Exception {
            long id = crearContratoId(minimo());
            Map<String, Object> p = completo();
            p.put("fechaInicio", fecha(3, 12));
            enviar("PUT", URL + "/" + id, p)
                    .andExpect(jsonPath("$.mensaje").value("La fecha de inicio exige la fecha de suscripción"));

            p.put("fechaSuscripcion", fecha(3, 15));
            enviar("PUT", URL + "/" + id, p).andExpect(jsonPath("$.mensaje").value(
                    "La fecha de inicio (" + fecha(3, 12) + ") no puede ser anterior a la fecha de suscripción (" + fecha(3, 15) + ")"));

            p.remove("fechaInicio");
            p.put("fechaSuscripcion", LocalDate.of(ANIO - 1, 12, 30).toString());
            enviar("PUT", URL + "/" + id, p).andExpect(jsonPath("$.mensaje").value(
                    "La fecha de suscripción (" + (ANIO - 1) + "-12-30) debe estar dentro del año del contrato (" + ANIO + ")"));

            p.put("fechaSuscripcion", fecha(3, 10));
            p.put("fechaTerminacion", fecha(7, 10));
            enviar("PUT", URL + "/" + id, p)
                    .andExpect(jsonPath("$.mensaje").value("La fecha de terminación exige la fecha de inicio"));
        }

        @Test
        void laSuscripcionDebeCaerEnElPeriodoDelOrdenador() throws Exception {
            OrdenadorGastoEntity rector = new OrdenadorGastoEntity();
            rector.setTercero(pedro);
            rector.setCargo("Rector");
            rector.setFechaDesde(LocalDate.of(ANIO, 4, 1));
            rector = ordenadorGastoRepositorio.save(rector);

            Map<String, Object> p = completo();
            p.put("ordenadorGastoId", rector.getId());
            p.put("fechaSuscripcion", fecha(3, 10));
            enviar("POST", URL, p)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("no está dentro del periodo de Pedro Pérez - Rector")));
        }

        @Test
        void recursosDeConvenioExigenUnaEntidadConNit() throws Exception {
            long id = crearContratoId(minimo());
            Map<String, Object> p = minimo();
            p.put("presupuesto", Map.of("recursosDeConvenio", true));
            enviar("PUT", URL + "/" + id, p).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("la entidad de donde provienen es obligatoria")));

            p.put("presupuesto", Map.of("recursosDeConvenio", true, "entidadFinanciadoraId", pedro.getId()));
            enviar("PUT", URL + "/" + id, p).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("La entidad de donde provienen los recursos debe tener NIT y Pedro Pérez no lo tiene"));

            p.put("presupuesto", Map.of("recursosDeConvenio", false, "entidadFinanciadoraId", dian.getId()));
            enviar("PUT", URL + "/" + id, p).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("solo aplica si provienen")));

            p.put("presupuesto", Map.of("recursosDeConvenio", true, "entidadFinanciadoraId", dian.getId()));
            enviar("PUT", URL + "/" + id, p).andExpect(status().isOk())
                    .andExpect(jsonPath("$.presupuesto.entidadFinanciadoraNombre").value("DIAN"))
                    .andExpect(jsonPath("$.presupuesto.entidadFinanciadoraNit").value("800197268"))
                    .andExpect(jsonPath("$.presupuesto.entidadFinanciadoraDv").value("4"));
        }

        @Test
        void elAnticipoNoPactadoValeCeroYLosDemasExigenUnValorQueNoSupereElInicial() throws Exception {
            long id = crearContratoId(minimo());
            Map<String, Object> p = completo();
            presupuestoDe(p).put("valorAnticipo", 5);
            enviar("PUT", URL + "/" + id, p).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("Con anticipo '3 NO PACTADOS' el valor del anticipo debe ser 0"));

            presupuestoDe(p).remove("valorAnticipo");
            enviar("PUT", URL + "/" + id, p).andExpect(status().isOk())
                    .andExpect(jsonPath("$.presupuesto.valorAnticipo").value(0))
                    .andExpect(jsonPath("$.presupuesto.porcentajeAnticipo").value(0.0));

            presupuestoDe(p).put("tipoAnticipoId", anticipo.getId());
            enviar("PUT", URL + "/" + id, p).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El anticipo '1 ANTICIPO' exige su valor"));

            presupuestoDe(p).put("valorAnticipo", 10000001);
            enviar("PUT", URL + "/" + id, p).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("no puede superar el valor inicial")));

            presupuestoDe(p).put("valorAnticipo", 2500000);
            enviar("PUT", URL + "/" + id, p).andExpect(status().isOk())
                    .andExpect(jsonPath("$.presupuesto.porcentajeAnticipo").value(25.0));

            Map<String, Object> sinTipo = minimo();
            sinTipo.put("presupuesto", Map.of("valorAnticipo", 100));
            enviar("PUT", URL + "/" + id, sinTipo).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("Indique el tipo de anticipo para registrar su valor"));
        }

        @Test
        void elTipoYElAnioNoCambian() throws Exception {
            long id = crearContratoId(minimo());
            Map<String, Object> p = minimo();
            p.put("tipoContratoId", pasantia.getId());
            enviar("PUT", URL + "/" + id, p).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("El tipo de contrato no se puede cambiar")));
            p.put("tipoContratoId", ops.getId());
            p.put("anio", ANIO - 1);
            enviar("PUT", URL + "/" + id, p).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("no se puede cambiar")));
            p.put("anio", ANIO);
            enviar("PUT", URL + "/" + id, p).andExpect(status().isOk());
        }

        @Test
        void unaRelacionQueNoCambiaNoSeVuelveAValidar() throws Exception {
            Map<String, Object> p = minimo();
            p.put("contratistaId", ana.getId());
            long id = crearContratoId(p);
            ana.setActivo(false);
            terceroRepositorio.save(ana);

            enviar("PUT", URL + "/" + id, p).andExpect(status().isOk());
            p.put("contratistaId", pedro.getId());
            enviar("PUT", URL + "/" + id, p).andExpect(status().isOk());
            p.put("contratistaId", ana.getId());
            enviar("PUT", URL + "/" + id, p).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El contratista Ana María Gómez López está inactivo"));
        }

        @Test
        void avisaSinBloquearSiElContratistaYaTieneOtroContratoEnElAnio() throws Exception {
            Map<String, Object> p = minimo();
            p.put("contratistaId", ana.getId());
            String primero = crearContrato(p).get("numero").asText();
            enviar("POST", URL, p)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.advertencias", hasSize(1)))
                    .andExpect(jsonPath("$.advertencias[0]").value("El contratista Ana María Gómez López ya tiene 1 contrato en "
                            + ANIO + " (" + primero + "): posible fraccionamiento"));
        }

        @Test
        void contratoInexistenteResponde404() throws Exception {
            mvc.perform(get(URL + "/999999")).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.mensaje").value("No existe un contrato con id 999999"));
            enviar("PUT", URL + "/999999", minimo()).andExpect(status().isNotFound());
        }
    }

    // ------------------------------------------------------------------ estados y cierre

    @Nested
    class Estados {

        @Test
        void elEstadoSigueLasFechasHastaLiquidado() throws Exception {
            long id = crearContratoId(minimo());
            Map<String, Object> p = completo();
            p.put("fechaSuscripcion", fecha(3, 10));
            enviar("PUT", URL + "/" + id, p).andExpect(jsonPath("$.estado").value("SUSCRITO"));
            p.put("fechaInicio", fecha(3, 12));
            enviar("PUT", URL + "/" + id, p).andExpect(jsonPath("$.estado").value("EN_EJECUCION"));

            enviar("PATCH", URL + "/" + id + "/cierre", Map.of("fechaTerminacion", fecha(7, 10)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("TERMINADO"));
            enviar("PATCH", URL + "/" + id + "/cierre", Map.of("fechaTerminacion", fecha(7, 10),
                    "fechaActaFinal", fecha(7, 25), "fechaCertificacionPago", fecha(7, 20)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("LIQUIDADO"))
                    .andExpect(jsonPath("$.fechaActaFinal").value(fecha(7, 25)))
                    .andExpect(jsonPath("$.presupuesto.fechaCertificacionPago").value(fecha(7, 20)));

            // Quitar el acta final devuelve el contrato a terminado.
            enviar("PATCH", URL + "/" + id + "/cierre", Map.of("fechaTerminacion", fecha(7, 10)))
                    .andExpect(jsonPath("$.estado").value("TERMINADO"));
        }

        @Test
        void elAbogadoTambienRegistraLaTerminacion() throws Exception {
            Map<String, Object> p = completo();
            p.put("fechaSuscripcion", fecha(3, 10));
            p.put("fechaInicio", fecha(3, 12));
            p.put("fechaTerminacion", fecha(7, 10));
            enviar("POST", URL, p).andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("TERMINADO"));
        }

        @Test
        void elCierreRespetaLaCadenaDeFechas() throws Exception {
            long id = crearContratoId(minimo());
            enviar("PATCH", URL + "/" + id + "/cierre", Map.of("fechaActaFinal", fecha(7, 25)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("La fecha de acta final exige la fecha de terminación"));
            enviar("PATCH", URL + "/" + id + "/cierre", Map.of("fechaTerminacion", fecha(7, 10)))
                    .andExpect(jsonPath("$.mensaje").value("La fecha de terminación exige la fecha de inicio"));
        }
    }

    // ------------------------------------------------------------------ anular

    @Nested
    class Anular {

        @Test
        void anulaUnContratoEnTramiteYSuNumeroNoSeReutiliza() throws Exception {
            long id = crearContratoId(minimo());
            enviar("PATCH", URL + "/" + id + "/anular", Map.of("motivo", "  El contratista   desistió "))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("ANULADO"))
                    .andExpect(jsonPath("$.motivoAnulacion").value("El contratista desistió"))
                    .andExpect(jsonPath("$.fechaAnulacion").value(LocalDate.now(ZoneId.of("America/Bogota")).toString()));

            enviar("PUT", URL + "/" + id, minimo()).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El contrato 6.1-17.13/001 está anulado y no se puede modificar"));
            enviar("PATCH", URL + "/" + id + "/cierre", Map.of()).andExpect(status().isBadRequest());
            enviar("PATCH", URL + "/" + id + "/anular", Map.of("motivo", "Otra vez")).andExpect(status().isBadRequest());

            assertThat(crearContrato(minimo()).get("numero").asText()).isEqualTo("6.1-17.13/002");
        }

        @Test
        void unContratoAnuladoNoCuentaParaElFraccionamiento() throws Exception {
            Map<String, Object> p = minimo();
            p.put("contratistaId", ana.getId());
            long id = crearContratoId(p);
            enviar("PATCH", URL + "/" + id + "/anular", Map.of("motivo", "No se usó")).andExpect(status().isOk());
            enviar("POST", URL, p).andExpect(jsonPath("$.advertencias", empty()));
        }

        @Test
        void soloSeAnulaEnTramiteYConMotivo() throws Exception {
            Map<String, Object> p = completo();
            p.put("fechaSuscripcion", fecha(3, 10));
            long id = crearContratoId(p);
            enviar("PATCH", URL + "/" + id + "/anular", Map.of("motivo", "Error"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("Solo se puede anular un contrato en trámite; el contrato 6.1-17.13/001 está SUSCRITO"));
            enviar("PATCH", URL + "/" + id + "/anular", Map.of("motivo", " "))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errores.motivo").value("El motivo de la anulación es obligatorio"));
        }
    }

    // ------------------------------------------------------------------ uso por otros dominios

    @Nested
    class Uso {

        @Autowired
        private UsoTipoContratoChecker usoTipo;

        @Autowired
        private UsoRubroChecker usoRubro;

        @Autowired
        private UsoOrdenadorChecker usoOrdenador;

        @Test
        void losCheckersConsultanLosContratos() throws Exception {
            assertThat(usoTipo.existenContratosConTipo(ops.getId())).isFalse();
            assertThat(usoTipo.existenContratosConSerie(serieOps.getId())).isFalse();
            assertThat(usoRubro.existenContratosConRubro(rubro.getId())).isFalse();
            assertThat(usoOrdenador.existenContratosConOrdenador(vicerrector.getId())).isFalse();

            crearContratoId(completo());

            assertThat(usoTipo.existenContratosConTipo(ops.getId())).isTrue();
            assertThat(usoTipo.existenContratosConTipo(pasantia.getId())).isFalse();
            assertThat(usoTipo.existenContratosConSerie(serieOps.getId())).isTrue();
            assertThat(usoRubro.existenContratosConRubro(rubro.getId())).isTrue();
            assertThat(usoOrdenador.existenContratosConOrdenador(vicerrector.getId())).isTrue();
        }

        @Test
        void unaSerieConNumerosEmitidosCuentaComoUsadaAunqueNoHayaContratos() {
            consecutivo(serieAprendizaje, ANIO, 1);
            assertThat(usoTipo.existenContratosConSerie(serieAprendizaje.getId())).isTrue();
        }
    }
}
