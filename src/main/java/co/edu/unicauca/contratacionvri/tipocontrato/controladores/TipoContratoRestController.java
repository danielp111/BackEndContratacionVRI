package co.edu.unicauca.contratacionvri.tipocontrato.controladores;

import java.net.URI;
import java.util.List;

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

import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTOPeticion.TipoContratoDTOPeticion;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.TipoContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.TipoContratoOpcionDTORespuesta;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios.ITipoContratoServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tipos-contrato")
@Validated
@RequiredArgsConstructor
public class TipoContratoRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final ITipoContratoServicio servicio;

    /** GET /api/tipos-contrato?activo=true&serieId=3 (ambos opcionales). */
    @GetMapping
    public ResponseEntity<List<TipoContratoDTORespuesta>> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) @Positive(message = "La serie debe ser un identificador válido") Long serieId) {
        return ResponseEntity.ok(servicio.listar(activo, serieId));
    }

    /** Para los selectores del contrato: tipos activos cuya serie también está activa. */
    @GetMapping("/activos")
    public ResponseEntity<List<TipoContratoOpcionDTORespuesta>> listarDisponibles() {
        return ResponseEntity.ok(servicio.listarDisponibles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoContratoDTORespuesta> consultarPorId(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id) {
        return ResponseEntity.ok(servicio.consultarPorId(id));
    }

    @PostMapping(consumes = JSON)
    public ResponseEntity<TipoContratoDTORespuesta> crear(@Valid @RequestBody TipoContratoDTOPeticion peticion) {
        TipoContratoDTORespuesta creado = servicio.crear(peticion);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping(path = "/{id}", consumes = JSON)
    public ResponseEntity<TipoContratoDTORespuesta> actualizar(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody TipoContratoDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.actualizar(id, peticion));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<TipoContratoDTORespuesta> cambiarEstado(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, activo));
    }
}
