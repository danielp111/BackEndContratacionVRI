package co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias;

import co.edu.unicauca.contratacionvri.excepciones.CodigoError;
import lombok.Getter;

/** El registro buscado no existe. Se responde con HTTP 404. */
@Getter
public class EntidadNoExisteException extends RuntimeException {

    private final CodigoError codigoError = CodigoError.ENTIDAD_NO_ENCONTRADA;

    public EntidadNoExisteException(String mensaje) {
        super(mensaje);
    }
}
