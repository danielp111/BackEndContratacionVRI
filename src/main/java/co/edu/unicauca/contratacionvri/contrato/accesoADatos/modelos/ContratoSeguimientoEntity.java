package co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos;

import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.TerceroEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Supervisor o interventor del contrato en un periodo. {@code hasta} nulo = vigente. Para cambiar de
 * supervisor se cierra el actual y se agrega el nuevo: así queda quién supervisó en cada momento.
 */
@Entity
@Table(name = "contrato_seguimiento",
        indexes = @Index(name = "ix_contrato_seguimiento_contrato_rol", columnList = "contrato_id, rol"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContratoSeguimientoEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_contrato_seguimiento_contrato"))
    @Setter(AccessLevel.NONE)
    private ContratoEntity contrato;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "rol", nullable = false, length = 15)
    private RolSeguimiento rol;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tercero_id", nullable = false, foreignKey = @ForeignKey(name = "fk_contrato_seguimiento_tercero"))
    private TerceroEntity tercero;

    @Column(name = "desde", nullable = false)
    private LocalDate desde;

    @Column(name = "hasta")
    private LocalDate hasta;

    public ContratoSeguimientoEntity(ContratoEntity contrato) {
        this.contrato = contrato;
    }

    /** ¿Cubre la fecha? Ambos extremos se incluyen. */
    public boolean estaVigenteEn(LocalDate fecha) {
        return !fecha.isBefore(desde) && (hasta == null || !fecha.isAfter(hasta));
    }
}
