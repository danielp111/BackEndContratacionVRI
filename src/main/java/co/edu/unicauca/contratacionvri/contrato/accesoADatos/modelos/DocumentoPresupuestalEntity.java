package co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos;

import java.math.BigDecimal;
import java.time.LocalDate;

import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CDP o RP del contrato: misma forma, tablas distintas. Cada subclase da el nombre de su columna de
 * número (numero_cdp, numero_rp). Valor y fecha son opcionales: la migración del Excel solo trae el número.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DocumentoPresupuestalEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private ContratoEntity contrato;

    /** La subclase cambia el nombre de la columna con @AttributeOverride. */
    @Column(name = "numero", nullable = false, length = 40)
    private String numero;

    @Column(name = "valor", precision = 18, scale = 2)
    private BigDecimal valor;

    @Column(name = "fecha")
    private LocalDate fecha;

    protected DocumentoPresupuestalEntity(ContratoEntity contrato) {
        this.contrato = contrato;
    }
}
