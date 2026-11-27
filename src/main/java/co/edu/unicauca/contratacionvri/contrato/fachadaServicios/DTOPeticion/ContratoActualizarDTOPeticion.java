package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Actualizar los datos del abogado. Tipo y año no cambian: son opcionales y, si vienen, deben
 * coincidir con los del contrato (si no, 400).
 */
@Getter
@Setter
@NoArgsConstructor
public class ContratoActualizarDTOPeticion extends ContratoDTOPeticion {

    private Long tipoContratoId;

    private Integer anio;
}
