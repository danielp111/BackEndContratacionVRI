package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Adición: en valor exige valor; en tiempo exige días; en valor y tiempo exige los dos. */
@Getter
@Setter
@NoArgsConstructor
public class ModificacionDTOPeticion {

    @NotNull(message = "El tipo de adición es obligatorio")
    @Positive(message = "El tipo de adición debe ser un identificador válido")
    private Long tipoAdicionId;

    @Positive(message = "El valor de la adición debe ser mayor que cero")
    @Digits(integer = 16, fraction = 2, message = "El valor admite 16 enteros y 2 decimales")
    private BigDecimal valor;

    @Positive(message = "Los días de la adición deben ser mayores que cero")
    private Integer dias;

    @NotNull(message = "La fecha de la modificación es obligatoria")
    private LocalDate fecha;

    @Size(max = 200, message = "El documento admite máximo 200 caracteres")
    private String documento;

    @Size(max = 390, message = "La observación admite máximo 390 caracteres")
    private String observacion;
}
