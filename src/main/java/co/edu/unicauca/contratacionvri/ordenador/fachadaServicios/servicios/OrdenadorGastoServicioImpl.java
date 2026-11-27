package co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.servicios;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadYaExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import co.edu.unicauca.contratacionvri.ordenador.accesoADatos.modelos.OrdenadorGastoEntity;
import co.edu.unicauca.contratacionvri.ordenador.accesoADatos.repositorios.OrdenadorGastoRepositorio;
import co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.DTOPeticion.OrdenadorGastoDTOPeticion;
import co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.DTORespuesta.OrdenadorGastoDTORespuesta;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.PersonaNaturalEntity;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.servicios.ITerceroServicio;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrdenadorGastoServicioImpl implements IOrdenadorGastoServicio {

    private static final String ROL = "ordenador del gasto";
    /** Fin "infinito" para comparar un periodo abierto (fecha_hasta nula). */
    private static final LocalDate SIN_FIN = LocalDate.of(9999, 12, 31);
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ISO_LOCAL_DATE;

    private final OrdenadorGastoRepositorio repositorio;
    private final ITerceroServicio terceroServicio;
    private final UsoOrdenadorChecker usoChecker;

    @Override
    public List<OrdenadorGastoDTORespuesta> listar(String texto, Boolean activo, LocalDate vigenteEn) {
        return repositorio.buscar(escaparLike(Textos.limpiar(texto)), activo, vigenteEn).stream()
                .map(this::aRespuesta).toList();
    }

    @Override
    public OrdenadorGastoDTORespuesta consultarPorId(Long id) {
        return aRespuesta(buscarPorId(id));
    }

    @Override
    @Transactional
    public OrdenadorGastoDTORespuesta crear(OrdenadorGastoDTOPeticion peticion) {
        String cargo = Textos.limpiar(peticion.getCargo());
        boolean esDelegado = Boolean.TRUE.equals(peticion.getEsDelegado());
        String acto = Textos.limpiar(peticion.getActoDelegacion());
        validarFechas(peticion.getFechaDesde(), peticion.getFechaHasta());
        validarDelegacion(esDelegado, acto);
        PersonaNaturalEntity tercero = terceroServicio.obtenerPersonaNaturalValida(peticion.getTerceroId(), null, ROL);
        validarSinCruce(tercero, cargo, peticion.getFechaDesde(), peticion.getFechaHasta(), null);

        OrdenadorGastoEntity ordenador = new OrdenadorGastoEntity();
        ordenador.setTercero(tercero);
        ordenador.setCargo(cargo);
        ordenador.setEsDelegado(esDelegado);
        ordenador.setActoDelegacion(acto);
        ordenador.setFechaDesde(peticion.getFechaDesde());
        ordenador.setFechaHasta(peticion.getFechaHasta());
        ordenador.setActivo(Boolean.TRUE);
        return aRespuesta(repositorio.save(ordenador));
    }

    @Override
    @Transactional
    public OrdenadorGastoDTORespuesta actualizar(Long id, OrdenadorGastoDTOPeticion peticion) {
        OrdenadorGastoEntity ordenador = buscarPorId(id);
        String cargo = Textos.limpiar(peticion.getCargo());
        boolean esDelegado = Boolean.TRUE.equals(peticion.getEsDelegado());
        String acto = Textos.limpiar(peticion.getActoDelegacion());

        boolean cambiaTercero = !Objects.equals(ordenador.getTercero().getId(), peticion.getTerceroId());
        boolean cambiaCargo = !ordenador.getCargo().equals(cargo);
        boolean cambiaDesde = !ordenador.getFechaDesde().equals(peticion.getFechaDesde());
        if ((cambiaTercero || cambiaCargo || cambiaDesde) && usoChecker.existenContratosConOrdenador(id)) {
            throw new ReglaNegocioException("El periodo de " + ordenador.getEtiqueta()
                    + " ya tiene contratos: su tercero, su cargo y su fecha desde no se pueden cambiar (sí puede cerrarlo)");
        }
        validarFechas(peticion.getFechaDesde(), peticion.getFechaHasta());
        validarDelegacion(esDelegado, acto);
        PersonaNaturalEntity tercero = terceroServicio.obtenerPersonaNaturalValida(
                peticion.getTerceroId(), ordenador.getTercero(), ROL);
        if (Boolean.TRUE.equals(ordenador.getActivo())) {
            validarSinCruce(tercero, cargo, peticion.getFechaDesde(), peticion.getFechaHasta(), id);
        }

        ordenador.setTercero(tercero);
        ordenador.setCargo(cargo);
        ordenador.setEsDelegado(esDelegado);
        ordenador.setActoDelegacion(acto);
        ordenador.setFechaDesde(peticion.getFechaDesde());
        ordenador.setFechaHasta(peticion.getFechaHasta());
        return aRespuesta(repositorio.save(ordenador));
    }

    @Override
    @Transactional
    public OrdenadorGastoDTORespuesta cerrarPeriodo(Long id, LocalDate fechaHasta) {
        OrdenadorGastoEntity ordenador = buscarPorId(id);
        validarFechas(ordenador.getFechaDesde(), fechaHasta);
        if (Boolean.TRUE.equals(ordenador.getActivo())) {
            // Cerrar normalmente acorta el periodo, pero si ya estaba cerrado y se corre la fecha hacia
            // adelante podría cruzarse con otro.
            validarSinCruce(ordenador.getTercero(), ordenador.getCargo(), ordenador.getFechaDesde(), fechaHasta, id);
        }
        ordenador.setFechaHasta(fechaHasta);
        return aRespuesta(repositorio.save(ordenador));
    }

    @Override
    @Transactional
    public OrdenadorGastoDTORespuesta cambiarEstado(Long id, boolean activo) {
        OrdenadorGastoEntity ordenador = buscarPorId(id);
        if (activo && !Boolean.TRUE.equals(ordenador.getActivo())) {
            validarSinCruce(ordenador.getTercero(), ordenador.getCargo(), ordenador.getFechaDesde(),
                    ordenador.getFechaHasta(), id);
        }
        ordenador.setActivo(activo);
        return aRespuesta(repositorio.save(ordenador));
    }

    @Override
    public OrdenadorGastoEntity obtenerOrdenadorValido(Long id, OrdenadorGastoEntity actual, LocalDate fechaSuscripcion) {
        OrdenadorGastoEntity ordenador;
        if (actual != null && Objects.equals(actual.getId(), id)) {
            ordenador = actual;
        } else {
            ordenador = repositorio.findById(id).orElseThrow(() ->
                    new ReglaNegocioException("El ordenador del gasto con id " + id + " no existe"));
            if (!Boolean.TRUE.equals(ordenador.getActivo())) {
                throw new ReglaNegocioException("El ordenador del gasto " + ordenador.getEtiqueta() + " está inactivo");
            }
        }
        if (fechaSuscripcion != null && (fechaSuscripcion.isBefore(ordenador.getFechaDesde())
                || (ordenador.getFechaHasta() != null && fechaSuscripcion.isAfter(ordenador.getFechaHasta())))) {
            throw new ReglaNegocioException("La fecha de suscripción " + fechaSuscripcion.format(FECHA)
                    + " no está dentro del periodo de " + ordenador.getEtiqueta() + " (" + periodo(ordenador) + ")");
        }
        return ordenador;
    }

    // ------------------------------------------------------------------ reglas

    private void validarFechas(LocalDate desde, LocalDate hasta) {
        if (hasta != null && hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha hasta (" + hasta.format(FECHA)
                    + ") no puede ser anterior a la fecha desde (" + desde.format(FECHA) + ")");
        }
    }

    private void validarDelegacion(boolean esDelegado, String acto) {
        if (esDelegado && acto == null) {
            throw new ReglaNegocioException("Si firma por delegación, el acto de delegación (número y fecha de la resolución) es obligatorio");
        }
        if (!esDelegado && acto != null) {
            throw new ReglaNegocioException("El acto de delegación solo aplica cuando el ordenador firma por delegación");
        }
    }

    /** Un mismo tercero no puede tener dos periodos ACTIVOS con el mismo cargo que se crucen. */
    private void validarSinCruce(PersonaNaturalEntity tercero, String cargo, LocalDate desde, LocalDate hasta, Long idExcluir) {
        long cruces = repositorio.contarCruces(tercero.getId(), cargo, desde, hasta == null ? SIN_FIN : hasta, idExcluir);
        if (cruces > 0) {
            throw new EntidadYaExisteException(tercero.getNombreCompleto() + " ya tiene un periodo activo como " + cargo
                    + " que se cruza con " + desde.format(FECHA) + " a " + (hasta == null ? "la fecha (abierto)" : hasta.format(FECHA)));
        }
    }

    private static String periodo(OrdenadorGastoEntity o) {
        return o.getFechaDesde().format(FECHA) + " a " + (o.getFechaHasta() == null ? "hoy (abierto)" : o.getFechaHasta().format(FECHA));
    }

    private OrdenadorGastoEntity buscarPorId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("No existe un ordenador del gasto con id " + id));
    }

    private static String escaparLike(String texto) {
        if (texto == null) {
            return null;
        }
        return texto.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private OrdenadorGastoDTORespuesta aRespuesta(OrdenadorGastoEntity o) {
        PersonaNaturalEntity t = o.getTercero();
        OrdenadorGastoDTORespuesta r = new OrdenadorGastoDTORespuesta();
        r.setId(o.getId());
        r.setTerceroId(t.getId());
        r.setNombre(t.getNombreCompleto());
        r.setCedula(t.getNumeroDocumento());
        r.setTipoIdentificacion(t.getTipoIdentificacion().getEtiqueta());
        r.setCargo(o.getCargo());
        r.setEsDelegado(o.getEsDelegado());
        r.setActoDelegacion(o.getActoDelegacion());
        r.setFechaDesde(o.getFechaDesde());
        r.setFechaHasta(o.getFechaHasta());
        r.setVigente(o.estaVigenteEn(LocalDate.now(ZONA)));
        r.setActivo(o.getActivo());
        r.setEtiqueta(o.getEtiqueta());
        r.setDatosEditables(o.getId() == null || !usoChecker.existenContratosConOrdenador(o.getId()));
        r.setCreadoEn(o.getCreadoEn());
        r.setActualizadoEn(o.getActualizadoEn());
        return r;
    }
}
