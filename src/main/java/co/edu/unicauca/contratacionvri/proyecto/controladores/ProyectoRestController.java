package co.edu.unicauca.contratacionvri.proyecto.controladores;

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

import co.edu.unicauca.contratacionvri.comun.fachadaServicios.PaginaRespuesta;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTOPeticion.ProyectoActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTOPeticion.ProyectoCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTORespuesta.ProyectoDTORespuesta;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTORespuesta.ProyectoOpcionDTORespuesta;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.servicios.IProyectoServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/proyectos")
@Validated
@RequiredArgsConstructor
public class ProyectoRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final IProyectoServicio servicio;

    @PostMapping(consumes = JSON)
    public ResponseEntity<ProyectoDTORespuesta> crear(@Valid @RequestBody ProyectoCrearDTOPeticion peticion) {
        ProyectoDTORespuesta creado = servicio.crear(peticion);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getIdProyecto()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /** Solo proyectos activos, para los selectores del contrato. GET /api/proyectos/activos?texto=50 */
    @GetMapping("/activos")
    public ResponseEntity<List<ProyectoOpcionDTORespuesta>> listarActivos(
            @RequestParam(required = false) @Size(max = 400, message = "El texto admite máximo 400 caracteres") String texto) {
        return ResponseEntity.ok(servicio.listarActivos(texto));
    }

    @GetMapping("/{idProyecto}")
    public ResponseEntity<ProyectoDTORespuesta> consultarPorId(
            @PathVariable @Positive(message = "El código del proyecto debe ser positivo") Integer idProyecto) {
        return ResponseEntity.ok(servicio.consultarPorId(idProyecto));
    }

    /** GET /api/proyectos?id=50&nombre=agua&director=ruiz&activo=true&pagina=0&tamanio=20 */
    @GetMapping
    public ResponseEntity<PaginaRespuesta<ProyectoDTORespuesta>> buscar(
            @RequestParam(required = false) @Pattern(regexp = "^\\s*\\d{0,10}\\s*$", message = "El código solo admite dígitos") String id,
            @RequestParam(required = false) @Size(max = 400, message = "El nombre admite máximo 400 caracteres") String nombre,
            @RequestParam(required = false) @Size(max = 200, message = "El director admite máximo 200 caracteres") String director,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "La página no puede ser negativa") int pagina,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "El tamaño mínimo de página es 1")
            @Max(value = 100, message = "El tamaño máximo de página es 100") int tamanio) {
        return ResponseEntity.ok(servicio.buscar(id, nombre, director, activo, pagina, tamanio));
    }

    @PutMapping(path = "/{idProyecto}", consumes = JSON)
    public ResponseEntity<ProyectoDTORespuesta> actualizar(
            @PathVariable @Positive(message = "El código del proyecto debe ser positivo") Integer idProyecto,
            @Valid @RequestBody ProyectoActualizarDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.actualizar(idProyecto, peticion));
    }

    /** PATCH /api/proyectos/{idProyecto}/estado?activo=false. Reemplaza al borrado. */
    @PatchMapping("/{idProyecto}/estado")
    public ResponseEntity<ProyectoDTORespuesta> cambiarEstado(
            @PathVariable @Positive(message = "El código del proyecto debe ser positivo") Integer idProyecto,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(servicio.cambiarEstado(idProyecto, activo));
    }
}
