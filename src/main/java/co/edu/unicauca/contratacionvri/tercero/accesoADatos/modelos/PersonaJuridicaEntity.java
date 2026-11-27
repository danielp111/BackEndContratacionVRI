package co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "persona_juridica")
@PrimaryKeyJoinColumn(name = "tercero_id", foreignKey = @ForeignKey(name = "fk_persona_juridica_tercero"))
@DiscriminatorValue(Naturaleza.VALOR_JURIDICA)
@Getter
@Setter
public class PersonaJuridicaEntity extends TerceroEntity {

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    /**
     * Apunta a persona_natural (no a tercero): así la propia base impide que el representante
     * legal sea una persona jurídica.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representante_legal_id",
            foreignKey = @ForeignKey(name = "fk_persona_juridica_representante"))
    private PersonaNaturalEntity representanteLegal;

    /**
     * Unión temporal o consorcio. El SIRECI distingue tres naturalezas (catálogo NATURALEZA):
     * 1 persona natural, 2 persona jurídica, 3 persona jurídica - unión temporal o consorcio; esta
     * marca da la tercera sin cambiar la columna discriminadora.
     */
    @Column(name = "es_consorcio_union_temporal", nullable = false)
    private Boolean esConsorcioUnionTemporal = Boolean.FALSE;

    public PersonaJuridicaEntity() {
        super(Naturaleza.JURIDICA);
    }

    @Override
    protected String construirNombreCompleto() {
        return razonSocial;
    }
}
