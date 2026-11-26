package co.edu.unicauca.contratacionvri.dependencia.fachadaServicios.servicios;

import java.util.List;

import co.edu.unicauca.contratacionvri.dependencia.accesoADatos.modelos.DependenciaEntity;
import co.edu.unicauca.contratacionvri.dependencia.fachadaServicios.DTOPeticion.DependenciaDTOPeticion;
import co.edu.unicauca.contratacionvri.dependencia.fachadaServicios.DTORespuesta.DependenciaDTORespuesta;

public interface IDependenciaServicio {

    List<DependenciaDTORespuesta> listar(Boolean activo);

    DependenciaDTORespuesta consultarPorId(Long id);

    DependenciaDTORespuesta crear(DependenciaDTOPeticion peticion);

    DependenciaDTORespuesta actualizar(Long id, DependenciaDTOPeticion peticion);

    DependenciaDTORespuesta cambiarEstado(Long id, boolean activo);

    /** Para otros dominios: devuelve la dependencia si existe (y si está activa cuando se exige). */
    DependenciaEntity obtenerDependenciaValida(Long id, boolean exigirActiva);
}
