package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta;

import java.time.LocalDate;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.RolSeguimiento;

/** Supervisor o interventor con los datos del tercero listos para el SIRECI (tipo y número de documento, DV y nombre). */
public record SeguimientoDTORespuesta(Long id, RolSeguimiento rol, Long terceroId, String nombre, String tipoIdentificacion,
                                      String numeroDocumento, String dv, LocalDate desde, LocalDate hasta, boolean vigente) {
}
