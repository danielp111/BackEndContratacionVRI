package co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios;

/**
 * Responde si ya hay contratos que usan una serie o un tipo de contrato. Mientras existan, el
 * código de la serie, y el nombre y la serie del tipo, no se pueden cambiar.
 *
 * PUNTO DE CONEXIÓN: la implementación actual ({@link UsoTipoContratoCheckerProvisional}) es
 * provisional porque la tabla contrato aún no existe. Cuando se cree el dominio de contratos, se
 * reemplaza por una implementación que consulte contrato.tipo_contrato_id y la serie de su tipo.
 */
public interface UsoTipoContratoChecker {

    boolean existenContratosConSerie(Long serieId);

    boolean existenContratosConTipo(Long tipoContratoId);
}
