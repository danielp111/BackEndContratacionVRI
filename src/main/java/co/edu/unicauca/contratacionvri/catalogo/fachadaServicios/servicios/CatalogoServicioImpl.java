package co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.servicios;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoEntity;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.repositorios.CatalogoRepositorio;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTORespuesta.CatalogoDTORespuesta;
import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadYaExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogoServicioImpl implements ICatalogoServicio {

    private final CatalogoRepositorio repositorio;
    private final ModelMapper modelMapper;

    @Override
    public List<CatalogoDTORespuesta> listar(Boolean activo) {
        return repositorio.buscar(activo).stream().map(this::aRespuesta).toList();
    }

    @Override
    public CatalogoDTORespuesta consultarPorCodigo(String codigo) {
        return aRespuesta(buscarPorCodigo(codigo));
    }

    @Override
    @Transactional
    public CatalogoDTORespuesta crear(CatalogoCrearDTOPeticion peticion) {
        String codigo = Textos.limpiarMayusculas(peticion.getCodigo());
        if (repositorio.existsByCodigo(codigo)) {
            throw new EntidadYaExisteException("Ya existe el catálogo " + codigo);
        }
        CatalogoEntity catalogo = new CatalogoEntity(codigo, Textos.limpiar(peticion.getNombre()));
        catalogo.setDescripcion(Textos.limpiar(peticion.getDescripcion()));
        catalogo.setActivo(Boolean.TRUE);
        return aRespuesta(repositorio.save(catalogo));
    }

    @Override
    @Transactional
    public CatalogoDTORespuesta actualizar(String codigo, CatalogoActualizarDTOPeticion peticion) {
        CatalogoEntity catalogo = buscarPorCodigo(codigo);
        String codigoCuerpo = Textos.limpiarMayusculas(peticion.getCodigo());
        if (codigoCuerpo != null && !codigoCuerpo.equals(catalogo.getCodigo())) {
            throw new ReglaNegocioException("El código del catálogo no se puede cambiar (" + catalogo.getCodigo()
                    + " → " + codigoCuerpo + "): el programa lo usa");
        }
        catalogo.setNombre(Textos.limpiar(peticion.getNombre()));
        catalogo.setDescripcion(Textos.limpiar(peticion.getDescripcion()));
        return aRespuesta(repositorio.save(catalogo));
    }

    @Override
    @Transactional
    public CatalogoDTORespuesta cambiarEstado(String codigo, boolean activo) {
        CatalogoEntity catalogo = buscarPorCodigo(codigo);
        catalogo.setActivo(activo);
        return aRespuesta(repositorio.save(catalogo));
    }

    @Override
    public CatalogoEntity buscarPorCodigo(String codigo) {
        String limpio = Textos.limpiarMayusculas(codigo);
        return repositorio.findByCodigo(limpio)
                .orElseThrow(() -> new EntidadNoExisteException("No existe el catálogo " + limpio));
    }

    private CatalogoDTORespuesta aRespuesta(CatalogoEntity catalogo) {
        return modelMapper.map(catalogo, CatalogoDTORespuesta.class);
    }
}
