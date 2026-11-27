package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta;

import java.math.BigDecimal;
import java.time.LocalDate;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.EstadoContrato;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Fila de los listados paginados de contratos. */
@Getter
@Setter
@NoArgsConstructor
public class ContratoResumenDTORespuesta {

    private Long id;
    private String numero;
    private Integer anio;
    private EstadoContrato estado;
    private String tipoContratoNombre;
    private String objeto;
    private Long contratistaId;
    private String contratistaNombre;
    private String contratistaDocumento;
    private Long responsableId;
    private String responsableNombre;
    private BigDecimal valorInicial;
    private Integer plazoDias;
    private LocalDate fechaSuscripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaTerminacion;
    private BigDecimal valorFinal;
    private BigDecimal saldo;
    private BigDecimal avanceFisicoReal;
    private BigDecimal avancePresupuestalReal;
    private long observacionesPendientes;
}
