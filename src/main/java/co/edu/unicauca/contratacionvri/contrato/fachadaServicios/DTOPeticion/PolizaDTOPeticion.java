package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PolizaDTOPeticion {

    @NotBlank(message = "El número de la póliza es obligatorio")
    @Size(max = 40, message = "El número de la póliza admite máximo 40 caracteres")
    private String numero;

    @NotNull(message = "La aseguradora es obligatoria")
    @Positive(message = "La aseguradora debe ser un identificador válido")
    private Long aseguradoraId;

    private LocalDate vigenciaHasta;
}
