package co.edu.unicauca.contratacionvri.ordenador.controladores;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.DTOPeticion.CerrarPeriodoDTOPeticion;
import co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.DTOPeticion.OrdenadorGastoDTOPeticion;
import co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.DTORespuesta.OrdenadorGastoDTORespuesta;
import co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.servicios.IOrdenadorGastoServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ordenadores-gasto")
@Validated
@RequiredArgsConstructor
public class OrdenadorGastoRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final IOrdenadorGastoServicio servicio;

    /**
     * GET /api/ordenadores-gasto?vigenteEn=2026-03-01&texto=pino&activo=true (todo opcional).
     * {@code vigenteEn} es para los selectores del contrato: periodos activos que cubren esa fecha.
     */
    @GetMapping
    public ResponseEntity<List<OrdenadorGastoDTORespuesta>> listar(
            @RequestParam(required = false) @Size(max = 200, message = "El texto admite máximo 200 caracteres") String texto,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vigenteEn) {
        return ResponseEntity.ok(servicio.listar(texto, activo, vigenteEn));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenadorGastoDTORespuesta> consultarPorId(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id) {
        return ResponseEntity.ok(servicio.consultarPorId(id));
    }

    @PostMapping(consumes = JSON)
    public ResponseEntity<OrdenadorGastoDTORespuesta> crear(@Valid @RequestBody OrdenadorGastoDTOPeticion peticion) {
        OrdenadorGastoDTORespuesta creado = servicio.crear(peticion);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping(path = "/{id}", consumes = JSON)
    public ResponseEntity<OrdenadorGastoDTORespuesta> actualizar(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody OrdenadorGastoDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.actualizar(id, peticion));
    }

    /** PATCH /api/ordenadores-gasto/{id}/cerrar con {"fechaHasta": "2026-06-30"}. */
    @PatchMapping(path = "/{id}/cerrar", consumes = JSON)
    public ResponseEntity<OrdenadorGastoDTORespuesta> cerrarPeriodo(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody CerrarPeriodoDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.cerrarPeriodo(id, peticion.getFechaHasta()));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrdenadorGastoDTORespuesta> cambiarEstado(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, activo));
    }
}
