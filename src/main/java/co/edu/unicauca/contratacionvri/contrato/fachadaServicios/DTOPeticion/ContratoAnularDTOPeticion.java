package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContratoAnularDTOPeticion {

    @NotBlank(message = "El motivo de la anulación es obligatorio")
    @Size(max = 390, message = "El motivo admite máximo 390 caracteres")
    private String motivo;
}
