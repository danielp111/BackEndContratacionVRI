package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion;

import java.math.BigDecimal;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.NaturalezaGasto;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Datos presupuestales que llena el abogado. La fecha de certificación de pago es del financiero (cierre). */
@Getter
@Setter
@NoArgsConstructor
public class PresupuestoDTOPeticion {

    @Positive(message = "El rubro debe ser un identificador válido")
    private Long rubroId;

    @Positive(message = "El proyecto debe ser un código válido")
    private Integer proyectoId;

    @Size(max = 40, message = "El PAA admite máximo 40 caracteres")
    private String paa;

    @Positive(message = "La fuente de financiación debe ser un identificador válido")
    private Long fuenteFinanciacionId;

    /** ¿Los recursos provienen de un contrato o convenio interadministrativo? */
    private Boolean recursosDeConvenio;

    @Positive(message = "La entidad financiadora debe ser un identificador válido")
    private Long entidadFinanciadoraId;

    @Positive(message = "La dependencia solicitante debe ser un identificador válido")
    private Long dependenciaSolicitanteId;

    private NaturalezaGasto naturalezaGasto;

    @Positive(message = "El tipo de anticipo debe ser un identificador válido")
    private Long tipoAnticipoId;

    @PositiveOrZero(message = "El valor del anticipo no puede ser negativo")
    @Digits(integer = 16, fraction = 2, message = "El valor del anticipo admite 16 enteros y 2 decimales")
    private BigDecimal valorAnticipo;
}
