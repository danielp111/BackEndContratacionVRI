package co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase de contrato o de convenio según el SIRECI (Contraloría). El código y el ámbito son
 * oficiales y no cambian después de crear la clase: no tienen setter y solo se asignan en el
 * constructor.
 */
@Entity
@Table(name = "clase_contrato",
        uniqueConstraints = @UniqueConstraint(name = "uk_clase_contrato_ambito_codigo",
                columnNames = {"ambito", "codigo_sireci"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClaseContratoEntity extends EntidadAuditable {

    /** Código de "OTROS" en el ámbito CONTRATO: exige describir la otra clase en el contrato. */
    public static final int CODIGO_OTROS = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_sireci", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Integer codigoSireci;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "ambito", nullable = false, length = 10, updatable = false)
    @Setter(AccessLevel.NONE)
    private AmbitoClase ambito;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;

    public ClaseContratoEntity(AmbitoClase ambito, Integer codigoSireci, String nombre) {
        this.ambito = ambito;
        this.codigoSireci = codigoSireci;
        this.nombre = nombre;
    }

    /**
     * ¿Es "30 OTROS" de contratos? El contrato exigirá otra_clase cuando lo sea y la dejará vacía
     * en cualquier otra clase. El código 30 en el ámbito CONVENIO no es "OTROS".
     */
    public boolean esOtros() {
        return ambito == AmbitoClase.CONTRATO && Integer.valueOf(CODIGO_OTROS).equals(codigoSireci);
    }

    /** Texto para los selectores: "23 PRESTACIÓN DE SERVICIOS". */
    public String getEtiqueta() {
        return codigoSireci + " " + nombre;
    }
}
