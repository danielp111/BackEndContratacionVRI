package co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos;

import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
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

/** Código UNSPSC (clasificación de bienes y servicios) del contrato: 8 dígitos. */
@Entity
@Table(name = "contrato_unspsc",
        uniqueConstraints = @UniqueConstraint(name = "uk_contrato_unspsc_codigo", columnNames = {"contrato_id", "codigo_unspsc"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContratoUnspscEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_contrato_unspsc_contrato"))
    private ContratoEntity contrato;

    @Column(name = "codigo_unspsc", nullable = false, updatable = false, length = 10)
    private String codigoUnspsc;

    public ContratoUnspscEntity(ContratoEntity contrato, String codigoUnspsc) {
        this.contrato = contrato;
        this.codigoUnspsc = codigoUnspsc;
    }
}
