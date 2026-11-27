package co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.edu.unicauca.contratacionvri.abogado.accesoADatos.modelos.AbogadoEntity;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.ClaseContratoEntity;
import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import co.edu.unicauca.contratacionvri.ordenador.accesoADatos.modelos.OrdenadorGastoEntity;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.TerceroEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.SerieDocumentalEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.TipoContratoEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios.INumeracionServicio;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contrato. El número ("6.1-17.13/023") no se guarda: se arma con la serie y el consecutivo, que
 * junto con el año son únicos. Serie, año, consecutivo y tipo los fija la creación y no cambian.
 * El estado lo calcula el sistema a partir de las fechas; solo ANULADO es una acción explícita.
 */
@Entity
@Table(name = "contrato",
        uniqueConstraints = @UniqueConstraint(name = "uk_contrato_numero", columnNames = {"serie_id", "anio", "consecutivo"}),
        indexes = {
                @Index(name = "ix_contrato_anio_estado", columnList = "anio, estado"),
                @Index(name = "ix_contrato_responsable", columnList = "responsable_id"),
                @Index(name = "ix_contrato_contratista", columnList = "contratista_id")
        })
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContratoEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** Copia de la serie del tipo al crear: hace única la numeración aunque los tipos compartan serie. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "serie_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_contrato_serie"))
    @Setter(AccessLevel.NONE)
    private SerieDocumentalEntity serie;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "anio", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Integer anio;

    @Column(name = "consecutivo", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Integer consecutivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_contrato_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_contrato_tipo"))
    @Setter(AccessLevel.NONE)
    private TipoContratoEntity tipoContrato;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado", nullable = false, length = 20)
    @Setter(AccessLevel.NONE)
    private EstadoContrato estado = EstadoContrato.EN_TRAMITE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsable_id", nullable = false, foreignKey = @ForeignKey(name = "fk_contrato_responsable"))
    private AbogadoEntity responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clase_contrato_id", foreignKey = @ForeignKey(name = "fk_contrato_clase"))
    private ClaseContratoEntity claseContrato;

    /** Solo si la clase es 30 OTROS. */
    @Column(name = "otra_clase", length = 150)
    private String otraClase;

    @Column(name = "objeto", nullable = false, length = 390)
    private String objeto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contratista_id", foreignKey = @ForeignKey(name = "fk_contrato_contratista"))
    private TerceroEntity contratista;

    /** El periodo del ordenador (no la persona): fija el cargo con el que firmó. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordenador_gasto_id", foreignKey = @ForeignKey(name = "fk_contrato_ordenador"))
    private OrdenadorGastoEntity ordenadorGasto;

    /** Ítem del catálogo TIPO_SEGUIMIENTO. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_seguimiento_id", foreignKey = @ForeignKey(name = "fk_contrato_tipo_seguimiento"))
    private CatalogoItemEntity tipoSeguimiento;

    @Column(name = "valor_sin_iva", precision = 18, scale = 2)
    private BigDecimal valorSinIva;

    @Column(name = "iva", precision = 18, scale = 2)
    private BigDecimal iva;

    @Column(name = "plazo_dias")
    private Integer plazoDias;

    /** Número de pagos pactados: los pagos 1 a N serán obligatorios. */
    @Column(name = "numero_pagos")
    private Integer numeroPagos;

    @Column(name = "fecha_suscripcion")
    private LocalDate fechaSuscripcion;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_terminacion")
    private LocalDate fechaTerminacion;

    @Column(name = "fecha_acta_final")
    private LocalDate fechaActaFinal;

    @Column(name = "motivo_anulacion", length = 390)
    @Setter(AccessLevel.NONE)
    private String motivoAnulacion;

    @Column(name = "fecha_anulacion")
    @Setter(AccessLevel.NONE)
    private LocalDate fechaAnulacion;

    /** 1:1, se crea junto con el contrato. */
    @OneToOne(mappedBy = "contrato", cascade = CascadeType.ALL, optional = false)
    @Setter(AccessLevel.NONE)
    private ContratoPresupuestoEntity presupuesto;

    /**
     * Contrato nuevo, en trámite y con su presupuesto vacío. El consecutivo se asigna al final de la
     * creación ({@link #asignarConsecutivo}), para tener bloqueado el consecutivo el menor tiempo posible.
     */
    public ContratoEntity(TipoContratoEntity tipoContrato, SerieDocumentalEntity serie, Integer anio) {
        this.tipoContrato = tipoContrato;
        this.serie = serie;
        this.anio = anio;
        this.presupuesto = new ContratoPresupuestoEntity(this);
    }

    /** Solo al crear: el número no cambia después. */
    public void asignarConsecutivo(int consecutivo) {
        if (this.consecutivo != null) {
            throw new IllegalStateException("El contrato " + getNumero() + " ya tiene número");
        }
        this.consecutivo = consecutivo;
    }

    /** "6.1-17.13/023". */
    public String getNumero() {
        return INumeracionServicio.formatear(serie.getCodigo(), consecutivo);
    }

    /** Valor inicial = valor sin IVA + IVA. Nulo mientras no haya valor sin IVA. */
    public BigDecimal getValorInicial() {
        if (valorSinIva == null) {
            return null;
        }
        return valorSinIva.add(iva == null ? BigDecimal.ZERO : iva);
    }

    public boolean estaAnulado() {
        return estado == EstadoContrato.ANULADO;
    }

    /** Recalcula el estado a partir de las fechas. Un contrato anulado queda anulado. */
    public void recalcularEstado() {
        if (estaAnulado()) {
            return;
        }
        if (fechaActaFinal != null) {
            estado = EstadoContrato.LIQUIDADO;
        } else if (fechaTerminacion != null) {
            estado = EstadoContrato.TERMINADO;
        } else if (fechaInicio != null) {
            estado = EstadoContrato.EN_EJECUCION;
        } else if (fechaSuscripcion != null) {
            estado = EstadoContrato.SUSCRITO;
        } else {
            estado = EstadoContrato.EN_TRAMITE;
        }
    }

    /** Anula el contrato. El servicio valida antes que esté en trámite. */
    public void anular(String motivo, LocalDate fecha) {
        this.estado = EstadoContrato.ANULADO;
        this.motivoAnulacion = motivo;
        this.fechaAnulacion = fecha;
    }
}
