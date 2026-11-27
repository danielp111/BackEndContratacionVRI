package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ModificacionDTORespuesta(Long id, Long tipoAdicionId, String tipoAdicionCodigo, String tipoAdicionEtiqueta,
                                       BigDecimal valor, Integer dias, LocalDate fecha, String documento, String observacion,
                                       List<String> advertencias) {
}
