package co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTORespuesta;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CatalogoItemDTORespuesta {

    private Long id;
    /** Código del catálogo al que pertenece (TIPO_IDENTIFICACION...). */
    private String catalogo;
    private String codigo;
    private String codigoSireci;
    private String nombre;
    /** "3 CÉDULA DE CIUDADANÍA", o solo el nombre si no tiene código SIRECI. */
    private String etiqueta;
    private Integer orden;
    private Boolean activo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
