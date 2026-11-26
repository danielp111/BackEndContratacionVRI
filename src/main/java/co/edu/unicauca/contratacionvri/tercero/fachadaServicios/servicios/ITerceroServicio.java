package co.edu.unicauca.contratacionvri.tercero.fachadaServicios.servicios;

import co.edu.unicauca.contratacionvri.comun.fachadaServicios.PaginaRespuesta;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.Naturaleza;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTOPeticion.PersonaJuridicaDTOPeticion;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTOPeticion.PersonaNaturalDTOPeticion;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta.PersonaJuridicaDTORespuesta;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta.PersonaNaturalDTORespuesta;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta.TerceroDTORespuesta;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta.TerceroResumenDTORespuesta;

public interface ITerceroServicio {

    PersonaNaturalDTORespuesta crearPersonaNatural(PersonaNaturalDTOPeticion peticion);

    PersonaJuridicaDTORespuesta crearPersonaJuridica(PersonaJuridicaDTOPeticion peticion);

    /** Devuelve PersonaNaturalDTORespuesta o PersonaJuridicaDTORespuesta según el tercero. */
    TerceroDTORespuesta consultarPorId(Long id);

    /**
     * Búsqueda paginada ordenada por nombre. Todos los filtros son opcionales:
     * documento por prefijo, nombre por contenido (sin distinguir mayúsculas).
     */
    PaginaRespuesta<TerceroResumenDTORespuesta> buscar(String documento, String nombre, Naturaleza naturaleza,
                                                       Boolean activo, int pagina, int tamanio);

    /** Falla con 400 si el id corresponde a una persona jurídica: la naturaleza no cambia. */
    PersonaNaturalDTORespuesta actualizarPersonaNatural(Long id, PersonaNaturalDTOPeticion peticion);

    /** Falla con 400 si el id corresponde a una persona natural: la naturaleza no cambia. */
    PersonaJuridicaDTORespuesta actualizarPersonaJuridica(Long id, PersonaJuridicaDTOPeticion peticion);

    /** Activa o inactiva. No hay borrado físico: el tercero puede estar en contratos. */
    TerceroDTORespuesta cambiarEstado(Long id, boolean activo);
}
