package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos del contrato que llena el abogado (formulario completo: lo que no se envía queda vacío).
 * Mientras el contrato está en trámite puede guardarse incompleto; al registrar la fecha de
 * suscripción el servicio exige los datos completos. Las reglas que cruzan campos las valida el servicio.
 */
@Getter
@Setter
@NoArgsConstructor
public class ContratoDTOPeticion {

    @NotNull(message = "El abogado responsable es obligatorio")
    @Positive(message = "El responsable debe ser un identificador válido")
    private Long responsableId;

    @Positive(message = "La clase de contrato debe ser un identificador válido")
    private Long claseContratoId;

    @Size(max = 150, message = "La descripción de la otra clase admite máximo 150 caracteres")
    private String otraClase;

    @NotBlank(message = "El objeto del contrato es obligatorio")
    @Size(max = 390, message = "El objeto admite máximo 390 caracteres")
    private String objeto;

    @Positive(message = "El contratista debe ser un identificador válido")
    private Long contratistaId;

    @Positive(message = "El ordenador del gasto debe ser un identificador válido")
    private Long ordenadorGastoId;

    @Positive(message = "El tipo de seguimiento debe ser un identificador válido")
    private Long tipoSeguimientoId;

    @PositiveOrZero(message = "El valor sin IVA no puede ser negativo")
    @Digits(integer = 16, fraction = 2, message = "El valor sin IVA admite 16 enteros y 2 decimales")
    private BigDecimal valorSinIva;

    @PositiveOrZero(message = "El IVA no puede ser negativo")
    @Digits(integer = 16, fraction = 2, message = "El IVA admite 16 enteros y 2 decimales")
    private BigDecimal iva;

    @Positive(message = "El plazo debe ser mayor que cero días")
    private Integer plazoDias;

    @PositiveOrZero(message = "El número de pagos no puede ser negativo")
    @Max(value = 999, message = "El número de pagos admite máximo 999")
    private Integer numeroPagos;

    private LocalDate fechaSuscripcion;

    private LocalDate fechaInicio;

    /** La pueden registrar el abogado (aquí) o el financiero (cierre). */
    private LocalDate fechaTerminacion;

    @Valid
    private PresupuestoDTOPeticion presupuesto;
}
