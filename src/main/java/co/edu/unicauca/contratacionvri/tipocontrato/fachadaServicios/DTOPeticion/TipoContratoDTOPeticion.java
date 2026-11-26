package co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoContratoDTOPeticion {

    /** Se guarda tal como se escribe (con tildes): se muestra al usuario y en reportes. */
    @NotBlank(message = "El nombre del tipo de contrato es obligatorio")
    @Size(max = 80, message = "El nombre admite máximo 80 caracteres")
    private String nombre;

    @NotNull(message = "La serie documental es obligatoria")
    @Positive(message = "La serie documental debe ser un identificador válido")
    private Long serieId;
}
