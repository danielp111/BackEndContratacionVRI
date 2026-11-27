package co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios;

import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoCdpEntity;

/** CDP del contrato. */
@Repository
public interface ContratoCdpRepositorio extends DocumentoPresupuestalRepositorio<ContratoCdpEntity> {
}
