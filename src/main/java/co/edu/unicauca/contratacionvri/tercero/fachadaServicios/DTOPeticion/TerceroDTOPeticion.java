package co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Campos comunes a persona natural y jurídica. No incluye dv (lo calcula el sistema),
 * naturaleza (la decide el endpoint), nombre_completo (lo arma el sistema) ni activo.
 * Si el cliente los envía, se ignoran.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class TerceroDTOPeticion {

    @NotNull(message = "El tipo de identificación es obligatorio")
    @Positive(message = "El tipo de identificación debe ser un identificador válido")
    private Long tipoIdentificacionId;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 20, message = "El número de documento admite máximo 20 caracteres")
    @Pattern(regexp = "^[0-9A-Za-z-]+$", message = "El número de documento solo admite números, letras y guiones")
    private String numeroDocumento;

    @Size(max = 120, message = "El correo admite máximo 120 caracteres")
    @Email(message = "El correo no tiene un formato válido")
    private String correo;

    @Size(max = 200, message = "La dirección admite máximo 200 caracteres")
    private String direccion;

    @Size(max = 20, message = "El teléfono admite máximo 20 caracteres")
    @Pattern(regexp = "^[0-9+() -]*$", message = "El teléfono solo admite números, espacios y los signos + - ( )")
    private String telefono;

    @Size(max = 20, message = "El celular admite máximo 20 caracteres")
    @Pattern(regexp = "^[0-9+() -]*$", message = "El celular solo admite números, espacios y los signos + - ( )")
    private String celular;
}
