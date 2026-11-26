package co.edu.unicauca.contratacionvri.excepciones;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

/**
 * Cuerpo JSON que devuelve la API cuando ocurre un error.
 * {@code errores} solo aparece en errores de validación: campo -> mensaje.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorRespuesta {

    private final String codigoError;
    private final String mensaje;
    private final int codigoHttp;
    private final String url;
    private final String metodo;
    private final LocalDateTime fecha;
    private final Map<String, String> errores;
}
