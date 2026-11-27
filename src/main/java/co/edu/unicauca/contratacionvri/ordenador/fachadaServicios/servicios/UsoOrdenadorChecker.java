package co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.servicios;

/**
 * Responde si ya hay contratos firmados con un periodo de ordenador del gasto. Mientras existan, el
 * tercero, el cargo y la fecha desde del periodo no se pueden cambiar (sí cerrarlo).
 *
 * PUNTO DE CONEXIÓN: la implementación actual ({@link UsoOrdenadorCheckerProvisional}) es provisional
 * porque la tabla contrato aún no existe. Cuando se cree el dominio de contratos, se reemplaza por una
 * implementación que consulte contrato.ordenador_gasto_id.
 */
public interface UsoOrdenadorChecker {

    boolean existenContratosConOrdenador(Long ordenadorGastoId);
}
