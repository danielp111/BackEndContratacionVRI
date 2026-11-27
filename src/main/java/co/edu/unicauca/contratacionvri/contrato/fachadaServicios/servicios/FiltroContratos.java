package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import java.time.LocalDate;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.EstadoContrato;
import lombok.Builder;
import lombok.Getter;

/**
 * Filtros de los listados de contratos; todos opcionales.
 * <ul>
 *   <li>{@code numero}: completo ("6.1-17.13/023"), solo el consecutivo ("23") o solo la serie ("6.1-17.13").</li>
 *   <li>{@code contratista}: prefijo del documento o parte del nombre.</li>
 *   <li>{@code texto}: parte del objeto.</li>
 *   <li>Rangos de fechas de inicio y de terminación: cada extremo es opcional e incluido.</li>
 *   <li>{@code conObservaciones}: verdadero = con observaciones pendientes; falso = sin pendientes.</li>
 * </ul>
 */
@Getter
@Builder(toBuilder = true)
public class FiltroContratos {

    private final String numero;
    private final Integer anio;
    private final EstadoContrato estado;
    private final Long tipoContratoId;
    private final Long claseContratoId;
    private final Long responsableId;
    private final String contratista;
    private final String texto;
    private final LocalDate inicioDesde;
    private final LocalDate inicioHasta;
    private final LocalDate terminacionDesde;
    private final LocalDate terminacionHasta;
    private final Boolean conObservaciones;
}
