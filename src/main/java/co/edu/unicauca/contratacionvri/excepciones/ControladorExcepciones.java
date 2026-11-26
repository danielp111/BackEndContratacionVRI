package co.edu.unicauca.contratacionvri.excepciones;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadYaExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

/**
 * Controlador global de excepciones: convierte cualquier error en un {@link ErrorRespuesta}
 * con el código HTTP adecuado, para que el frontend reciba siempre el mismo formato.
 */
@RestControllerAdvice
public class ControladorExcepciones {

    private static final Logger log = LoggerFactory.getLogger(ControladorExcepciones.class);

    // ---------- Excepciones propias del negocio ----------

    @ExceptionHandler(EntidadNoExisteException.class)
    public ResponseEntity<ErrorRespuesta> manejarEntidadNoExiste(HttpServletRequest req, EntidadNoExisteException ex) {
        return construir(req, HttpStatus.NOT_FOUND, ex.getCodigoError(), ex.getMessage(), null);
    }

    @ExceptionHandler(EntidadYaExisteException.class)
    public ResponseEntity<ErrorRespuesta> manejarEntidadYaExiste(HttpServletRequest req, EntidadYaExisteException ex) {
        return construir(req, HttpStatus.CONFLICT, ex.getCodigoError(), ex.getMessage(), null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorRespuesta> manejarReglaNegocio(HttpServletRequest req, ReglaNegocioException ex) {
        return construir(req, HttpStatus.BAD_REQUEST, ex.getCodigoError(), ex.getMessage(), null);
    }

    // ---------- Validación de la petición ----------

    /** Falla alguna anotación de jakarta.validation en un DTO de petición (@Valid). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(HttpServletRequest req, MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> errores.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));
        return construir(req, HttpStatus.BAD_REQUEST, CodigoError.DATOS_INVALIDOS,
                CodigoError.DATOS_INVALIDOS.getDescripcion(), errores);
    }

    /** Falla la validación de un parámetro de ruta o de consulta (@PathVariable, @RequestParam). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacionParametros(HttpServletRequest req, HandlerMethodValidationException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(resultado -> {
            String parametro = resultado.getMethodParameter().getParameterName();
            resultado.getResolvableErrors()
                    .forEach(error -> errores.putIfAbsent(parametro, error.getDefaultMessage()));
        });
        return construir(req, HttpStatus.BAD_REQUEST, CodigoError.DATOS_INVALIDOS,
                CodigoError.DATOS_INVALIDOS.getDescripcion(), errores);
    }

    /** Falla una restricción de un parámetro cuando el controlador está anotado con @Validated. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarRestriccion(HttpServletRequest req, ConstraintViolationException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (ConstraintViolation<?> violacion : ex.getConstraintViolations()) {
            // La ruta llega como "metodo.parametro"; al cliente solo le interesa el parámetro.
            String ruta = violacion.getPropertyPath().toString();
            String campo = ruta.substring(ruta.lastIndexOf('.') + 1);
            errores.putIfAbsent(campo, violacion.getMessage());
        }
        return construir(req, HttpStatus.BAD_REQUEST, CodigoError.DATOS_INVALIDOS,
                CodigoError.DATOS_INVALIDOS.getDescripcion(), errores);
    }

    /** JSON mal formado, tipo incorrecto, fecha inválida o valor de enumerado desconocido. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> manejarJsonInvalido(HttpServletRequest req, HttpMessageNotReadableException ex) {
        return construir(req, HttpStatus.BAD_REQUEST, CodigoError.PETICION_MAL_FORMADA,
                "El cuerpo de la petición no es válido: revise el formato JSON, las fechas (AAAA-MM-DD) y los valores de las listas", null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> manejarTipoParametro(HttpServletRequest req, MethodArgumentTypeMismatchException ex) {
        return construir(req, HttpStatus.BAD_REQUEST, CodigoError.PETICION_MAL_FORMADA,
                "El valor '" + ex.getValue() + "' no es válido para el parámetro '" + ex.getName() + "'", null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorRespuesta> manejarParametroFaltante(HttpServletRequest req, MissingServletRequestParameterException ex) {
        return construir(req, HttpStatus.BAD_REQUEST, CodigoError.PETICION_MAL_FORMADA,
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'", null);
    }

    // ---------- Infraestructura ----------

    /** Respaldo por si dos peticiones simultáneas pasan la validación de unicidad del servicio. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarIntegridad(HttpServletRequest req, DataIntegrityViolationException ex) {
        log.warn("Violación de integridad en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return construir(req, HttpStatus.CONFLICT, CodigoError.CONFLICTO_DE_DATOS,
                "La operación viola una restricción de la base de datos (registro duplicado o referencia inválida)", null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorRespuesta> manejarRutaInexistente(HttpServletRequest req, NoResourceFoundException ex) {
        return construir(req, HttpStatus.NOT_FOUND, CodigoError.RECURSO_NO_ENCONTRADO,
                CodigoError.RECURSO_NO_ENCONTRADO.getDescripcion(), null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorRespuesta> manejarMetodoNoPermitido(HttpServletRequest req, HttpRequestMethodNotSupportedException ex) {
        return construir(req, HttpStatus.METHOD_NOT_ALLOWED, CodigoError.METODO_NO_PERMITIDO,
                "El método " + ex.getMethod() + " no está permitido para esta ruta", null);
    }

    /** El cuerpo no viene como JSON (Content-Type ausente o distinto de application/json). */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorRespuesta> manejarTipoContenido(HttpServletRequest req, HttpMediaTypeNotSupportedException ex) {
        String recibido = ex.getContentType() == null ? "ninguno" : ex.getContentType().toString();
        return construir(req, HttpStatus.UNSUPPORTED_MEDIA_TYPE, CodigoError.TIPO_CONTENIDO_NO_SOPORTADO,
                "El cuerpo debe enviarse como application/json (se recibió: " + recibido + ")", null);
    }

    /** Cualquier error no previsto: se registra completo en el log y al cliente no se le filtran detalles. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarGenerico(HttpServletRequest req, Exception ex) {
        log.error("Error no controlado en {} {}", req.getMethod(), req.getRequestURI(), ex);
        return construir(req, HttpStatus.INTERNAL_SERVER_ERROR, CodigoError.ERROR_GENERICO,
                CodigoError.ERROR_GENERICO.getDescripcion(), null);
    }

    private ResponseEntity<ErrorRespuesta> construir(HttpServletRequest req, HttpStatus estado, CodigoError codigo,
                                                     String mensaje, Map<String, String> errores) {
        ErrorRespuesta cuerpo = ErrorRespuesta.builder()
                .codigoError(codigo.getCodigo())
                .mensaje(mensaje)
                .codigoHttp(estado.value())
                .url(req.getRequestURL().toString())
                .metodo(req.getMethod())
                .fecha(LocalDateTime.now())
                .errores(errores)
                .build();
        return new ResponseEntity<>(cuerpo, estado);
    }
}
