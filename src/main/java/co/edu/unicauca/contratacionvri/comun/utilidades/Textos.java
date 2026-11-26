package co.edu.unicauca.contratacionvri.comun.utilidades;

import java.util.Locale;

/** Normalización de textos que llegan en las peticiones. */
public final class Textos {

    /** Correos aceptados por el sistema (para usar en anotaciones @Pattern). */
    public static final String PATRON_CORREO_UNICAUCA = "(?i)^[^@\\s]+@unicauca\\.edu\\.co$";
    public static final String MENSAJE_CORREO_UNICAUCA = "debe ser del dominio @unicauca.edu.co";

    private Textos() {
    }

    /** Quita espacios al inicio y al final, colapsa espacios internos y convierte vacío en nulo. */
    public static String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim().replaceAll("\\s+", " ");
        return limpio.isEmpty() ? null : limpio;
    }

    /** Igual que {@link #limpiar(String)} y además en minúsculas (correos). */
    public static String limpiarCorreo(String valor) {
        String limpio = limpiar(valor);
        return limpio == null ? null : limpio.toLowerCase(Locale.ROOT);
    }

    /** Igual que {@link #limpiar(String)} y además en mayúsculas (códigos y nombres de catálogo). */
    public static String limpiarMayusculas(String valor) {
        String limpio = limpiar(valor);
        return limpio == null ? null : limpio.toUpperCase(new Locale("es", "CO"));
    }
}
