package co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.PagoEntity;

@Repository
public interface PagoRepositorio extends JpaRepository<PagoEntity, Long> {

    List<PagoEntity> findByContratoIdOrderByNumeroAsc(Long contratoId);

    Optional<PagoEntity> findByIdAndContratoId(Long id, Long contratoId);

    /** Último número de pago del contrato (0 si no tiene). */
    @Query("SELECT COALESCE(MAX(p.numero), 0) FROM PagoEntity p WHERE p.contrato.id = :contratoId")
    int ultimoNumero(@Param("contratoId") Long contratoId);

    long countByContratoId(Long contratoId);

    /** Filas [contratoId, suma de valores, cantidad] de los contratos dados. */
    @Query("""
            SELECT p.contrato.id, SUM(p.valor), COUNT(p) FROM PagoEntity p
            WHERE p.contrato.id IN :contratoIds
            GROUP BY p.contrato.id
            """)
    List<Object[]> totales(@Param("contratoIds") Collection<Long> contratoIds);
}
