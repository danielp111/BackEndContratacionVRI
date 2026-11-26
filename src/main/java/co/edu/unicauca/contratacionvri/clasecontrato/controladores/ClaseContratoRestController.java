package co.edu.unicauca.contratacionvri.clasecontrato.controladores;

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

import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.AmbitoClase;
import co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.DTOPeticion.ClaseContratoActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.DTOPeticion.ClaseContratoCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.DTORespuesta.ClaseContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.servicios.IClaseContratoServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clases-contrato")
@Validated
@RequiredArgsConstructor
public class ClaseContratoRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final IClaseContratoServicio servicio;

    /** GET /api/clases-contrato?ambito=CONTRATO&activo=true (ambos opcionales; activo=true para selectores). */
    @GetMapping
    public ResponseEntity<List<ClaseContratoDTORespuesta>> listar(
            @RequestParam(required = false) AmbitoClase ambito,
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(servicio.listar(ambito, activo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaseContratoDTORespuesta> consultarPorId(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id) {
        return ResponseEntity.ok(servicio.consultarPorId(id));
    }

    @PostMapping(consumes = JSON)
    public ResponseEntity<ClaseContratoDTORespuesta> crear(@Valid @RequestBody ClaseContratoCrearDTOPeticion peticion) {
        ClaseContratoDTORespuesta creada = servicio.crear(peticion);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping(path = "/{id}", consumes = JSON)
    public ResponseEntity<ClaseContratoDTORespuesta> actualizar(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody ClaseContratoActualizarDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.actualizar(id, peticion));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ClaseContratoDTORespuesta> cambiarEstado(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, activo));
    }
}
