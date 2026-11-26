package co.edu.unicauca.contratacionvri.abogado.fachadaServicios.servicios;

import java.util.List;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.abogado.accesoADatos.modelos.AbogadoEntity;
import co.edu.unicauca.contratacionvri.abogado.accesoADatos.repositorios.AbogadoRepositorio;
import co.edu.unicauca.contratacionvri.abogado.fachadaServicios.DTOPeticion.AbogadoDTOPeticion;
import co.edu.unicauca.contratacionvri.abogado.fachadaServicios.DTORespuesta.AbogadoDTORespuesta;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.Catalogo;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.servicios.ICatalogoItemServicio;
import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.dependencia.fachadaServicios.servicios.IDependenciaServicio;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadYaExisteException;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.Rol;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.UsuarioEntity;
import co.edu.unicauca.contratacionvri.usuario.fachadaServicios.servicios.IUsuarioServicio;

@Service
@Transactional(readOnly = true)
public class AbogadoServicioImpl implements IAbogadoServicio {

    private final AbogadoRepositorio repositorio;
    private final IUsuarioServicio usuarioServicio;
    private final ICatalogoItemServicio catalogoServicio;
    private final IDependenciaServicio dependenciaServicio;
    private final ModelMapper modelMapper;

    public AbogadoServicioImpl(AbogadoRepositorio repositorio, IUsuarioServicio usuarioServicio,
                               ICatalogoItemServicio catalogoServicio, IDependenciaServicio dependenciaServicio,
                               ModelMapper modelMapper) {
        this.repositorio = repositorio;
        this.usuarioServicio = usuarioServicio;
        this.catalogoServicio = catalogoServicio;
        this.dependenciaServicio = dependenciaServicio;
        this.modelMapper = modelMapper;
        configurarMapeo();
    }

    /**
     * Las relaciones (usuario, tipo de identificación, dependencia) no las copia ModelMapper:
     * si lo hiciera, crearía entidades nuevas a partir del id o, peor, cambiaría el id de la
     * entidad ya asociada. Las asigna el servicio después de validarlas.
     */
    private void configurarMapeo() {
        if (modelMapper.getTypeMap(AbogadoDTOPeticion.class, AbogadoEntity.class) == null) {
            modelMapper.createTypeMap(AbogadoDTOPeticion.class, AbogadoEntity.class)
                    .addMappings(m -> {
                        m.skip(AbogadoEntity::setId);
                        m.skip(AbogadoEntity::setUsuario);
                        m.skip(AbogadoEntity::setTipoIdentificacion);
                        m.skip(AbogadoEntity::setDependencia);
                    });
        }
    }

    @Override
    public List<AbogadoDTORespuesta> listar(Boolean activo) {
        List<AbogadoEntity> abogados = (activo == null)
                ? repositorio.findAllByOrderByApellidosAscNombresAsc()
                : repositorio.findByActivoOrderByApellidosAscNombresAsc(activo);
        return abogados.stream().map(this::aRespuesta).toList();
    }

    @Override
    public AbogadoDTORespuesta consultarPorId(Long id) {
        return aRespuesta(buscar(id));
    }

    @Override
    @Transactional
    public AbogadoDTORespuesta crear(AbogadoDTOPeticion peticion) {
        normalizar(peticion);
        if (repositorio.existsByUsuarioId(peticion.getUsuarioId())) {
            throw new EntidadYaExisteException(
                    "El usuario " + peticion.getUsuarioId() + " ya está asociado a otro abogado");
        }
        if (repositorio.existsByNumeroDocumento(peticion.getNumeroDocumento())) {
            throw new EntidadYaExisteException(
                    "Ya existe un abogado con el número de documento " + peticion.getNumeroDocumento());
        }

        AbogadoEntity abogado = modelMapper.map(peticion, AbogadoEntity.class);
        asignarRelaciones(abogado, peticion, true);
        abogado.setActivo(Boolean.TRUE);
        return aRespuesta(repositorio.save(abogado));
    }

