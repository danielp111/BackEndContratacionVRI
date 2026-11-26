package co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.DTOPeticion;

import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.AmbitoClase;
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
public class ClaseContratoCrearDTOPeticion {

    @NotNull(message = "El código SIRECI es obligatorio")
    @Positive(message = "El código SIRECI debe ser un número positivo")
    private Integer codigoSireci;

    /** Texto oficial del SIRECI, con tildes. Se guarda tal como se escribe. */
    @NotBlank(message = "El nombre de la clase es obligatorio")
    @Size(max = 120, message = "El nombre admite máximo 120 caracteres")
    private String nombre;

    @NotNull(message = "El ámbito es obligatorio (CONTRATO o CONVENIO)")
    private AmbitoClase ambito;
}
