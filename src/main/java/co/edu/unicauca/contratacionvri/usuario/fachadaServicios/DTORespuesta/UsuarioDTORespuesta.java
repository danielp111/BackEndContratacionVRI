package co.edu.unicauca.contratacionvri.usuario.fachadaServicios.DTORespuesta;

import java.time.LocalDateTime;

import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.Rol;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** No expone el google_id: solo indica si la cuenta ya ingresó alguna vez con Google. */
@Getter
@Setter
@NoArgsConstructor
public class UsuarioDTORespuesta {

    private Long id;
    private String correo;
    private String nombre;
    private Rol rol;
    private Boolean activo;
    private boolean vinculadoConGoogle;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
