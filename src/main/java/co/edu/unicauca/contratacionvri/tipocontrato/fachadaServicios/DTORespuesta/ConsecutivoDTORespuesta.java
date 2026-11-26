package co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta;

/**
 * Estado del consecutivo de una serie en un año. Si aún no se ha emitido ningún número,
 * ultimoNumero es 0 y siguienteNumero es "serie/001".
 */
public record ConsecutivoDTORespuesta(
        Long serieId,
        String serieCodigo,
        Integer anio,
        Integer ultimoNumero,
        /** Número que recibiría el próximo contrato, p. ej. "6.1-17.13/109". Informativo: no lo reserva. */
        String siguienteNumero) {
}
