package co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios;

/**
 * Responde si ya hay contratos que usan una serie o un tipo de contrato. Mientras existan, el
 * código de la serie, y el nombre y la serie del tipo, no se pueden cambiar.
 *
 * Implementación: contrato.UsoTipoContratoCheckerContratos (consulta la tabla contrato).
 */
public interface UsoTipoContratoChecker {

    boolean existenContratosConSerie(Long serieId);

    boolean existenContratosConTipo(Long tipoContratoId);
}
