package co.edu.unicauca.contratacionvri.dependencia.controladores;

import java.net.URI;
import java.util.List;

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

import co.edu.unicauca.contratacionvri.dependencia.fachadaServicios.DTOPeticion.DependenciaDTOPeticion;
import co.edu.unicauca.contratacionvri.dependencia.fachadaServicios.DTORespuesta.DependenciaDTORespuesta;
import co.edu.unicauca.contratacionvri.dependencia.fachadaServicios.servicios.IDependenciaServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dependencias")
@Validated
@RequiredArgsConstructor
public class DependenciaRestController {

    private final IDependenciaServicio servicio;

    @GetMapping
    public ResponseEntity<List<DependenciaDTORespuesta>> listar(@RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(servicio.listar(activo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DependenciaDTORespuesta> consultarPorId(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id) {
        return ResponseEntity.ok(servicio.consultarPorId(id));
    }

    @PostMapping
    public ResponseEntity<DependenciaDTORespuesta> crear(@Valid @RequestBody DependenciaDTOPeticion peticion) {
        DependenciaDTORespuesta creada = servicio.crear(peticion);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DependenciaDTORespuesta> actualizar(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody DependenciaDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.actualizar(id, peticion));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<DependenciaDTORespuesta> cambiarEstado(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, activo));
    }
}
