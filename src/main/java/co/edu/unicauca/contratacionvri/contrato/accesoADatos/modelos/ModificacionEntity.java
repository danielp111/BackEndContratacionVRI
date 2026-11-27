package co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos;

import java.math.BigDecimal;
import java.time.LocalDate;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** Adición u otrosí del contrato: en valor, en tiempo o en ambos. */
@Entity
@Table(name = "modificacion", indexes = @Index(name = "ix_modificacion_contrato", columnList = "contrato_id"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModificacionEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_modificacion_contrato"))
    @Setter(AccessLevel.NONE)
    private ContratoEntity contrato;

    /** Ítem del catálogo TIPO_ADICION (VALOR, TIEMPO o VALOR_TIEMPO). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_adicion_id", nullable = false, foreignKey = @ForeignKey(name = "fk_modificacion_tipo_adicion"))
    private CatalogoItemEntity tipoAdicion;

    /** Solo en adiciones en valor. */
    @Column(name = "valor", precision = 18, scale = 2)
    private BigDecimal valor;

    /** Solo en adiciones en tiempo. */
    @Column(name = "dias")
    private Integer dias;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    /** Número del otrosí o documento que la soporta. */
    @Column(name = "documento", length = 200)
    private String documento;

    @Column(name = "observacion", length = 390)
    private String observacion;

    public ModificacionEntity(ContratoEntity contrato) {
        this.contrato = contrato;
    }
}
