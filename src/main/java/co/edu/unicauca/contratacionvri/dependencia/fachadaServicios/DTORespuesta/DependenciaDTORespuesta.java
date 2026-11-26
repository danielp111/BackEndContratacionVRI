package co.edu.unicauca.contratacionvri.dependencia.fachadaServicios.DTORespuesta;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DependenciaDTORespuesta {

    private Long id;
    private String nombre;
    private Boolean activo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
