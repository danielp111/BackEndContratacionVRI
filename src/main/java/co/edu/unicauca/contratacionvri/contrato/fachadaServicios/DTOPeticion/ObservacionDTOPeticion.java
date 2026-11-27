package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Observación sobre un campo del contrato. Los códigos de campo los da GET /api/contratos/campos. */
@Getter
@Setter
@NoArgsConstructor
public class ObservacionDTOPeticion {

    @NotBlank(message = "El campo observado es obligatorio")
    @Size(max = 40, message = "El campo admite máximo 40 caracteres")
    private String campo;

    @NotBlank(message = "El texto de la observación es obligatorio")
    @Size(max = 390, message = "La observación admite máximo 390 caracteres")
    private String texto;
}
