package co.edu.unicauca.contratacionvri.proyecto.accesoADatos.modelos;

import org.springframework.data.domain.Persistable;

import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.PersonaNaturalEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Proyecto de investigación. La llave es el código institucional que asigna la VRI (p. ej. 5019):
 * lo digita el usuario, no es autoincremental y no cambia después de crear el proyecto.
 *
 * Implementa {@link Persistable} porque, con un id asignado a mano, Spring Data trataría cualquier
 * save() como "actualizar si existe" (merge) y crear un código repetido sobrescribiría el proyecto
 * existente. Con isNew() = true un proyecto nuevo siempre se inserta (persist), y si el código ya
 * existe la llave primaria lo rechaza.
 */
@Entity
@Table(name = "proyecto", indexes = @Index(name = "ix_proyecto_activo", columnList = "activo"))
@Getter
@Setter
@NoArgsConstructor
public class ProyectoEntity extends EntidadAuditable implements Persistable<Integer> {

    @Id
    @Column(name = "id_proyecto", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Integer idProyecto;

    @Column(name = "nombre", nullable = false, length = 400)
    private String nombre;

    /** Apunta a persona_natural: la propia base impide que el director sea persona jurídica. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "director_tercero_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_proyecto_director"))
    private PersonaNaturalEntity director;

    @Column(name = "bpin", length = 20)
    private String bpin;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;

    @Transient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean nuevo = true;

    /** El código solo se asigna al crear; después no hay forma de cambiarlo. */
    public ProyectoEntity(Integer idProyecto) {
        this.idProyecto = idProyecto;
    }

    @Override
    public Integer getId() {
        return idProyecto;
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
