package co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos;

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Certificado de disponibilidad presupuestal del contrato. */
@Entity
@Table(name = "contrato_cdp",
        uniqueConstraints = @UniqueConstraint(name = "uk_contrato_cdp_numero", columnNames = {"contrato_id", "numero_cdp"}))
@AttributeOverride(name = "numero", column = @Column(name = "numero_cdp", nullable = false, length = 40))
@AssociationOverride(name = "contrato", joinColumns = @JoinColumn(name = "contrato_id", nullable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_contrato_cdp_contrato")))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContratoCdpEntity extends DocumentoPresupuestalEntity {

    public ContratoCdpEntity(ContratoEntity contrato) {
        super(contrato);
    }
}
