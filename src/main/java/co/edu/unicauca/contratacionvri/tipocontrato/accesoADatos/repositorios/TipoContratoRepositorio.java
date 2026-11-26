package co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.TipoContratoEntity;

@Repository
public interface TipoContratoRepositorio extends JpaRepository<TipoContratoEntity, Long> {

    @Query("""
            SELECT t FROM TipoContratoEntity t JOIN FETCH t.serie s
            WHERE (:activo IS NULL OR t.activo = :activo)
              AND (:serieId IS NULL OR s.id = :serieId)
            ORDER BY t.nombre
            """)
    List<TipoContratoEntity> buscar(@Param("activo") Boolean activo, @Param("serieId") Long serieId);

    /** Para los selectores del contrato: el tipo y su serie deben estar activos. */
    @Query("""
            SELECT t FROM TipoContratoEntity t JOIN FETCH t.serie s
            WHERE t.activo = true AND s.activo = true
            ORDER BY t.nombre
            """)
    List<TipoContratoEntity> buscarDisponibles();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
