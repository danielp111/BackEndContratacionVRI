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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import co.edu.unicauca.contratacionvri.comun.fachadaServicios.PaginaRespuesta;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.EstadoContrato;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ContratoActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ContratoAnularDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ContratoCierreDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ContratoCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.CampoContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ContratoResumenDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ResumenContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.FiltroContratos;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.IContratoServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contratos")
@Validated
@RequiredArgsConstructor
public class ContratoRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final IContratoServicio servicio;

    /** Crea el contrato en trámite y devuelve su número. */
    @PostMapping(consumes = JSON)
    public ResponseEntity<ContratoDTORespuesta> crear(@Valid @RequestBody ContratoCrearDTOPeticion peticion) {
        ContratoDTORespuesta creado = servicio.crear(peticion);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * GET /api/contratos?numero=6.1-17.13/023&anio=2026&estado=EN_TRAMITE&tipoContratoId=1&claseContratoId=23
     * &responsableId=4&contratista=ana&texto=apoyo&pagina=0&tamanio=20 (todo opcional).
     */
    @GetMapping
    public ResponseEntity<PaginaRespuesta<ContratoResumenDTORespuesta>> buscar(
            @RequestParam(required = false) @Size(max = 40, message = "El número admite máximo 40 caracteres") String numero,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) EstadoContrato estado,
            @RequestParam(required = false) Long tipoContratoId,
            @RequestParam(required = false) Long claseContratoId,
            @RequestParam(required = false) Long responsableId,
            @RequestParam(required = false) @Size(max = 200, message = "El contratista admite máximo 200 caracteres") String contratista,
            @RequestParam(required = false) @Size(max = 200, message = "El texto admite máximo 200 caracteres") String texto,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "La página no puede ser negativa") int pagina,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "El tamaño mínimo de página es 1")
            @Max(value = 100, message = "El tamaño máximo de página es 100") int tamanio) {
        FiltroContratos filtro = FiltroContratos.builder()
                .numero(numero).anio(anio).estado(estado).tipoContratoId(tipoContratoId)
                .claseContratoId(claseContratoId).responsableId(responsableId)
                .contratista(contratista).texto(texto)
                .build();
        return ResponseEntity.ok(servicio.buscar(filtro, pagina, tamanio));
    }

    /** Campos del contrato que admiten observaciones, con su etiqueta. */
    @GetMapping("/campos")
    public ResponseEntity<List<CampoContratoDTORespuesta>> listarCampos() {
        return ResponseEntity.ok(servicio.listarCampos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContratoDTORespuesta> consultarPorId(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id) {
        return ResponseEntity.ok(servicio.consultarPorId(id));
    }

    /** Solo los campos calculados: valor final, plazo total, saldo, avances, semestre en que finaliza... */
    @GetMapping("/{id}/resumen")
    public ResponseEntity<ResumenContratoDTORespuesta> consultarResumen(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id) {
        return ResponseEntity.ok(servicio.consultarResumen(id));
    }

    /** Datos del abogado (formulario completo). */
    @PutMapping(path = "/{id}", consumes = JSON)
    public ResponseEntity<ContratoDTORespuesta> actualizar(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody ContratoActualizarDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.actualizar(id, peticion));
    }

    /** Datos del financiero: terminación, acta final y certificación de pago. */
    @PatchMapping(path = "/{id}/cierre", consumes = JSON)
    public ResponseEntity<ContratoDTORespuesta> registrarCierre(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody ContratoCierreDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.registrarCierre(id, peticion));
    }

    /** PATCH /api/contratos/{id}/anular con {"motivo": "..."}. Solo en trámite. */
    @PatchMapping(path = "/{id}/anular", consumes = JSON)
    public ResponseEntity<ContratoDTORespuesta> anular(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id,
            @Valid @RequestBody ContratoAnularDTOPeticion peticion) {
        return ResponseEntity.ok(servicio.anular(id, peticion.getMotivo()));
    }
}
