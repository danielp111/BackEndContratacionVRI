package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cómo se atendió la observación (opcional). */
@Getter
@Setter
@NoArgsConstructor
public class ResolverObservacionDTOPeticion {

    @Size(max = 390, message = "La respuesta admite máximo 390 caracteres")
    private String respuesta;
}
