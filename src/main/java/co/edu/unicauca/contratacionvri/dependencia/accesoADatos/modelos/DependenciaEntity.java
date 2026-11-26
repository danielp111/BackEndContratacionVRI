package co.edu.unicauca.contratacionvri.dependencia.accesoADatos.modelos;

import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dependencia de la universidad (solicitante, de afectación presupuestal, del abogado...). */
@Entity
@Table(name = "dependencia",
        uniqueConstraints = @UniqueConstraint(name = "uk_dependencia_nombre", columnNames = "nombre"))
@Getter
@Setter
@NoArgsConstructor
public class DependenciaEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;
}
