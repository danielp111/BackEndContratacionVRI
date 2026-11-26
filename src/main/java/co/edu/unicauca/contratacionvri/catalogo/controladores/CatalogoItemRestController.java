package co.edu.unicauca.contratacionvri.catalogo.controladores;

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

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.Catalogo;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoItemDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTORespuesta.CatalogoItemDTORespuesta;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.servicios.ICatalogoItemServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalogo-items")
@Validated
@RequiredArgsConstructor
public class CatalogoItemRestController {

    private final ICatalogoItemServicio servicio;

    /** GET /api/catalogo-items?catalogo=TIPO_IDENTIFICACION&activo=true (ambos opcionales). */
    @GetMapping
    public ResponseEntity<List<CatalogoItemDTORespuesta>> listar(
            @RequestParam(required = false) Catalogo catalogo,
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(servicio.listar(catalogo, activo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogoItemDTORespuesta> consultarPorId(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id) {
        return ResponseEntity.ok(servicio.consultarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CatalogoItemDTORespuesta> crear(@Valid @RequestBody CatalogoItemDTOPeticion peticion) {
        CatalogoItemDTORespuesta creado = servicio.crear(peticion);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatalogoItemDTORespuesta> actualizar(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody CatalogoItemDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.actualizar(id, peticion));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<CatalogoItemDTORespuesta> cambiarEstado(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, activo));
    }
}
