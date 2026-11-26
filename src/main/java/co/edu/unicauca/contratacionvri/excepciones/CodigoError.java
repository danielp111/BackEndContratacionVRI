package co.edu.unicauca.contratacionvri.excepciones;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Códigos de error que devuelve la API. El frontend puede usar el código para
 * decidir qué mostrar sin depender del texto del mensaje.
 */
@Getter
@RequiredArgsConstructor
public enum CodigoError {

    ERROR_GENERICO("GC-0001", "Error interno del servidor"),
    ENTIDAD_YA_EXISTE("GC-0002", "El registro ya existe"),
    ENTIDAD_NO_ENCONTRADA("GC-0003", "Registro no encontrado"),
    VIOLACION_REGLA_DE_NEGOCIO("GC-0004", "Violación de una regla de negocio"),
    DATOS_INVALIDOS("GC-0005", "Los datos enviados no son válidos"),
    PETICION_MAL_FORMADA("GC-0006", "La petición no se pudo leer"),
    CONFLICTO_DE_DATOS("GC-0007", "La operación entra en conflicto con datos existentes"),
    RECURSO_NO_ENCONTRADO("GC-0008", "La ruta solicitada no existe"),
    METODO_NO_PERMITIDO("GC-0009", "Método HTTP no permitido para esta ruta"),
    TIPO_CONTENIDO_NO_SOPORTADO("GC-0010", "Tipo de contenido no soportado");

    private final String codigo;
    private final String descripcion;
}
