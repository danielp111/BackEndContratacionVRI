package co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta;

import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.Naturaleza;

/** Fila de la búsqueda paginada: lo mínimo para listar y elegir un tercero. */
public record TerceroResumenDTORespuesta(
        Long id,
        Naturaleza naturaleza,
        String tipoIdentificacionEtiqueta,
        String numeroDocumento,
        String dv,
        String nombreCompleto,
        Boolean activo) {
}
