package co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.servicios;

/**
 * Responde si ya hay contratos firmados con un periodo de ordenador del gasto. Mientras existan, el
 * tercero, el cargo y la fecha desde del periodo no se pueden cambiar (sí cerrarlo).
 *
 * Implementación: contrato.UsoOrdenadorCheckerContratos (consulta contrato.ordenador_gasto_id).
 */
public interface UsoOrdenadorChecker {

    boolean existenContratosConOrdenador(Long ordenadorGastoId);
}
