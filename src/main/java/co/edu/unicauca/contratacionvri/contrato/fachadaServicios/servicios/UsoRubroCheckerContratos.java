package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import org.springframework.stereotype.Component;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ContratoPresupuestoRepositorio;
import co.edu.unicauca.contratacionvri.rubro.fachadaServicios.servicios.UsoRubroChecker;
import lombok.RequiredArgsConstructor;

/** Uso de un rubro por los contratos (contrato_presupuesto.rubro_id). */
@Component
@RequiredArgsConstructor
public class UsoRubroCheckerContratos implements UsoRubroChecker {

    private final ContratoPresupuestoRepositorio presupuestoRepositorio;

    @Override
    public boolean existenContratosConRubro(Long rubroId) {
        return presupuestoRepositorio.existsByRubroId(rubroId);
    }
}
