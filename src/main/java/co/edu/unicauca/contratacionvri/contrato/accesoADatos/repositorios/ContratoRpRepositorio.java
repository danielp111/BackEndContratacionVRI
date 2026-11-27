package co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios;

import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoRpEntity;

/** RP del contrato. */
@Repository
public interface ContratoRpRepositorio extends DocumentoPresupuestalRepositorio<ContratoRpEntity> {
}
