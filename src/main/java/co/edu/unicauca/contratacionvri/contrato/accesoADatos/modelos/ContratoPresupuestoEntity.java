package co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import co.edu.unicauca.contratacionvri.dependencia.accesoADatos.modelos.DependenciaEntity;
import co.edu.unicauca.contratacionvri.proyecto.accesoADatos.modelos.ProyectoEntity;
import co.edu.unicauca.contratacionvri.rubro.accesoADatos.modelos.RubroEntity;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.TerceroEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos presupuestales del contrato (1:1, la llave es el id del contrato). La dependencia de
 * afectación es el nombre del rubro y el porcentaje de anticipo se calcula: no se guardan.
 */
@Entity
@Table(name = "contrato_presupuesto",
        indexes = @Index(name = "ix_contrato_presupuesto_rubro", columnList = "rubro_id"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContratoPresupuestoEntity extends EntidadAuditable {

    @Id
    @Column(name = "contrato_id")
    @Setter(AccessLevel.NONE)
    private Long contratoId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", foreignKey = @ForeignKey(name = "fk_contrato_presupuesto_contrato"))
    @Setter(AccessLevel.NONE)
    private ContratoEntity contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubro_id", foreignKey = @ForeignKey(name = "fk_contrato_presupuesto_rubro"))
    private RubroEntity rubro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", foreignKey = @ForeignKey(name = "fk_contrato_presupuesto_proyecto"))
    private ProyectoEntity proyecto;

    /** Código del plan anual de adquisiciones. */
    @Column(name = "paa", length = 40)
    private String paa;

    /** Ítem del catálogo FUENTE_FINANCIACION. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fuente_financiacion_id", foreignKey = @ForeignKey(name = "fk_contrato_presupuesto_fuente"))
    private CatalogoItemEntity fuenteFinanciacion;

    /** ¿Los recursos provienen de un contrato o convenio interadministrativo? Si es verdadero exige la entidad. */
    @Column(name = "recursos_de_convenio")
    private Boolean recursosDeConvenio;

    /** Entidad de donde provienen los recursos: tercero con NIT. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entidad_financiadora_id", foreignKey = @ForeignKey(name = "fk_contrato_presupuesto_entidad"))
    private TerceroEntity entidadFinanciadora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependencia_solicitante_id", foreignKey = @ForeignKey(name = "fk_contrato_presupuesto_dependencia"))
    private DependenciaEntity dependenciaSolicitante;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "naturaleza_gasto", length = 3)
    private NaturalezaGasto naturalezaGasto;

    /** Ítem del catálogo TIPO_ANTICIPO. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_anticipo_id", foreignKey = @ForeignKey(name = "fk_contrato_presupuesto_tipo_anticipo"))
    private CatalogoItemEntity tipoAnticipo;

    /** 0 si "No pactados". */
    @Column(name = "valor_anticipo", precision = 18, scale = 2)
    private BigDecimal valorAnticipo;

    /** La registra el financiero. */
    @Column(name = "fecha_certificacion_pago")
    private LocalDate fechaCertificacionPago;

    ContratoPresupuestoEntity(ContratoEntity contrato) {
        this.contrato = contrato;
    }

    /** Porcentaje de anticipo = valor del anticipo × 100 ÷ valor inicial, con 2 decimales. */
    public BigDecimal getPorcentajeAnticipo() {
        BigDecimal valorInicial = contrato.getValorInicial();
        if (valorAnticipo == null || valorInicial == null || valorInicial.signum() == 0) {
            return null;
        }
        return valorAnticipo.multiply(BigDecimal.valueOf(100)).divide(valorInicial, 2, RoundingMode.HALF_UP);
    }
}
