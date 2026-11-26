package co.edu.unicauca.contratacionvri.abogado.fachadaServicios.servicios;

import java.util.List;

import co.edu.unicauca.contratacionvri.abogado.fachadaServicios.DTOPeticion.AbogadoDTOPeticion;
import co.edu.unicauca.contratacionvri.abogado.fachadaServicios.DTORespuesta.AbogadoDTORespuesta;

/** Fachada de servicios para gestionar abogados. */
public interface IAbogadoServicio {

    /** Lista los abogados ordenados por apellidos. Si {@code activo} es nulo, los devuelve todos. */
    List<AbogadoDTORespuesta> listar(Boolean activo);

    AbogadoDTORespuesta consultarPorId(Long id);

    AbogadoDTORespuesta crear(AbogadoDTOPeticion peticion);

    AbogadoDTORespuesta actualizar(Long id, AbogadoDTOPeticion peticion);

    /**
     * Activa o inactiva un abogado. No existe borrado físico: el abogado puede ser
     * responsable de contratos que deben conservarse.
     */
    AbogadoDTORespuesta cambiarEstado(Long id, boolean activo);
}
