package co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "persona_natural")
@PrimaryKeyJoinColumn(name = "tercero_id", foreignKey = @ForeignKey(name = "fk_persona_natural_tercero"))
@DiscriminatorValue(Naturaleza.VALOR_NATURAL)
@Getter
@Setter
public class PersonaNaturalEntity extends TerceroEntity {

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "ciudad_nacimiento", length = 80)
    private String ciudadNacimiento;

    @Column(name = "fecha_expedicion")
    private LocalDate fechaExpedicion;

    @Column(name = "lugar_expedicion", length = 80)
    private String lugarExpedicion;

    @Column(name = "profesion", length = 120)
    private String profesion;

    @Column(name = "tarjeta_profesional", length = 40)
    private String tarjetaProfesional;

    @Column(name = "entidad_tarjeta", length = 120)
    private String entidadTarjeta;

    @Column(name = "correo_institucional", length = 120)
    private String correoInstitucional;

    public PersonaNaturalEntity() {
        super(Naturaleza.NATURAL);
    }

    @Override
    protected String construirNombreCompleto() {
        return (nombres + " " + apellidos).trim();
    }
}
