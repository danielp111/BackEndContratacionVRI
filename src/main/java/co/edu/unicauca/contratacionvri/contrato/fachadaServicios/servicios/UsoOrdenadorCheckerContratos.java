package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import org.springframework.stereotype.Component;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ContratoRepositorio;
import co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.servicios.UsoOrdenadorChecker;
import lombok.RequiredArgsConstructor;

/** Uso de un periodo de ordenador del gasto por los contratos (contrato.ordenador_gasto_id). */
@Component
@RequiredArgsConstructor
public class UsoOrdenadorCheckerContratos implements UsoOrdenadorChecker {

    private final ContratoRepositorio contratoRepositorio;

    @Override
    public boolean existenContratosConOrdenador(Long ordenadorGastoId) {
        return contratoRepositorio.existsByOrdenadorGastoId(ordenadorGastoId);
    }
}
