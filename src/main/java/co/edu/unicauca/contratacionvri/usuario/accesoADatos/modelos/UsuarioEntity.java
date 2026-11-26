package co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cuenta de acceso con Google. No guarda contraseña. La crea un ADMIN con el correo
 * institucional; google_id queda nulo hasta el primer inicio de sesión.
 */
@Entity
@Table(name = "usuario", uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_google_id", columnNames = "google_id"),
        @UniqueConstraint(name = "uk_usuario_correo", columnNames = "correo")
})
@Getter
@Setter
@NoArgsConstructor
public class UsuarioEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_id", length = 64)
    private String googleId;

    @Column(name = "correo", nullable = false, length = 120)
    private String correo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;
}
