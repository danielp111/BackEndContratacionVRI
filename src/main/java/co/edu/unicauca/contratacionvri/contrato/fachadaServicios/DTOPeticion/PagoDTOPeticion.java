package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** El número del pago no se envía: lo asigna el sistema. */
@Getter
@Setter
@NoArgsConstructor
public class PagoDTOPeticion {

    @NotNull(message = "El valor del pago es obligatorio")
    @Positive(message = "El valor del pago debe ser mayor que cero")
    @Digits(integer = 16, fraction = 2, message = "El valor admite 16 enteros y 2 decimales")
    private BigDecimal valor;

    @NotNull(message = "La fecha del pago es obligatoria")
    private LocalDate fecha;
}
