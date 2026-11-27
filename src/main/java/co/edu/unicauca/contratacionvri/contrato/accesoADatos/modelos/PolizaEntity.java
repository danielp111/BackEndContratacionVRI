package co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos;

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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Póliza de seguros del contrato. */
@Entity
@Table(name = "poliza", uniqueConstraints = @UniqueConstraint(name = "uk_poliza_numero", columnNames = {"contrato_id", "numero"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolizaEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_poliza_contrato"))
    @Setter(AccessLevel.NONE)
    private ContratoEntity contrato;

    @Column(name = "numero", nullable = false, length = 40)
    private String numero;

    /** Ítem del catálogo ASEGURADORA. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aseguradora_id", nullable = false, foreignKey = @ForeignKey(name = "fk_poliza_aseguradora"))
    private CatalogoItemEntity aseguradora;

    @Column(name = "vigencia_hasta")
    private LocalDate vigenciaHasta;

    public PolizaEntity(ContratoEntity contrato) {
        this.contrato = contrato;
    }
}
