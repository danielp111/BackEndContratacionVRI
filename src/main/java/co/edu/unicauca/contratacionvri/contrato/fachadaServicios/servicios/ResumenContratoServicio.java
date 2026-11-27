package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ModificacionRepositorio;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.PagoRepositorio;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ResumenContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.CalculadoraResumen.Totales;
import lombok.RequiredArgsConstructor;

/**
 * Reúne los totales de modificaciones y pagos y calcula el resumen. Para los listados lo hace con dos
 * consultas agrupadas por página, no una por contrato.
 */
@Component
@RequiredArgsConstructor
public class ResumenContratoServicio {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final ModificacionRepositorio modificacionRepositorio;
    private final PagoRepositorio pagoRepositorio;

    public ResumenContratoDTORespuesta resumen(ContratoEntity contrato) {
        if (contrato.getId() == null) {
            return CalculadoraResumen.calcular(contrato, Totales.VACIOS, hoy());
        }
        return resumenes(List.of(contrato)).get(contrato.getId());
    }

    public Map<Long, ResumenContratoDTORespuesta> resumenes(Collection<ContratoEntity> contratos) {
        Map<Long, ResumenContratoDTORespuesta> resultado = new HashMap<>();
        if (contratos.isEmpty()) {
            return resultado;
        }
        List<Long> ids = contratos.stream().map(ContratoEntity::getId).toList();
        Map<Long, Object[]> modificaciones = porContrato(modificacionRepositorio.totales(ids));
        Map<Long, Object[]> pagos = porContrato(pagoRepositorio.totales(ids));
        LocalDate hoy = hoy();
        for (ContratoEntity c : contratos) {
            Object[] m = modificaciones.get(c.getId());
            Object[] p = pagos.get(c.getId());
            Totales t = new Totales(
                    m == null || m[1] == null ? BigDecimal.ZERO : (BigDecimal) m[1],
                    m == null || m[2] == null ? 0 : ((Number) m[2]).intValue(),
                    p == null || p[1] == null ? BigDecimal.ZERO : (BigDecimal) p[1],
                    p == null ? 0 : ((Number) p[2]).longValue());
            resultado.put(c.getId(), CalculadoraResumen.calcular(c, t, hoy));
        }
        return resultado;
    }

    private static Map<Long, Object[]> porContrato(List<Object[]> filas) {
        Map<Long, Object[]> mapa = new HashMap<>();
        for (Object[] fila : filas) {
            mapa.put((Long) fila[0], fila);
        }
        return mapa;
    }

    private static LocalDate hoy() {
        return LocalDate.now(ZONA);
    }
}
