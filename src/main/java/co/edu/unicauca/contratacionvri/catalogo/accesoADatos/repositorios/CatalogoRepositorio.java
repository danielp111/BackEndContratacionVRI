package co.edu.unicauca.contratacionvri.catalogo.accesoADatos.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoEntity;

@Repository
public interface CatalogoRepositorio extends JpaRepository<CatalogoEntity, Long> {

    @Query("""
            SELECT c FROM CatalogoEntity c
            WHERE (:activo IS NULL OR c.activo = :activo)
            ORDER BY c.nombre
            """)
    List<CatalogoEntity> buscar(@Param("activo") Boolean activo);

    Optional<CatalogoEntity> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
