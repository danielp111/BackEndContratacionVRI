package co.edu.unicauca.contratacionvri.abogado.accesoADatos.modelos;

import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import co.edu.unicauca.contratacionvri.dependencia.accesoADatos.modelos.DependenciaEntity;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.UsuarioEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos profesionales del abogado. Enlaza la cuenta de Google ({@code usuario}) con el
 * abogado responsable de los contratos ({@code contrato.responsable_id}).
 */
@Entity
@Table(name = "abogado", uniqueConstraints = {
        @UniqueConstraint(name = "uk_abogado_usuario", columnNames = "usuario_id"),
        @UniqueConstraint(name = "uk_abogado_numero_documento", columnNames = "numero_documento")
})
@Getter
@Setter
@NoArgsConstructor
public class AbogadoEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_abogado_usuario"))
    private UsuarioEntity usuario;

    /** Debe ser un ítem del catálogo TIPO_IDENTIFICACION. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_identificacion_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_abogado_tipo_identificacion"))
    private CatalogoItemEntity tipoIdentificacion;

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "correo_institucional", length = 120)
    private String correoInstitucional;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "tarjeta_profesional", length = 40)
    private String tarjetaProfesional;

    @Column(name = "cargo", length = 120)
    private String cargo;

    // VARCHAR y no ENUM de MySQL: así agregar un valor nuevo no exige alterar la tabla.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "tipo_vinculacion", nullable = false, length = 20)
    private TipoVinculacion tipoVinculacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependencia_id", foreignKey = @ForeignKey(name = "fk_abogado_dependencia"))
    private DependenciaEntity dependencia;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;
}
