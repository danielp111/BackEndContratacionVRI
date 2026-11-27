package co.edu.unicauca.contratacionvri.rubro.fachadaServicios.servicios;

/**
 * Responde si ya hay contratos que usan un rubro. Mientras existan, el código del rubro no se puede
 * cambiar.
 *
 * Implementación: contrato.UsoRubroCheckerContratos (consulta contrato_presupuesto.rubro_id).
 */
public interface UsoRubroChecker {

    boolean existenContratosConRubro(Long rubroId);
}
