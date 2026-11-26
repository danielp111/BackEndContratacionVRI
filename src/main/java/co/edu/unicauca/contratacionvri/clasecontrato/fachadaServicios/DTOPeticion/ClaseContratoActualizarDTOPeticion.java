package co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.DTOPeticion;

import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.AmbitoClase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Solo se actualiza el nombre. El código y el ámbito no cambian: son opcionales aquí y se aceptan
 * si coinciden con los actuales (clientes que reenvían el objeto completo); si son distintos, 400.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaseContratoActualizarDTOPeticion {

    @NotBlank(message = "El nombre de la clase es obligatorio")
    @Size(max = 120, message = "El nombre admite máximo 120 caracteres")
    private String nombre;

    private Integer codigoSireci;

    private AmbitoClase ambito;
}
