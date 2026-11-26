package co.edu.unicauca.contratacionvri.usuario.fachadaServicios.servicios;

import java.util.List;

import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.Rol;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.UsuarioEntity;
import co.edu.unicauca.contratacionvri.usuario.fachadaServicios.DTOPeticion.UsuarioDTOPeticion;
import co.edu.unicauca.contratacionvri.usuario.fachadaServicios.DTORespuesta.UsuarioDTORespuesta;

public interface IUsuarioServicio {

    List<UsuarioDTORespuesta> listar(Rol rol, Boolean activo);

    UsuarioDTORespuesta consultarPorId(Long id);

    UsuarioDTORespuesta crear(UsuarioDTOPeticion peticion);

    /** No permite quitar el rol ABOGADO a una cuenta que tiene datos de abogado asociados. */
    UsuarioDTORespuesta actualizar(Long id, UsuarioDTOPeticion peticion);

    UsuarioDTORespuesta cambiarEstado(Long id, boolean activo);

    /** Para otros dominios: devuelve el usuario si existe, está activo y tiene el rol esperado. */
    UsuarioEntity obtenerUsuarioValido(Long id, Rol rolEsperado);
}
