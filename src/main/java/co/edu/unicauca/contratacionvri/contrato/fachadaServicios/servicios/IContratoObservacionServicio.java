package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import java.util.List;

import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ObservacionDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ConteoObservacionesDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ObservacionDTORespuesta;

/** Observaciones por campo del contrato (las escriben y resuelven abogados y financiero). */
public interface IContratoObservacionServicio {

    /** 404 si el contrato no existe; 400 si el campo no existe o el contrato está anulado. */
    ObservacionDTORespuesta agregar(Long contratoId, ObservacionDTOPeticion peticion);

    /** La más reciente primero. {@code campo} y {@code resuelta} son opcionales. */
    List<ObservacionDTORespuesta> listar(Long contratoId, String campo, Boolean resuelta);

    /** Una sola vez (400 si ya estaba resuelta). La respuesta es opcional. */
    ObservacionDTORespuesta resolver(Long contratoId, Long observacionId, String respuesta);

    ConteoObservacionesDTORespuesta contar(Long contratoId);
}
