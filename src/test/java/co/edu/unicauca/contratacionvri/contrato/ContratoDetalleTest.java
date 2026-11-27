package co.edu.unicauca.contratacionvri.contrato;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CodigoCatalogo;

/**
 * Detalle del contrato: CDP, RP, UNSPSC, pólizas, supervisores e interventores, modificaciones,
 * pagos y los calculados. El contrato "en ejecución" vale 10.000.000 sin IVA, a 120 días, con 4
 * pagos pactados, suscrito el 10 de marzo e iniciado el 12.
 */
class ContratoDetalleTest extends ContratoPruebaBase {

    private CatalogoItemEntity enValor;
    private CatalogoItemEntity enTiempo;
    private CatalogoItemEntity enValorYTiempo;
    private CatalogoItemEntity sinAdicion;
    private CatalogoItemEntity interventoria;
    private CatalogoItemEntity ambos;
    private CatalogoItemEntity mundial;

    @BeforeEach
    void catalogos() {
        enValor = item(CodigoCatalogo.TIPO_ADICION, "VALOR", "1", "ADICIÓN EN VALOR");
        enTiempo = item(CodigoCatalogo.TIPO_ADICION, "TIEMPO", "2", "ADICIÓN EN TIEMPO");
        enValorYTiempo = item(CodigoCatalogo.TIPO_ADICION, "VALOR_TIEMPO", "3", "ADICIÓN EN VALOR y EN TIEMPO");
        sinAdicion = item(CodigoCatalogo.TIPO_ADICION, "SIN_ADICION", "4", "NO SE HA ADICIONADO NI EN VALOR y EN TIEMPO");
        interventoria = item(CodigoCatalogo.TIPO_SEGUIMIENTO, "INTERVENTOR", "1", "INTERVENTOR");
        ambos = item(CodigoCatalogo.TIPO_SEGUIMIENTO, "INTERVENTOR_SUPERVISOR", "3", "INTERVENTOR y SUPERVISOR");
        mundial = item(CodigoCatalogo.ASEGURADORA, "SEGUROS_MUNDIAL", null, "SEGUROS MUNDIAL");
    }

    private Map<String, Object> enEjecucion() {
        Map<String, Object> p = completo();
        p.put("fechaSuscripcion", fecha(3, 10));
        p.put("fechaInicio", fecha(3, 12));
        return p;
    }

    private long contratoEnEjecucion() throws Exception {
        return crearSuscritoId(enEjecucion());
    }

    private String url(long contrato, String recurso) {
        return URL + "/" + contrato + "/" + recurso;
    }

    private long idDe(org.springframework.test.web.servlet.ResultActions r) throws Exception {
        return json.readTree(r.andReturn().getResponse().getContentAsString()).get("id").asLong();
    }

    private Map<String, Object> adicion(CatalogoItemEntity tipo, Integer valor, Integer dias, String fecha) {
        Map<String, Object> m = new HashMap<>();
        m.put("tipoAdicionId", tipo.getId());
        m.put("valor", valor);
        m.put("dias", dias);
        m.put("fecha", fecha);
        return m;
    }

    // ------------------------------------------------------------------ CDP y RP

    @Nested
    class CdpYRp {

