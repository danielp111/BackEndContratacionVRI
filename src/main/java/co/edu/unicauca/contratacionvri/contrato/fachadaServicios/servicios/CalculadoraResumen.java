package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoEntity;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ResumenContratoDTORespuesta;

/**
 * Fórmulas de los campos calculados del contrato (ver "Campos calculados" en CLAUDE.md). Sin acceso
 * a la base: recibe los totales de modificaciones y pagos y la fecha de hoy, para poder probarla.
 */
public final class CalculadoraResumen {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final int DECIMALES = 2;

    private CalculadoraResumen() {
    }

    /** Totales del detalle de un contrato. */
    public record Totales(BigDecimal adicionesValor, int adicionesDias, BigDecimal pagos, long cantidadPagos) {

        public static final Totales VACIOS = new Totales(BigDecimal.ZERO, 0, BigDecimal.ZERO, 0);
    }

    public static ResumenContratoDTORespuesta calcular(ContratoEntity c, Totales t, LocalDate hoy) {
        ResumenContratoDTORespuesta r = new ResumenContratoDTORespuesta();
        BigDecimal valorInicial = c.getValorInicial();
        BigDecimal anticipo = c.getPresupuesto().getValorAnticipo() == null ? BigDecimal.ZERO : c.getPresupuesto().getValorAnticipo();
        BigDecimal valorFinal = valorInicial == null ? null : valorInicial.add(t.adicionesValor());
        Integer plazoTotal = c.getPlazoDias() == null ? null : c.getPlazoDias() + t.adicionesDias();

        r.setValorInicial(valorInicial);
        r.setAdicionesValor(t.adicionesValor());
        r.setValorFinal(valorFinal);
        r.setPlazoDias(c.getPlazoDias());
        r.setAdicionesDias(t.adicionesDias());
        r.setPlazoTotal(plazoTotal);
        r.setTipoAdicion(tipoAdicion(t));
        r.setValorAnticipo(anticipo);
        r.setPagosAcumulados(t.pagos());
        r.setPagosRegistrados(t.cantidadPagos());
        r.setNumeroPagosPactados(c.getNumeroPagos());

        if (valorFinal != null) {
            r.setSaldo(valorFinal.subtract(t.pagos()).subtract(anticipo));
            if (valorFinal.signum() > 0) {
                r.setAvanceFisicoReal(porcentaje(t.pagos(), valorFinal));
                r.setAvancePresupuestalReal(porcentaje(t.pagos().add(anticipo), valorFinal));
            }
        }
        if (valorFinal != null && plazoTotal != null && plazoTotal > 0) {
            BigDecimal valorDiario = valorFinal.divide(BigDecimal.valueOf(plazoTotal), DECIMALES, RoundingMode.HALF_UP);
            r.setValorDiario(valorDiario);
            if (valorFinal.signum() > 0) {
                // pagos ÷ valor diario, calculado sin redondear el valor diario.
                r.setDiasEjecutados(t.pagos().multiply(BigDecimal.valueOf(plazoTotal)).divide(valorFinal, DECIMALES, RoundingMode.HALF_UP));
            }
        }

        BigDecimal programado = avanceProgramado(c, plazoTotal, hoy);
        r.setAvanceFisicoProgramado(programado);
        r.setAvancePresupuestalProgramado(programado);

        LocalDate estimada = c.getFechaInicio() == null || plazoTotal == null ? null : c.getFechaInicio().plusDays(plazoTotal - 1L);
        r.setFechaTerminacionEstimada(c.getFechaTerminacion() == null ? estimada : null);
        LocalDate fin = c.getFechaTerminacion() != null ? c.getFechaTerminacion() : estimada;
        r.setSemestreFinalizacion(fin == null ? null : fin.getYear() + "-" + (fin.getMonthValue() <= 6 ? 1 : 2));
        return r;
    }

    /** Sin inicio: 0. Terminado: 100. En ejecución: días transcurridos (incluido el de hoy) sobre el plazo total. */
    private static BigDecimal avanceProgramado(ContratoEntity c, Integer plazoTotal, LocalDate hoy) {
        if (plazoTotal == null || plazoTotal <= 0) {
            return null;
        }
        if (c.getFechaInicio() == null) {
            return BigDecimal.ZERO.setScale(DECIMALES);
        }
        if (c.getFechaTerminacion() != null) {
            return CIEN.setScale(DECIMALES);
        }
        long transcurridos = Math.max(0, Math.min(plazoTotal, ChronoUnit.DAYS.between(c.getFechaInicio(), hoy) + 1));
        return porcentaje(BigDecimal.valueOf(transcurridos), BigDecimal.valueOf(plazoTotal));
    }

    private static String tipoAdicion(Totales t) {
        boolean enValor = t.adicionesValor().signum() > 0;
        boolean enTiempo = t.adicionesDias() > 0;
        if (enValor && enTiempo) {
            return "VALOR_TIEMPO";
        }
        if (enValor) {
            return "VALOR";
        }
        return enTiempo ? "TIEMPO" : "SIN_ADICION";
    }

    private static BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
        return parte.multiply(CIEN).divide(total, DECIMALES, RoundingMode.HALF_UP);
    }
}
