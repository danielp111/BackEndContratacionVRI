package co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos;

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
 * Catálogo de listas cortas (TIPO_IDENTIFICACION, NATURALEZA, ASEGURADORA...). El código lo usa el
 * programa (ver CodigoCatalogo): se asigna al crear y no cambia.
 */
@Entity
@Table(name = "catalogo", uniqueConstraints = @UniqueConstraint(name = "uk_catalogo_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogoEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 40, updatable = false)
    @Setter(AccessLevel.NONE)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;

    public CatalogoEntity(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }
}
