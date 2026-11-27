package co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.servicios;

import java.util.List;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoItemActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoItemCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTORespuesta.CatalogoItemDTORespuesta;

public interface ICatalogoItemServicio {

    /**
     * Ítems del catálogo, ordenados por orden y luego por nombre. Con {@code activo=true} (selectores)
     * solo los activos, y ninguno si el catálogo está inactivo.
     */
    List<CatalogoItemDTORespuesta> listar(String codigoCatalogo, Boolean activo);

    /** 404 si el ítem no existe o no es de ese catálogo. */
    CatalogoItemDTORespuesta consultar(String codigoCatalogo, Long id);

    /** Falla con 409 si el código o el nombre ya existen en el catálogo. */
    CatalogoItemDTORespuesta crear(String codigoCatalogo, CatalogoItemCrearDTOPeticion peticion);

    /** Cambia nombre, código SIRECI y orden. Un código distinto en el cuerpo da 400. */
    CatalogoItemDTORespuesta actualizar(String codigoCatalogo, Long id, CatalogoItemActualizarDTOPeticion peticion);

    /** Activa o inactiva. Un ítem inactivo no se ofrece en formularios nuevos; los registros existentes lo conservan. */
    CatalogoItemDTORespuesta cambiarEstado(String codigoCatalogo, Long id, boolean activo);

    /**
     * VALIDADOR REUTILIZABLE para las entidades que referencian ítems (tipo_identificacion_id de tercero
     * y abogado, y luego los campos del contrato). Confirma que el ítem existe, pertenece al catálogo
     * {@code codigoCatalogo}, está activo y su catálogo también. Si no, ReglaNegocioException (400).
     *
     * Llamarlo solo cuando el ítem se asigna o cambia: un registro existente conserva su ítem aunque
     * luego se inactive.
     */
    CatalogoItemEntity validarItem(Long itemId, String codigoCatalogo);
}
