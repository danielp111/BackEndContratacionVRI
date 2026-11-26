package co.edu.unicauca.contratacionvri.comun.utilidades;

/**
 * Dígito de verificación (DV) del NIT según el algoritmo de la DIAN (módulo 11).
 *
 * Cada dígito del NIT, de derecha a izquierda, se multiplica por un peso fijo
 * (3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71). Se suman los productos y se
 * toma el residuo de dividir por 11: si es 0 o 1, ese es el DV; si no, el DV es 11 - residuo.
 */
public final class DigitoVerificacionDian {

    private static final int[] PESOS = {3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71};

    /** Longitud máxima de un NIT sin DV que admite el algoritmo. */
    public static final int LONGITUD_MAXIMA = PESOS.length;

    private DigitoVerificacionDian() {
    }

    /** Indica si el texto es un NIT que se puede calcular: solo dígitos, de 1 a 15. */
    public static boolean esNitValido(String nit) {
        return nit != null && nit.matches("\\d{1," + LONGITUD_MAXIMA + "}");
    }

    /**
     * Calcula el DV de un NIT sin dígito de verificación.
     *
     * @throws IllegalArgumentException si el NIT no tiene solo dígitos o supera 15 dígitos
     */
    public static int calcular(String nit) {
        if (!esNitValido(nit)) {
            throw new IllegalArgumentException("El NIT debe tener entre 1 y " + LONGITUD_MAXIMA + " dígitos: " + nit);
        }
        int suma = 0;
        for (int i = 0; i < nit.length(); i++) {
            int digito = nit.charAt(nit.length() - 1 - i) - '0';
            suma += digito * PESOS[i];
        }
        int residuo = suma % 11;
        return residuo > 1 ? 11 - residuo : residuo;
    }
}
