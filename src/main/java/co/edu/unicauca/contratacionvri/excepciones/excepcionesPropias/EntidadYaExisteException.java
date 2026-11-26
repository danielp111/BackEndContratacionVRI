package co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias;

import co.edu.unicauca.contratacionvri.excepciones.CodigoError;
import lombok.Getter;

/** Se intenta crear un registro que choca con un campo único. Se responde con HTTP 409. */
@Getter
public class EntidadYaExisteException extends RuntimeException {

    private final CodigoError codigoError = CodigoError.ENTIDAD_YA_EXISTE;

    public EntidadYaExisteException(String mensaje) {
        super(mensaje);
    }
}
