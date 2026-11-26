package co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos;

import org.springframework.data.domain.Persistable;

import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Último número asignado de una serie en un año. Es por serie (no por tipo) para que los tipos
 * que comparten serie (Aprendizaje, Pasantía y Judicatura usan 6.1-17.3) no repitan números.
 *
 * Implementa {@link Persistable}: con la llave asignada a mano, save() haría merge, y si otra
 * petición ya creó la fila, el merge la actualizaría con ultimo_numero = 0 y reiniciaría el
 * consecutivo. Con isNew() = true la creación siempre es un INSERT, que falla si la fila ya existe.
 */
@Entity
@Table(name = "consecutivo")
@Getter
@Setter
@NoArgsConstructor
public class ConsecutivoEntity extends EntidadAuditable implements Persistable<ConsecutivoId> {

    @EmbeddedId
    @Setter(AccessLevel.NONE)
    private ConsecutivoId id;

    @MapsId("serieId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "serie_id", foreignKey = @ForeignKey(name = "fk_consecutivo_serie"))
    @Setter(AccessLevel.NONE)
    private SerieDocumentalEntity serie;

    @Column(name = "ultimo_numero", nullable = false)
    private Integer ultimoNumero = 0;

    @Transient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean nuevo = true;

    public ConsecutivoEntity(SerieDocumentalEntity serie, Integer anio) {
        this.id = new ConsecutivoId(serie.getId(), anio);
        this.serie = serie;
    }

    /** Siguiente número; solo debe llamarse con la fila bloqueada (SELECT ... FOR UPDATE). */
    public int incrementar() {
        ultimoNumero = ultimoNumero + 1;
        return ultimoNumero;
    }

    @Override
    public boolean isNew() {
        return nuevo;
    }

    @PostLoad
    @PostPersist
    void marcarComoExistente() {
        this.nuevo = false;
    }
}
