package co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios;

import org.springframework.stereotype.Component;

import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios.ConsecutivoRepositorio;
import lombok.RequiredArgsConstructor;

/**
 * Implementación PROVISIONAL de {@link UsoTipoContratoChecker} mientras no exista la tabla contrato.
 *
 * <ul>
 *   <li>Serie: se considera usada si ya emitió algún número en cualquier año (consecutivo &gt; 0).
 *       Cubre también los números anulados, que llevan el código de la serie aunque no se usen.</li>
 *   <li>Tipo: el consecutivo es por serie, así que no dice qué tipo usó cada número. Responde
 *       {@code false} (sin contratos) hasta que el dominio de contratos lo reemplace.</li>
 * </ul>
 *
 * TODO(contrato): reemplazar por una implementación que consulte la tabla contrato.
 */
@Component
@RequiredArgsConstructor
public class UsoTipoContratoCheckerProvisional implements UsoTipoContratoChecker {

    private final ConsecutivoRepositorio consecutivoRepositorio;

    @Override
    public boolean existenContratosConSerie(Long serieId) {
        return consecutivoRepositorio.existsByIdSerieIdAndUltimoNumeroGreaterThan(serieId, 0);
    }

    @Override
    public boolean existenContratosConTipo(Long tipoContratoId) {
        return false;
    }
}
