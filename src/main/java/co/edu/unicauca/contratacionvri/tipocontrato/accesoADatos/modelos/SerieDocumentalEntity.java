package co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos;

import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Serie documental de la TRD (p. ej. 6.1-17.13). Es el prefijo del número del contrato
 * ("6.1-17.13/023") y la unidad del consecutivo: varios tipos de contrato pueden compartirla.
 */
@Entity
@Table(name = "serie_documental",
        uniqueConstraints = @UniqueConstraint(name = "uk_serie_documental_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
public class SerieDocumentalEntity extends EntidadAuditable {

    /** Las series de convenios son las 6.1-18.x. */
    public static final String PREFIJO_CONVENIO = "6.1-18.";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 20)
    @Setter(AccessLevel.NONE)
    private String codigo;

    /** Lo calcula el sistema a partir del código; no se recibe ni se edita, así no puede quedar inconsistente. */
    @Column(name = "es_convenio", nullable = false)
    @Setter(AccessLevel.NONE)
    private Boolean esConvenio;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;

    /** Asigna el código y recalcula es_convenio en el mismo paso. */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
        this.esConvenio = esCodigoDeConvenio(codigo);
    }

    public static boolean esCodigoDeConvenio(String codigo) {
        return codigo != null && codigo.startsWith(PREFIJO_CONVENIO);
    }
}
