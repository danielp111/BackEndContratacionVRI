package co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTOPeticion;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PersonaNaturalDTOPeticion extends TerceroDTOPeticion {

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres admiten máximo 100 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos admiten máximo 100 caracteres")
    private String apellidos;

    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private LocalDate fechaNacimiento;

    @Size(max = 80, message = "La ciudad de nacimiento admite máximo 80 caracteres")
    private String ciudadNacimiento;

    @PastOrPresent(message = "La fecha de expedición no puede ser futura")
    private LocalDate fechaExpedicion;

    @Size(max = 80, message = "El lugar de expedición admite máximo 80 caracteres")
    private String lugarExpedicion;

    @Size(max = 120, message = "La profesión admite máximo 120 caracteres")
    private String profesion;

    @Size(max = 40, message = "La tarjeta profesional admite máximo 40 caracteres")
    private String tarjetaProfesional;

    @Size(max = 120, message = "La entidad de la tarjeta admite máximo 120 caracteres")
    private String entidadTarjeta;

    @Size(max = 120, message = "El correo institucional admite máximo 120 caracteres")
    @Email(message = "El correo institucional no tiene un formato válido")
    private String correoInstitucional;
}
