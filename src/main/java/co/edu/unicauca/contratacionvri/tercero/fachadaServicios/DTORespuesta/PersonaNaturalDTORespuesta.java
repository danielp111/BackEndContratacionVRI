package co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PersonaNaturalDTORespuesta extends TerceroDTORespuesta {

    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String ciudadNacimiento;
    private LocalDate fechaExpedicion;
    private String lugarExpedicion;
    private String profesion;
    private String tarjetaProfesional;
    private String entidadTarjeta;
    private String correoInstitucional;
}
