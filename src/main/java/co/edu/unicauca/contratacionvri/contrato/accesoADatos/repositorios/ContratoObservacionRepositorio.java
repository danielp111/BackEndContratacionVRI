package co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.CampoContrato;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoObservacionEntity;

@Repository
public interface ContratoObservacionRepositorio extends JpaRepository<ContratoObservacionEntity, Long> {

    /** Observaciones del contrato, la más reciente primero. Filtros opcionales por campo y por resuelta. */
    @Query("""
            SELECT o FROM ContratoObservacionEntity o
            WHERE o.contrato.id = :contratoId
              AND (:campo IS NULL OR o.campo = :campo)
              AND (:resuelta IS NULL OR o.resuelta = :resuelta)
            ORDER BY o.creadoEn DESC, o.id DESC
            """)
    List<ContratoObservacionEntity> buscar(@Param("contratoId") Long contratoId,
                                           @Param("campo") CampoContrato campo,
                                           @Param("resuelta") Boolean resuelta);

    Optional<ContratoObservacionEntity> findByIdAndContratoId(Long id, Long contratoId);

    /** Filas [campo, resuelta, cantidad] para el conteo por campo. */
    @Query("""
            SELECT o.campo, o.resuelta, COUNT(o) FROM ContratoObservacionEntity o
            WHERE o.contrato.id = :contratoId
            GROUP BY o.campo, o.resuelta
            """)
    List<Object[]> contarPorCampo(@Param("contratoId") Long contratoId);

    /** Filas [contratoId, pendientes] de los contratos de una página del listado. */
    @Query("""
            SELECT o.contrato.id, COUNT(o) FROM ContratoObservacionEntity o
            WHERE o.contrato.id IN :contratoIds AND o.resuelta = false
            GROUP BY o.contrato.id
            """)
    List<Object[]> contarPendientes(@Param("contratoIds") Collection<Long> contratoIds);

    long countByContratoIdAndResueltaFalse(Long contratoId);
}
