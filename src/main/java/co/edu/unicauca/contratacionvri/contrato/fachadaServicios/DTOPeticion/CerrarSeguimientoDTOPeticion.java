package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CerrarSeguimientoDTOPeticion {

    @NotNull(message = "La fecha hasta es obligatoria")
    private LocalDate hasta;
}
