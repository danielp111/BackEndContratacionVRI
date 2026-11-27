package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.EstadoContrato;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Detalle del contrato con su presupuesto. Incluye los datos de las relaciones listos para mostrar
 * (nombre del contratista, ordenador con su cédula y cargo...) y los calculados de esta fase
 * (número, valor inicial, porcentaje de anticipo). {@code advertencias} no bloquea el guardado.
 */
@Getter
@Setter
@NoArgsConstructor
public class ContratoDTORespuesta {

    private Long id;
    /** "6.1-17.13/023". */
    private String numero;
    private String serieCodigo;
    private Integer anio;
    private Integer consecutivo;
    private EstadoContrato estado;

    private Long tipoContratoId;
    private String tipoContratoNombre;

    private Long responsableId;
    private String responsableNombre;

    private Long claseContratoId;
    /** "23 PRESTACIÓN DE SERVICIOS". */
    private String claseContratoEtiqueta;
    private String otraClase;
    private String objeto;

    private Long contratistaId;
    private String contratistaNombre;
    private String contratistaDocumento;

    private Long ordenadorGastoId;
    private String ordenadorNombre;
    private String ordenadorCedula;
    private String ordenadorCargo;

    private Long tipoSeguimientoId;
    private String tipoSeguimientoEtiqueta;

    private BigDecimal valorSinIva;
    private BigDecimal iva;
    /** Calculado: valor sin IVA + IVA. */
    private BigDecimal valorInicial;
    private Integer plazoDias;
    private Integer numeroPagos;

    private LocalDate fechaSuscripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaTerminacion;
    private LocalDate fechaActaFinal;

    private String motivoAnulacion;
    private LocalDate fechaAnulacion;

    private PresupuestoDTORespuesta presupuesto;

    /** Calculados: valor final, saldo, avances... */
    private ResumenContratoDTORespuesta resumen;

    private long observacionesPendientes;
    /** Avisos que no impiden guardar (p. ej. posible fraccionamiento). */
    private List<String> advertencias;

    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
