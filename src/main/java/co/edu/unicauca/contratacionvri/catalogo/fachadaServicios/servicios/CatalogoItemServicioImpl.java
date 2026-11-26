package co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.servicios;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.Catalogo;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.repositorios.CatalogoItemRepositorio;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoItemDTOPeticion;
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
    private final ModelMapper modelMapper;

    @Override
    public List<CatalogoItemDTORespuesta> listar(Catalogo catalogo, Boolean activo) {
        return repositorio.buscar(catalogo, activo).stream().map(this::aRespuesta).toList();
    }

    @Override
    public CatalogoItemDTORespuesta consultarPorId(Long id) {
        return aRespuesta(buscar(id));
    }

    @Override
    @Transactional
    public CatalogoItemDTORespuesta crear(CatalogoItemDTOPeticion peticion) {
        normalizar(peticion);
        if (repositorio.existsByCatalogoAndCodigoSireci(peticion.getCatalogo(), peticion.getCodigoSireci())) {
            throw new EntidadYaExisteException("Ya existe el código " + peticion.getCodigoSireci()
                    + " en el catálogo " + peticion.getCatalogo());
        }
        CatalogoItemEntity item = modelMapper.map(peticion, CatalogoItemEntity.class);
        item.setActivo(Boolean.TRUE);
        return aRespuesta(repositorio.save(item));
    }

    @Override
    @Transactional
    public CatalogoItemDTORespuesta actualizar(Long id, CatalogoItemDTOPeticion peticion) {
        CatalogoItemEntity item = buscar(id);
        normalizar(peticion);
        if (item.getCatalogo() != peticion.getCatalogo()) {
            throw new ReglaNegocioException("No se puede cambiar el catálogo de un ítem (" + item.getCatalogo()
                    + "). Cree un ítem nuevo en " + peticion.getCatalogo() + " e inactive este");
        }
        if (repositorio.existsByCatalogoAndCodigoSireciAndIdNot(peticion.getCatalogo(), peticion.getCodigoSireci(), id)) {
            throw new EntidadYaExisteException("Ya existe el código " + peticion.getCodigoSireci()
                    + " en el catálogo " + peticion.getCatalogo());
        }
        modelMapper.map(peticion, item);
        return aRespuesta(repositorio.save(item));
    }

    @Override
    @Transactional
    public CatalogoItemDTORespuesta cambiarEstado(Long id, boolean activo) {
        CatalogoItemEntity item = buscar(id);
        item.setActivo(activo);
        return aRespuesta(repositorio.save(item));
    }

    @Override
    public CatalogoItemEntity obtenerItemValido(Long id, Catalogo catalogoEsperado, boolean exigirActivo, String nombreCampo) {
        CatalogoItemEntity item = repositorio.findById(id).orElseThrow(() ->
                new ReglaNegocioException("El " + nombreCampo + " con id " + id + " no existe"));
        if (item.getCatalogo() != catalogoEsperado) {
            throw new ReglaNegocioException("El " + nombreCampo + " debe pertenecer al catálogo " + catalogoEsperado
                    + " y el ítem " + id + " es de " + item.getCatalogo());
        }
        if (exigirActivo && !Boolean.TRUE.equals(item.getActivo())) {
            throw new ReglaNegocioException("El " + nombreCampo + " '" + item.getEtiqueta() + "' está inactivo");
        }
        return item;
    }

    private CatalogoItemEntity buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("No existe un ítem de catálogo con id " + id));
    }

    private CatalogoItemDTORespuesta aRespuesta(CatalogoItemEntity item) {
        return modelMapper.map(item, CatalogoItemDTORespuesta.class);
    }

    private void normalizar(CatalogoItemDTOPeticion p) {
        p.setCodigoSireci(Textos.limpiarMayusculas(p.getCodigoSireci()));
        p.setNombre(Textos.limpiarMayusculas(p.getNombre()));
    }
}
