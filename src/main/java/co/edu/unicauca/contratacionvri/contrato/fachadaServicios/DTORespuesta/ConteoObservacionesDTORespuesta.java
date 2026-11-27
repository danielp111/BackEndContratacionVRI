package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta;

import java.util.Map;

/**
 * Cantidad de observaciones del contrato, en total y por campo (solo aparecen los campos que tienen
 * alguna, en el orden del formulario).
 */
public record ConteoObservacionesDTORespuesta(long total, long pendientes, Map<String, ConteoCampo> porCampo) {

    public record ConteoCampo(long total, long pendientes) {
    }
}
