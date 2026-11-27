package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CodigoCatalogo;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.servicios.ICatalogoItemServicio;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ModificacionEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ModificacionRepositorio;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ModificacionDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ModificacionDTORespuesta;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import lombok.RequiredArgsConstructor;

/**
 * Adiciones del contrato. Solo en contratos suscritos. Tocan el valor final, así que bloquean la
 * fila del contrato y no pueden dejar el saldo negativo (al reducir o borrar una adición ya pagada).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModificacionServicio {

    private final ModificacionRepositorio repositorio;
    private final ICatalogoItemServicio catalogoServicio;
    private final ContratoDetalleSoporte soporte;

    public List<ModificacionDTORespuesta> listar(Long contratoId) {
        soporte.contrato(contratoId);
        return repositorio.buscarDelContrato(contratoId).stream().map(m -> aRespuesta(m, List.of())).toList();
    }

    @Transactional
    public ModificacionDTORespuesta crear(Long contratoId, ModificacionDTOPeticion p) {
        ContratoEntity contrato = soporte.contratoBloqueado(contratoId);
        ModificacionEntity m = new ModificacionEntity(contrato);
        asignar(contrato, m, p);
        repositorio.save(m);
        return aRespuesta(m, advertencias(contrato));
    }

    @Transactional
    public ModificacionDTORespuesta actualizar(Long contratoId, Long id, ModificacionDTOPeticion p) {
        ContratoEntity contrato = soporte.contratoBloqueado(contratoId);
        ModificacionEntity m = buscar(contratoId, id);
        asignar(contrato, m, p);
        repositorio.save(m);
        soporte.validarSaldo(contrato);
        return aRespuesta(m, advertencias(contrato));
    }

    @Transactional
    public void eliminar(Long contratoId, Long id) {
        ContratoEntity contrato = soporte.contratoBloqueado(contratoId);
        repositorio.delete(buscar(contratoId, id));
        repositorio.flush();
        soporte.validarSaldo(contrato);
    }

    private void asignar(ContratoEntity contrato, ModificacionEntity m, ModificacionDTOPeticion p) {
        if (contrato.getFechaSuscripcion() == null) {
            throw new ReglaNegocioException("Solo se pueden registrar adiciones en un contrato suscrito; el contrato "
                    + contrato.getNumero() + " está " + contrato.getEstado());
        }
        CatalogoItemEntity tipo = m.getTipoAdicion() != null && m.getTipoAdicion().getId().equals(p.getTipoAdicionId())
                ? m.getTipoAdicion() : catalogoServicio.validarItem(p.getTipoAdicionId(), CodigoCatalogo.TIPO_ADICION);
        boolean enValor;
        boolean enTiempo;
        switch (tipo.getCodigo()) {
            case "VALOR" -> { enValor = true; enTiempo = false; }
            case "TIEMPO" -> { enValor = false; enTiempo = true; }
            case "VALOR_TIEMPO" -> { enValor = true; enTiempo = true; }
            default -> throw new ReglaNegocioException("'" + tipo.getEtiqueta() + "' no es una adición que se pueda registrar; "
                    + "si el contrato no tiene adiciones, no registre ninguna");
        }
        exigir(enValor, p.getValor() != null, tipo, "el valor");
        exigir(enTiempo, p.getDias() != null, tipo, "los días");
        ContratoDetalleSoporte.noAnteriorA(p.getFecha(), "de la modificación", contrato.getFechaSuscripcion(), "suscripción");

        m.setTipoAdicion(tipo);
        m.setValor(p.getValor());
        m.setDias(p.getDias());
        m.setFecha(p.getFecha());
        m.setDocumento(ContratoDetalleSoporte.texto(p.getDocumento()));
        m.setObservacion(ContratoDetalleSoporte.texto(p.getObservacion()));
    }

    private static void exigir(boolean exige, boolean viene, CatalogoItemEntity tipo, String campo) {
        if (exige && !viene) {
            throw new ReglaNegocioException("La adición '" + tipo.getEtiqueta() + "' exige " + campo);
        }
        if (!exige && viene) {
            throw new ReglaNegocioException("La adición '" + tipo.getEtiqueta() + "' no lleva " + campo);
        }
    }

    private List<String> advertencias(ContratoEntity contrato) {
        return soporte.advertenciaAdiciones(soporte.resumen(contrato));
    }

    private ModificacionEntity buscar(Long contratoId, Long id) {
        return repositorio.findByIdAndContratoId(id, contratoId).orElseThrow(() ->
                new EntidadNoExisteException("No existe la modificación con id " + id + " en el contrato con id " + contratoId));
    }

    private static ModificacionDTORespuesta aRespuesta(ModificacionEntity m, List<String> advertencias) {
        CatalogoItemEntity t = m.getTipoAdicion();
        return new ModificacionDTORespuesta(m.getId(), t.getId(), t.getCodigo(), t.getEtiqueta(), m.getValor(), m.getDias(),
                m.getFecha(), m.getDocumento(), m.getObservacion(), advertencias);
    }
}
