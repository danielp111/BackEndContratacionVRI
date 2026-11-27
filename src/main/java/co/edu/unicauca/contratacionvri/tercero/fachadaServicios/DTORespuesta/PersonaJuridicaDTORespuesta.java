package co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PersonaJuridicaDTORespuesta extends TerceroDTORespuesta {

    private String razonSocial;
    private Boolean esConsorcioUnionTemporal;
    private Long representanteLegalId;
    private String representanteLegalNombre;
    private String representanteLegalDocumento;
}
