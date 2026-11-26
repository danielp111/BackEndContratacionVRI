package co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.DTORespuesta;

import java.time.LocalDateTime;

import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.AmbitoClase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClaseContratoDTORespuesta {

    private Long id;
    private Integer codigoSireci;
    private String nombre;
    private AmbitoClase ambito;
    /** "23 PRESTACIÓN DE SERVICIOS", para los selectores. */
    private String etiqueta;
    /** Verdadero solo para "30 OTROS" de contratos: el contrato debe describir la otra clase. */
    private boolean esOtros;
    private Boolean activo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
