package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import java.util.List;

import co.edu.unicauca.contratacionvri.comun.fachadaServicios.PaginaRespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ContratoActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ContratoCierreDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ContratoCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.CampoContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ContratoResumenDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ResumenContratoDTORespuesta;

public interface IContratoServicio {

    /**
     * Crea el contrato en trámite y le asigna el número en la misma transacción: si algo falla, el
     * número no se consume. El año debe ser el actual o hasta {@code vri.contrato.anios-atras} anteriores.
     */
    ContratoDTORespuesta crear(ContratoCrearDTOPeticion peticion);

    ContratoDTORespuesta consultarPorId(Long id);

    /** Solo los campos calculados (valor final, saldo, avances...). */
    ResumenContratoDTORespuesta consultarResumen(Long id);

    /** Listado general paginado, ordenado del más reciente al más antiguo. */
    PaginaRespuesta<ContratoResumenDTORespuesta> buscar(FiltroContratos filtro, int pagina, int tamanio);

    /** Contratos de un abogado responsable (404 si el abogado no existe). */
    PaginaRespuesta<ContratoResumenDTORespuesta> buscarDeAbogado(Long abogadoId, FiltroContratos filtro, int pagina, int tamanio);

    /**
     * Datos del abogado (formulario completo). Tipo y año no cambian. Con fecha de suscripción
     * exige el contrato completo. El estado se recalcula a partir de las fechas.
     */
    ContratoDTORespuesta actualizar(Long id, ContratoActualizarDTOPeticion peticion);

    /** Datos del financiero: terminación, acta final y certificación de pago. */
    ContratoDTORespuesta registrarCierre(Long id, ContratoCierreDTOPeticion peticion);

    /** Solo un contrato en trámite. Irreversible: el número no se reutiliza. */
    ContratoDTORespuesta anular(Long id, String motivo);

    /** Campos del contrato que admiten observaciones. */
    List<CampoContratoDTORespuesta> listarCampos();
}
