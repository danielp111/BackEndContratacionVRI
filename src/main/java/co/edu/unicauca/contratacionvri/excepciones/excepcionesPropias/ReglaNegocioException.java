package co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias;

import co.edu.unicauca.contratacionvri.excepciones.CodigoError;
import lombok.Getter;

/**
 * Se viola una regla de negocio (obligatoriedad condicional, límites, orden de fechas...).
 * Se responde con HTTP 400.
 */
@Getter
public class ReglaNegocioException extends RuntimeException {

    private final CodigoError codigoError = CodigoError.VIOLACION_REGLA_DE_NEGOCIO;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
