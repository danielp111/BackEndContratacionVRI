package co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoPresupuestoEntity;

@Repository
public interface ContratoPresupuestoRepositorio extends JpaRepository<ContratoPresupuestoEntity, Long> {

    boolean existsByRubroId(Long rubroId);
}
