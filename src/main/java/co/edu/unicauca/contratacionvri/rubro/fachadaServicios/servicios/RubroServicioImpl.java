package co.edu.unicauca.contratacionvri.rubro.fachadaServicios.servicios;

import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.comun.fachadaServicios.PaginaRespuesta;
import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadYaExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import co.edu.unicauca.contratacionvri.rubro.accesoADatos.modelos.RubroEntity;
import co.edu.unicauca.contratacionvri.rubro.accesoADatos.repositorios.RubroRepositorio;
import co.edu.unicauca.contratacionvri.rubro.fachadaServicios.DTOPeticion.RubroDTOPeticion;
import co.edu.unicauca.contratacionvri.rubro.fachadaServicios.DTORespuesta.RubroDTORespuesta;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RubroServicioImpl implements IRubroServicio {

    private final RubroRepositorio repositorio;
    private final UsoRubroChecker usoChecker;
    private final ModelMapper modelMapper;

    @Override
    public PaginaRespuesta<RubroDTORespuesta> buscar(String texto, Boolean activo, int pagina, int tamanio) {
        // Sin Sort: el orden (longitud y valor del código) lo define la consulta.
        return PaginaRespuesta.de(
                repositorio.buscar(escaparLike(Textos.limpiar(texto)), activo, PageRequest.of(pagina, tamanio)),
                this::aRespuesta);
    }

    @Override
    public RubroDTORespuesta consultarPorId(Long id) {
        return aRespuesta(buscarPorId(id));
    }

    @Override
    @Transactional
    public RubroDTORespuesta crear(RubroDTOPeticion peticion) {
        String codigo = Textos.limpiar(peticion.getCodigo());
        if (repositorio.existsByCodigoIgnoreCase(codigo)) {
            throw new EntidadYaExisteException("Ya existe el rubro con código " + codigo);
        }
        RubroEntity rubro = new RubroEntity();
        rubro.setCodigo(codigo);
        rubro.setNombre(Textos.limpiar(peticion.getNombre()));
        rubro.setActivo(Boolean.TRUE);
        return aRespuesta(repositorio.save(rubro));
    }

    @Override
    @Transactional
    public RubroDTORespuesta actualizar(Long id, RubroDTOPeticion peticion) {
        RubroEntity rubro = buscarPorId(id);
        String codigo = Textos.limpiar(peticion.getCodigo());
        if (!rubro.getCodigo().equals(codigo)) {
            if (usoChecker.existenContratosConRubro(id)) {
                throw new ReglaNegocioException("El código del rubro " + rubro.getCodigo()
                        + " no se puede cambiar: ya hay contratos que lo usan");
            }
            if (repositorio.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
                throw new EntidadYaExisteException("Ya existe el rubro con código " + codigo);
            }
        }
        rubro.setCodigo(codigo);
        rubro.setNombre(Textos.limpiar(peticion.getNombre()));
        return aRespuesta(repositorio.save(rubro));
    }

    @Override
    @Transactional
    public RubroDTORespuesta cambiarEstado(Long id, boolean activo) {
        RubroEntity rubro = buscarPorId(id);
        rubro.setActivo(activo);
        return aRespuesta(repositorio.save(rubro));
    }

    @Override
    public RubroEntity obtenerRubroValido(Long id, RubroEntity actual) {
        if (actual != null && Objects.equals(actual.getId(), id)) {
            return actual;
        }
        RubroEntity rubro = repositorio.findById(id).orElseThrow(() ->
                new ReglaNegocioException("El rubro con id " + id + " no existe"));
        if (!Boolean.TRUE.equals(rubro.getActivo())) {
            throw new ReglaNegocioException("El rubro " + rubro.getEtiqueta()
                    + " está inactivo y no se puede usar en contratos nuevos");
        }
        return rubro;
    }

    private RubroEntity buscarPorId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("No existe un rubro con id " + id));
    }

    private static String escaparLike(String texto) {
        if (texto == null) {
            return null;
        }
        return texto.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private RubroDTORespuesta aRespuesta(RubroEntity rubro) {
        RubroDTORespuesta respuesta = modelMapper.map(rubro, RubroDTORespuesta.class);
        respuesta.setEtiqueta(rubro.getEtiqueta());
        respuesta.setCodigoEditable(rubro.getId() == null || !usoChecker.existenContratosConRubro(rubro.getId()));
        return respuesta;
    }
}
