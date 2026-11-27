package co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ModificacionEntity;

@Repository
public interface ModificacionRepositorio extends JpaRepository<ModificacionEntity, Long> {

    @Query("""
            SELECT m FROM ModificacionEntity m JOIN FETCH m.tipoAdicion
            WHERE m.contrato.id = :contratoId
            ORDER BY m.fecha, m.id
            """)
    List<ModificacionEntity> buscarDelContrato(@Param("contratoId") Long contratoId);

    Optional<ModificacionEntity> findByIdAndContratoId(Long id, Long contratoId);

    /** Filas [contratoId, suma de valores, suma de días] de los contratos dados. */
    @Query("""
            SELECT m.contrato.id, SUM(m.valor), SUM(m.dias) FROM ModificacionEntity m
            WHERE m.contrato.id IN :contratoIds
            GROUP BY m.contrato.id
            """)
    List<Object[]> totales(@Param("contratoIds") Collection<Long> contratoIds);
}
