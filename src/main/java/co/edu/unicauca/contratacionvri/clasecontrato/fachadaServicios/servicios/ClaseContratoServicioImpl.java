package co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.servicios;

import java.util.List;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.AmbitoClase;
import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.ClaseContratoEntity;
import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.repositorios.ClaseContratoRepositorio;
import co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.DTOPeticion.ClaseContratoActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.DTOPeticion.ClaseContratoCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.DTORespuesta.ClaseContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadYaExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClaseContratoServicioImpl implements IClaseContratoServicio {

    private final ClaseContratoRepositorio repositorio;
    private final ModelMapper modelMapper;

    @Override
    public List<ClaseContratoDTORespuesta> listar(AmbitoClase ambito, Boolean activo) {
        return repositorio.buscar(ambito, activo).stream().map(this::aRespuesta).toList();
    }

    @Override
    public ClaseContratoDTORespuesta consultarPorId(Long id) {
        return aRespuesta(buscarPorId(id));
    }

    @Override
    @Transactional
    public ClaseContratoDTORespuesta crear(ClaseContratoCrearDTOPeticion peticion) {
        if (peticion.getCodigoSireci() == CODIGO_NO_DILIGENCIA) {
            throw new ReglaNegocioException("El código " + CODIGO_NO_DILIGENCIA
                    + " (no se diligencia información en este período) no es una clase de contrato: pertenece al reporte SIRECI");
        }
        if (repositorio.existsByAmbitoAndCodigoSireci(peticion.getAmbito(), peticion.getCodigoSireci())) {
            throw new EntidadYaExisteException("Ya existe la clase " + peticion.getCodigoSireci()
                    + " en el ámbito " + peticion.getAmbito());
        }
        ClaseContratoEntity clase = new ClaseContratoEntity(peticion.getAmbito(), peticion.getCodigoSireci(),
                Textos.limpiar(peticion.getNombre()));
        clase.setActivo(Boolean.TRUE);
        return aRespuesta(repositorio.save(clase));
    }

    @Override
    @Transactional
    public ClaseContratoDTORespuesta actualizar(Long id, ClaseContratoActualizarDTOPeticion peticion) {
        ClaseContratoEntity clase = buscarPorId(id);
        if (peticion.getCodigoSireci() != null && !peticion.getCodigoSireci().equals(clase.getCodigoSireci())) {
            throw new ReglaNegocioException("El código SIRECI de una clase no se puede cambiar ("
                    + clase.getCodigoSireci() + " → " + peticion.getCodigoSireci() + "). Cree una clase nueva e inactive esta");
        }
        if (peticion.getAmbito() != null && peticion.getAmbito() != clase.getAmbito()) {
            throw new ReglaNegocioException("El ámbito de una clase no se puede cambiar ("
                    + clase.getAmbito() + " → " + peticion.getAmbito() + "). Cree una clase nueva e inactive esta");
        }
        clase.setNombre(Textos.limpiar(peticion.getNombre()));
        return aRespuesta(repositorio.save(clase));
    }

    @Override
    @Transactional
    public ClaseContratoDTORespuesta cambiarEstado(Long id, boolean activo) {
        ClaseContratoEntity clase = buscarPorId(id);
        clase.setActivo(activo);
        return aRespuesta(repositorio.save(clase));
    }

    @Override
    public ClaseContratoEntity obtenerClaseValida(Long id, ClaseContratoEntity actual) {
        if (actual != null && Objects.equals(actual.getId(), id)) {
            return actual;
        }
        ClaseContratoEntity clase = repositorio.findById(id).orElseThrow(() ->
                new ReglaNegocioException("La clase de contrato con id " + id + " no existe"));
        if (!Boolean.TRUE.equals(clase.getActivo())) {
            throw new ReglaNegocioException("La clase de contrato '" + clase.getEtiqueta()
                    + "' está inactiva y no se puede usar en contratos nuevos");
        }
        return clase;
    }

    private ClaseContratoEntity buscarPorId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("No existe una clase de contrato con id " + id));
    }

    private ClaseContratoDTORespuesta aRespuesta(ClaseContratoEntity clase) {
        ClaseContratoDTORespuesta respuesta = modelMapper.map(clase, ClaseContratoDTORespuesta.class);
        respuesta.setEtiqueta(clase.getEtiqueta());
        respuesta.setEsOtros(clase.esOtros());
        return respuesta;
    }
}
