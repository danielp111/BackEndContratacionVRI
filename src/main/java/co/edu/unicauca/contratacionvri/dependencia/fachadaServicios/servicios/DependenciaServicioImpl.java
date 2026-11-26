package co.edu.unicauca.contratacionvri.dependencia.fachadaServicios.servicios;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.dependencia.accesoADatos.modelos.DependenciaEntity;
import co.edu.unicauca.contratacionvri.dependencia.accesoADatos.repositorios.DependenciaRepositorio;
import co.edu.unicauca.contratacionvri.dependencia.fachadaServicios.DTOPeticion.DependenciaDTOPeticion;
import co.edu.unicauca.contratacionvri.dependencia.fachadaServicios.DTORespuesta.DependenciaDTORespuesta;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadYaExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DependenciaServicioImpl implements IDependenciaServicio {

    private final DependenciaRepositorio repositorio;
    private final ModelMapper modelMapper;

    @Override
    public List<DependenciaDTORespuesta> listar(Boolean activo) {
        return repositorio.buscar(activo).stream().map(this::aRespuesta).toList();
    }

    @Override
    public DependenciaDTORespuesta consultarPorId(Long id) {
        return aRespuesta(buscar(id));
    }

    @Override
    @Transactional
    public DependenciaDTORespuesta crear(DependenciaDTOPeticion peticion) {
        peticion.setNombre(Textos.limpiar(peticion.getNombre()));
        if (repositorio.existsByNombreIgnoreCase(peticion.getNombre())) {
            throw new EntidadYaExisteException("Ya existe la dependencia " + peticion.getNombre());
        }
        DependenciaEntity dependencia = modelMapper.map(peticion, DependenciaEntity.class);
        dependencia.setActivo(Boolean.TRUE);
        return aRespuesta(repositorio.save(dependencia));
    }

    @Override
    @Transactional
    public DependenciaDTORespuesta actualizar(Long id, DependenciaDTOPeticion peticion) {
        DependenciaEntity dependencia = buscar(id);
        peticion.setNombre(Textos.limpiar(peticion.getNombre()));
        if (repositorio.existsByNombreIgnoreCaseAndIdNot(peticion.getNombre(), id)) {
            throw new EntidadYaExisteException("Ya existe otra dependencia llamada " + peticion.getNombre());
        }
        modelMapper.map(peticion, dependencia);
        return aRespuesta(repositorio.save(dependencia));
    }

    @Override
    @Transactional
    public DependenciaDTORespuesta cambiarEstado(Long id, boolean activo) {
        DependenciaEntity dependencia = buscar(id);
        dependencia.setActivo(activo);
        return aRespuesta(repositorio.save(dependencia));
    }

    @Override
    public DependenciaEntity obtenerDependenciaValida(Long id, boolean exigirActiva) {
        DependenciaEntity dependencia = repositorio.findById(id).orElseThrow(() ->
                new ReglaNegocioException("La dependencia con id " + id + " no existe"));
        if (exigirActiva && !Boolean.TRUE.equals(dependencia.getActivo())) {
            throw new ReglaNegocioException("La dependencia " + dependencia.getNombre() + " está inactiva");
        }
        return dependencia;
    }

    private DependenciaEntity buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("No existe una dependencia con id " + id));
    }

    private DependenciaDTORespuesta aRespuesta(DependenciaEntity dependencia) {
        return modelMapper.map(dependencia, DependenciaDTORespuesta.class);
    }
}
