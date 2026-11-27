package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** CDP o RP. {@code advertencias}: avisos que no impiden guardar (p. ej. los CDP no cubren el valor final). */
public record DocumentoPresupuestalDTORespuesta(Long id, String numero, BigDecimal valor, LocalDate fecha, List<String> advertencias) {
}
