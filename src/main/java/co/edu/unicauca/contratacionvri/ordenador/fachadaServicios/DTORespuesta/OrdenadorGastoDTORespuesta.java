package co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.DTORespuesta;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Periodo del ordenador con los datos del tercero listos para el reporte SIRECI: nombre, cédula y
 * cargo del ordenador del gasto o su delegado.
 */
@Getter
@Setter
@NoArgsConstructor
public class OrdenadorGastoDTORespuesta {

    private Long id;
    private Long terceroId;
    private String nombre;
    private String cedula;
    /** "3 CÉDULA DE CIUDADANÍA". */
    private String tipoIdentificacion;
    private String cargo;
    private Boolean esDelegado;
    private String actoDelegacion;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    /** ¿Activo y vigente hoy? */
    private boolean vigente;
    private Boolean activo;
    /** "FRANCISCO JOSE PINO CORREA - Vicerrector de Investigaciones". */
    private String etiqueta;
    /** Falso si ya hay contratos con este periodo: tercero, cargo y fecha desde quedan fijos. */
    private boolean datosEditables;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
