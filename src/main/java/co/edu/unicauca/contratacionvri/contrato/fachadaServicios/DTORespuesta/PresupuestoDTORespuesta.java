package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta;

import java.math.BigDecimal;
import java.time.LocalDate;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.NaturalezaGasto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PresupuestoDTORespuesta {

    private Long rubroId;
    /** Código de afectación presupuestal. */
    private String rubroCodigo;
    /** Dependencia de afectación: el nombre del rubro. */
    private String dependenciaAfectacion;

    private Integer proyectoId;
    private String proyectoNombre;
    private String paa;

    private Long fuenteFinanciacionId;
    private String fuenteFinanciacionEtiqueta;

    private Boolean recursosDeConvenio;
    private Long entidadFinanciadoraId;
    private String entidadFinanciadoraNombre;
    private String entidadFinanciadoraNit;
    private String entidadFinanciadoraDv;

    private Long dependenciaSolicitanteId;
    private String dependenciaSolicitanteNombre;
    private NaturalezaGasto naturalezaGasto;

    private Long tipoAnticipoId;
    private String tipoAnticipoEtiqueta;
    private BigDecimal valorAnticipo;
    /** Calculado: valor del anticipo × 100 ÷ valor inicial. */
    private BigDecimal porcentajeAnticipo;

    private LocalDate fechaCertificacionPago;
}
