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

import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion.CatalogoCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTORespuesta.CatalogoDTORespuesta;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.servicios.ICatalogoServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalogos")
@Validated
@RequiredArgsConstructor
public class CatalogoRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final ICatalogoServicio servicio;

    @GetMapping
    public ResponseEntity<List<CatalogoDTORespuesta>> listar(@RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(servicio.listar(activo));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<CatalogoDTORespuesta> consultar(@PathVariable String codigo) {
        return ResponseEntity.ok(servicio.consultarPorCodigo(codigo));
    }

    @PostMapping(consumes = JSON)
    public ResponseEntity<CatalogoDTORespuesta> crear(@Valid @RequestBody CatalogoCrearDTOPeticion peticion) {
        CatalogoDTORespuesta creado = servicio.crear(peticion);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}").buildAndExpand(creado.getCodigo()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping(path = "/{codigo}", consumes = JSON)
    public ResponseEntity<CatalogoDTORespuesta> actualizar(@PathVariable String codigo,
                                                          @Valid @RequestBody CatalogoActualizarDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.actualizar(codigo, peticion));
    }

    @PatchMapping("/{codigo}/estado")
    public ResponseEntity<CatalogoDTORespuesta> cambiarEstado(@PathVariable String codigo, @RequestParam boolean activo) {
        return ResponseEntity.ok(servicio.cambiarEstado(codigo, activo));
    }
}
