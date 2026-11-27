package co.edu.unicauca.contratacionvri.contrato.controladores;

import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ObservacionDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ResolverObservacionDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ConteoObservacionesDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ObservacionDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.IContratoObservacionServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contratos/{contratoId}/observaciones")
@Validated
@RequiredArgsConstructor
public class ContratoObservacionRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final IContratoObservacionServicio servicio;

    /** POST con {"campo": "VALOR_SIN_IVA", "texto": "..."}. */
    @PostMapping(consumes = JSON)
    public ResponseEntity<ObservacionDTORespuesta> agregar(
            @PathVariable @Positive(message = "El id del contrato debe ser positivo") Long contratoId,
            @Valid @RequestBody ObservacionDTOPeticion peticion) {
        ObservacionDTORespuesta creada = servicio.agregar(contratoId, peticion);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .queryParam("campo", creada.getCampo()).build().toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    /** GET ?campo=VALOR_SIN_IVA&resuelta=false (ambos opcionales). */
    @GetMapping
    public ResponseEntity<List<ObservacionDTORespuesta>> listar(
            @PathVariable @Positive(message = "El id del contrato debe ser positivo") Long contratoId,
            @RequestParam(required = false) @Size(max = 40, message = "El campo admite máximo 40 caracteres") String campo,
            @RequestParam(required = false) Boolean resuelta) {
        return ResponseEntity.ok(servicio.listar(contratoId, campo, resuelta));
    }

    /** Cantidad de observaciones, en total y por campo, con las pendientes. */
    @GetMapping("/conteo")
    public ResponseEntity<ConteoObservacionesDTORespuesta> contar(
            @PathVariable @Positive(message = "El id del contrato debe ser positivo") Long contratoId) {
        return ResponseEntity.ok(servicio.contar(contratoId));
    }

    /** PATCH con {"respuesta": "..."} (opcional; el cuerpo puede ser {}). */
    @PatchMapping(path = "/{observacionId}/resolver", consumes = JSON)
    public ResponseEntity<ObservacionDTORespuesta> resolver(
            @PathVariable @Positive(message = "El id del contrato debe ser positivo") Long contratoId,
            @PathVariable @Positive(message = "El id de la observación debe ser positivo") Long observacionId,
            @Valid @RequestBody ResolverObservacionDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.resolver(contratoId, observacionId, peticion.getRespuesta()));
    }
}
