package co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tipo de contrato (OPS, Suministro, Pasantía...). Varios tipos pueden compartir una serie documental. */
@Entity
@Table(name = "tipo_contrato",
        uniqueConstraints = @UniqueConstraint(name = "uk_tipo_contrato_nombre", columnNames = "nombre"))
@Getter
@Setter
@NoArgsConstructor
public class TipoContratoEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "serie_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tipo_contrato_serie"))
    private SerieDocumentalEntity serie;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;
}
