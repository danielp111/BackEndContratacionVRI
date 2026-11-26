package co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** No incluye es_convenio: lo calcula el sistema (código 6.1-18.x). Si el cliente lo envía, se ignora. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SerieDocumentalDTOPeticion {

    @NotBlank(message = "El código de la serie es obligatorio")
    @Size(max = 20, message = "El código de la serie admite máximo 20 caracteres")
    @Pattern(regexp = "^\\d+(\\.\\d+)*-\\d+(\\.\\d+)*$",
            message = "El código de la serie debe tener el formato de la TRD, por ejemplo 6.1-17.13")
    private String codigo;
}
