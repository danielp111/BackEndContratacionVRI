package co.edu.unicauca.contratacionvri.usuario.controladores;

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

import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.Rol;
import co.edu.unicauca.contratacionvri.usuario.fachadaServicios.DTOPeticion.UsuarioDTOPeticion;
import co.edu.unicauca.contratacionvri.usuario.fachadaServicios.DTORespuesta.UsuarioDTORespuesta;
import co.edu.unicauca.contratacionvri.usuario.fachadaServicios.servicios.IUsuarioServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@Validated
@RequiredArgsConstructor
public class UsuarioRestController {

    private final IUsuarioServicio servicio;

    /** GET /api/usuarios?rol=ABOGADO&activo=true (ambos opcionales). */
    @GetMapping
    public ResponseEntity<List<UsuarioDTORespuesta>> listar(
            @RequestParam(required = false) Rol rol,
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(servicio.listar(rol, activo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTORespuesta> consultarPorId(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id) {
        return ResponseEntity.ok(servicio.consultarPorId(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTORespuesta> crear(@Valid @RequestBody UsuarioDTOPeticion peticion) {
        UsuarioDTORespuesta creado = servicio.crear(peticion);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTORespuesta> actualizar(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody UsuarioDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.actualizar(id, peticion));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioDTORespuesta> cambiarEstado(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, activo));
    }
}