        @Test
        void agregaCdpConAvisoSiNoCubrenElValorFinal() throws Exception {
            long c = contratoEnEjecucion();
            enviar("POST", url(c, "cdp"), Map.of("numero", " cdp-1234 ", "valor", 6000000, "fecha", fecha(2, 20)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/cdp/")))
                    .andExpect(jsonPath("$.numero").value("CDP-1234"))
                    .andExpect(jsonPath("$.advertencias[0]").value(containsString("es menor que el valor final del contrato")));
            mvc.perform(get(URL + "/" + c)).andExpect(jsonPath("$.advertencias[0]").value(containsString("La suma de los CDP")));

            enviar("POST", url(c, "cdp"), Map.of("numero", "CDP-1290", "valor", 4000000))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.advertencias", empty()));
            mvc.perform(get(url(c, "cdp"))).andExpect(jsonPath("$[*].numero", contains("CDP-1234", "CDP-1290")));
        }

        @Test
        void elNumeroEsUnicoPorContratoYElRpEsOtraLista() throws Exception {
            long c = crearContratoId(minimo());
            enviar("POST", url(c, "cdp"), Map.of("numero", "1234")).andExpect(status().isCreated());
            enviar("POST", url(c, "cdp"), Map.of("numero", "1234"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.mensaje").value("El contrato 6.1-17.13/001 ya tiene el CDP 1234"));
            enviar("POST", url(c, "rp"), Map.of("numero", "1234", "valor", 500)).andExpect(status().isCreated());
            mvc.perform(get(url(c, "rp"))).andExpect(jsonPath("$", hasSize(1)));

            long otro = crearContratoId(minimo());
            enviar("POST", url(otro, "cdp"), Map.of("numero", "1234")).andExpect(status().isCreated());
        }

        @Test
        void actualizaYBorra() throws Exception {
            long c = crearContratoId(minimo());
            long id = idDe(enviar("POST", url(c, "rp"), Map.of("numero", "567")));
            long otro = idDe(enviar("POST", url(c, "rp"), Map.of("numero", "568")));
            enviar("PUT", url(c, "rp/" + id), Map.of("numero", "567", "valor", 100, "fecha", fecha(3, 1)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.valor").value(100));
            enviar("PUT", url(c, "rp/" + id), Map.of("numero", "568")).andExpect(status().isConflict());
            mvc.perform(delete(url(c, "rp/" + otro))).andExpect(status().isNoContent());
            mvc.perform(get(url(c, "rp"))).andExpect(jsonPath("$[*].numero", contains("567")));

            long ajeno = crearContratoId(minimo());
            mvc.perform(delete(url(ajeno, "rp/" + id))).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.mensaje").value("No existe el RP con id " + id + " en el contrato con id " + ajeno));
        }

        @Test
        void validaLosCampos() throws Exception {
            long c = crearContratoId(minimo());
            enviar("POST", url(c, "cdp"), Map.of("numero", " ", "valor", -1))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errores.numero").value("El número es obligatorio"))
                    .andExpect(jsonPath("$.errores.valor").value("El valor debe ser mayor que cero"));
            mvc.perform(get(url(c, "cdps"))).andExpect(status().isNotFound());
            mvc.perform(get(url(999999, "cdp"))).andExpect(status().isNotFound());
        }
    }

    // ------------------------------------------------------------------ UNSPSC y pólizas

    @Nested
    class UnspscYPolizas {

        @Test
        void unspscDeOchoDigitosSinRepetir() throws Exception {
            long c = crearContratoId(minimo());
            long id = idDe(enviar("POST", url(c, "unspsc"), Map.of("codigo", "80 11 16 00"))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.codigo").value("80111600")));
            enviar("POST", url(c, "unspsc"), Map.of("codigo", "80111600")).andExpect(status().isConflict());
            enviar("POST", url(c, "unspsc"), Map.of("codigo", "801116"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El código UNSPSC debe tener 8 dígitos: '801116' no los tiene"));
            enviar("POST", url(c, "unspsc"), Map.of("codigo", "93141500")).andExpect(status().isCreated());
            mvc.perform(get(url(c, "unspsc"))).andExpect(jsonPath("$[*].codigo", contains("80111600", "93141500")));
            mvc.perform(delete(url(c, "unspsc/" + id))).andExpect(status().isNoContent());
            mvc.perform(get(url(c, "unspsc"))).andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        void polizasConAseguradoraDelCatalogo() throws Exception {
            long c = crearContratoId(minimo());
            long id = idDe(enviar("POST", url(c, "polizas"), Map.of("numero", "cu-001", "aseguradoraId", mundial.getId(),
                    "vigenciaHasta", fecha(12, 31)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.numero").value("CU-001"))
                    .andExpect(jsonPath("$.aseguradoraNombre").value("SEGUROS MUNDIAL")));
            enviar("POST", url(c, "polizas"), Map.of("numero", "CU-001", "aseguradoraId", mundial.getId()))
                    .andExpect(status().isConflict());
            enviar("POST", url(c, "polizas"), Map.of("numero", "CU-002", "aseguradoraId", supervisor.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("se esperaba uno del catálogo ASEGURADORA")));

            mundial.setActivo(false);
            catalogoRepositorio.save(mundial);
            // La aseguradora no cambia: se conserva aunque se haya inactivado.
            enviar("PUT", url(c, "polizas/" + id), Map.of("numero", "CU-001", "aseguradoraId", mundial.getId()))
                    .andExpect(status().isOk());
            enviar("POST", url(c, "polizas"), Map.of("numero", "CU-003", "aseguradoraId", mundial.getId()))
                    .andExpect(status().isBadRequest());
            mvc.perform(delete(url(c, "polizas/" + id))).andExpect(status().isNoContent());
        }

        @Test
        void unContratoAnuladoNoAdmiteCambiosEnSuDetalle() throws Exception {
            long c = crearContratoId(minimo());
            enviar("PATCH", URL + "/" + c + "/anular", Map.of("motivo", "No se usó")).andExpect(status().isOk());
            for (String recurso : new String[]{"cdp", "rp", "unspsc"}) {
                enviar("POST", url(c, recurso), Map.of("numero", "1", "codigo", "80111600"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.mensaje").value("El contrato 6.1-17.13/001 está anulado y no se puede modificar"));
            }
            mvc.perform(get(url(c, "cdp"))).andExpect(status().isOk());
        }
    }

    // ------------------------------------------------------------------ supervisores e interventores

    @Nested
    class Seguimiento {

        @Test
        void exigeElTipoDeSeguimientoYQueElRolCuadre() throws Exception {
            long c = crearContratoId(minimo());
            Map<String, Object> s = Map.of("rol", "SUPERVISOR", "terceroId", pedro.getId(), "desde", fecha(3, 1));
            enviar("POST", url(c, "seguimientos"), s)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("Defina primero el tipo de seguimiento del contrato 6.1-17.13/001"));

            long conSupervisor = crearContratoId(completo());
            enviar("POST", url(conSupervisor, "seguimientos"), Map.of("rol", "INTERVENTOR", "terceroId", dian.getId(), "desde", fecha(3, 1)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El tipo de seguimiento '2 SUPERVISOR' no admite un interventor"));
            enviar("POST", url(conSupervisor, "seguimientos"), Map.of("rol", "SUPERVISOR", "terceroId", dian.getId(), "desde", fecha(3, 1)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El supervisor debe ser una persona natural y DIAN es persona jurídica"));
            enviar("POST", url(conSupervisor, "seguimientos"), s)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nombre").value("Pedro Pérez"))
                    .andExpect(jsonPath("$.tipoIdentificacion").value("3 CÉDULA DE CIUDADANÍA"))
                    .andExpect(jsonPath("$.numeroDocumento").value("1062222222"))
                    .andExpect(jsonPath("$.vigente").value(true));

            Map<String, Object> p = completo();
            p.put("tipoSeguimientoId", ambos.getId());
            long mixto = crearContratoId(p);
            enviar("POST", url(mixto, "seguimientos"), Map.of("rol", "INTERVENTOR", "terceroId", dian.getId(), "desde", fecha(3, 1)))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.dv").value("4"));
        }

        @Test
        void paraCambiarDeSupervisorSeCierraElActual() throws Exception {
            long c = crearContratoId(completo());
            long primero = idDe(enviar("POST", url(c, "seguimientos"),
                    Map.of("rol", "SUPERVISOR", "terceroId", pedro.getId(), "desde", fecha(3, 1))));
            Map<String, Object> nuevo = Map.of("rol", "SUPERVISOR", "terceroId", ana.getId(), "desde", fecha(4, 1));
            enviar("POST", url(c, "seguimientos"), nuevo)
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.mensaje").value(containsString("cierre el actual")));
            enviar("PATCH", url(c, "seguimientos/" + primero + "/cerrar"), Map.of("hasta", fecha(2, 1)))
                    .andExpect(status().isBadRequest());
            enviar("PATCH", url(c, "seguimientos/" + primero + "/cerrar"), Map.of("hasta", fecha(3, 31)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.hasta").value(fecha(3, 31)));
            enviar("POST", url(c, "seguimientos"), nuevo).andExpect(status().isCreated());
            mvc.perform(get(url(c, "seguimientos"))).andExpect(jsonPath("$[*].nombre", contains("Pedro Pérez", "Ana María Gómez López")));
        }

        @Test
        void laSuscripcionExigeElSupervisorYElTipoNoCambiaSiNoCuadra() throws Exception {
            long c = crearContratoId(completo());
            Map<String, Object> p = completo();
            p.put("fechaSuscripcion", fecha(3, 10));
            enviar("PUT", URL + "/" + c, p)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("Para registrar la suscripción faltan: supervisor"));

            designarSupervisor(c);
            p.put("tipoSeguimientoId", interventoria.getId());
            enviar("PUT", URL + "/" + c, p)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El contrato tiene un supervisor registrado; el tipo de seguimiento '1 INTERVENTOR' no lo admite"));

            p.put("tipoSeguimientoId", supervisor.getId());
            enviar("PUT", URL + "/" + c, p).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("SUSCRITO"));
            // Con el contrato suscrito, un periodo no empieza antes de la suscripción.
            enviar("POST", url(c, "seguimientos"), Map.of("rol", "SUPERVISOR", "terceroId", ana.getId(), "desde", fecha(3, 1),
                    "hasta", fecha(3, 5)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("no puede ser anterior a la fecha de suscripción")));
        }
    }

    // ------------------------------------------------------------------ modificaciones

    @Nested
    class Modificaciones {

        @Test
        void soloEnContratosSuscritos() throws Exception {
            long c = crearContratoId(minimo());
            enviar("POST", url(c, "modificaciones"), adicion(enTiempo, null, 30, fecha(6, 1)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("Solo se pueden registrar adiciones en un contrato suscrito")));
        }

        @Test
        void cadaTipoExigeSusCampos() throws Exception {
            long c = contratoEnEjecucion();
            enviar("POST", url(c, "modificaciones"), adicion(enValor, null, null, fecha(6, 1)))
                    .andExpect(jsonPath("$.mensaje").value("La adición '1 ADICIÓN EN VALOR' exige el valor"));
            enviar("POST", url(c, "modificaciones"), adicion(enValor, 1000, 10, fecha(6, 1)))
                    .andExpect(jsonPath("$.mensaje").value("La adición '1 ADICIÓN EN VALOR' no lleva los días"));
            enviar("POST", url(c, "modificaciones"), adicion(enTiempo, 1000, null, fecha(6, 1)))
                    .andExpect(jsonPath("$.mensaje").value("La adición '2 ADICIÓN EN TIEMPO' no lleva el valor"));
            enviar("POST", url(c, "modificaciones"), adicion(enTiempo, null, null, fecha(6, 1)))
                    .andExpect(jsonPath("$.mensaje").value("La adición '2 ADICIÓN EN TIEMPO' exige los días"));
            enviar("POST", url(c, "modificaciones"), adicion(enValorYTiempo, 1000, null, fecha(6, 1)))
                    .andExpect(jsonPath("$.mensaje").value("La adición '3 ADICIÓN EN VALOR y EN TIEMPO' exige los días"));
            enviar("POST", url(c, "modificaciones"), adicion(sinAdicion, null, null, fecha(6, 1)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("no es una adición que se pueda registrar")));
            enviar("POST", url(c, "modificaciones"), adicion(enTiempo, null, 10, fecha(3, 1)))
                    .andExpect(jsonPath("$.mensaje").value(containsString("no puede ser anterior a la fecha de suscripción")));

            Map<String, Object> m = adicion(enValorYTiempo, 2000000, 20, fecha(6, 1));
            m.put("documento", " Otrosí   1 ");
            enviar("POST", url(c, "modificaciones"), m)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.tipoAdicionCodigo").value("VALOR_TIEMPO"))
                    .andExpect(jsonPath("$.documento").value("Otrosí 1"))
                    .andExpect(jsonPath("$.advertencias", empty()));
            mvc.perform(get(URL + "/" + c + "/resumen"))
                    .andExpect(jsonPath("$.valorFinal").value(12000000.0))
                    .andExpect(jsonPath("$.plazoTotal").value(140))
                    .andExpect(jsonPath("$.tipoAdicion").value("VALOR_TIEMPO"));
        }

        @Test
        void avisaSiLasAdicionesSuperanElCincuentaPorCiento() throws Exception {
            long c = contratoEnEjecucion();
            enviar("POST", url(c, "modificaciones"), adicion(enValor, 5000000, null, fecha(6, 1)))
                    .andExpect(jsonPath("$.advertencias", empty()));
            enviar("POST", url(c, "modificaciones"), adicion(enValor, 1, null, fecha(6, 2)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.advertencias[0]").value(containsString("superan el 50 % del valor inicial")));
            mvc.perform(get(URL + "/" + c)).andExpect(jsonPath("$.advertencias[0]").value(containsString("superan el 50 %")));
        }

        @Test
        void noSeBorraUnaAdicionYaPagada() throws Exception {
            long c = contratoEnEjecucion();
            long adicionId = idDe(enviar("POST", url(c, "modificaciones"), adicion(enValor, 2000000, null, fecha(4, 1))));
            pagar(c, 11000000, fecha(5, 1));
            mvc.perform(delete(url(c, "modificaciones/" + adicionId)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("quedaría negativo")));
            enviar("PUT", url(c, "modificaciones/" + adicionId), adicion(enValor, 500000, null, fecha(4, 1)))
                    .andExpect(status().isBadRequest());
            enviar("PUT", url(c, "modificaciones/" + adicionId), adicion(enValor, 1000000, null, fecha(4, 1)))
                    .andExpect(status().isOk());
        }
    }

    // ------------------------------------------------------------------ pagos

    @Nested
    class Pagos {

        @Test
        void soloConFechaDeInicioYNoAntes() throws Exception {
            Map<String, Object> p = completo();
            p.put("fechaSuscripcion", fecha(3, 10));
            long suscrito = crearSuscritoId(p);
            enviar("POST", url(suscrito, "pagos"), Map.of("valor", 100, "fecha", fecha(4, 1)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("Solo se pueden registrar pagos de un contrato con fecha de inicio")));

            long c = contratoEnEjecucion();
            enviar("POST", url(c, "pagos"), Map.of("valor", 100, "fecha", fecha(3, 11)))
                    .andExpect(jsonPath("$.mensaje").value(containsString("no puede ser anterior a la fecha de inicio")));
            enviar("POST", url(c, "pagos"), Map.of("fecha", fecha(4, 1)))
                    .andExpect(jsonPath("$.errores.valor").value("El valor del pago es obligatorio"));
        }

        @Test
        void numeraLosPagosYNoDejaElSaldoNegativo() throws Exception {
            long c = contratoEnEjecucion();
            enviar("POST", url(c, "pagos"), Map.of("valor", 2500000, "fecha", fecha(4, 12)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.numero").value(1))
                    .andExpect(jsonPath("$.saldo").value(7500000.0));
            enviar("POST", url(c, "pagos"), Map.of("valor", 2500000, "fecha", fecha(5, 12)))
                    .andExpect(jsonPath("$.numero").value(2));
            enviar("POST", url(c, "pagos"), Map.of("valor", 5000001, "fecha", fecha(6, 12)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("quedaría negativo")));
            mvc.perform(get(url(c, "pagos"))).andExpect(jsonPath("$[*].numero", contains(1, 2)));
            mvc.perform(get(URL + "/" + c + "/resumen"))
                    .andExpect(jsonPath("$.pagosAcumulados").value(5000000.0))
                    .andExpect(jsonPath("$.pagosRegistrados").value(2))
                    .andExpect(jsonPath("$.saldo").value(5000000.0))
                    .andExpect(jsonPath("$.avanceFisicoReal").value(50.0));
        }

        @Test
        void soloElUltimoPagoSeCorrigeOSeBorra() throws Exception {
            long c = contratoEnEjecucion();
            long primero = idDe(enviar("POST", url(c, "pagos"), Map.of("valor", 1000, "fecha", fecha(4, 12))));
            long segundo = idDe(enviar("POST", url(c, "pagos"), Map.of("valor", 1000, "fecha", fecha(5, 12))));
            enviar("PUT", url(c, "pagos/" + primero), Map.of("valor", 2000, "fecha", fecha(4, 12)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("Solo se puede modificar el último pago (el 2); el pago 1 no"));
            mvc.perform(delete(url(c, "pagos/" + primero))).andExpect(status().isBadRequest());
            enviar("PUT", url(c, "pagos/" + segundo), Map.of("valor", 3000, "fecha", fecha(5, 13)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.valor").value(3000));
            mvc.perform(delete(url(c, "pagos/" + segundo))).andExpect(status().isNoContent());
            enviar("POST", url(c, "pagos"), Map.of("valor", 1000, "fecha", fecha(5, 12))).andExpect(jsonPath("$.numero").value(2));
        }

        @Test
        void avisaSiSePaganMasPagosDeLosPactados() throws Exception {
            Map<String, Object> p = enEjecucion();
            p.put("numeroPagos", 1);
            long c = crearSuscritoId(p);
            pagar(c, 1000, fecha(4, 1));
            enviar("POST", url(c, "pagos"), Map.of("valor", 1000, "fecha", fecha(4, 2)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.advertencias[0]").value("El contrato pactó 1 pago y este es el pago 2"));
        }

        @Test
        void elActaFinalExigeLosPagosPactados() throws Exception {
            Map<String, Object> p = enEjecucion();
            p.put("numeroPagos", 2);
            long c = crearSuscritoId(p);
            pagar(c, 5000000, fecha(4, 12));
            Map<String, Object> cierre = Map.of("fechaTerminacion", fecha(7, 10), "fechaActaFinal", fecha(7, 25));
            enviar("PATCH", URL + "/" + c + "/cierre", cierre)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("Para registrar el acta final faltan pagos: se pactaron 2 y hay 1 registrado"));
            long ultimo = idDe(enviar("POST", url(c, "pagos"), Map.of("valor", 5000000, "fecha", fecha(7, 15))));
            enviar("PATCH", URL + "/" + c + "/cierre", cierre).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("LIQUIDADO"));
            mvc.perform(delete(url(c, "pagos/" + ultimo)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("ya tiene acta final")));
        }

        @Test
        void bajarElValorDelContratoNoPuedeDejarElSaldoNegativo() throws Exception {
            long c = contratoEnEjecucion();
            pagar(c, 8000000, fecha(4, 12));
            Map<String, Object> p = enEjecucion();
            p.put("valorSinIva", 7000000);
            enviar("PUT", URL + "/" + c, p)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value(containsString("quedaría negativo")));
        }
    }

    // ------------------------------------------------------------------ calculados

    @Nested
    class Calculados {

        @Test
        void elDetalleYLosListadosTraenLosCalculados() throws Exception {
            Map<String, Object> p = enEjecucion();
            p.put("plazoDias", 100);
            presupuestoDe(p).put("tipoAnticipoId", anticipo.getId());
            presupuestoDe(p).put("valorAnticipo", 1000000);
            long c = crearSuscritoId(p);
            enviar("POST", url(c, "modificaciones"), adicion(enValorYTiempo, 2000000, 20, fecha(5, 1))).andExpect(status().isCreated());
            pagar(c, 3000000, fecha(4, 12));
            pagar(c, 3000000, fecha(5, 12));

            mvc.perform(get(URL + "/" + c))
                    .andExpect(jsonPath("$.resumen.valorInicial").value(10000000.0))
                    .andExpect(jsonPath("$.resumen.adicionesValor").value(2000000.0))
                    .andExpect(jsonPath("$.resumen.valorFinal").value(12000000.0))
                    .andExpect(jsonPath("$.resumen.plazoTotal").value(120))
                    .andExpect(jsonPath("$.resumen.pagosAcumulados").value(6000000.0))
                    .andExpect(jsonPath("$.resumen.saldo").value(5000000.0))
                    .andExpect(jsonPath("$.resumen.valorDiario").value(100000.0))
                    .andExpect(jsonPath("$.resumen.diasEjecutados").value(60.0))
                    .andExpect(jsonPath("$.resumen.avanceFisicoReal").value(50.0))
                    .andExpect(jsonPath("$.resumen.avancePresupuestalReal").value(58.33))
                    .andExpect(jsonPath("$.resumen.fechaTerminacionEstimada").value(fecha(7, 9)))
                    .andExpect(jsonPath("$.resumen.semestreFinalizacion").value(ANIO + "-2"));

            mvc.perform(get(URL).param("numero", "6.1-17.13/001"))
                    .andExpect(jsonPath("$.contenido[0].valorFinal").value(12000000.0))
                    .andExpect(jsonPath("$.contenido[0].saldo").value(5000000.0))
                    .andExpect(jsonPath("$.contenido[0].avanceFisicoReal").value(50.0))
                    .andExpect(jsonPath("$.contenido[0].avancePresupuestalReal").value(58.33));
            mvc.perform(get(URL + "/999999/resumen")).andExpect(status().isNotFound());
        }

        @Test
        void losCamposDelDetalleAdmitenObservaciones() throws Exception {
            long c = crearContratoId(minimo());
            enviar("POST", URL + "/" + c + "/observaciones", Map.of("campo", "PAGOS", "texto", "Falta el pago de abril"))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.campoEtiqueta").value("Pagos"));
        }
    }
}
