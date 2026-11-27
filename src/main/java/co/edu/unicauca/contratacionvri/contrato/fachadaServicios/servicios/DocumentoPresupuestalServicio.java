package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoCdpEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoRpEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.DocumentoPresupuestalEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ContratoCdpRepositorio;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ContratoRpRepositorio;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.DocumentoPresupuestalRepositorio;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.DocumentoPresupuestalDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.DocumentoPresupuestalDTORespuesta;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadYaExisteException;
import lombok.RequiredArgsConstructor;

/** CDP y RP del contrato. El número es único por contrato; se guarda en mayúsculas. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentoPresupuestalServicio {

    private final ContratoCdpRepositorio cdpRepositorio;
    private final ContratoRpRepositorio rpRepositorio;
    private final ContratoDetalleSoporte soporte;

    public List<DocumentoPresupuestalDTORespuesta> listar(TipoDocumentoPresupuestal tipo, Long contratoId) {
        soporte.contrato(contratoId);
        return repositorio(tipo).findByContratoIdOrderByIdAsc(contratoId).stream()
                .map(d -> aRespuesta(d, List.of())).toList();
    }

    @Transactional
    public DocumentoPresupuestalDTORespuesta crear(TipoDocumentoPresupuestal tipo, Long contratoId, DocumentoPresupuestalDTOPeticion p) {
        ContratoEntity contrato = soporte.contratoEditable(contratoId);
        String numero = Textos.limpiarMayusculas(p.getNumero());
        if (repositorio(tipo).existsByContratoIdAndNumero(contratoId, numero)) {
            throw repetido(tipo, contrato, numero);
        }
        DocumentoPresupuestalEntity d = tipo == TipoDocumentoPresupuestal.CDP
                ? new ContratoCdpEntity(contrato) : new ContratoRpEntity(contrato);
        asignar(d, numero, p);
        repositorio(tipo).save(d);
        return aRespuesta(d, advertencias(tipo, contrato));
    }

    @Transactional
    public DocumentoPresupuestalDTORespuesta actualizar(TipoDocumentoPresupuestal tipo, Long contratoId, Long id,
                                                        DocumentoPresupuestalDTOPeticion p) {
        ContratoEntity contrato = soporte.contratoEditable(contratoId);
        DocumentoPresupuestalEntity d = buscar(tipo, contratoId, id);
        String numero = Textos.limpiarMayusculas(p.getNumero());
        if (repositorio(tipo).existsByContratoIdAndNumeroAndIdNot(contratoId, numero, id)) {
            throw repetido(tipo, contrato, numero);
        }
        asignar(d, numero, p);
        repositorio(tipo).save(d);
        return aRespuesta(d, advertencias(tipo, contrato));
    }

    @Transactional
    public void eliminar(TipoDocumentoPresupuestal tipo, Long contratoId, Long id) {
        soporte.contratoEditable(contratoId);
        repositorio(tipo).delete(buscar(tipo, contratoId, id));
    }

    private static void asignar(DocumentoPresupuestalEntity d, String numero, DocumentoPresupuestalDTOPeticion p) {
        d.setNumero(numero);
        d.setValor(p.getValor());
        d.setFecha(p.getFecha());
    }

    private List<String> advertencias(TipoDocumentoPresupuestal tipo, ContratoEntity contrato) {
        return tipo == TipoDocumentoPresupuestal.CDP
                ? soporte.advertenciaCdp(contrato, soporte.resumen(contrato)) : List.of();
    }

    private DocumentoPresupuestalEntity buscar(TipoDocumentoPresupuestal tipo, Long contratoId, Long id) {
        return repositorio(tipo).findByIdAndContratoId(id, contratoId).orElseThrow(() ->
                new EntidadNoExisteException("No existe " + tipo.getNombre() + " con id " + id + " en el contrato con id " + contratoId));
    }

    private static EntidadYaExisteException repetido(TipoDocumentoPresupuestal tipo, ContratoEntity c, String numero) {
        return new EntidadYaExisteException("El contrato " + c.getNumero() + " ya tiene " + tipo.getNombre() + " " + numero);
    }

    @SuppressWarnings("unchecked")
    private DocumentoPresupuestalRepositorio<DocumentoPresupuestalEntity> repositorio(TipoDocumentoPresupuestal tipo) {
        Object repositorio = tipo == TipoDocumentoPresupuestal.CDP ? cdpRepositorio : rpRepositorio;
        return (DocumentoPresupuestalRepositorio<DocumentoPresupuestalEntity>) repositorio;
    }

    private static DocumentoPresupuestalDTORespuesta aRespuesta(DocumentoPresupuestalEntity d, List<String> advertencias) {
        return new DocumentoPresupuestalDTORespuesta(d.getId(), d.getNumero(), d.getValor(), d.getFecha(), advertencias);
    }
}
