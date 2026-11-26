package co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.servicios;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.comun.fachadaServicios.PaginaRespuesta;
import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadYaExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import co.edu.unicauca.contratacionvri.proyecto.accesoADatos.modelos.ProyectoEntity;
import co.edu.unicauca.contratacionvri.proyecto.accesoADatos.repositorios.ProyectoRepositorio;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTOPeticion.ProyectoActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTOPeticion.ProyectoCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTOPeticion.ProyectoDTOPeticion;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTORespuesta.ProyectoDTORespuesta;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTORespuesta.ProyectoOpcionDTORespuesta;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.servicios.ITerceroServicio;

@Service
@Transactional(readOnly = true)
public class ProyectoServicioImpl implements IProyectoServicio {

    private static final String ROL_DIRECTOR = "director del proyecto";

    private final ProyectoRepositorio repositorio;
    private final ITerceroServicio terceroServicio;
    private final ModelMapper modelMapper;

    public ProyectoServicioImpl(ProyectoRepositorio repositorio, ITerceroServicio terceroServicio,
                                ModelMapper modelMapper) {
        this.repositorio = repositorio;
        this.terceroServicio = terceroServicio;
        this.modelMapper = modelMapper;
        configurarMapeo();
    }

    /**
     * ModelMapper copia nombre y bpin. El director lo asigna el servicio después de validarlo, y el
     * código no tiene setter (solo se asigna en el constructor de la entidad).
     */
    private void configurarMapeo() {
        if (modelMapper.getTypeMap(ProyectoCrearDTOPeticion.class, ProyectoEntity.class) == null) {
            modelMapper.createTypeMap(ProyectoCrearDTOPeticion.class, ProyectoEntity.class)
                    .addMappings(m -> m.skip(ProyectoEntity::setDirector));
        }
        if (modelMapper.getTypeMap(ProyectoActualizarDTOPeticion.class, ProyectoEntity.class) == null) {
            modelMapper.createTypeMap(ProyectoActualizarDTOPeticion.class, ProyectoEntity.class)
                    .addMappings(m -> m.skip(ProyectoEntity::setDirector));
        }
    }

    @Override
    @Transactional
    public ProyectoDTORespuesta crear(ProyectoCrearDTOPeticion peticion) {
        normalizar(peticion);
        // Revisión explícita para responder 409 con un mensaje claro. Si dos peticiones con el mismo
        // código pasan esta revisión a la vez, la llave primaria rechaza la segunda (persist, no merge).
        if (repositorio.existsById(peticion.getIdProyecto())) {
            throw new EntidadYaExisteException("Ya existe el proyecto con código " + peticion.getIdProyecto());
        }
        ProyectoEntity proyecto = new ProyectoEntity(peticion.getIdProyecto());
        modelMapper.map(peticion, proyecto);
        proyecto.setDirector(terceroServicio.obtenerPersonaNaturalValida(peticion.getDirectorTerceroId(), null, ROL_DIRECTOR));
        proyecto.setActivo(Boolean.TRUE);
        return aRespuesta(repositorio.save(proyecto));
    }

    @Override
    public ProyectoDTORespuesta consultarPorId(Integer idProyecto) {
        return aRespuesta(buscarPorId(idProyecto));
    }

    @Override
    public PaginaRespuesta<ProyectoDTORespuesta> buscar(String id, String nombre, String director, Boolean activo,
                                                        int pagina, int tamanio) {
        PageRequest paginacion = PageRequest.of(pagina, tamanio, Sort.by("idProyecto"));
        return PaginaRespuesta.de(
                repositorio.buscar(Textos.limpiar(id), escaparLike(Textos.limpiar(nombre)),
                        escaparLike(Textos.limpiar(director)), activo, paginacion),
                this::aRespuesta);
    }

    @Override
    public List<ProyectoOpcionDTORespuesta> listarActivos(String texto) {
        return repositorio.buscarActivos(escaparLike(Textos.limpiar(texto))).stream()
                .map(p -> new ProyectoOpcionDTORespuesta(p.getIdProyecto(), p.getNombre(),
                        p.getIdProyecto() + " - " + p.getNombre(), p.getDirector().getNombreCompleto()))
                .toList();
    }

    @Override
    @Transactional
    public ProyectoDTORespuesta actualizar(Integer idProyecto, ProyectoActualizarDTOPeticion peticion) {
        ProyectoEntity proyecto = buscarPorId(idProyecto);
        if (peticion.getIdProyecto() != null && !peticion.getIdProyecto().equals(idProyecto)) {
            throw new ReglaNegocioException("El código del proyecto no se puede cambiar (" + idProyecto
                    + " → " + peticion.getIdProyecto() + "). Cree un proyecto nuevo e inactive este");
        }
        normalizar(peticion);
        modelMapper.map(peticion, proyecto);
        // Si el director no cambia, se conserva aunque se haya inactivado después.
        proyecto.setDirector(terceroServicio.obtenerPersonaNaturalValida(
                peticion.getDirectorTerceroId(), proyecto.getDirector(), ROL_DIRECTOR));
        return aRespuesta(repositorio.save(proyecto));
    }

    @Override
    @Transactional
    public ProyectoDTORespuesta cambiarEstado(Integer idProyecto, boolean activo) {
        ProyectoEntity proyecto = buscarPorId(idProyecto);
        proyecto.setActivo(activo);
        return aRespuesta(repositorio.save(proyecto));
    }

    @Override
    public ProyectoEntity obtenerProyectoValido(Integer idProyecto, boolean exigirActivo) {
        ProyectoEntity proyecto = repositorio.findById(idProyecto).orElseThrow(() ->
                new ReglaNegocioException("El proyecto con código " + idProyecto + " no existe"));
        if (exigirActivo && !Boolean.TRUE.equals(proyecto.getActivo())) {
            throw new ReglaNegocioException("El proyecto " + idProyecto + " está inactivo y no se puede usar en contratos nuevos");
        }
        return proyecto;
    }

    private ProyectoEntity buscarPorId(Integer idProyecto) {
        return repositorio.findById(idProyecto)
                .orElseThrow(() -> new EntidadNoExisteException("No existe el proyecto con código " + idProyecto));
    }

    /** Jackson ya recortó y convirtió "" en null; aquí lo propio de cada campo (también si el DTO se armó sin JSON). */
    private void normalizar(ProyectoDTOPeticion p) {
        p.setNombre(Textos.limpiar(p.getNombre()));
        p.setBpin(p.getBpin());
    }

    private static String escaparLike(String texto) {
        if (texto == null) {
            return null;
        }
        return texto.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private ProyectoDTORespuesta aRespuesta(ProyectoEntity proyecto) {
        ProyectoDTORespuesta respuesta = modelMapper.map(proyecto, ProyectoDTORespuesta.class);
        respuesta.setDirectorTerceroId(proyecto.getDirector().getId());
        respuesta.setDirectorNombre(proyecto.getDirector().getNombreCompleto());
        respuesta.setDirectorDocumento(proyecto.getDirector().getNumeroDocumento());
        return respuesta;
    }
}
