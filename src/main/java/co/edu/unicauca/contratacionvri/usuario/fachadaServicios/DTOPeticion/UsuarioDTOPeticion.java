package co.edu.unicauca.contratacionvri.usuario.fachadaServicios.DTOPeticion;

import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Lo que envía el ADMIN para crear o editar una cuenta. El google_id no se envía: lo llena el login. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTOPeticion {

    @NotBlank(message = "El correo es obligatorio")
    @Size(max = 120, message = "El correo admite máximo 120 caracteres")
    @Email(message = "El correo no tiene un formato válido")
    @Pattern(regexp = Textos.PATRON_CORREO_UNICAUCA, message = "El correo " + Textos.MENSAJE_CORREO_UNICAUCA)
    private String correo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre admite máximo 150 caracteres")
    private String nombre;

    @NotNull(message = "El rol es obligatorio (ABOGADO, FINANCIERO, ADMIN o CONSULTA)")
    private Rol rol;
}
