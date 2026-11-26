package co.edu.unicauca.contratacionvri.usuario.fachadaServicios.servicios;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.abogado.accesoADatos.repositorios.AbogadoRepositorio;
import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadYaExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.Rol;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.UsuarioEntity;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.repositorios.UsuarioRepositorio;
import co.edu.unicauca.contratacionvri.usuario.fachadaServicios.DTOPeticion.UsuarioDTOPeticion;
import co.edu.unicauca.contratacionvri.usuario.fachadaServicios.DTORespuesta.UsuarioDTORespuesta;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioServicioImpl implements IUsuarioServicio {

    private final UsuarioRepositorio repositorio;
    private final AbogadoRepositorio abogadoRepositorio;
    private final ModelMapper modelMapper;

    @Override
    public List<UsuarioDTORespuesta> listar(Rol rol, Boolean activo) {
        return repositorio.buscar(rol, activo).stream().map(this::aRespuesta).toList();
    }

    @Override
    public UsuarioDTORespuesta consultarPorId(Long id) {
        return aRespuesta(buscar(id));
    }

    @Override
    @Transactional
    public UsuarioDTORespuesta crear(UsuarioDTOPeticion peticion) {
        normalizar(peticion);
        if (repositorio.existsByCorreo(peticion.getCorreo())) {
            throw new EntidadYaExisteException("Ya existe un usuario con el correo " + peticion.getCorreo());
        }
        UsuarioEntity usuario = modelMapper.map(peticion, UsuarioEntity.class);
        usuario.setActivo(Boolean.TRUE);
        return aRespuesta(repositorio.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioDTORespuesta actualizar(Long id, UsuarioDTOPeticion peticion) {
        UsuarioEntity usuario = buscar(id);
        normalizar(peticion);
        if (repositorio.existsByCorreoAndIdNot(peticion.getCorreo(), id)) {
            throw new EntidadYaExisteException("Ya existe otro usuario con el correo " + peticion.getCorreo());
        }
        if (usuario.getRol() == Rol.ABOGADO && peticion.getRol() != Rol.ABOGADO
                && abogadoRepositorio.existsByUsuarioId(id)) {
            throw new ReglaNegocioException("El usuario " + usuario.getCorreo()
                    + " tiene datos de abogado asociados; no se le puede quitar el rol ABOGADO");
        }
        if (usuario.getGoogleId() != null && !usuario.getCorreo().equals(peticion.getCorreo())) {
            throw new ReglaNegocioException("El usuario ya ingresó con Google; su correo no se puede cambiar");
        }
        modelMapper.map(peticion, usuario);
        return aRespuesta(repositorio.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioDTORespuesta cambiarEstado(Long id, boolean activo) {
        UsuarioEntity usuario = buscar(id);
        usuario.setActivo(activo);
        return aRespuesta(repositorio.save(usuario));
    }

    @Override
    public UsuarioEntity obtenerUsuarioValido(Long id, Rol rolEsperado) {
        UsuarioEntity usuario = repositorio.findById(id).orElseThrow(() ->
                new ReglaNegocioException("El usuario con id " + id + " no existe"));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ReglaNegocioException("El usuario " + usuario.getCorreo() + " está inactivo");
        }
        if (rolEsperado != null && usuario.getRol() != rolEsperado) {
            throw new ReglaNegocioException("El usuario " + usuario.getCorreo() + " tiene rol " + usuario.getRol()
                    + " y se esperaba " + rolEsperado);
        }
        return usuario;
    }

    private UsuarioEntity buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("No existe un usuario con id " + id));
    }

    private UsuarioDTORespuesta aRespuesta(UsuarioEntity usuario) {
        UsuarioDTORespuesta respuesta = modelMapper.map(usuario, UsuarioDTORespuesta.class);
        respuesta.setVinculadoConGoogle(usuario.getGoogleId() != null);
        return respuesta;
    }

    private void normalizar(UsuarioDTOPeticion p) {
        p.setCorreo(Textos.limpiarCorreo(p.getCorreo()));
        p.setNombre(Textos.limpiar(p.getNombre()));
    }
}
