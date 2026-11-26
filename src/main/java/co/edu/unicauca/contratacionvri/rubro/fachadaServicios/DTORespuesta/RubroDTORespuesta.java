package co.edu.unicauca.contratacionvri.rubro.fachadaServicios.DTORespuesta;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RubroDTORespuesta {

    private Long id;
    private String codigo;
    private String nombre;
    /** "1052002 - Museo de Historia Natural", para los selectores. */
    private String etiqueta;
    private Boolean activo;
    /** Falso si ya hay contratos con el rubro: el código queda fijo. */
    private boolean codigoEditable;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
