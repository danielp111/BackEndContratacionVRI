package co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.servicios;

import java.util.List;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.Catalogo;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoItemDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTORespuesta.CatalogoItemDTORespuesta;

public interface ICatalogoItemServicio {

    /** Filtros opcionales: catálogo y estado. */
    List<CatalogoItemDTORespuesta> listar(Catalogo catalogo, Boolean activo);

    CatalogoItemDTORespuesta consultarPorId(Long id);

    CatalogoItemDTORespuesta crear(CatalogoItemDTOPeticion peticion);

    /** No permite mover un ítem a otro catálogo: los registros que lo usan quedarían inconsistentes. */
    CatalogoItemDTORespuesta actualizar(Long id, CatalogoItemDTOPeticion peticion);

    CatalogoItemDTORespuesta cambiarEstado(Long id, boolean activo);

    /**
     * Para otros dominios: devuelve el ítem si pertenece al catálogo esperado.
     * Si {@code exigirActivo} es verdadero, además debe estar activo.
     * Lanza ReglaNegocioException si no cumple.
     */
    CatalogoItemEntity obtenerItemValido(Long id, Catalogo catalogoEsperado, boolean exigirActivo, String nombreCampo);
}
