package co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTORespuesta;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProyectoDTORespuesta {

    private Integer idProyecto;
    private String nombre;
    private Long directorTerceroId;
    private String directorNombre;
    private String directorDocumento;
    private String bpin;
    private Boolean activo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
