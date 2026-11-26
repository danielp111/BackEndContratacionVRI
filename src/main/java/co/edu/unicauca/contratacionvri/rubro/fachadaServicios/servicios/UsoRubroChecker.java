package co.edu.unicauca.contratacionvri.rubro.fachadaServicios.servicios;

/**
 * Responde si ya hay contratos que usan un rubro. Mientras existan, el código del rubro no se puede
 * cambiar.
 *
 * PUNTO DE CONEXIÓN: la implementación actual ({@link UsoRubroCheckerProvisional}) es provisional
 * porque la tabla contrato aún no existe. Cuando se cree el dominio de contratos, se reemplaza por
 * una implementación que consulte contrato_presupuesto.rubro_id.
 */
public interface UsoRubroChecker {

    boolean existenContratosConRubro(Long rubroId);
}
