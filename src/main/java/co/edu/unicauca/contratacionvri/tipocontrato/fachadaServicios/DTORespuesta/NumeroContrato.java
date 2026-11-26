package co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta;

/**
 * Número asignado por generarNumero. El contrato guarda {@code numero} ("6.1-17.13/023") y,
 * por separado, {@code consecutivo} y {@code anio} para ordenar y validar unicidad.
 */
public record NumeroContrato(String numero, Long serieId, String serieCodigo, Integer anio, Integer consecutivo) {
}
