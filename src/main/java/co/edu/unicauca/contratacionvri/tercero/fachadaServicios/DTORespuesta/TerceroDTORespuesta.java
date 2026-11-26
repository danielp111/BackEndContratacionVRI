package co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta;

import java.time.LocalDateTime;

import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.Naturaleza;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Campos comunes de la respuesta. GET /api/terceros/{id} devuelve la subclase que corresponda;
 * el campo {@code naturaleza} le dice al cliente cuál de las dos formas recibió.
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class TerceroDTORespuesta {

    private Long id;
    private Naturaleza naturaleza;
    private Long tipoIdentificacionId;
    private String tipoIdentificacionEtiqueta;
    private String numeroDocumento;
    private String dv;
    private String nombreCompleto;
    private String correo;
    private String direccion;
    private String telefono;
    private String celular;
    private Boolean activo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
