package co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.PolizaEntity;

@Repository
public interface PolizaRepositorio extends JpaRepository<PolizaEntity, Long> {

    @Query("""
            SELECT p FROM PolizaEntity p JOIN FETCH p.aseguradora
            WHERE p.contrato.id = :contratoId
            ORDER BY p.id
            """)
    List<PolizaEntity> buscarDelContrato(@Param("contratoId") Long contratoId);

    Optional<PolizaEntity> findByIdAndContratoId(Long id, Long contratoId);

    boolean existsByContratoIdAndNumero(Long contratoId, String numero);

    boolean existsByContratoIdAndNumeroAndIdNot(Long contratoId, String numero, Long id);
}
