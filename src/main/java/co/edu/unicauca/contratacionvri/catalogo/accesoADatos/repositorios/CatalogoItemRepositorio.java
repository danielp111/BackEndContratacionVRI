package co.edu.unicauca.contratacionvri.catalogo.accesoADatos.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.Catalogo;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;

@Repository
public interface CatalogoItemRepositorio extends JpaRepository<CatalogoItemEntity, Long> {

    /** Filtros opcionales: un parámetro nulo no filtra. */
    @Query("""
            SELECT c FROM CatalogoItemEntity c
            WHERE (:catalogo IS NULL OR c.catalogo = :catalogo)
              AND (:activo IS NULL OR c.activo = :activo)
            ORDER BY c.catalogo, LENGTH(c.codigoSireci), c.codigoSireci
            """)
    List<CatalogoItemEntity> buscar(@Param("catalogo") Catalogo catalogo, @Param("activo") Boolean activo);

    boolean existsByCatalogoAndCodigoSireci(Catalogo catalogo, String codigoSireci);

    boolean existsByCatalogoAndCodigoSireciAndIdNot(Catalogo catalogo, String codigoSireci, Long id);
}
