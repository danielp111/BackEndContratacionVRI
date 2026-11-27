package co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.servicios;

import org.springframework.stereotype.Component;

/**
 * Implementación PROVISIONAL de {@link UsoOrdenadorChecker}: mientras no exista la tabla contrato,
 * ningún periodo tiene contratos.
 *
 * TODO(contrato): reemplazar por una implementación que consulte contrato.ordenador_gasto_id.
 */
@Component
public class UsoOrdenadorCheckerProvisional implements UsoOrdenadorChecker {

    @Override
    public boolean existenContratosConOrdenador(Long ordenadorGastoId) {
        return false;
    }
}
