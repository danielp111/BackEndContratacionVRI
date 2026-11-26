package co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta;

/** Opción para los selectores del contrato: tipo activo con serie activa. */
public record TipoContratoOpcionDTORespuesta(
        Long id,
        String nombre,
        String serieCodigo,
        Boolean esConvenio,
        /** "Pasantía (6.1-17.3)". */
        String etiqueta) {
}
