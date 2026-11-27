package co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.servicios;

import java.util.List;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoEntity;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTORespuesta.CatalogoDTORespuesta;

public interface ICatalogoServicio {

    List<CatalogoDTORespuesta> listar(Boolean activo);

    /** El código se busca en mayúsculas ("tipo_identificacion" encuentra TIPO_IDENTIFICACION). */
    CatalogoDTORespuesta consultarPorCodigo(String codigo);

    /** Falla con 409 si el código ya existe. */
    CatalogoDTORespuesta crear(CatalogoCrearDTOPeticion peticion);

    /** Cambia nombre y descripción. Un código distinto en el cuerpo da 400. */
    CatalogoDTORespuesta actualizar(String codigo, CatalogoActualizarDTOPeticion peticion);

    /** Activa o inactiva. Con el catálogo inactivo, sus ítems no se ofrecen ni se aceptan en registros nuevos. */
    CatalogoDTORespuesta cambiarEstado(String codigo, boolean activo);

    /** Para el servicio de ítems: el catálogo con ese código, o 404. */
    CatalogoEntity buscarPorCodigo(String codigo);
}
