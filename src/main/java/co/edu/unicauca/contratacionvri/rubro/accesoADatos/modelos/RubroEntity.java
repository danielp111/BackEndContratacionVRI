package co.edu.unicauca.contratacionvri.rubro.accesoADatos.modelos;

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

/**
 * Rubro presupuestal (código de afectación presupuestal). Su nombre es lo que el libro de Excel
 * llama "dependencia de afectación": el contrato la toma del rubro, no de la tabla dependencia.
 *
 * El código es texto, no número: conserva ceros a la izquierda, puntos y guiones tal como los
 * entrega el sistema financiero.
 */
@Entity
@Table(name = "rubro_presupuestal",
        uniqueConstraints = @UniqueConstraint(name = "uk_rubro_presupuestal_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
public class RubroEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 40)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 300)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;

    /** Texto para los selectores: "1052002 - Museo de Historia Natural". */
    public String getEtiqueta() {
        return codigo + " - " + nombre;
    }
}
