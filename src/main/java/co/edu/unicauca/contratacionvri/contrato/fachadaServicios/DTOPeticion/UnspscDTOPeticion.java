package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UnspscDTOPeticion {

    /** 8 dígitos; se aceptan espacios entre grupos ("80 11 16 00"), que el servicio quita. */
    @NotBlank(message = "El código UNSPSC es obligatorio")
    @Size(max = 20, message = "El código UNSPSC admite máximo 20 caracteres")
    private String codigo;
}
