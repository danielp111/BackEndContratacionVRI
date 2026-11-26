package co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.servicios;

import java.util.List;

import co.edu.unicauca.contratacionvri.comun.fachadaServicios.PaginaRespuesta;
import co.edu.unicauca.contratacionvri.proyecto.accesoADatos.modelos.ProyectoEntity;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTOPeticion.ProyectoActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTOPeticion.ProyectoCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTORespuesta.ProyectoDTORespuesta;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTORespuesta.ProyectoOpcionDTORespuesta;

public interface IProyectoServicio {

    /** Falla con 409 si el código ya existe; nunca sobrescribe un proyecto. */
    ProyectoDTORespuesta crear(ProyectoCrearDTOPeticion peticion);

    ProyectoDTORespuesta consultarPorId(Integer idProyecto);

    /**
     * Búsqueda paginada ordenada por código. Filtros opcionales: prefijo del código, contenido del
     * nombre, nombre o documento del director y estado.
     */
    PaginaRespuesta<ProyectoDTORespuesta> buscar(String id, String nombre, String director, Boolean activo,
                                                 int pagina, int tamanio);

    /** Solo activos, para los selectores del contrato. {@code texto} filtra por código o nombre. */
    List<ProyectoOpcionDTORespuesta> listarActivos(String texto);

    /** El código no cambia: si el cuerpo trae un idProyecto distinto al de la URL, falla con 400. */
    ProyectoDTORespuesta actualizar(Integer idProyecto, ProyectoActualizarDTOPeticion peticion);

    /** Activa o inactiva. No hay borrado: los contratos existentes conservan su proyecto. */
    ProyectoDTORespuesta cambiarEstado(Integer idProyecto, boolean activo);

    /**
     * Para el dominio de contratos: devuelve el proyecto si existe y, cuando {@code exigirActivo}
     * es verdadero, si está activo (un contrato nuevo no puede usar un proyecto inactivo).
     */
    ProyectoEntity obtenerProyectoValido(Integer idProyecto, boolean exigirActivo);
}
