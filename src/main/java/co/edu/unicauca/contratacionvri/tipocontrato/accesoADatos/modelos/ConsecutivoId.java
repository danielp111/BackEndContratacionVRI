package co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos;

import java.io.Serializable;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Llave compuesta del consecutivo: (serie_id, anio). */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ConsecutivoId implements Serializable {

    @Column(name = "serie_id", nullable = false)
    private Long serieId;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "anio", nullable = false)
    private Integer anio;
}
