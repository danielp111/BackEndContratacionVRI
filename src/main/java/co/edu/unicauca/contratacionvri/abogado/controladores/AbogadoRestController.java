package co.edu.unicauca.contratacionvri.abogado.controladores;

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

import co.edu.unicauca.contratacionvri.abogado.fachadaServicios.DTOPeticion.AbogadoDTOPeticion;
import co.edu.unicauca.contratacionvri.abogado.fachadaServicios.DTORespuesta.AbogadoDTORespuesta;
import co.edu.unicauca.contratacionvri.abogado.fachadaServicios.servicios.IAbogadoServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/abogados")
@Validated
@RequiredArgsConstructor
public class AbogadoRestController {

    private final IAbogadoServicio servicio;

    /** GET /api/abogados?activo=true|false (sin parámetro devuelve todos). */
    @GetMapping
    public ResponseEntity<List<AbogadoDTORespuesta>> listar(@RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(servicio.listar(activo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AbogadoDTORespuesta> consultarPorId(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id) {
        return ResponseEntity.ok(servicio.consultarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AbogadoDTORespuesta> crear(@Valid @RequestBody AbogadoDTOPeticion peticion) {
        AbogadoDTORespuesta creado = servicio.crear(peticion);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AbogadoDTORespuesta> actualizar(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody AbogadoDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.actualizar(id, peticion));
    }

    /** PATCH /api/abogados/{id}/estado?activo=false. Reemplaza al borrado: los abogados no se eliminan. */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<AbogadoDTORespuesta> cambiarEstado(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, activo));
    }
}
