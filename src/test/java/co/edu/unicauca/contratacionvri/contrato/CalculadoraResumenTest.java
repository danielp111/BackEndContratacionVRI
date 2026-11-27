package co.edu.unicauca.contratacionvri.contrato;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoEntity;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ResumenContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.CalculadoraResumen;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.CalculadoraResumen.Totales;

/** Fórmulas de los campos calculados, con fechas fijas (sin base de datos). */
class CalculadoraResumenTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 3, 1);

    private static ContratoEntity contrato(Integer valor, Integer plazo, LocalDate inicio) {
        ContratoEntity c = new ContratoEntity(null, null, 2026);
        c.setValorSinIva(valor == null ? null : BigDecimal.valueOf(valor));
        c.setIva(valor == null ? null : BigDecimal.ZERO);
        c.setPlazoDias(plazo);
        c.setFechaSuscripcion(inicio);
        c.setFechaInicio(inicio);
        return c;
    }

    private static Totales totales(int adicionValor, int adicionDias, int pagos, long cantidad) {
        return new Totales(BigDecimal.valueOf(adicionValor), adicionDias, BigDecimal.valueOf(pagos), cantidad);
    }

    private static BigDecimal d(String valor) {
        return new BigDecimal(valor);
    }

    @Test
    void valoresPlazosSaldoYAvancesReales() {
        ContratoEntity c = contrato(10_000_000, 100, INICIO);
        c.getPresupuesto().setValorAnticipo(BigDecimal.valueOf(1_000_000));
        ResumenContratoDTORespuesta r = CalculadoraResumen.calcular(c, totales(2_000_000, 20, 6_000_000, 2), INICIO.plusDays(10));

        assertThat(r.getValorInicial()).isEqualByComparingTo("10000000");
        assertThat(r.getValorFinal()).isEqualByComparingTo("12000000");
        assertThat(r.getPlazoTotal()).isEqualTo(120);
        assertThat(r.getTipoAdicion()).isEqualTo("VALOR_TIEMPO");
        assertThat(r.getPagosAcumulados()).isEqualByComparingTo("6000000");
        assertThat(r.getPagosRegistrados()).isEqualTo(2);
        // Saldo = valor final − pagos − anticipo.
        assertThat(r.getSaldo()).isEqualByComparingTo("5000000");
        assertThat(r.getValorDiario()).isEqualTo(d("100000.00"));
        assertThat(r.getDiasEjecutados()).isEqualTo(d("60.00"));
        assertThat(r.getAvanceFisicoReal()).isEqualTo(d("50.00"));
        // (6.000.000 + 1.000.000) × 100 ÷ 12.000.000
        assertThat(r.getAvancePresupuestalReal()).isEqualTo(d("58.33"));
    }

    @Test
    void redondeaADosDecimales() {
        ResumenContratoDTORespuesta r = CalculadoraResumen.calcular(contrato(1_000_000, 3, INICIO), totales(0, 0, 500_000, 1), INICIO);
        assertThat(r.getValorDiario()).isEqualTo(d("333333.33"));
        assertThat(r.getDiasEjecutados()).isEqualTo(d("1.50"));
        assertThat(r.getAvanceFisicoReal()).isEqualTo(d("50.00"));
    }

    @Test
    void elAvanceProgramadoCuentaLosDiasTranscurridosConTopeDe100() {
        ContratoEntity c = contrato(10_000_000, 100, INICIO);
        // El día de inicio cuenta: del 1 al 25 de marzo son 25 días de 100.
        assertThat(CalculadoraResumen.calcular(c, Totales.VACIOS, LocalDate.of(2026, 3, 25)).getAvanceFisicoProgramado()).isEqualTo(d("25.00"));
        assertThat(CalculadoraResumen.calcular(c, Totales.VACIOS, LocalDate.of(2026, 3, 25)).getAvancePresupuestalProgramado()).isEqualTo(d("25.00"));
        assertThat(CalculadoraResumen.calcular(c, Totales.VACIOS, LocalDate.of(2026, 2, 1)).getAvanceFisicoProgramado()).isEqualTo(d("0.00"));
        assertThat(CalculadoraResumen.calcular(c, Totales.VACIOS, LocalDate.of(2027, 1, 1)).getAvanceFisicoProgramado()).isEqualTo(d("100.00"));
        // Las adiciones en tiempo alargan el plazo: 25 de 125.
        assertThat(CalculadoraResumen.calcular(c, totales(0, 25, 0, 0), LocalDate.of(2026, 3, 25)).getAvanceFisicoProgramado()).isEqualTo(d("20.00"));

        c.setFechaTerminacion(LocalDate.of(2026, 4, 1));
        assertThat(CalculadoraResumen.calcular(c, Totales.VACIOS, LocalDate.of(2026, 3, 2)).getAvanceFisicoProgramado()).isEqualTo(d("100.00"));

        ContratoEntity sinInicio = contrato(10_000_000, 100, null);
        assertThat(CalculadoraResumen.calcular(sinInicio, Totales.VACIOS, INICIO).getAvanceFisicoProgramado()).isEqualTo(d("0.00"));
        ContratoEntity sinPlazo = contrato(10_000_000, null, INICIO);
        assertThat(CalculadoraResumen.calcular(sinPlazo, Totales.VACIOS, INICIO).getAvanceFisicoProgramado()).isNull();
    }

    @Test
    void fechaEstimadaYSemestreEnQueFinaliza() {
        ContratoEntity c = contrato(10_000_000, 100, INICIO);
        ResumenContratoDTORespuesta r = CalculadoraResumen.calcular(c, Totales.VACIOS, INICIO);
        // 1 de marzo + 100 días contando el primero = 8 de junio.
        assertThat(r.getFechaTerminacionEstimada()).isEqualTo(LocalDate.of(2026, 6, 8));
        assertThat(r.getSemestreFinalizacion()).isEqualTo("2026-1");

        assertThat(CalculadoraResumen.calcular(c, totales(0, 30, 0, 0), INICIO).getSemestreFinalizacion()).isEqualTo("2026-2");

        c.setFechaTerminacion(LocalDate.of(2027, 2, 1));
        r = CalculadoraResumen.calcular(c, Totales.VACIOS, INICIO);
        assertThat(r.getFechaTerminacionEstimada()).isNull();
        assertThat(r.getSemestreFinalizacion()).isEqualTo("2027-1");
    }

    @Test
    void sinValorNiPlazoLosCalculadosQuedanVacios() {
        ResumenContratoDTORespuesta r = CalculadoraResumen.calcular(contrato(null, null, null), Totales.VACIOS, INICIO);
        assertThat(r.getValorFinal()).isNull();
        assertThat(r.getSaldo()).isNull();
        assertThat(r.getValorDiario()).isNull();
        assertThat(r.getAvanceFisicoReal()).isNull();
        assertThat(r.getPlazoTotal()).isNull();
        assertThat(r.getSemestreFinalizacion()).isNull();
        assertThat(r.getTipoAdicion()).isEqualTo("SIN_ADICION");
        assertThat(CalculadoraResumen.calcular(contrato(null, null, null), totales(100, 0, 0, 0), INICIO).getTipoAdicion()).isEqualTo("VALOR");
        assertThat(CalculadoraResumen.calcular(contrato(null, null, null), totales(0, 5, 0, 0), INICIO).getTipoAdicion()).isEqualTo("TIEMPO");
    }
}
