package co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.DTOPeticion;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cierra el periodo del ordenador en una fecha (último día en que puede firmar). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CerrarPeriodoDTOPeticion {

    @NotNull(message = "La fecha hasta es obligatoria para cerrar el periodo")
    private LocalDate fechaHasta;
}
