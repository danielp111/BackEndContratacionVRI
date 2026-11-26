package co.edu.unicauca.contratacionvri.rubro.controladores;

import java.net.URI;

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

import co.edu.unicauca.contratacionvri.comun.fachadaServicios.PaginaRespuesta;
import co.edu.unicauca.contratacionvri.rubro.fachadaServicios.DTOPeticion.RubroDTOPeticion;
import co.edu.unicauca.contratacionvri.rubro.fachadaServicios.DTORespuesta.RubroDTORespuesta;
import co.edu.unicauca.contratacionvri.rubro.fachadaServicios.servicios.IRubroServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rubros")
@Validated
@RequiredArgsConstructor
public class RubroRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final IRubroServicio servicio;

    /**
     * GET /api/rubros?texto=1037&activo=true&pagina=0&tamanio=20 (todo opcional). {@code texto} busca
     * por prefijo del código o por contenido del nombre.
     */
    @GetMapping
    public ResponseEntity<PaginaRespuesta<RubroDTORespuesta>> buscar(
            @RequestParam(required = false) @Size(max = 300, message = "El texto admite máximo 300 caracteres") String texto,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "La página no puede ser negativa") int pagina,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "El tamaño mínimo de página es 1")
            @Max(value = 100, message = "El tamaño máximo de página es 100") int tamanio) {
        return ResponseEntity.ok(servicio.buscar(texto, activo, pagina, tamanio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RubroDTORespuesta> consultarPorId(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id) {
        return ResponseEntity.ok(servicio.consultarPorId(id));
    }

    @PostMapping(consumes = JSON)
    public ResponseEntity<RubroDTORespuesta> crear(@Valid @RequestBody RubroDTOPeticion peticion) {
        RubroDTORespuesta creado = servicio.crear(peticion);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping(path = "/{id}", consumes = JSON)
    public ResponseEntity<RubroDTORespuesta> actualizar(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody RubroDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.actualizar(id, peticion));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<RubroDTORespuesta> cambiarEstado(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, activo));
    }
}
