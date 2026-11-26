package co.edu.unicauca.contratacionvri.tercero.controladores;

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
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.Naturaleza;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTOPeticion.PersonaJuridicaDTOPeticion;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTOPeticion.PersonaNaturalDTOPeticion;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta.PersonaJuridicaDTORespuesta;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta.PersonaNaturalDTORespuesta;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta.TerceroDTORespuesta;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta.TerceroResumenDTORespuesta;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.servicios.ITerceroServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/terceros")
@Validated
@RequiredArgsConstructor
public class TerceroRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final ITerceroServicio servicio;

    @PostMapping(path = "/naturales", consumes = JSON)
    public ResponseEntity<PersonaNaturalDTORespuesta> crearPersonaNatural(
            @Valid @RequestBody PersonaNaturalDTOPeticion peticion) {
        PersonaNaturalDTORespuesta creada = servicio.crearPersonaNatural(peticion);
        return ResponseEntity.created(ubicacion(creada.getId())).body(creada);
    }

    @PostMapping(path = "/juridicas", consumes = JSON)
    public ResponseEntity<PersonaJuridicaDTORespuesta> crearPersonaJuridica(
            @Valid @RequestBody PersonaJuridicaDTOPeticion peticion) {
        PersonaJuridicaDTORespuesta creada = servicio.crearPersonaJuridica(peticion);
        return ResponseEntity.created(ubicacion(creada.getId())).body(creada);
    }

    /** Devuelve la forma natural o jurídica; el campo "naturaleza" indica cuál. */
    @GetMapping("/{id}")
    public ResponseEntity<TerceroDTORespuesta> consultarPorId(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id) {
        return ResponseEntity.ok(servicio.consultarPorId(id));
    }

    /**
     * GET /api/terceros?documento=900&nombre=ana&naturaleza=NATURAL&activo=true&pagina=0&tamanio=20
     * Todos los filtros son opcionales. Orden fijo por nombre.
     */
    @GetMapping
    public ResponseEntity<PaginaRespuesta<TerceroResumenDTORespuesta>> buscar(
            @RequestParam(required = false) @Size(max = 20, message = "El documento admite máximo 20 caracteres") String documento,
            @RequestParam(required = false) @Size(max = 200, message = "El nombre admite máximo 200 caracteres") String nombre,
            @RequestParam(required = false) Naturaleza naturaleza,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "La página no puede ser negativa") int pagina,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "El tamaño mínimo de página es 1")
            @Max(value = 100, message = "El tamaño máximo de página es 100") int tamanio) {
        return ResponseEntity.ok(servicio.buscar(documento, nombre, naturaleza, activo, pagina, tamanio));
    }

    @PutMapping(path = "/naturales/{id}", consumes = JSON)
    public ResponseEntity<PersonaNaturalDTORespuesta> actualizarPersonaNatural(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody PersonaNaturalDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.actualizarPersonaNatural(id, peticion));
    }

    @PutMapping(path = "/juridicas/{id}", consumes = JSON)
    public ResponseEntity<PersonaJuridicaDTORespuesta> actualizarPersonaJuridica(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody PersonaJuridicaDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.actualizarPersonaJuridica(id, peticion));
    }

    /** PATCH /api/terceros/{id}/estado?activo=false. Reemplaza al borrado. */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TerceroDTORespuesta> cambiarEstado(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, activo));
    }

    private URI ubicacion(Long id) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/terceros/{id}").buildAndExpand(id).toUri();
    }
}
