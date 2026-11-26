package co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Tercero: contratistas, supervisores, interventores, ordenadores, directores de proyecto y
 * entidades financiadoras. Herencia JOINED: los datos comunes viven en {@code tercero} y los
 * propios de cada naturaleza en {@code persona_natural} o {@code persona_juridica}.
 */
@Entity
@Table(name = "tercero",
        uniqueConstraints = @UniqueConstraint(name = "uk_tercero_documento",
                columnNames = {"tipo_identificacion_id", "numero_documento"}),
        indexes = {
                @Index(name = "ix_tercero_numero_documento", columnList = "numero_documento"),
                @Index(name = "ix_tercero_nombre_completo", columnList = "nombre_completo")
        })
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "naturaleza", discriminatorType = DiscriminatorType.STRING, length = 10)
@Getter
@Setter
public abstract class TerceroEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Columna discriminadora expuesta como atributo de solo lectura: Hibernate la escribe a partir
     * de la clase concreta, por eso no se puede modificar (la naturaleza no cambia nunca).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "naturaleza", insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Naturaleza naturaleza;

    /** Debe ser un ítem del catálogo TIPO_IDENTIFICACION. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_identificacion_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_tercero_tipo_identificacion"))
    private CatalogoItemEntity tipoIdentificacion;

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    /** Dígito de verificación DIAN. Solo para NIT; lo calcula el sistema, nunca la API. */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "dv", length = 1)
    private String dv;

    /** Nombres + apellidos o razón social. Lo arma el sistema para buscar y mostrar. */
    @Column(name = "nombre_completo", nullable = false, length = 200)
    @Setter(AccessLevel.NONE)
    private String nombreCompleto;

    @Column(name = "correo", length = 120)
    private String correo;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "celular", length = 20)
    private String celular;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;

    /** Requerido por JPA (proxies). */
    protected TerceroEntity() {
    }

    protected TerceroEntity(Naturaleza naturaleza) {
        this.naturaleza = naturaleza;
    }

    /** Nombre para mostrar según la naturaleza. */
    protected abstract String construirNombreCompleto();

    /**
     * Recalcula nombre_completo. El servicio lo llama antes de responder (para que la respuesta
     * ya lo traiga) y JPA lo vuelve a llamar antes de insertar o actualizar, por seguridad.
     */
    @PrePersist
    @PreUpdate
    public void actualizarNombreCompleto() {
        this.nombreCompleto = construirNombreCompleto();
    }
}
