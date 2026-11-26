package co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios;

import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.ConsecutivoDTORespuesta;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.NumeroContrato;

/** Numeración de contratos: un consecutivo por serie documental y año. */
public interface INumeracionServicio {

    /** Años aceptados mientras no exista la tabla vigencia. */
    int ANIO_MINIMO = 2000;
    int ANIO_MAXIMO = 2100;

    /**
     * Asigna el siguiente número del tipo de contrato en el año: "serie/NNN" (p. ej. 6.1-17.13/023).
     * El tipo y su serie deben estar activos. Seguro ante peticiones simultáneas: dos llamadas nunca
     * reciben el mismo número. Si la transacción que llama falla, el número no se consume.
     */
    NumeroContrato generarNumero(Long tipoContratoId, Integer anio);

    /** Último número emitido de la serie en el año (0 si ninguno) y el que recibiría el próximo contrato. */
    ConsecutivoDTORespuesta consultarConsecutivo(Long serieId, Integer anio);

    /** "6.1-17.13" + 23 → "6.1-17.13/023". Desde 1000 crece a 4 dígitos en lugar de fallar. */
    static String formatear(String serieCodigo, int numero) {
        return String.format("%s/%03d", serieCodigo, numero);
    }
}
