package co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTORespuesta;

/** Opción para los selectores del contrato: solo proyectos activos. */
public record ProyectoOpcionDTORespuesta(
        Integer idProyecto,
        String nombre,
        /** "5019 - Nombre del proyecto". */
        String etiqueta,
        String directorNombre) {
}
