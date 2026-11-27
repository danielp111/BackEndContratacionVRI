package co.edu.unicauca.contratacionvri.ordenador.accesoADatos.modelos;

import java.time.LocalDate;

import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.PersonaNaturalEntity;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Periodo en que una persona firma contratos como ordenador del gasto (o su delegado) con un cargo.
 * Los datos personales (nombre, cédula) no se repiten aquí: vienen del tercero.
 *
 * El contrato guardará ordenador_gasto_id (el periodo, no la persona): así el cargo con el que firmó
 * queda fijo aunque la persona luego tenga otro cargo.
 */
@Entity
@Table(name = "ordenador_gasto",
        uniqueConstraints = @UniqueConstraint(name = "uk_ordenador_gasto_periodo",
                columnNames = {"tercero_id", "cargo", "fecha_desde"}))
@Getter
@Setter
@NoArgsConstructor
public class OrdenadorGastoEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Apunta a persona_natural: la propia base impide que el ordenador sea una persona jurídica. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tercero_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ordenador_gasto_tercero"))
    private PersonaNaturalEntity tercero;

    @Column(name = "cargo", nullable = false, length = 120)
    private String cargo;

    @Column(name = "es_delegado", nullable = false)
    private Boolean esDelegado = Boolean.FALSE;

    /** Número y fecha de la resolución de delegación. Obligatorio si es delegado; vacío si no. */
    @Column(name = "acto_delegacion", length = 200)
    private String actoDelegacion;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDate fechaDesde;

    /** Nula mientras el periodo sigue abierto. */
    @Column(name = "fecha_hasta")
    private LocalDate fechaHasta;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;

    /** ¿El periodo está activo y cubre la fecha? Ambos extremos se incluyen. */
    public boolean estaVigenteEn(LocalDate fecha) {
        return Boolean.TRUE.equals(activo)
                && !fecha.isBefore(fechaDesde)
                && (fechaHasta == null || !fecha.isAfter(fechaHasta));
    }

    /** "FRANCISCO JOSE PINO CORREA - Vicerrector de Investigaciones", para los selectores. */
    public String getEtiqueta() {
        return tercero.getNombreCompleto() + " - " + cargo;
    }
}
