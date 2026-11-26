package co.edu.unicauca.contratacionvri.rubro.fachadaServicios.servicios;

import co.edu.unicauca.contratacionvri.comun.fachadaServicios.PaginaRespuesta;
import co.edu.unicauca.contratacionvri.rubro.accesoADatos.modelos.RubroEntity;
import co.edu.unicauca.contratacionvri.rubro.fachadaServicios.DTOPeticion.RubroDTOPeticion;
import co.edu.unicauca.contratacionvri.rubro.fachadaServicios.DTORespuesta.RubroDTORespuesta;

public interface IRubroServicio {

    /**
     * Búsqueda paginada ordenada por código. {@code texto} (opcional) busca por prefijo del código o
     * por contenido del nombre; {@code activo=true} para los selectores.
     */
    PaginaRespuesta<RubroDTORespuesta> buscar(String texto, Boolean activo, int pagina, int tamanio);

    RubroDTORespuesta consultarPorId(Long id);

    /** Falla con 409 si el código ya existe. */
    RubroDTORespuesta crear(RubroDTOPeticion peticion);

    /** Falla con 400 si cambia el código de un rubro que ya tiene contratos, y con 409 si choca con otro. */
    RubroDTORespuesta actualizar(Long id, RubroDTOPeticion peticion);

    /** Activa o inactiva. Un rubro inactivo no se ofrece para contratos nuevos. */
    RubroDTORespuesta cambiarEstado(Long id, boolean activo);

    /**
     * Para el dominio de contratos: devuelve el rubro si existe. Si es el mismo que ya tenía el
     * contrato ({@code actual}) no exige más; si cambia, debe estar activo. Lanza 400 si no cumple.
     */
    RubroEntity obtenerRubroValido(Long id, RubroEntity actual);
}
