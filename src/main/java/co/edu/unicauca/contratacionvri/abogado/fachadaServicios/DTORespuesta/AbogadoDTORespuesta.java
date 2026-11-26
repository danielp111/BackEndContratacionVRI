package co.edu.unicauca.contratacionvri.abogado.fachadaServicios.DTORespuesta;

import java.time.LocalDate;
import java.time.LocalDateTime;

import co.edu.unicauca.contratacionvri.abogado.accesoADatos.modelos.TipoVinculacion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos del abogado que la API devuelve. Además de los ids de las relaciones incluye su
 * texto (correo del usuario, etiqueta del tipo de identificación, nombre de la dependencia)
 * para que el frontend no tenga que hacer más consultas.
 */
@Getter
@Setter
@NoArgsConstructor
public class AbogadoDTORespuesta {

    private Long id;
    private Long usuarioId;
    private String usuarioCorreo;
    private Long tipoIdentificacionId;
    private String tipoIdentificacionEtiqueta;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String correoInstitucional;
    private String telefono;
    private String tarjetaProfesional;
    private String cargo;
    private TipoVinculacion tipoVinculacion;
    private Long dependenciaId;
    private String dependenciaNombre;
    private LocalDate fechaIngreso;
    private Boolean activo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
