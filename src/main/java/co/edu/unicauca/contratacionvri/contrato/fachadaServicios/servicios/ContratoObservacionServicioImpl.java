package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.CampoContrato;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoObservacionEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ContratoObservacionRepositorio;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ContratoRepositorio;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ObservacionDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ConteoObservacionesDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ConteoObservacionesDTORespuesta.ConteoCampo;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ObservacionDTORespuesta;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContratoObservacionServicioImpl implements IContratoObservacionServicio {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final ContratoObservacionRepositorio repositorio;
    private final ContratoRepositorio contratoRepositorio;

    @Override
    @Transactional
    public ObservacionDTORespuesta agregar(Long contratoId, ObservacionDTOPeticion peticion) {
        ContratoEntity contrato = buscarContrato(contratoId);
        if (contrato.estaAnulado()) {
            throw new ReglaNegocioException("El contrato " + contrato.getNumero() + " está anulado y no admite observaciones nuevas");
        }
        CampoContrato campo = campo(peticion.getCampo());
        // El autor queda nulo hasta que exista el inicio de sesión.
        ContratoObservacionEntity observacion = new ContratoObservacionEntity(contrato, campo,
                Textos.limpiar(peticion.getTexto()), null);
        return aRespuesta(repositorio.save(observacion));
    }

    @Override
    public List<ObservacionDTORespuesta> listar(Long contratoId, String campo, Boolean resuelta) {
        buscarContrato(contratoId);
        CampoContrato filtro = Textos.limpiar(campo) == null ? null : campo(campo);
        return repositorio.buscar(contratoId, filtro, resuelta).stream().map(this::aRespuesta).toList();
    }

    @Override
    @Transactional
    public ObservacionDTORespuesta resolver(Long contratoId, Long observacionId, String respuesta) {
        buscarContrato(contratoId);
        ContratoObservacionEntity observacion = repositorio.findByIdAndContratoId(observacionId, contratoId)
                .orElseThrow(() -> new EntidadNoExisteException("No existe la observación " + observacionId
                        + " en el contrato con id " + contratoId));
        if (Boolean.TRUE.equals(observacion.getResuelta())) {
            throw new ReglaNegocioException("La observación " + observacionId + " ya está resuelta");
        }
        // Quién la resuelve queda nulo hasta que exista el inicio de sesión.
        observacion.resolver(Textos.limpiar(respuesta), null, LocalDateTime.now(ZONA));
        return aRespuesta(repositorio.save(observacion));
    }

    @Override
    public ConteoObservacionesDTORespuesta contar(Long contratoId) {
        buscarContrato(contratoId);
        Map<CampoContrato, long[]> conteo = new EnumMap<>(CampoContrato.class);
        for (Object[] fila : repositorio.contarPorCampo(contratoId)) {
            long[] c = conteo.computeIfAbsent((CampoContrato) fila[0], k -> new long[2]);
            long cantidad = (Long) fila[2];
            c[0] += cantidad;
            if (!Boolean.TRUE.equals(fila[1])) {
                c[1] += cantidad;
            }
        }
        // EnumMap recorre en el orden del formulario.
        Map<String, ConteoCampo> porCampo = new LinkedHashMap<>();
        long total = 0;
        long pendientes = 0;
        for (Map.Entry<CampoContrato, long[]> e : conteo.entrySet()) {
            porCampo.put(e.getKey().name(), new ConteoCampo(e.getValue()[0], e.getValue()[1]));
            total += e.getValue()[0];
            pendientes += e.getValue()[1];
        }
        return new ConteoObservacionesDTORespuesta(total, pendientes, porCampo);
    }

    private ContratoEntity buscarContrato(Long id) {
        return contratoRepositorio.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("No existe un contrato con id " + id));
    }

    private static CampoContrato campo(String codigo) {
        return CampoContrato.desde(codigo).orElseThrow(() -> new ReglaNegocioException("El campo '" + codigo
                + "' no existe en el contrato; los campos válidos los da GET /api/contratos/campos"));
    }

    private ObservacionDTORespuesta aRespuesta(ContratoObservacionEntity o) {
        ObservacionDTORespuesta r = new ObservacionDTORespuesta();
        r.setId(o.getId());
        r.setContratoId(o.getContrato().getId());
        r.setCampo(o.getCampo().name());
        r.setCampoEtiqueta(o.getCampo().getEtiqueta());
        r.setTexto(o.getTexto());
        r.setAutorId(o.getAutor() == null ? null : o.getAutor().getId());
        r.setCreadoEn(o.getCreadoEn());
        r.setResuelta(o.getResuelta());
        r.setRespuesta(o.getRespuesta());
        r.setResueltaPorId(o.getResueltaPor() == null ? null : o.getResueltaPor().getId());
        r.setResueltaEn(o.getResueltaEn());
        return r;
    }
}
