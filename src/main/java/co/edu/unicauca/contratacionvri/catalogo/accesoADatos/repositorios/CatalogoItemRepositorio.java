package co.edu.unicauca.contratacionvri.catalogo.accesoADatos.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;

@Repository
public interface CatalogoItemRepositorio extends JpaRepository<CatalogoItemEntity, Long> {

    /** Ítems de un catálogo, filtro opcional por estado. Orden de los selectores: orden y luego nombre. */
    @Query("""
            SELECT i FROM CatalogoItemEntity i
            WHERE i.catalogo.id = :catalogoId
              AND (:activo IS NULL OR i.activo = :activo)
            ORDER BY i.orden, i.nombre
            """)
    List<CatalogoItemEntity> buscar(@Param("catalogoId") Long catalogoId, @Param("activo") Boolean activo);

    Optional<CatalogoItemEntity> findByIdAndCatalogoId(Long id, Long catalogoId);

    boolean existsByCatalogoIdAndCodigo(Long catalogoId, String codigo);

    boolean existsByCatalogoIdAndNombreIgnoreCase(Long catalogoId, String nombre);

    boolean existsByCatalogoIdAndNombreIgnoreCaseAndIdNot(Long catalogoId, String nombre, Long id);
}
