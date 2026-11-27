package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.PagoEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.PagoRepositorio;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.PagoDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.PagoDTORespuesta;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import lombok.RequiredArgsConstructor;

/**
 * Pagos del contrato (los registra el financiero). El número lo asigna el sistema: consecutivo desde 1.
 * Cada cambio bloquea la fila del contrato, así dos pagos simultáneos no reciben el mismo número ni
 * dejan el saldo negativo. Solo el último pago se corrige o se borra, para no romper la numeración.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PagoServicio {

    private final PagoRepositorio repositorio;
    private final ContratoDetalleSoporte soporte;

    public List<PagoDTORespuesta> listar(Long contratoId) {
        soporte.contrato(contratoId);
        return repositorio.findByContratoIdOrderByNumeroAsc(contratoId).stream()
                .map(p -> aRespuesta(p, null, List.of())).toList();
    }

    @Transactional
    public PagoDTORespuesta crear(Long contratoId, PagoDTOPeticion p) {
        ContratoEntity contrato = soporte.contratoBloqueado(contratoId);
        if (contrato.getFechaInicio() == null) {
            throw new ReglaNegocioException("Solo se pueden registrar pagos de un contrato con fecha de inicio; el contrato "
                    + contrato.getNumero() + " está " + contrato.getEstado());
        }
        validarFecha(contrato, p);
        // El inicio de sesión aún no existe: quién lo registró queda nulo.
        PagoEntity pago = new PagoEntity(contrato, repositorio.ultimoNumero(contratoId) + 1, null);
        pago.setValor(p.getValor());
        pago.setFecha(p.getFecha());
        repositorio.save(pago);
        soporte.validarSaldo(contrato);
        return aRespuesta(pago, saldo(contrato), advertencias(contrato, pago));
    }

    @Transactional
    public PagoDTORespuesta actualizar(Long contratoId, Long id, PagoDTOPeticion p) {
        ContratoEntity contrato = soporte.contratoBloqueado(contratoId);
        PagoEntity pago = ultimo(contrato, id, "modificar");
        validarFecha(contrato, p);
        pago.setValor(p.getValor());
        pago.setFecha(p.getFecha());
        repositorio.save(pago);
        soporte.validarSaldo(contrato);
        return aRespuesta(pago, saldo(contrato), advertencias(contrato, pago));
    }

    @Transactional
    public void eliminar(Long contratoId, Long id) {
        ContratoEntity contrato = soporte.contratoBloqueado(contratoId);
        PagoEntity pago = ultimo(contrato, id, "borrar");
        if (contrato.getFechaActaFinal() != null && contrato.getNumeroPagos() != null
                && pago.getNumero() <= contrato.getNumeroPagos()) {
            throw new ReglaNegocioException("El contrato " + contrato.getNumero() + " ya tiene acta final con "
                    + contrato.getNumeroPagos() + " pagos pactados: el pago " + pago.getNumero() + " no se puede borrar");
        }
        repositorio.delete(pago);
    }

    /** Solo el último pago se modifica o se borra. */
    private PagoEntity ultimo(ContratoEntity contrato, Long id, String accion) {
        PagoEntity pago = repositorio.findByIdAndContratoId(id, contrato.getId()).orElseThrow(() ->
                new EntidadNoExisteException("No existe el pago con id " + id + " en el contrato con id " + contrato.getId()));
        int ultimo = repositorio.ultimoNumero(contrato.getId());
        if (pago.getNumero() != ultimo) {
            throw new ReglaNegocioException("Solo se puede " + accion + " el último pago (el " + ultimo + "); el pago "
                    + pago.getNumero() + " no");
        }
        return pago;
    }

    private static void validarFecha(ContratoEntity contrato, PagoDTOPeticion p) {
        ContratoDetalleSoporte.noAnteriorA(p.getFecha(), "del pago", contrato.getFechaInicio(), "inicio");
    }

    /** Aviso (no bloquea) si se pagan más pagos de los pactados. */
    private static List<String> advertencias(ContratoEntity contrato, PagoEntity pago) {
        Integer pactados = contrato.getNumeroPagos();
        if (pactados == null || pago.getNumero() <= pactados) {
            return List.of();
        }
        return List.of("El contrato pactó " + pactados + (pactados == 1 ? " pago" : " pagos") + " y este es el pago " + pago.getNumero());
    }

    private BigDecimal saldo(ContratoEntity contrato) {
        return soporte.resumen(contrato).getSaldo();
    }

    private static PagoDTORespuesta aRespuesta(PagoEntity p, BigDecimal saldo, List<String> advertencias) {
        return new PagoDTORespuesta(p.getId(), p.getNumero(), p.getValor(), p.getFecha(),
                p.getRegistradoPor() == null ? null : p.getRegistradoPor().getId(), saldo, advertencias);
    }
}
