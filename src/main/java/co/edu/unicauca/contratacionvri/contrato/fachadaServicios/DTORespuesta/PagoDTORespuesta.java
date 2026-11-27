package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** {@code saldo}: el saldo del contrato después de este cambio. */
public record PagoDTORespuesta(Long id, Integer numero, BigDecimal valor, LocalDate fecha, Long registradoPorId,
                               BigDecimal saldo, List<String> advertencias) {
}
