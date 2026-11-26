package co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.SerieDocumentalEntity;

@Repository
public interface SerieDocumentalRepositorio extends JpaRepository<SerieDocumentalEntity, Long> {

    @Query("""
            SELECT s FROM SerieDocumentalEntity s
            WHERE (:activo IS NULL OR s.activo = :activo)
            ORDER BY s.codigo
            """)
    List<SerieDocumentalEntity> buscar(@Param("activo") Boolean activo);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
}
