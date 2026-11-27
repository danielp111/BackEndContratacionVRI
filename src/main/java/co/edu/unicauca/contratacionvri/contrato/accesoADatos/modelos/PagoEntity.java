package co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.UsuarioEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Pago del contrato. El número lo asigna el sistema: consecutivo desde 1 por contrato. */
@Entity
@Table(name = "pago", uniqueConstraints = @UniqueConstraint(name = "uk_pago_numero", columnNames = {"contrato_id", "numero"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PagoEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_pago_contrato"))
    @Setter(AccessLevel.NONE)
    private ContratoEntity contrato;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "numero", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Integer numero;

    @Column(name = "valor", nullable = false, precision = 18, scale = 2)
    private BigDecimal valor;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    /** Quién lo registró. Nulo hasta que exista el inicio de sesión. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por", updatable = false, foreignKey = @ForeignKey(name = "fk_pago_registrado_por"))
    @Setter(AccessLevel.NONE)
    private UsuarioEntity registradoPor;

    public PagoEntity(ContratoEntity contrato, int numero, UsuarioEntity registradoPor) {
        this.contrato = contrato;
        this.numero = numero;
        this.registradoPor = registradoPor;
    }
}
