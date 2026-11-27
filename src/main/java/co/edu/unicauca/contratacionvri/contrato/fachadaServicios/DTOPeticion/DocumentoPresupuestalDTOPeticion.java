package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** CDP o RP. Valor y fecha son opcionales (los contratos migrados del Excel solo traen el número). */
@Getter
@Setter
@NoArgsConstructor
public class DocumentoPresupuestalDTOPeticion {

    @NotBlank(message = "El número es obligatorio")
    @Size(max = 40, message = "El número admite máximo 40 caracteres")
    private String numero;

    @Positive(message = "El valor debe ser mayor que cero")
    @Digits(integer = 16, fraction = 2, message = "El valor admite 16 enteros y 2 decimales")
    private BigDecimal valor;

    private LocalDate fecha;
}
