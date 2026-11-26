package co.edu.unicauca.contratacionvri.dependencia.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DependenciaDTOPeticion {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre admite máximo 200 caracteres")
    private String nombre;
}
