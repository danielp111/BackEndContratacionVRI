package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import org.springframework.stereotype.Component;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ContratoRepositorio;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios.ConsecutivoRepositorio;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios.UsoTipoContratoChecker;
import lombok.RequiredArgsConstructor;

/**
 * Uso de series y tipos por los contratos. Una serie también cuenta como usada si ya emitió algún
 * número (consecutivo &gt; 0): cubre los números emitidos antes de que existiera la tabla contrato,
 * como los de la carga inicial.
 */
@Component
@RequiredArgsConstructor
public class UsoTipoContratoCheckerContratos implements UsoTipoContratoChecker {

    private final ContratoRepositorio contratoRepositorio;
    private final ConsecutivoRepositorio consecutivoRepositorio;

    @Override
    public boolean existenContratosConSerie(Long serieId) {
        return contratoRepositorio.existsBySerieId(serieId)
                || consecutivoRepositorio.existsByIdSerieIdAndUltimoNumeroGreaterThan(serieId, 0);
    }

    @Override
    public boolean existenContratosConTipo(Long tipoContratoId) {
        return contratoRepositorio.existsByTipoContratoId(tipoContratoId);
    }
}
