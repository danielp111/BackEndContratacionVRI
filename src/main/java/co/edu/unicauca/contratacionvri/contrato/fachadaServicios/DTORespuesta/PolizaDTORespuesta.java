package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta;

import java.time.LocalDate;

public record PolizaDTORespuesta(Long id, String numero, Long aseguradoraId, String aseguradoraNombre, LocalDate vigenciaHasta) {
}
