package co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoSeguimientoEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.RolSeguimiento;

@Repository
public interface ContratoSeguimientoRepositorio extends JpaRepository<ContratoSeguimientoEntity, Long> {

    @Query("""
            SELECT s FROM ContratoSeguimientoEntity s JOIN FETCH s.tercero
            WHERE s.contrato.id = :contratoId
            ORDER BY s.rol, s.desde, s.id
            """)
    List<ContratoSeguimientoEntity> buscarDelContrato(@Param("contratoId") Long contratoId);

    Optional<ContratoSeguimientoEntity> findByIdAndContratoId(Long id, Long contratoId);

    boolean existsByContratoIdAndRol(Long contratoId, RolSeguimiento rol);

    /** ¿Otro periodo del mismo rol en el contrato se cruza con [desde, hasta]? {@code hasta} nunca es nulo aquí. */
    @Query("""
            SELECT COUNT(s) FROM ContratoSeguimientoEntity s
            WHERE s.contrato.id = :contratoId AND s.rol = :rol
              AND (:idExcluir IS NULL OR s.id <> :idExcluir)
              AND s.desde <= :hasta
              AND (s.hasta IS NULL OR s.hasta >= :desde)
            """)
    long contarCruces(@Param("contratoId") Long contratoId,
                      @Param("rol") RolSeguimiento rol,
                      @Param("desde") LocalDate desde,
                      @Param("hasta") LocalDate hasta,
                      @Param("idExcluir") Long idExcluir);
}
