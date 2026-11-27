package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CodigoCatalogo;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.servicios.ICatalogoItemServicio;
import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoUnspscEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.PolizaEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ContratoUnspscRepositorio;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.PolizaRepositorio;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.PolizaDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.PolizaDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.UnspscDTORespuesta;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadYaExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import lombok.RequiredArgsConstructor;

/** Códigos UNSPSC y pólizas del contrato: listas simples sin reglas de dinero. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnspscPolizaServicio {

    private final ContratoUnspscRepositorio unspscRepositorio;
    private final PolizaRepositorio polizaRepositorio;
    private final ICatalogoItemServicio catalogoServicio;
    private final ContratoDetalleSoporte soporte;

    // ------------------------------------------------------------------ UNSPSC

    public List<UnspscDTORespuesta> listarUnspsc(Long contratoId) {
        soporte.contrato(contratoId);
        return unspscRepositorio.findByContratoIdOrderByCodigoUnspscAsc(contratoId).stream()
                .map(u -> new UnspscDTORespuesta(u.getId(), u.getCodigoUnspsc())).toList();
    }

    /** El código lleva 8 dígitos; se quitan los espacios ("80 11 16 00" → 80111600). */
    @Transactional
    public UnspscDTORespuesta agregarUnspsc(Long contratoId, String codigo) {
        ContratoEntity contrato = soporte.contratoEditable(contratoId);
        String limpio = codigo == null ? "" : codigo.replaceAll("\\s", "");
        if (!limpio.matches("\\d{8}")) {
            throw new ReglaNegocioException("El código UNSPSC debe tener 8 dígitos: '" + Textos.limpiar(codigo) + "' no los tiene");
        }
        if (unspscRepositorio.existsByContratoIdAndCodigoUnspsc(contratoId, limpio)) {
            throw new EntidadYaExisteException("El contrato " + contrato.getNumero() + " ya tiene el código UNSPSC " + limpio);
        }
        ContratoUnspscEntity u = unspscRepositorio.save(new ContratoUnspscEntity(contrato, limpio));
        return new UnspscDTORespuesta(u.getId(), u.getCodigoUnspsc());
    }

    @Transactional
    public void eliminarUnspsc(Long contratoId, Long id) {
        soporte.contratoEditable(contratoId);
        unspscRepositorio.delete(unspscRepositorio.findByIdAndContratoId(id, contratoId).orElseThrow(() ->
                new EntidadNoExisteException("No existe el código UNSPSC con id " + id + " en el contrato con id " + contratoId)));
    }

    // ------------------------------------------------------------------ pólizas

    public List<PolizaDTORespuesta> listarPolizas(Long contratoId) {
        soporte.contrato(contratoId);
        return polizaRepositorio.buscarDelContrato(contratoId).stream().map(UnspscPolizaServicio::aRespuesta).toList();
    }

    @Transactional
    public PolizaDTORespuesta crearPoliza(Long contratoId, PolizaDTOPeticion p) {
        ContratoEntity contrato = soporte.contratoEditable(contratoId);
        String numero = Textos.limpiarMayusculas(p.getNumero());
        if (polizaRepositorio.existsByContratoIdAndNumero(contratoId, numero)) {
            throw polizaRepetida(contrato, numero);
        }
        PolizaEntity poliza = new PolizaEntity(contrato);
        poliza.setAseguradora(catalogoServicio.validarItem(p.getAseguradoraId(), CodigoCatalogo.ASEGURADORA));
        poliza.setNumero(numero);
        poliza.setVigenciaHasta(p.getVigenciaHasta());
        return aRespuesta(polizaRepositorio.save(poliza));
    }

    @Transactional
    public PolizaDTORespuesta actualizarPoliza(Long contratoId, Long id, PolizaDTOPeticion p) {
        ContratoEntity contrato = soporte.contratoEditable(contratoId);
        PolizaEntity poliza = buscarPoliza(contratoId, id);
        String numero = Textos.limpiarMayusculas(p.getNumero());
        if (polizaRepositorio.existsByContratoIdAndNumeroAndIdNot(contratoId, numero, id)) {
            throw polizaRepetida(contrato, numero);
        }
        if (!poliza.getAseguradora().getId().equals(p.getAseguradoraId())) {
            poliza.setAseguradora(catalogoServicio.validarItem(p.getAseguradoraId(), CodigoCatalogo.ASEGURADORA));
        }
        poliza.setNumero(numero);
        poliza.setVigenciaHasta(p.getVigenciaHasta());
        return aRespuesta(polizaRepositorio.save(poliza));
    }

    @Transactional
    public void eliminarPoliza(Long contratoId, Long id) {
        soporte.contratoEditable(contratoId);
        polizaRepositorio.delete(buscarPoliza(contratoId, id));
    }

    private PolizaEntity buscarPoliza(Long contratoId, Long id) {
        return polizaRepositorio.findByIdAndContratoId(id, contratoId).orElseThrow(() ->
                new EntidadNoExisteException("No existe la póliza con id " + id + " en el contrato con id " + contratoId));
    }

    private static EntidadYaExisteException polizaRepetida(ContratoEntity c, String numero) {
        return new EntidadYaExisteException("El contrato " + c.getNumero() + " ya tiene la póliza " + numero);
    }

    private static PolizaDTORespuesta aRespuesta(PolizaEntity p) {
        return new PolizaDTORespuesta(p.getId(), p.getNumero(), p.getAseguradora().getId(), p.getAseguradora().getNombre(),
                p.getVigenciaHasta());
    }
}
