package co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios;

import java.util.List;

import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTOPeticion.TipoContratoDTOPeticion;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.TipoContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.TipoContratoOpcionDTORespuesta;

public interface ITipoContratoServicio {

    /** Filtros opcionales: estado del tipo y serie. */
    List<TipoContratoDTORespuesta> listar(Boolean activo, Long serieId);

    /** Para los selectores del contrato: tipo activo y serie activa. */
    List<TipoContratoOpcionDTORespuesta> listarDisponibles();

    TipoContratoDTORespuesta consultarPorId(Long id);

    TipoContratoDTORespuesta crear(TipoContratoDTOPeticion peticion);

    /** Falla con 400 si cambia el nombre o la serie de un tipo que ya tiene contratos. */
    TipoContratoDTORespuesta actualizar(Long id, TipoContratoDTOPeticion peticion);

    /** Activa o inactiva. Un tipo inactivo no se ofrece para contratos nuevos ni genera números. */
    TipoContratoDTORespuesta cambiarEstado(Long id, boolean activo);
}
