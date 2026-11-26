package co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios;

import java.util.List;

import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.SerieDocumentalEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTOPeticion.SerieDocumentalDTOPeticion;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.SerieDocumentalDTORespuesta;

public interface ISerieDocumentalServicio {

    List<SerieDocumentalDTORespuesta> listar(Boolean activo);

    SerieDocumentalDTORespuesta consultarPorId(Long id);

    SerieDocumentalDTORespuesta crear(SerieDocumentalDTOPeticion peticion);

    /** Falla con 400 si se cambia el código de una serie que ya tiene contratos. */
    SerieDocumentalDTORespuesta actualizar(Long id, SerieDocumentalDTOPeticion peticion);

    /** Activa o inactiva. Sus tipos dejan de ofrecerse y no pueden generar números mientras esté inactiva. */
    SerieDocumentalDTORespuesta cambiarEstado(Long id, boolean activo);

    /**
     * Para el tipo de contrato: devuelve la serie si existe. Si es la misma que ya tenía
     * ({@code actual}) no exige nada más; si cambia, debe estar activa. Lanza 400 si no cumple.
     */
    SerieDocumentalEntity obtenerSerieValida(Long id, SerieDocumentalEntity actual);
}
