package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoSeguimientoEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.RolSeguimiento;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ContratoSeguimientoRepositorio;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.SeguimientoDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.SeguimientoDTORespuesta;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadYaExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.TerceroEntity;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.servicios.ITerceroServicio;
import lombok.RequiredArgsConstructor;

/**
 * Supervisores e interventores del contrato. Deben cuadrar con el tipo de seguimiento del contrato
 * (catálogo TIPO_SEGUIMIENTO): SUPERVISOR solo admite supervisores, INTERVENTOR solo interventores e
 * INTERVENTOR_SUPERVISOR ambos. No puede haber dos periodos del mismo rol que se crucen.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeguimientoServicio {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    /** Fin "infinito" para comparar un periodo vigente (hasta nulo). */
    private static final LocalDate SIN_FIN = LocalDate.of(9999, 12, 31);

    private final ContratoSeguimientoRepositorio repositorio;
    private final ITerceroServicio terceroServicio;
    private final ContratoDetalleSoporte soporte;

    public List<SeguimientoDTORespuesta> listar(Long contratoId) {
        soporte.contrato(contratoId);
        LocalDate hoy = LocalDate.now(ZONA);
        return repositorio.buscarDelContrato(contratoId).stream().map(s -> aRespuesta(s, hoy)).toList();
    }

    @Transactional
    public SeguimientoDTORespuesta crear(Long contratoId, SeguimientoDTOPeticion p) {
        ContratoEntity contrato = soporte.contratoEditable(contratoId);
        ContratoSeguimientoEntity s = new ContratoSeguimientoEntity(contrato);
        asignar(contrato, s, p);
        return aRespuesta(repositorio.save(s), LocalDate.now(ZONA));
    }

    @Transactional
    public SeguimientoDTORespuesta actualizar(Long contratoId, Long id, SeguimientoDTOPeticion p) {
        ContratoEntity contrato = soporte.contratoEditable(contratoId);
        ContratoSeguimientoEntity s = buscar(contratoId, id);
        asignar(contrato, s, p);
        return aRespuesta(repositorio.save(s), LocalDate.now(ZONA));
    }

    /** Pone la fecha hasta (cambio de supervisor: se cierra el actual y se agrega el nuevo). */
    @Transactional
    public SeguimientoDTORespuesta cerrar(Long contratoId, Long id, LocalDate hasta) {
        ContratoEntity contrato = soporte.contratoEditable(contratoId);
        ContratoSeguimientoEntity s = buscar(contratoId, id);
        validarPeriodo(contrato, s.getRol(), s.getDesde(), hasta, s.getId());
        s.setHasta(hasta);
        return aRespuesta(repositorio.save(s), LocalDate.now(ZONA));
    }

    // ------------------------------------------------------------------ para el contrato

    /** Al cambiar el tipo de seguimiento del contrato, sus supervisores e interventores deben seguir cuadrando. */
    public void validarTipoCompatible(ContratoEntity contrato, CatalogoItemEntity nuevoTipo) {
        if (contrato.getId() == null) {
            return;
        }
        Set<RolSeguimiento> admitidos = rolesAdmitidos(nuevoTipo);
        for (RolSeguimiento rol : RolSeguimiento.values()) {
            if (!admitidos.contains(rol) && repositorio.existsByContratoIdAndRol(contrato.getId(), rol)) {
                throw new ReglaNegocioException("El contrato tiene " + articulo(rol) + " registrado; "
                        + (nuevoTipo == null ? "quitar el tipo de seguimiento" : "el tipo de seguimiento '" + nuevoTipo.getEtiqueta() + "'")
                        + " no lo admite");
            }
        }
    }

    /** Roles que exige el tipo de seguimiento y aún no tienen ningún periodo ("supervisor", "interventor"). */
    public List<String> rolesFaltantes(ContratoEntity contrato) {
        List<String> faltan = new ArrayList<>();
        if (contrato.getTipoSeguimiento() == null) {
            return faltan;
        }
        for (RolSeguimiento rol : rolesAdmitidos(contrato.getTipoSeguimiento())) {
            if (contrato.getId() == null || !repositorio.existsByContratoIdAndRol(contrato.getId(), rol)) {
                faltan.add(rol.name().toLowerCase());
            }
        }
        return faltan;
    }

    // ------------------------------------------------------------------ reglas

    private void asignar(ContratoEntity contrato, ContratoSeguimientoEntity s, SeguimientoDTOPeticion p) {
        if (contrato.getTipoSeguimiento() == null) {
            throw new ReglaNegocioException("Defina primero el tipo de seguimiento del contrato " + contrato.getNumero());
        }
        if (!rolesAdmitidos(contrato.getTipoSeguimiento()).contains(p.getRol())) {
            throw new ReglaNegocioException("El tipo de seguimiento '" + contrato.getTipoSeguimiento().getEtiqueta()
                    + "' no admite " + articulo(p.getRol()));
        }
        validarPeriodo(contrato, p.getRol(), p.getDesde(), p.getHasta(), s.getId());
        String rol = p.getRol().name().toLowerCase();
        boolean mismo = s.getTercero() != null && s.getTercero().getId().equals(p.getTerceroId()) && s.getRol() == p.getRol();
        if (!mismo) {
            // El supervisor es persona natural; el interventor puede ser natural o jurídica.
            TerceroEntity tercero = p.getRol() == RolSeguimiento.SUPERVISOR
                    ? terceroServicio.obtenerPersonaNaturalValida(p.getTerceroId(), null, rol)
                    : terceroServicio.obtenerTerceroValido(p.getTerceroId(), null, rol);
            s.setTercero(tercero);
        }
        s.setRol(p.getRol());
        s.setDesde(p.getDesde());
        s.setHasta(p.getHasta());
    }

    private void validarPeriodo(ContratoEntity contrato, RolSeguimiento rol, LocalDate desde, LocalDate hasta, Long idExcluir) {
        if (hasta != null && hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha hasta (" + hasta.format(ContratoDetalleSoporte.FECHA)
                    + ") no puede ser anterior a la fecha desde (" + desde.format(ContratoDetalleSoporte.FECHA) + ")");
        }
        ContratoDetalleSoporte.noAnteriorA(desde, "desde", contrato.getFechaSuscripcion(), "suscripción");
        if (repositorio.contarCruces(contrato.getId(), rol, desde, hasta == null ? SIN_FIN : hasta, idExcluir) > 0) {
            throw new EntidadYaExisteException("El contrato " + contrato.getNumero() + " ya tiene " + articulo(rol)
                    + " en ese periodo; cierre el actual (fecha hasta) antes de agregar otro");
        }
    }

    /** Roles que admite el tipo de seguimiento; un tipo nuevo del catálogo que no se conoce admite ambos. */
    private static Set<RolSeguimiento> rolesAdmitidos(CatalogoItemEntity tipo) {
        if (tipo == null) {
            return EnumSet.noneOf(RolSeguimiento.class);
        }
        return switch (tipo.getCodigo()) {
            case "SUPERVISOR" -> EnumSet.of(RolSeguimiento.SUPERVISOR);
            case "INTERVENTOR" -> EnumSet.of(RolSeguimiento.INTERVENTOR);
            default -> EnumSet.allOf(RolSeguimiento.class);
        };
    }

    private static String articulo(RolSeguimiento rol) {
        return rol == RolSeguimiento.SUPERVISOR ? "un supervisor" : "un interventor";
    }

    private ContratoSeguimientoEntity buscar(Long contratoId, Long id) {
        return repositorio.findByIdAndContratoId(id, contratoId).orElseThrow(() ->
                new EntidadNoExisteException("No existe el seguimiento con id " + id + " en el contrato con id " + contratoId));
    }

    private static SeguimientoDTORespuesta aRespuesta(ContratoSeguimientoEntity s, LocalDate hoy) {
        TerceroEntity t = s.getTercero();
        return new SeguimientoDTORespuesta(s.getId(), s.getRol(), t.getId(), t.getNombreCompleto(),
                t.getTipoIdentificacion().getEtiqueta(), t.getNumeroDocumento(), t.getDv(), s.getDesde(), s.getHasta(),
                s.estaVigenteEn(hoy));
    }
}
