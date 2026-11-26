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

import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTOPeticion.SerieDocumentalDTOPeticion;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.ConsecutivoDTORespuesta;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.SerieDocumentalDTORespuesta;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios.INumeracionServicio;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios.ISerieDocumentalServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/series-documentales")
@Validated
@RequiredArgsConstructor
public class SerieDocumentalRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final ISerieDocumentalServicio servicio;
    private final INumeracionServicio numeracionServicio;

    @GetMapping
    public ResponseEntity<List<SerieDocumentalDTORespuesta>> listar(@RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(servicio.listar(activo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SerieDocumentalDTORespuesta> consultarPorId(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id) {
        return ResponseEntity.ok(servicio.consultarPorId(id));
    }

    @PostMapping(consumes = JSON)
    public ResponseEntity<SerieDocumentalDTORespuesta> crear(@Valid @RequestBody SerieDocumentalDTOPeticion peticion) {
        SerieDocumentalDTORespuesta creada = servicio.crear(peticion);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping(path = "/{id}", consumes = JSON)
    public ResponseEntity<SerieDocumentalDTORespuesta> actualizar(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody SerieDocumentalDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.actualizar(id, peticion));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<SerieDocumentalDTORespuesta> cambiarEstado(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, activo));
    }

    /** GET /api/series-documentales/{id}/consecutivos/2026 → último número emitido y el siguiente. */
    @GetMapping("/{id}/consecutivos/{anio}")
    public ResponseEntity<ConsecutivoDTORespuesta> consultarConsecutivo(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @PathVariable @Min(value = INumeracionServicio.ANIO_MINIMO, message = "El año debe ser 2000 o posterior")
            @Max(value = INumeracionServicio.ANIO_MAXIMO, message = "El año debe ser 2100 o anterior") Integer anio) {
        return ResponseEntity.ok(numeracionServicio.consultarConsecutivo(id, anio));
    }
}
