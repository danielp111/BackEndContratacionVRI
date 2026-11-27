package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Campos calculados del contrato (no se guardan). Los valores quedan nulos mientras falten los datos
 * de los que salen (p. ej. sin valor sin IVA no hay valor final). Porcentajes y valor diario con 2 decimales.
 */
@Getter
@Setter
@NoArgsConstructor
public class ResumenContratoDTORespuesta {

    /** Valor sin IVA + IVA. */
    private BigDecimal valorInicial;
    /** Suma de las adiciones en valor. */
    private BigDecimal adicionesValor;
    /** Valor inicial + adiciones en valor. */
    private BigDecimal valorFinal;

    private Integer plazoDias;
    /** Suma de los días de las adiciones en tiempo. */
    private Integer adicionesDias;
    /** Plazo + días de adición. */
    private Integer plazoTotal;
    /** VALOR, TIEMPO, VALOR_TIEMPO o SIN_ADICION, según las modificaciones (códigos del catálogo TIPO_ADICION). */
    private String tipoAdicion;

    private BigDecimal valorAnticipo;
    /** Suma de los pagos. */
    private BigDecimal pagosAcumulados;
    private long pagosRegistrados;
    private Integer numeroPagosPactados;
    /** Valor final − pagos acumulados − anticipo. */
    private BigDecimal saldo;

    /** Valor final ÷ plazo total. */
    private BigDecimal valorDiario;
    /** Pagos acumulados ÷ valor diario. */
    private BigDecimal diasEjecutados;

    /** Días transcurridos desde el inicio (hasta hoy, o 100 % si ya terminó) × 100 ÷ plazo total, máximo 100. */
    private BigDecimal avanceFisicoProgramado;
    /** Días ejecutados × 100 ÷ plazo total. */
    private BigDecimal avanceFisicoReal;
    /** Igual al físico programado (los pagos se programan con el tiempo). */
    private BigDecimal avancePresupuestalProgramado;
    /** (Pagos acumulados + anticipo) × 100 ÷ valor final. */
    private BigDecimal avancePresupuestalReal;

    /** Inicio + plazo total − 1, mientras no haya fecha de terminación. */
    private LocalDate fechaTerminacionEstimada;
    /** "2026-1" o "2026-2": de la fecha de terminación o, si no hay, de la estimada. */
    private String semestreFinalizacion;
}
