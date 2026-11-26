package co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTORespuesta;

import java.time.LocalDateTime;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.Catalogo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CatalogoItemDTORespuesta {

    private Long id;
    private Catalogo catalogo;
    private String codigoSireci;
    private String nombre;
    /** "código nombre", p. ej. "3 CÉDULA DE CIUDADANÍA". */
    private String etiqueta;
    private Boolean activo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
