package co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.servicios;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoEntity;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.repositorios.CatalogoItemRepositorio;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoItemActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoItemCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTORespuesta.CatalogoItemDTORespuesta;
import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadYaExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogoItemServicioImpl implements ICatalogoItemServicio {

    private final CatalogoItemRepositorio repositorio;
    private final ICatalogoServicio catalogoServicio;

    @Override
    public List<CatalogoItemDTORespuesta> listar(String codigoCatalogo, Boolean activo) {
        CatalogoEntity catalogo = catalogoServicio.buscarPorCodigo(codigoCatalogo);
        if (Boolean.TRUE.equals(activo) && !Boolean.TRUE.equals(catalogo.getActivo())) {
            return List.of();
        }
        return repositorio.buscar(catalogo.getId(), activo).stream().map(this::aRespuesta).toList();
    }

    @Override
    public CatalogoItemDTORespuesta consultar(String codigoCatalogo, Long id) {
        return aRespuesta(buscar(catalogoServicio.buscarPorCodigo(codigoCatalogo), id));
    }

    @Override
    @Transactional
    public CatalogoItemDTORespuesta crear(String codigoCatalogo, CatalogoItemCrearDTOPeticion peticion) {
        CatalogoEntity catalogo = catalogoServicio.buscarPorCodigo(codigoCatalogo);
        String codigo = Textos.limpiarMayusculas(peticion.getCodigo());
        String nombre = Textos.limpiar(peticion.getNombre());
        if (repositorio.existsByCatalogoIdAndCodigo(catalogo.getId(), codigo)) {
            throw new EntidadYaExisteException("Ya existe el código " + codigo + " en el catálogo " + catalogo.getCodigo());
        }
        if (repositorio.existsByCatalogoIdAndNombreIgnoreCase(catalogo.getId(), nombre)) {
            throw new EntidadYaExisteException("Ya existe el ítem '" + nombre + "' en el catálogo " + catalogo.getCodigo());
        }
        CatalogoItemEntity item = new CatalogoItemEntity(catalogo, codigo, nombre);
        item.setCodigoSireci(Textos.limpiarMayusculas(peticion.getCodigoSireci()));
        item.setOrden(peticion.getOrden() == null ? 0 : peticion.getOrden());
        item.setActivo(Boolean.TRUE);
        return aRespuesta(repositorio.save(item));
    }

    @Override
    @Transactional
    public CatalogoItemDTORespuesta actualizar(String codigoCatalogo, Long id, CatalogoItemActualizarDTOPeticion peticion) {
        CatalogoEntity catalogo = catalogoServicio.buscarPorCodigo(codigoCatalogo);
        CatalogoItemEntity item = buscar(catalogo, id);
        String codigoCuerpo = Textos.limpiarMayusculas(peticion.getCodigo());
        if (codigoCuerpo != null && !codigoCuerpo.equals(item.getCodigo())) {
            throw new ReglaNegocioException("El código del ítem no se puede cambiar (" + item.getCodigo()
                    + " → " + codigoCuerpo + "): el programa lo usa. Cree un ítem nuevo e inactive este");
        }
        String nombre = Textos.limpiar(peticion.getNombre());
        if (repositorio.existsByCatalogoIdAndNombreIgnoreCaseAndIdNot(catalogo.getId(), nombre, id)) {
            throw new EntidadYaExisteException("Ya existe el ítem '" + nombre + "' en el catálogo " + catalogo.getCodigo());
        }
        item.setNombre(nombre);
        item.setCodigoSireci(Textos.limpiarMayusculas(peticion.getCodigoSireci()));
        item.setOrden(peticion.getOrden() == null ? item.getOrden() : peticion.getOrden());
        return aRespuesta(repositorio.save(item));
    }

    @Override
    @Transactional
    public CatalogoItemDTORespuesta cambiarEstado(String codigoCatalogo, Long id, boolean activo) {
        CatalogoItemEntity item = buscar(catalogoServicio.buscarPorCodigo(codigoCatalogo), id);
        item.setActivo(activo);
        return aRespuesta(repositorio.save(item));
    }

    @Override
    public CatalogoItemEntity validarItem(Long itemId, String codigoCatalogo) {
        CatalogoItemEntity item = repositorio.findById(itemId).orElseThrow(() ->
                new ReglaNegocioException("El ítem con id " + itemId + " no existe (se esperaba uno del catálogo " + codigoCatalogo + ")"));
        CatalogoEntity catalogo = item.getCatalogo();
        if (!catalogo.getCodigo().equals(codigoCatalogo)) {
            throw new ReglaNegocioException("El ítem '" + item.getEtiqueta() + "' es del catálogo " + catalogo.getCodigo()
                    + " y se esperaba uno del catálogo " + codigoCatalogo);
        }
        if (!Boolean.TRUE.equals(catalogo.getActivo())) {
            throw new ReglaNegocioException("El catálogo " + catalogo.getCodigo() + " está inactivo");
        }
        if (!Boolean.TRUE.equals(item.getActivo())) {
            throw new ReglaNegocioException("El ítem '" + item.getEtiqueta() + "' del catálogo " + catalogo.getCodigo() + " está inactivo");
        }
        return item;
    }

    /** 404 si el ítem no existe o no pertenece al catálogo de la URL. */
    private CatalogoItemEntity buscar(CatalogoEntity catalogo, Long id) {
        return repositorio.findByIdAndCatalogoId(id, catalogo.getId()).orElseThrow(() ->
                new EntidadNoExisteException("No existe un ítem con id " + id + " en el catálogo " + catalogo.getCodigo()));
    }

    private CatalogoItemDTORespuesta aRespuesta(CatalogoItemEntity item) {
        CatalogoItemDTORespuesta r = new CatalogoItemDTORespuesta();
        r.setId(item.getId());
        r.setCatalogo(item.getCatalogo().getCodigo());
        r.setCodigo(item.getCodigo());
        r.setCodigoSireci(item.getCodigoSireci());
        r.setNombre(item.getNombre());
        r.setEtiqueta(item.getEtiqueta());
        r.setOrden(item.getOrden());
        r.setActivo(item.getActivo());
        r.setCreadoEn(item.getCreadoEn());
        r.setActualizadoEn(item.getActualizadoEn());
        return r;
    }
}
