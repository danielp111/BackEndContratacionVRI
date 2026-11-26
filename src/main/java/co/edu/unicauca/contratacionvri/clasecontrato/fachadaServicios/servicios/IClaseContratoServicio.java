package co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.servicios;

import java.util.List;

import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.AmbitoClase;
import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.ClaseContratoEntity;
import co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.DTOPeticion.ClaseContratoActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.DTOPeticion.ClaseContratoCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.DTORespuesta.ClaseContratoDTORespuesta;

public interface IClaseContratoServicio {

    /**
     * Código que el SIRECI usa para "no se diligencia información en este período". No es una
     * clase: pertenece al reporte SIRECI y no se puede crear aquí.
     */
    int CODIGO_NO_DILIGENCIA = 99_999_998;

    /** Filtros opcionales por ámbito y estado (activo=true para los selectores). */
    List<ClaseContratoDTORespuesta> listar(AmbitoClase ambito, Boolean activo);

    ClaseContratoDTORespuesta consultarPorId(Long id);

    /** Falla con 409 si ya existe el código en el ámbito, y con 400 si el código es 99999998. */
    ClaseContratoDTORespuesta crear(ClaseContratoCrearDTOPeticion peticion);

    /** Solo cambia el nombre. Un código o ámbito distinto en la petición da 400. */
    ClaseContratoDTORespuesta actualizar(Long id, ClaseContratoActualizarDTOPeticion peticion);

    /** Activa o inactiva. Una clase inactiva no se ofrece para contratos nuevos. */
    ClaseContratoDTORespuesta cambiarEstado(Long id, boolean activo);

    /**
     * Para el dominio de contratos: devuelve la clase si existe. Si es la misma que ya tenía el
     * contrato ({@code actual}) no exige más; si cambia, debe estar activa. Lanza 400 si no cumple.
     */
    ClaseContratoEntity obtenerClaseValida(Long id, ClaseContratoEntity actual);
}
