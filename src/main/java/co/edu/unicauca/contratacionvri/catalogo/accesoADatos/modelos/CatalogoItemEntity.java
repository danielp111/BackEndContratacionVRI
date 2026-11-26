package co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ítem de una lista corta (tipo de identificación, naturaleza, aseguradora...).
 * El código SIRECI es el número que pide la Contraloría; se muestra como "código nombre".
 */
@Entity
@Table(name = "catalogo_item",
        uniqueConstraints = @UniqueConstraint(name = "uk_catalogo_item_codigo", columnNames = {"catalogo", "codigo_sireci"}),
        indexes = @Index(name = "ix_catalogo_item_catalogo_activo", columnList = "catalogo, activo"))
@Getter
@Setter
@NoArgsConstructor
public class CatalogoItemEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "catalogo", nullable = false, length = 40)
    private Catalogo catalogo;

    @Column(name = "codigo_sireci", nullable = false, length = 10)
    private String codigoSireci;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;

    /** Texto para mostrar, p. ej. "3 CÉDULA DE CIUDADANÍA". */
    public String getEtiqueta() {
        return codigoSireci + " " + nombre;
    }
}
