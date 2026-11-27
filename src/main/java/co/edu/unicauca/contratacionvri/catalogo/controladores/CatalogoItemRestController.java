package co.edu.unicauca.contratacionvri.catalogo.controladores;

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

import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoItemActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoItemCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTORespuesta.CatalogoItemDTORespuesta;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.servicios.ICatalogoItemServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/** Ítems de un catálogo: el catálogo va en la URL, así un ítem nunca se mueve de catálogo. */
@RestController
@RequestMapping("/api/catalogos/{codigoCatalogo}/items")
@Validated
@RequiredArgsConstructor
public class CatalogoItemRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final ICatalogoItemServicio servicio;

    /** GET /api/catalogos/TIPO_IDENTIFICACION/items?activo=true → selector (orden y luego nombre). */
    @GetMapping
    public ResponseEntity<List<CatalogoItemDTORespuesta>> listar(@PathVariable String codigoCatalogo,
                                                                @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(servicio.listar(codigoCatalogo, activo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogoItemDTORespuesta> consultar(@PathVariable String codigoCatalogo,
            @PathVariable @Positive(message = "El id debe ser positivo") Long id) {
        return ResponseEntity.ok(servicio.consultar(codigoCatalogo, id));
    }

    @PostMapping(consumes = JSON)
    public ResponseEntity<CatalogoItemDTORespuesta> crear(@PathVariable String codigoCatalogo,
                                                         @Valid @RequestBody CatalogoItemCrearDTOPeticion peticion) {
        CatalogoItemDTORespuesta creado = servicio.crear(codigoCatalogo, peticion);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping(path = "/{id}", consumes = JSON)
    public ResponseEntity<CatalogoItemDTORespuesta> actualizar(@PathVariable String codigoCatalogo,
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody CatalogoItemActualizarDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.actualizar(codigoCatalogo, id, peticion));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<CatalogoItemDTORespuesta> cambiarEstado(@PathVariable String codigoCatalogo,
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(servicio.cambiarEstado(codigoCatalogo, id, activo));
    }
}