    @Override
    @Transactional
    public AbogadoDTORespuesta actualizar(Long id, AbogadoDTOPeticion peticion) {
        AbogadoEntity abogado = buscar(id);
        normalizar(peticion);
        if (repositorio.existsByUsuarioIdAndIdNot(peticion.getUsuarioId(), id)) {
            throw new EntidadYaExisteException(
                    "El usuario " + peticion.getUsuarioId() + " ya está asociado a otro abogado");
        }
        if (repositorio.existsByNumeroDocumentoAndIdNot(peticion.getNumeroDocumento(), id)) {
            throw new EntidadYaExisteException(
                    "Ya existe otro abogado con el número de documento " + peticion.getNumeroDocumento());
        }

        modelMapper.map(peticion, abogado);
        asignarRelaciones(abogado, peticion, false);
        return aRespuesta(repositorio.save(abogado));
    }

    @Override
    @Transactional
    public AbogadoDTORespuesta cambiarEstado(Long id, boolean activo) {
        AbogadoEntity abogado = buscar(id);
        abogado.setActivo(activo);
        return aRespuesta(repositorio.save(abogado));
    }

    /**
     * Valida y asigna las relaciones. Al crear, todas deben estar activas. Al actualizar, solo
     * se exige que esté activo un valor que cambia: si el abogado ya tenía una dependencia que
     * luego se inactivó, puede seguir editando sus otros datos sin que se lo impida.
     */
    private void asignarRelaciones(AbogadoEntity abogado, AbogadoDTOPeticion peticion, boolean esNuevo) {
        if (esNuevo || !Objects.equals(idDe(abogado.getUsuario()), peticion.getUsuarioId())) {
            abogado.setUsuario(usuarioServicio.obtenerUsuarioValido(peticion.getUsuarioId(), Rol.ABOGADO));
        }

        Long tipoActual = abogado.getTipoIdentificacion() == null ? null : abogado.getTipoIdentificacion().getId();
        boolean tipoCambia = esNuevo || !Objects.equals(tipoActual, peticion.getTipoIdentificacionId());
        if (tipoCambia) {
            abogado.setTipoIdentificacion(catalogoServicio.obtenerItemValido(
                    peticion.getTipoIdentificacionId(), Catalogo.TIPO_IDENTIFICACION, true, "tipo de identificación"));
        }

        Long dependenciaActual = abogado.getDependencia() == null ? null : abogado.getDependencia().getId();
        if (peticion.getDependenciaId() == null) {
            abogado.setDependencia(null);
        } else if (esNuevo || !Objects.equals(dependenciaActual, peticion.getDependenciaId())) {
            abogado.setDependencia(dependenciaServicio.obtenerDependenciaValida(peticion.getDependenciaId(), true));
        }
    }

    private static Long idDe(UsuarioEntity usuario) {
        return usuario == null ? null : usuario.getId();
    }

    private AbogadoEntity buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("No existe un abogado con id " + id));
    }

    private AbogadoDTORespuesta aRespuesta(AbogadoEntity abogado) {
        AbogadoDTORespuesta respuesta = modelMapper.map(abogado, AbogadoDTORespuesta.class);
        // Los datos de las relaciones se copian explícitamente para no depender de la
        // coincidencia de nombres anidados de ModelMapper.
        respuesta.setUsuarioId(abogado.getUsuario().getId());
        respuesta.setUsuarioCorreo(abogado.getUsuario().getCorreo());
        respuesta.setTipoIdentificacionId(abogado.getTipoIdentificacion().getId());
        respuesta.setTipoIdentificacionEtiqueta(abogado.getTipoIdentificacion().getEtiqueta());
        if (abogado.getDependencia() != null) {
            respuesta.setDependenciaId(abogado.getDependencia().getId());
            respuesta.setDependenciaNombre(abogado.getDependencia().getNombre());
        }
        return respuesta;
    }

    private void normalizar(AbogadoDTOPeticion p) {
        p.setNumeroDocumento(Textos.limpiar(p.getNumeroDocumento()));
        p.setNombres(Textos.limpiar(p.getNombres()));
        p.setApellidos(Textos.limpiar(p.getApellidos()));
        p.setCorreoInstitucional(Textos.limpiarCorreo(p.getCorreoInstitucional()));
        p.setTelefono(Textos.limpiar(p.getTelefono()));
        p.setTarjetaProfesional(Textos.limpiar(p.getTarjetaProfesional()));
        p.setCargo(Textos.limpiar(p.getCargo()));
    }
}
