package co.edu.unicauca.contratacionvri.contrato.controladores;

import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.CerrarSeguimientoDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.DocumentoPresupuestalDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ModificacionDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.PagoDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.PolizaDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.SeguimientoDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.UnspscDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.DocumentoPresupuestalDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ModificacionDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.PagoDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.PolizaDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.SeguimientoDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.UnspscDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.DocumentoPresupuestalServicio;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.ModificacionServicio;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.PagoServicio;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.SeguimientoServicio;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.TipoDocumentoPresupuestal;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.UnspscPolizaServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/**
 * Detalle del contrato (1 contrato : N filas): CDP, RP, UNSPSC, supervisores e interventores,
 * modificaciones, pagos y pólizas. Todas las rutas cuelgan de /api/contratos/{contratoId}.
 */
@RestController
@RequestMapping("/api/contratos/{contratoId}")
@Validated
@RequiredArgsConstructor
public class ContratoDetalleRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String ID_CONTRATO = "El id del contrato debe ser positivo";
    private static final String ID_FILA = "El id debe ser positivo";

    private final DocumentoPresupuestalServicio documentoServicio;
    private final UnspscPolizaServicio unspscPolizaServicio;
    private final SeguimientoServicio seguimientoServicio;
    private final ModificacionServicio modificacionServicio;
    private final PagoServicio pagoServicio;

    // ------------------------------------------------------------------ CDP y RP (/cdp, /rp)

    @GetMapping("/{tipo:cdp|rp}")
    public ResponseEntity<List<DocumentoPresupuestalDTORespuesta>> listarDocumentos(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @PathVariable String tipo) {
        return ResponseEntity.ok(documentoServicio.listar(TipoDocumentoPresupuestal.desdeRuta(tipo), contratoId));
    }

    @PostMapping(path = "/{tipo:cdp|rp}", consumes = JSON)
    public ResponseEntity<DocumentoPresupuestalDTORespuesta> crearDocumento(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @PathVariable String tipo,
            @Valid @RequestBody DocumentoPresupuestalDTOPeticion peticion) {
        DocumentoPresupuestalDTORespuesta creado = documentoServicio.crear(TipoDocumentoPresupuestal.desdeRuta(tipo), contratoId, peticion);
        return creado(creado.id(), creado);
    }

    @PutMapping(path = "/{tipo:cdp|rp}/{id}", consumes = JSON)
    public ResponseEntity<DocumentoPresupuestalDTORespuesta> actualizarDocumento(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @PathVariable String tipo,
            @PathVariable @Positive(message = ID_FILA) Long id, @Valid @RequestBody DocumentoPresupuestalDTOPeticion peticion) {
        return ResponseEntity.ok(documentoServicio.actualizar(TipoDocumentoPresupuestal.desdeRuta(tipo), contratoId, id, peticion));
    }

    @DeleteMapping("/{tipo:cdp|rp}/{id}")
    public ResponseEntity<Void> eliminarDocumento(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @PathVariable String tipo,
            @PathVariable @Positive(message = ID_FILA) Long id) {
        documentoServicio.eliminar(TipoDocumentoPresupuestal.desdeRuta(tipo), contratoId, id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ UNSPSC

    @GetMapping("/unspsc")
    public ResponseEntity<List<UnspscDTORespuesta>> listarUnspsc(@PathVariable @Positive(message = ID_CONTRATO) Long contratoId) {
        return ResponseEntity.ok(unspscPolizaServicio.listarUnspsc(contratoId));
    }

    @PostMapping(path = "/unspsc", consumes = JSON)
    public ResponseEntity<UnspscDTORespuesta> agregarUnspsc(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @Valid @RequestBody UnspscDTOPeticion peticion) {
        UnspscDTORespuesta creado = unspscPolizaServicio.agregarUnspsc(contratoId, peticion.getCodigo());
        return creado(creado.id(), creado);
    }

    @DeleteMapping("/unspsc/{id}")
    public ResponseEntity<Void> eliminarUnspsc(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @PathVariable @Positive(message = ID_FILA) Long id) {
        unspscPolizaServicio.eliminarUnspsc(contratoId, id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ pólizas

    @GetMapping("/polizas")
    public ResponseEntity<List<PolizaDTORespuesta>> listarPolizas(@PathVariable @Positive(message = ID_CONTRATO) Long contratoId) {
        return ResponseEntity.ok(unspscPolizaServicio.listarPolizas(contratoId));
    }

    @PostMapping(path = "/polizas", consumes = JSON)
    public ResponseEntity<PolizaDTORespuesta> crearPoliza(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @Valid @RequestBody PolizaDTOPeticion peticion) {
        PolizaDTORespuesta creada = unspscPolizaServicio.crearPoliza(contratoId, peticion);
        return creado(creada.id(), creada);
    }

    @PutMapping(path = "/polizas/{id}", consumes = JSON)
    public ResponseEntity<PolizaDTORespuesta> actualizarPoliza(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @PathVariable @Positive(message = ID_FILA) Long id,
            @Valid @RequestBody PolizaDTOPeticion peticion) {
        return ResponseEntity.ok(unspscPolizaServicio.actualizarPoliza(contratoId, id, peticion));
    }

    @DeleteMapping("/polizas/{id}")
    public ResponseEntity<Void> eliminarPoliza(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @PathVariable @Positive(message = ID_FILA) Long id) {
        unspscPolizaServicio.eliminarPoliza(contratoId, id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ supervisores e interventores

    @GetMapping("/seguimientos")
    public ResponseEntity<List<SeguimientoDTORespuesta>> listarSeguimientos(@PathVariable @Positive(message = ID_CONTRATO) Long contratoId) {
        return ResponseEntity.ok(seguimientoServicio.listar(contratoId));
    }

    @PostMapping(path = "/seguimientos", consumes = JSON)
    public ResponseEntity<SeguimientoDTORespuesta> crearSeguimiento(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @Valid @RequestBody SeguimientoDTOPeticion peticion) {
        SeguimientoDTORespuesta creado = seguimientoServicio.crear(contratoId, peticion);
        return creado(creado.id(), creado);
    }

    @PutMapping(path = "/seguimientos/{id}", consumes = JSON)
    public ResponseEntity<SeguimientoDTORespuesta> actualizarSeguimiento(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @PathVariable @Positive(message = ID_FILA) Long id,
            @Valid @RequestBody SeguimientoDTOPeticion peticion) {
        return ResponseEntity.ok(seguimientoServicio.actualizar(contratoId, id, peticion));
    }

    /** PATCH .../seguimientos/{id}/cerrar con {"hasta": "2026-06-30"}: para cambiar de supervisor. */
    @PatchMapping(path = "/seguimientos/{id}/cerrar", consumes = JSON)
    public ResponseEntity<SeguimientoDTORespuesta> cerrarSeguimiento(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @PathVariable @Positive(message = ID_FILA) Long id,
            @Valid @RequestBody CerrarSeguimientoDTOPeticion peticion) {
        return ResponseEntity.ok(seguimientoServicio.cerrar(contratoId, id, peticion.getHasta()));
    }

    // ------------------------------------------------------------------ modificaciones

    @GetMapping("/modificaciones")
    public ResponseEntity<List<ModificacionDTORespuesta>> listarModificaciones(@PathVariable @Positive(message = ID_CONTRATO) Long contratoId) {
        return ResponseEntity.ok(modificacionServicio.listar(contratoId));
    }

    @PostMapping(path = "/modificaciones", consumes = JSON)
    public ResponseEntity<ModificacionDTORespuesta> crearModificacion(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @Valid @RequestBody ModificacionDTOPeticion peticion) {
        ModificacionDTORespuesta creada = modificacionServicio.crear(contratoId, peticion);
        return creado(creada.id(), creada);
    }

    @PutMapping(path = "/modificaciones/{id}", consumes = JSON)
    public ResponseEntity<ModificacionDTORespuesta> actualizarModificacion(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @PathVariable @Positive(message = ID_FILA) Long id,
            @Valid @RequestBody ModificacionDTOPeticion peticion) {
        return ResponseEntity.ok(modificacionServicio.actualizar(contratoId, id, peticion));
    }

    @DeleteMapping("/modificaciones/{id}")
    public ResponseEntity<Void> eliminarModificacion(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @PathVariable @Positive(message = ID_FILA) Long id) {
        modificacionServicio.eliminar(contratoId, id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ pagos

    @GetMapping("/pagos")
    public ResponseEntity<List<PagoDTORespuesta>> listarPagos(@PathVariable @Positive(message = ID_CONTRATO) Long contratoId) {
        return ResponseEntity.ok(pagoServicio.listar(contratoId));
    }

    /** El número del pago lo asigna el sistema. */
    @PostMapping(path = "/pagos", consumes = JSON)
    public ResponseEntity<PagoDTORespuesta> crearPago(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @Valid @RequestBody PagoDTOPeticion peticion) {
        PagoDTORespuesta creado = pagoServicio.crear(contratoId, peticion);
        return creado(creado.id(), creado);
    }

    /** Solo el último pago. */
    @PutMapping(path = "/pagos/{id}", consumes = JSON)
    public ResponseEntity<PagoDTORespuesta> actualizarPago(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @PathVariable @Positive(message = ID_FILA) Long id,
            @Valid @RequestBody PagoDTOPeticion peticion) {
        return ResponseEntity.ok(pagoServicio.actualizar(contratoId, id, peticion));
    }

    /** Solo el último pago. */
    @DeleteMapping("/pagos/{id}")
    public ResponseEntity<Void> eliminarPago(
            @PathVariable @Positive(message = ID_CONTRATO) Long contratoId, @PathVariable @Positive(message = ID_FILA) Long id) {
        pagoServicio.eliminar(contratoId, id);
        return ResponseEntity.noContent().build();
    }

    private static <T> ResponseEntity<T> creado(Long id, T cuerpo) {
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(ubicacion).body(cuerpo);
    }
}
