package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Crear el contrato y obtener su número. Basta con tipo, año, responsable y objeto; el resto de
 * los datos es opcional y se puede completar después.
 */
@Getter
@Setter
@NoArgsConstructor
public class ContratoCrearDTOPeticion extends ContratoDTOPeticion {

    @NotNull(message = "El tipo de contrato es obligatorio")
    @Positive(message = "El tipo de contrato debe ser un identificador válido")
    private Long tipoContratoId;

    @NotNull(message = "El año del contrato es obligatorio")
    private Integer anio;
}
