package co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios;

import java.util.List;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadYaExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.SerieDocumentalEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.TipoContratoEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios.TipoContratoRepositorio;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTOPeticion.TipoContratoDTOPeticion;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.TipoContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.TipoContratoOpcionDTORespuesta;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TipoContratoServicioImpl implements ITipoContratoServicio {

    private final TipoContratoRepositorio repositorio;
    private final ISerieDocumentalServicio serieServicio;
    private final UsoTipoContratoChecker usoChecker;
    private final ModelMapper modelMapper;

    @Override
    public List<TipoContratoDTORespuesta> listar(Boolean activo, Long serieId) {
        return repositorio.buscar(activo, serieId).stream().map(this::aRespuesta).toList();
    }

    @Override
    public List<TipoContratoOpcionDTORespuesta> listarDisponibles() {
        return repositorio.buscarDisponibles().stream()
                .map(t -> new TipoContratoOpcionDTORespuesta(t.getId(), t.getNombre(), t.getSerie().getCodigo(),
                        t.getSerie().getEsConvenio(), t.getNombre() + " (" + t.getSerie().getCodigo() + ")"))
                .toList();
    }

    @Override
    public TipoContratoDTORespuesta consultarPorId(Long id) {
        return aRespuesta(buscarPorId(id));
    }

    @Override
    @Transactional
    public TipoContratoDTORespuesta crear(TipoContratoDTOPeticion peticion) {
        String nombre = Textos.limpiar(peticion.getNombre());
        if (repositorio.existsByNombreIgnoreCase(nombre)) {
            throw new EntidadYaExisteException("Ya existe el tipo de contrato " + nombre);
        }
        TipoContratoEntity tipo = new TipoContratoEntity();
        tipo.setNombre(nombre);
        tipo.setSerie(serieServicio.obtenerSerieValida(peticion.getSerieId(), null));
        tipo.setActivo(Boolean.TRUE);
        return aRespuesta(repositorio.save(tipo));
    }

    @Override
    @Transactional
    public TipoContratoDTORespuesta actualizar(Long id, TipoContratoDTOPeticion peticion) {
        TipoContratoEntity tipo = buscarPorId(id);
        String nombre = Textos.limpiar(peticion.getNombre());
        boolean cambiaNombre = !tipo.getNombre().equals(nombre);
        boolean cambiaSerie = !Objects.equals(tipo.getSerie().getId(), peticion.getSerieId());

        if ((cambiaNombre || cambiaSerie) && usoChecker.existenContratosConTipo(id)) {
            throw new ReglaNegocioException("El tipo de contrato " + tipo.getNombre()
                    + " ya tiene contratos: su nombre y su serie no se pueden cambiar");
        }
        if (cambiaNombre && repositorio.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new EntidadYaExisteException("Ya existe el tipo de contrato " + nombre);
        }
        SerieDocumentalEntity serie = serieServicio.obtenerSerieValida(peticion.getSerieId(), tipo.getSerie());

        tipo.setNombre(nombre);
        tipo.setSerie(serie);
        return aRespuesta(repositorio.save(tipo));
    }

    @Override
    @Transactional
    public TipoContratoDTORespuesta cambiarEstado(Long id, boolean activo) {
        TipoContratoEntity tipo = buscarPorId(id);
        tipo.setActivo(activo);
        return aRespuesta(repositorio.save(tipo));
    }

    private TipoContratoEntity buscarPorId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("No existe un tipo de contrato con id " + id));
    }

    private TipoContratoDTORespuesta aRespuesta(TipoContratoEntity tipo) {
        TipoContratoDTORespuesta respuesta = modelMapper.map(tipo, TipoContratoDTORespuesta.class);
        respuesta.setSerieId(tipo.getSerie().getId());
        respuesta.setSerieCodigo(tipo.getSerie().getCodigo());
        respuesta.setEsConvenio(tipo.getSerie().getEsConvenio());
        respuesta.setNombreYSerieEditables(tipo.getId() == null || !usoChecker.existenContratosConTipo(tipo.getId()));
        return respuesta;
    }
}
