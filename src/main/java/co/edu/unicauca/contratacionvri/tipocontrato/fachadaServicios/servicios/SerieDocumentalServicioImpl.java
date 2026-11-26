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
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios.SerieDocumentalRepositorio;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTOPeticion.SerieDocumentalDTOPeticion;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.SerieDocumentalDTORespuesta;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SerieDocumentalServicioImpl implements ISerieDocumentalServicio {

    private final SerieDocumentalRepositorio repositorio;
    private final UsoTipoContratoChecker usoChecker;
    private final ModelMapper modelMapper;

    @Override
    public List<SerieDocumentalDTORespuesta> listar(Boolean activo) {
        return repositorio.buscar(activo).stream().map(this::aRespuesta).toList();
    }

    @Override
    public SerieDocumentalDTORespuesta consultarPorId(Long id) {
        return aRespuesta(buscarPorId(id));
    }

    @Override
    @Transactional
    public SerieDocumentalDTORespuesta crear(SerieDocumentalDTOPeticion peticion) {
        String codigo = Textos.limpiar(peticion.getCodigo());
        if (repositorio.existsByCodigoIgnoreCase(codigo)) {
            throw new EntidadYaExisteException("Ya existe la serie documental " + codigo);
        }
        SerieDocumentalEntity serie = new SerieDocumentalEntity();
        serie.setCodigo(codigo);
        serie.setActivo(Boolean.TRUE);
        return aRespuesta(repositorio.save(serie));
    }

    @Override
    @Transactional
    public SerieDocumentalDTORespuesta actualizar(Long id, SerieDocumentalDTOPeticion peticion) {
        SerieDocumentalEntity serie = buscarPorId(id);
        String codigo = Textos.limpiar(peticion.getCodigo());
        if (!serie.getCodigo().equalsIgnoreCase(codigo)) {
            if (usoChecker.existenContratosConSerie(id)) {
                throw new ReglaNegocioException("El código de la serie " + serie.getCodigo()
                        + " no se puede cambiar: ya hay contratos numerados con esa serie");
            }
            if (repositorio.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
                throw new EntidadYaExisteException("Ya existe la serie documental " + codigo);
            }
        }
        serie.setCodigo(codigo);
        return aRespuesta(repositorio.save(serie));
    }

    @Override
    @Transactional
    public SerieDocumentalDTORespuesta cambiarEstado(Long id, boolean activo) {
        SerieDocumentalEntity serie = buscarPorId(id);
        serie.setActivo(activo);
        return aRespuesta(repositorio.save(serie));
    }

    @Override
    public SerieDocumentalEntity obtenerSerieValida(Long id, SerieDocumentalEntity actual) {
        if (actual != null && Objects.equals(actual.getId(), id)) {
            return actual;
        }
        SerieDocumentalEntity serie = repositorio.findById(id).orElseThrow(() ->
                new ReglaNegocioException("La serie documental con id " + id + " no existe"));
        if (!Boolean.TRUE.equals(serie.getActivo())) {
            throw new ReglaNegocioException("La serie documental " + serie.getCodigo() + " está inactiva");
        }
        return serie;
    }

    private SerieDocumentalEntity buscarPorId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("No existe una serie documental con id " + id));
    }

    private SerieDocumentalDTORespuesta aRespuesta(SerieDocumentalEntity serie) {
        SerieDocumentalDTORespuesta respuesta = modelMapper.map(serie, SerieDocumentalDTORespuesta.class);
        respuesta.setCodigoEditable(serie.getId() == null || !usoChecker.existenContratosConSerie(serie.getId()));
        return respuesta;
    }
}
