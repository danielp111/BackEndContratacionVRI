package co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoUnspscEntity;

@Repository
public interface ContratoUnspscRepositorio extends JpaRepository<ContratoUnspscEntity, Long> {

    List<ContratoUnspscEntity> findByContratoIdOrderByCodigoUnspscAsc(Long contratoId);

    Optional<ContratoUnspscEntity> findByIdAndContratoId(Long id, Long contratoId);

    boolean existsByContratoIdAndCodigoUnspsc(Long contratoId, String codigoUnspsc);
}
