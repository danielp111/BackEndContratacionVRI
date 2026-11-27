package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion;

import java.time.LocalDate;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.RolSeguimiento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SeguimientoDTOPeticion {

    @NotNull(message = "El rol (SUPERVISOR o INTERVENTOR) es obligatorio")
    private RolSeguimiento rol;

    @NotNull(message = "El tercero es obligatorio")
    @Positive(message = "El tercero debe ser un identificador válido")
    private Long terceroId;

    @NotNull(message = "La fecha desde es obligatoria")
    private LocalDate desde;

    /** Vacía mientras siga vigente. */
    private LocalDate hasta;
}
