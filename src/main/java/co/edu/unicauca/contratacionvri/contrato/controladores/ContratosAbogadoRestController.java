package co.edu.unicauca.contratacionvri.contrato.controladores;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.contratacionvri.comun.fachadaServicios.PaginaRespuesta;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.EstadoContrato;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ContratoResumenDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.FiltroContratos;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.IContratoServicio;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/**
 * Contratos de un abogado responsable. Vive en el dominio de contratos para que el dominio abogado
 * no dependa de él. Cuando exista el inicio de sesión, un ABOGADO solo podrá consultar los suyos.
 */
@RestController
@RequestMapping("/api/abogados/{abogadoId}/contratos")
@Validated
@RequiredArgsConstructor
public class ContratosAbogadoRestController {

    private final IContratoServicio servicio;

    /**
     * GET /api/abogados/4/contratos?inicioDesde=2026-01-01&inicioHasta=2026-03-31
     * &terminacionDesde=&terminacionHasta=&conObservaciones=true&estado=&anio=&pagina=0&tamanio=20.
     * {@code conObservaciones}: true = con observaciones pendientes, false = sin pendientes.
     */
    @GetMapping
    public ResponseEntity<PaginaRespuesta<ContratoResumenDTORespuesta>> buscar(
            @PathVariable @Positive(message = "El id del abogado debe ser positivo") Long abogadoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicioDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicioHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate terminacionDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate terminacionHasta,
            @RequestParam(required = false) Boolean conObservaciones,
            @RequestParam(required = false) EstadoContrato estado,
            @RequestParam(required = false) Integer anio,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "La página no puede ser negativa") int pagina,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "El tamaño mínimo de página es 1")
            @Max(value = 100, message = "El tamaño máximo de página es 100") int tamanio) {
        FiltroContratos filtro = FiltroContratos.builder()
                .inicioDesde(inicioDesde).inicioHasta(inicioHasta)
                .terminacionDesde(terminacionDesde).terminacionHasta(terminacionHasta)
                .conObservaciones(conObservaciones).estado(estado).anio(anio)
                .build();
        return ResponseEntity.ok(servicio.buscarDeAbogado(abogadoId, filtro, pagina, tamanio));
    }
}
