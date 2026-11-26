package co.edu.unicauca.contratacionvri.rubro.fachadaServicios.servicios;

import org.springframework.stereotype.Component;

/**
 * Implementación PROVISIONAL de {@link UsoRubroChecker}: mientras no exista la tabla contrato,
 * ningún rubro tiene contratos y todos los códigos son editables.
 *
 * TODO(contrato): reemplazar por una implementación que consulte contrato_presupuesto.
 */
@Component
public class UsoRubroCheckerProvisional implements UsoRubroChecker {

    @Override
    public boolean existenContratosConRubro(Long rubroId) {
        return false;
    }
}
