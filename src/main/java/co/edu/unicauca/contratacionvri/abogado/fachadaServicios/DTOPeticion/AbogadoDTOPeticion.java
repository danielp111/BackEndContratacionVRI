package co.edu.unicauca.contratacionvri.abogado.fachadaServicios.DTOPeticion;

import java.time.LocalDate;

import co.edu.unicauca.contratacionvri.abogado.accesoADatos.modelos.TipoVinculacion;
import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos que envía el cliente para crear o actualizar un abogado.
 * No incluye id, activo ni auditoría: el id lo genera la base de datos, el estado se cambia
 * con su propio endpoint y la auditoría la llena el sistema.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbogadoDTOPeticion {

    @NotNull(message = "El usuario (cuenta de Google) es obligatorio")
    @Positive(message = "El usuario debe ser un identificador válido")
    private Long usuarioId;

    @NotNull(message = "El tipo de identificación es obligatorio")
    @Positive(message = "El tipo de identificación debe ser un identificador válido")
    private Long tipoIdentificacionId;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 20, message = "El número de documento admite máximo 20 caracteres")
    @Pattern(regexp = "^[0-9A-Za-z-]+$", message = "El número de documento solo admite números, letras y guiones")
    private String numeroDocumento;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres admiten máximo 100 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos admiten máximo 100 caracteres")
    private String apellidos;

    @Size(max = 120, message = "El correo institucional admite máximo 120 caracteres")
    @Email(message = "El correo institucional no tiene un formato válido")
    @Pattern(regexp = Textos.PATRON_CORREO_UNICAUCA, message = "El correo institucional " + Textos.MENSAJE_CORREO_UNICAUCA)
    private String correoInstitucional;

    @Size(max = 20, message = "El teléfono admite máximo 20 caracteres")
    @Pattern(regexp = "^[0-9+()\\s-]*$", message = "El teléfono solo admite números, espacios y los signos + - ( )")
    private String telefono;

    @Size(max = 40, message = "La tarjeta profesional admite máximo 40 caracteres")
    private String tarjetaProfesional;

    @Size(max = 120, message = "El cargo admite máximo 120 caracteres")
    private String cargo;

    @NotNull(message = "El tipo de vinculación es obligatorio (PLANTA o CONTRATISTA)")
    private TipoVinculacion tipoVinculacion;

    @Positive(message = "La dependencia debe ser un identificador válido")
    private Long dependenciaId;

    @PastOrPresent(message = "La fecha de ingreso no puede ser futura")
    private LocalDate fechaIngreso;
}
