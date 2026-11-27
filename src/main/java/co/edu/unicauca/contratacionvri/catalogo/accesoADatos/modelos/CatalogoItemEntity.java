package co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos;

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

/**
 * Ítem de un catálogo. Tiene dos códigos:
 * <ul>
 *   <li>{@code codigo}: mnemónico que usa el programa (CC, NIT, SUPERVISOR). No cambia.</li>
 *   <li>{@code codigoSireci}: número que pide la Contraloría ("3" de "3 CÉDULA DE CIUDADANÍA").
 *       Opcional: fuente de financiación y aseguradora no lo tienen.</li>
 * </ul>
 * El catálogo tampoco cambia: un ítem no se mueve de catálogo.
 */
@Entity
@Table(name = "catalogo_item", uniqueConstraints = {
        @UniqueConstraint(name = "uk_catalogo_item_codigo", columnNames = {"catalogo_id", "codigo"}),
        @UniqueConstraint(name = "uk_catalogo_item_nombre", columnNames = {"catalogo_id", "nombre"})
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogoItemEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalogo_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_catalogo_item_catalogo"))
    @Setter(AccessLevel.NONE)
    private CatalogoEntity catalogo;

    @Column(name = "codigo", nullable = false, length = 40, updatable = false)
    @Setter(AccessLevel.NONE)
    private String codigo;

    @Column(name = "codigo_sireci", length = 10)
    private String codigoSireci;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    /** Posición en los selectores (luego se ordena por nombre). */
    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;

    public CatalogoItemEntity(CatalogoEntity catalogo, String codigo, String nombre) {
        this.catalogo = catalogo;
        this.codigo = codigo;
        this.nombre = nombre;
    }

    /** "3 CÉDULA DE CIUDADANÍA" si tiene código SIRECI; si no, solo el nombre ("Funcionamiento"). */
    public String getEtiqueta() {
        return codigoSireci == null ? nombre : codigoSireci + " " + nombre;
    }
}
