package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.abogado.accesoADatos.modelos.AbogadoEntity;
import co.edu.unicauca.contratacionvri.abogado.fachadaServicios.servicios.IAbogadoServicio;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CodigoCatalogo;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.servicios.ICatalogoItemServicio;
import co.edu.unicauca.contratacionvri.clasecontrato.fachadaServicios.servicios.IClaseContratoServicio;
import co.edu.unicauca.contratacionvri.comun.fachadaServicios.PaginaRespuesta;
import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.CampoContrato;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoPresupuestoEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.EstadoContrato;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ContratoEspecificaciones;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ContratoObservacionRepositorio;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ContratoRepositorio;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.PagoRepositorio;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ContratoActualizarDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ContratoCierreDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ContratoCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ContratoDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.PresupuestoDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.CampoContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ContratoResumenDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.PresupuestoDTORespuesta;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ResumenContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.dependencia.fachadaServicios.servicios.IDependenciaServicio;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import co.edu.unicauca.contratacionvri.ordenador.accesoADatos.modelos.OrdenadorGastoEntity;
import co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.servicios.IOrdenadorGastoServicio;
import co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.servicios.IProyectoServicio;
import co.edu.unicauca.contratacionvri.rubro.fachadaServicios.servicios.IRubroServicio;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.TerceroEntity;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.servicios.ITerceroServicio;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.TipoContratoEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios.TipoContratoRepositorio;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.NumeroContrato;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios.INumeracionServicio;

@Service
@Transactional(readOnly = true)
public class ContratoServicioImpl implements IContratoServicio {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ISO_LOCAL_DATE;
    /** Código mnemónico del ítem "No pactados" del catálogo TIPO_ANTICIPO. */
    private static final String ANTICIPO_NO_PACTADO = "NO_PACTADO";
    /** "6.1-17.13/023", "6.1-17.13/" o "/023". */
    private static final Pattern NUMERO = Pattern.compile("^([^/]*)/(\\d*)$");
    private static final Sort ORDEN = Sort.by(Sort.Order.desc("anio"), Sort.Order.desc("consecutivo"), Sort.Order.desc("id"));

    private final ContratoRepositorio repositorio;
    private final ContratoObservacionRepositorio observacionRepositorio;
    private final TipoContratoRepositorio tipoRepositorio;
    private final INumeracionServicio numeracion;
    private final IAbogadoServicio abogadoServicio;
    private final ITerceroServicio terceroServicio;
    private final IClaseContratoServicio claseServicio;
    private final IOrdenadorGastoServicio ordenadorServicio;
    private final ICatalogoItemServicio catalogoServicio;
    private final IRubroServicio rubroServicio;
    private final IProyectoServicio proyectoServicio;
    private final IDependenciaServicio dependenciaServicio;
    private final SeguimientoServicio seguimientoServicio;
    private final PagoRepositorio pagoRepositorio;
    private final ResumenContratoServicio resumenServicio;
    private final ContratoDetalleSoporte soporte;
    private final int aniosAtras;

    public ContratoServicioImpl(ContratoRepositorio repositorio, ContratoObservacionRepositorio observacionRepositorio,
                                TipoContratoRepositorio tipoRepositorio, INumeracionServicio numeracion,
                                IAbogadoServicio abogadoServicio, ITerceroServicio terceroServicio,
                                IClaseContratoServicio claseServicio, IOrdenadorGastoServicio ordenadorServicio,
                                ICatalogoItemServicio catalogoServicio, IRubroServicio rubroServicio,
                                IProyectoServicio proyectoServicio, IDependenciaServicio dependenciaServicio,
                                SeguimientoServicio seguimientoServicio, PagoRepositorio pagoRepositorio,
                                ResumenContratoServicio resumenServicio, ContratoDetalleSoporte soporte,
                                @Value("${vri.contrato.anios-atras:1}") int aniosAtras) {
        this.repositorio = repositorio;
        this.observacionRepositorio = observacionRepositorio;
        this.tipoRepositorio = tipoRepositorio;
        this.numeracion = numeracion;
        this.abogadoServicio = abogadoServicio;
        this.terceroServicio = terceroServicio;
        this.claseServicio = claseServicio;
        this.ordenadorServicio = ordenadorServicio;
        this.catalogoServicio = catalogoServicio;
        this.rubroServicio = rubroServicio;
        this.proyectoServicio = proyectoServicio;
        this.dependenciaServicio = dependenciaServicio;
        this.seguimientoServicio = seguimientoServicio;
        this.pagoRepositorio = pagoRepositorio;
        this.resumenServicio = resumenServicio;
        this.soporte = soporte;
        this.aniosAtras = aniosAtras;
    }

    // ------------------------------------------------------------------ operaciones

    /**
     * Valida todo primero y pide el número al final: así el consecutivo queda bloqueado el menor
     * tiempo posible. Si algo falla después, la transacción se deshace y el número no se consume.
     */
    @Override
    @Transactional
    public ContratoDTORespuesta crear(ContratoCrearDTOPeticion peticion) {
        validarAnioDeCreacion(peticion.getAnio());
        TipoContratoEntity tipo = tipoRepositorio.findById(peticion.getTipoContratoId()).orElseThrow(() ->
                new ReglaNegocioException("El tipo de contrato con id " + peticion.getTipoContratoId() + " no existe"));
        ContratoEntity contrato = new ContratoEntity(tipo, tipo.getSerie(), peticion.getAnio());
        asignarDatos(contrato, peticion);
        contrato.recalcularEstado();

        NumeroContrato numero = numeracion.generarNumero(tipo.getId(), peticion.getAnio());
        contrato.asignarConsecutivo(numero.consecutivo());
        return aRespuesta(repositorio.save(contrato));
    }

    @Override
    public ContratoDTORespuesta consultarPorId(Long id) {
        return aRespuesta(buscarPorId(id));
    }

    @Override
    public ResumenContratoDTORespuesta consultarResumen(Long id) {
        return resumenServicio.resumen(buscarPorId(id));
    }

    @Override
    public PaginaRespuesta<ContratoResumenDTORespuesta> buscar(FiltroContratos filtro, int pagina, int tamanio) {
        validarRango("inicio", filtro.getInicioDesde(), filtro.getInicioHasta());
        validarRango("terminación", filtro.getTerminacionDesde(), filtro.getTerminacionHasta());
        Page<ContratoEntity> resultado = repositorio.findAll(ContratoEspecificaciones.de(criterios(filtro)),
                PageRequest.of(pagina, tamanio, ORDEN));
        Map<Long, Long> pendientes = pendientesPorContrato(resultado.getContent());
        Map<Long, ResumenContratoDTORespuesta> resumenes = resumenServicio.resumenes(resultado.getContent());
        return PaginaRespuesta.de(resultado, c -> aResumen(c, pendientes.getOrDefault(c.getId(), 0L), resumenes.get(c.getId())));
    }

    @Override
    public PaginaRespuesta<ContratoResumenDTORespuesta> buscarDeAbogado(Long abogadoId, FiltroContratos filtro,
                                                                       int pagina, int tamanio) {
        abogadoServicio.consultarPorId(abogadoId); // 404 si no existe
        return buscar(filtro.toBuilder().responsableId(abogadoId).build(), pagina, tamanio);
    }

    @Override
    @Transactional
    public ContratoDTORespuesta actualizar(Long id, ContratoActualizarDTOPeticion peticion) {
        ContratoEntity contrato = buscarBloqueado(id);
        validarNoAnulado(contrato);
        if (peticion.getTipoContratoId() != null && !peticion.getTipoContratoId().equals(contrato.getTipoContrato().getId())) {
            throw new ReglaNegocioException("El tipo de contrato no se puede cambiar: el número " + contrato.getNumero()
                    + " pertenece a su serie documental");
        }
        if (peticion.getAnio() != null && !peticion.getAnio().equals(contrato.getAnio())) {
            throw new ReglaNegocioException("El año del contrato " + contrato.getNumero() + " (" + contrato.getAnio()
                    + ") no se puede cambiar");
        }
        asignarDatos(contrato, peticion);
        validarPagosParaActaFinal(contrato);
        contrato.recalcularEstado();
        repositorio.save(contrato);
        soporte.validarSaldo(contrato);
        return aRespuesta(contrato);
    }

    @Override
    @Transactional
    public ContratoDTORespuesta registrarCierre(Long id, ContratoCierreDTOPeticion peticion) {
        ContratoEntity contrato = buscarBloqueado(id);
        validarNoAnulado(contrato);
        contrato.setFechaTerminacion(peticion.getFechaTerminacion());
        contrato.setFechaActaFinal(peticion.getFechaActaFinal());
        contrato.getPresupuesto().setFechaCertificacionPago(peticion.getFechaCertificacionPago());
        validarFechas(contrato);
        validarPagosParaActaFinal(contrato);
        contrato.recalcularEstado();
        return aRespuesta(repositorio.save(contrato));
    }

    @Override
    @Transactional
    public ContratoDTORespuesta anular(Long id, String motivo) {
        ContratoEntity contrato = buscarPorId(id);
        validarNoAnulado(contrato);
        if (contrato.getEstado() != EstadoContrato.EN_TRAMITE) {
            throw new ReglaNegocioException("Solo se puede anular un contrato en trámite; el contrato "
                    + contrato.getNumero() + " está " + contrato.getEstado());
        }
        contrato.anular(Textos.limpiar(motivo), LocalDate.now(ZONA));
        return aRespuesta(repositorio.save(contrato));
    }

    @Override
    public List<CampoContratoDTORespuesta> listarCampos() {
        return Arrays.stream(CampoContrato.values())
                .map(c -> new CampoContratoDTORespuesta(c.name(), c.getEtiqueta())).toList();
    }

    // ------------------------------------------------------------------ asignación y reglas

    /**
     * Copia los datos del abogado validando cada relación. Una relación que no cambia no se vuelve a
     * validar (puede haberse inactivado después); una que cambia debe estar activa.
     */
    private void asignarDatos(ContratoEntity c, ContratoDTOPeticion p) {
        c.setResponsable(abogadoServicio.obtenerAbogadoValido(p.getResponsableId(), c.getResponsable()));

        c.setClaseContrato(p.getClaseContratoId() == null ? null
                : claseServicio.obtenerClaseValida(p.getClaseContratoId(), c.getClaseContrato()));
        String otraClase = Textos.limpiar(p.getOtraClase());
        boolean esOtros = c.getClaseContrato() != null && c.getClaseContrato().esOtros();
        if (esOtros && otraClase == null) {
            throw new ReglaNegocioException("La clase " + c.getClaseContrato().getEtiqueta() + " exige describir la otra clase de contrato");
        }
        if (!esOtros && otraClase != null) {
            throw new ReglaNegocioException("La descripción de otra clase de contrato solo aplica cuando la clase es 30 OTROS");
        }
        c.setOtraClase(otraClase);
        c.setObjeto(Textos.limpiar(p.getObjeto()));

        c.setContratista(tercero(p.getContratistaId(), c.getContratista(), "contratista"));
        CatalogoItemEntity tipoSeguimiento = item(p.getTipoSeguimientoId(), c.getTipoSeguimiento(), CodigoCatalogo.TIPO_SEGUIMIENTO);
        if (tipoSeguimiento != c.getTipoSeguimiento()) {
            seguimientoServicio.validarTipoCompatible(c, tipoSeguimiento);
        }
        c.setTipoSeguimiento(tipoSeguimiento);
        c.setValorSinIva(p.getValorSinIva());
        c.setIva(p.getIva());
        c.setPlazoDias(p.getPlazoDias());
        c.setNumeroPagos(p.getNumeroPagos());
        c.setFechaSuscripcion(p.getFechaSuscripcion());
        c.setFechaInicio(p.getFechaInicio());
        c.setFechaTerminacion(p.getFechaTerminacion());
        validarFechas(c);

        // El periodo del ordenador debe cubrir la suscripción aunque el ordenador no cambie.
        c.setOrdenadorGasto(p.getOrdenadorGastoId() == null ? null
                : ordenadorServicio.obtenerOrdenadorValido(p.getOrdenadorGastoId(), c.getOrdenadorGasto(), c.getFechaSuscripcion()));

        asignarPresupuesto(c, p.getPresupuesto() == null ? new PresupuestoDTOPeticion() : p.getPresupuesto());
        validarSuscripcionCompleta(c);
    }

    private void asignarPresupuesto(ContratoEntity c, PresupuestoDTOPeticion p) {
        ContratoPresupuestoEntity pr = c.getPresupuesto();
        pr.setRubro(p.getRubroId() == null ? null : rubroServicio.obtenerRubroValido(p.getRubroId(), pr.getRubro()));
        if (p.getProyectoId() == null) {
            pr.setProyecto(null);
        } else if (pr.getProyecto() == null || !p.getProyectoId().equals(pr.getProyecto().getIdProyecto())) {
            pr.setProyecto(proyectoServicio.obtenerProyectoValido(p.getProyectoId(), true));
        }
        pr.setPaa(Textos.limpiar(p.getPaa()));
        pr.setFuenteFinanciacion(item(p.getFuenteFinanciacionId(), pr.getFuenteFinanciacion(), CodigoCatalogo.FUENTE_FINANCIACION));

        if (Boolean.TRUE.equals(p.getRecursosDeConvenio())) {
            if (p.getEntidadFinanciadoraId() == null) {
                throw new ReglaNegocioException("Si los recursos provienen de un contrato o convenio interadministrativo, "
                        + "la entidad de donde provienen es obligatoria");
            }
            TerceroEntity entidad = tercero(p.getEntidadFinanciadoraId(), pr.getEntidadFinanciadora(),
                    "entidad de donde provienen los recursos");
            if (entidad.getDv() == null) {
                throw new ReglaNegocioException("La entidad de donde provienen los recursos debe tener NIT y "
                        + entidad.getNombreCompleto() + " no lo tiene");
            }
            pr.setEntidadFinanciadora(entidad);
        } else {
            if (p.getEntidadFinanciadoraId() != null) {
                throw new ReglaNegocioException("La entidad de donde provienen los recursos solo aplica si provienen "
                        + "de un contrato o convenio interadministrativo");
            }
            pr.setEntidadFinanciadora(null);
        }
        pr.setRecursosDeConvenio(p.getRecursosDeConvenio());

        if (p.getDependenciaSolicitanteId() == null) {
            pr.setDependenciaSolicitante(null);
        } else if (pr.getDependenciaSolicitante() == null
                || !p.getDependenciaSolicitanteId().equals(pr.getDependenciaSolicitante().getId())) {
            pr.setDependenciaSolicitante(dependenciaServicio.obtenerDependenciaValida(p.getDependenciaSolicitanteId(), true));
        }
        pr.setNaturalezaGasto(p.getNaturalezaGasto());

        pr.setTipoAnticipo(item(p.getTipoAnticipoId(), pr.getTipoAnticipo(), CodigoCatalogo.TIPO_ANTICIPO));
        pr.setValorAnticipo(valorAnticipo(pr.getTipoAnticipo(), p.getValorAnticipo(), c.getValorInicial()));
    }

    /** "No pactados" exige 0; "Anticipo" o "Pago anticipado" exigen un valor que no supere el valor inicial. */
    private static BigDecimal valorAnticipo(CatalogoItemEntity tipo, BigDecimal valor, BigDecimal valorInicial) {
        boolean hayValor = valor != null && valor.signum() != 0;
        if (tipo == null) {
            if (hayValor) {
                throw new ReglaNegocioException("Indique el tipo de anticipo para registrar su valor");
            }
            return valor;
        }
        if (ANTICIPO_NO_PACTADO.equals(tipo.getCodigo())) {
            if (hayValor) {
                throw new ReglaNegocioException("Con anticipo '" + tipo.getEtiqueta() + "' el valor del anticipo debe ser 0");
            }
            return BigDecimal.ZERO;
        }
        if (!hayValor) {
            throw new ReglaNegocioException("El anticipo '" + tipo.getEtiqueta() + "' exige su valor");
        }
        if (valorInicial != null && valor.compareTo(valorInicial) > 0) {
            throw new ReglaNegocioException("El valor del anticipo (" + valor.toPlainString()
                    + ") no puede superar el valor inicial del contrato (" + valorInicial.toPlainString() + ")");
        }
        return valor;
    }

    /**
     * Cada fecha exige la anterior y no puede ser anterior a ella: suscripción → inicio → terminación
     * → acta final. La suscripción debe caer dentro del año del contrato.
     */
    private static void validarFechas(ContratoEntity c) {
        LocalDate suscripcion = c.getFechaSuscripcion();
        if (suscripcion != null && suscripcion.getYear() != c.getAnio()) {
            throw new ReglaNegocioException("La fecha de suscripción (" + suscripcion.format(FECHA)
                    + ") debe estar dentro del año del contrato (" + c.getAnio() + ")");
        }
        encadenar(c.getFechaInicio(), "inicio", suscripcion, "suscripción");
        encadenar(c.getFechaTerminacion(), "terminación", c.getFechaInicio(), "inicio");
        encadenar(c.getFechaActaFinal(), "acta final", c.getFechaTerminacion(), "terminación");
    }

    private static void encadenar(LocalDate fecha, String nombre, LocalDate anterior, String nombreAnterior) {
        if (fecha == null) {
            return;
        }
        if (anterior == null) {
            throw new ReglaNegocioException("La fecha de " + nombre + " exige la fecha de " + nombreAnterior);
        }
        if (fecha.isBefore(anterior)) {
            throw new ReglaNegocioException("La fecha de " + nombre + " (" + fecha.format(FECHA)
                    + ") no puede ser anterior a la fecha de " + nombreAnterior + " (" + anterior.format(FECHA) + ")");
        }
    }

    /** Con fecha de suscripción el contrato debe estar completo. */
    private void validarSuscripcionCompleta(ContratoEntity c) {
        if (c.getFechaSuscripcion() == null) {
            return;
        }
        ContratoPresupuestoEntity pr = c.getPresupuesto();
        List<String> faltan = new ArrayList<>();
        faltaSi(faltan, c.getClaseContrato(), "clase de contrato");
        faltaSi(faltan, c.getContratista(), "contratista");
        faltaSi(faltan, c.getOrdenadorGasto(), "ordenador del gasto");
        faltaSi(faltan, c.getTipoSeguimiento(), "tipo de seguimiento");
        faltaSi(faltan, c.getValorSinIva(), "valor sin IVA");
        faltaSi(faltan, c.getIva(), "IVA");
        faltaSi(faltan, c.getPlazoDias(), "plazo");
        faltaSi(faltan, pr.getRubro(), "código de afectación presupuestal");
        faltaSi(faltan, pr.getFuenteFinanciacion(), "fuente de financiación");
        faltaSi(faltan, pr.getTipoAnticipo(), "tipo de anticipo");
        faltaSi(faltan, pr.getDependenciaSolicitante(), "dependencia que solicita");
        faltan.addAll(seguimientoServicio.rolesFaltantes(c));
        if (!faltan.isEmpty()) {
            throw new ReglaNegocioException("Para registrar la suscripción faltan: " + String.join(", ", faltan));
        }
    }

    private static void faltaSi(List<String> faltan, Object valor, String nombre) {
        if (valor == null) {
            faltan.add(nombre);
        }
    }

    private void validarAnioDeCreacion(Integer anio) {
        int actual = LocalDate.now(ZONA).getYear();
        int desde = actual - aniosAtras;
        if (anio < desde || anio > actual) {
            throw new ReglaNegocioException(desde == actual
                    ? "Solo se pueden crear contratos del año " + actual
                    : "Solo se pueden crear contratos de los años " + desde + " a " + actual);
        }
    }

    /** Si el contrato pactó N pagos, el acta final exige que estén los N. */
    private void validarPagosParaActaFinal(ContratoEntity c) {
        if (c.getFechaActaFinal() == null || c.getNumeroPagos() == null || c.getId() == null) {
            return;
        }
        long registrados = pagoRepositorio.countByContratoId(c.getId());
        if (registrados < c.getNumeroPagos()) {
            throw new ReglaNegocioException("Para registrar el acta final faltan pagos: se pactaron " + c.getNumeroPagos()
                    + " y hay " + registrados + (registrados == 1 ? " registrado" : " registrados"));
        }
    }

    /** Bloquea la fila del contrato (los cambios de valor no deben cruzarse con un pago simultáneo) y trae el detalle. */
    private ContratoEntity buscarBloqueado(Long id) {
        repositorio.bloquear(id).orElseThrow(() -> new EntidadNoExisteException("No existe un contrato con id " + id));
        return buscarPorId(id);
    }

    private static void validarNoAnulado(ContratoEntity c) {
        if (c.estaAnulado()) {
            throw new ReglaNegocioException("El contrato " + c.getNumero() + " está anulado y no se puede modificar");
        }
    }

    private static void validarRango(String nombre, LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new ReglaNegocioException("En el filtro de fecha de " + nombre + ", la fecha hasta (" + hasta.format(FECHA)
                    + ") no puede ser anterior a la fecha desde (" + desde.format(FECHA) + ")");
        }
    }

    private CatalogoItemEntity item(Long id, CatalogoItemEntity actual, String codigoCatalogo) {
        if (id == null) {
            return null;
        }
        if (actual != null && actual.getId().equals(id)) {
            return actual;
        }
        return catalogoServicio.validarItem(id, codigoCatalogo);
    }

    private TerceroEntity tercero(Long id, TerceroEntity actual, String rol) {
        return id == null ? null : terceroServicio.obtenerTerceroValido(id, actual, rol);
    }

    // ------------------------------------------------------------------ búsqueda

    /** Separa el número en serie y consecutivo, y limpia y escapa los textos. */
    private static ContratoEspecificaciones.Criterios criterios(FiltroContratos f) {
        String serie = null;
        Integer consecutivo = null;
        String numero = Textos.limpiar(f.getNumero());
        if (numero != null) {
            Matcher m = NUMERO.matcher(numero);
            if (m.matches()) {
                serie = m.group(1).isBlank() ? null : m.group(1).trim();
                consecutivo = m.group(2).isEmpty() ? null : aEntero(m.group(2), numero);
            } else if (numero.chars().allMatch(Character::isDigit)) {
                consecutivo = aEntero(numero, numero);
            } else {
                serie = numero;
            }
        }
        return ContratoEspecificaciones.Criterios.builder()
                .serieCodigo(serie)
                .consecutivo(consecutivo)
                .anio(f.getAnio())
                .estado(f.getEstado())
                .tipoContratoId(f.getTipoContratoId())
                .claseContratoId(f.getClaseContratoId())
                .responsableId(f.getResponsableId())
                .contratista(escaparLike(Textos.limpiar(f.getContratista())))
                .texto(escaparLike(Textos.limpiar(f.getTexto())))
                .inicioDesde(f.getInicioDesde())
                .inicioHasta(f.getInicioHasta())
                .terminacionDesde(f.getTerminacionDesde())
                .terminacionHasta(f.getTerminacionHasta())
                .conObservaciones(f.getConObservaciones())
                .build();
    }

    private static Integer aEntero(String digitos, String numero) {
        try {
            return Integer.valueOf(digitos);
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("El número de contrato '" + numero + "' no es válido");
        }
    }

    private static String escaparLike(String texto) {
        return texto == null ? null : texto.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private Map<Long, Long> pendientesPorContrato(List<ContratoEntity> contratos) {
        if (contratos.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> pendientes = new HashMap<>();
        for (Object[] fila : observacionRepositorio.contarPendientes(contratos.stream().map(ContratoEntity::getId).toList())) {
            pendientes.put((Long) fila[0], (Long) fila[1]);
        }
        return pendientes;
    }

    // ------------------------------------------------------------------ respuestas

    private ContratoEntity buscarPorId(Long id) {
        return repositorio.findConDetalleById(id)
                .orElseThrow(() -> new EntidadNoExisteException("No existe un contrato con id " + id));
    }

    /** Aviso (no bloquea) si el contratista tiene otros contratos no anulados en el año. */
    private List<String> advertencias(ContratoEntity c) {
        if (c.getContratista() == null || c.estaAnulado()) {
            return List.of();
        }
        List<ContratoEntity> otros = repositorio.buscarOtrosDelContratista(
                c.getContratista().getId(), c.getAnio(), c.getId(), EstadoContrato.ANULADO);
        if (otros.isEmpty()) {
            return List.of();
        }
        String numeros = String.join(", ", otros.stream().map(ContratoEntity::getNumero).toList());
        return List.of("El contratista " + c.getContratista().getNombreCompleto() + " ya tiene " + otros.size()
                + (otros.size() == 1 ? " contrato" : " contratos") + " en " + c.getAnio() + " (" + numeros
                + "): posible fraccionamiento");
    }

    private static String nombre(AbogadoEntity a) {
        return a.getNombres() + " " + a.getApellidos();
    }

    private static <E, T> T siHay(E valor, Function<E, T> extraer) {
        return valor == null ? null : extraer.apply(valor);
    }

    private ContratoDTORespuesta aRespuesta(ContratoEntity c) {
        ContratoDTORespuesta r = new ContratoDTORespuesta();
        r.setId(c.getId());
        r.setNumero(c.getNumero());
        r.setSerieCodigo(c.getSerie().getCodigo());
        r.setAnio(c.getAnio());
        r.setConsecutivo(c.getConsecutivo());
        r.setEstado(c.getEstado());
        r.setTipoContratoId(c.getTipoContrato().getId());
        r.setTipoContratoNombre(c.getTipoContrato().getNombre());
        r.setResponsableId(c.getResponsable().getId());
        r.setResponsableNombre(nombre(c.getResponsable()));
        r.setClaseContratoId(siHay(c.getClaseContrato(), x -> x.getId()));
        r.setClaseContratoEtiqueta(siHay(c.getClaseContrato(), x -> x.getEtiqueta()));
        r.setOtraClase(c.getOtraClase());
        r.setObjeto(c.getObjeto());
        r.setContratistaId(siHay(c.getContratista(), TerceroEntity::getId));
        r.setContratistaNombre(siHay(c.getContratista(), TerceroEntity::getNombreCompleto));
        r.setContratistaDocumento(siHay(c.getContratista(), TerceroEntity::getNumeroDocumento));
        OrdenadorGastoEntity o = c.getOrdenadorGasto();
        r.setOrdenadorGastoId(siHay(o, OrdenadorGastoEntity::getId));
        r.setOrdenadorNombre(siHay(o, x -> x.getTercero().getNombreCompleto()));
        r.setOrdenadorCedula(siHay(o, x -> x.getTercero().getNumeroDocumento()));
        r.setOrdenadorCargo(siHay(o, OrdenadorGastoEntity::getCargo));
        r.setTipoSeguimientoId(siHay(c.getTipoSeguimiento(), CatalogoItemEntity::getId));
        r.setTipoSeguimientoEtiqueta(siHay(c.getTipoSeguimiento(), CatalogoItemEntity::getEtiqueta));
        r.setValorSinIva(c.getValorSinIva());
        r.setIva(c.getIva());
        r.setValorInicial(c.getValorInicial());
        r.setPlazoDias(c.getPlazoDias());
        r.setNumeroPagos(c.getNumeroPagos());
        r.setFechaSuscripcion(c.getFechaSuscripcion());
        r.setFechaInicio(c.getFechaInicio());
        r.setFechaTerminacion(c.getFechaTerminacion());
        r.setFechaActaFinal(c.getFechaActaFinal());
        r.setMotivoAnulacion(c.getMotivoAnulacion());
        r.setFechaAnulacion(c.getFechaAnulacion());
        r.setPresupuesto(aRespuesta(c.getPresupuesto()));
        r.setObservacionesPendientes(c.getId() == null ? 0 : observacionRepositorio.countByContratoIdAndResueltaFalse(c.getId()));
        ResumenContratoDTORespuesta resumen = resumenServicio.resumen(c);
        r.setResumen(resumen);
        List<String> avisos = new ArrayList<>(advertencias(c));
        if (c.getId() != null && !c.estaAnulado()) {
            avisos.addAll(soporte.advertenciaCdp(c, resumen));
            avisos.addAll(soporte.advertenciaAdiciones(resumen));
        }
        r.setAdvertencias(avisos);
        r.setCreadoEn(c.getCreadoEn());
        r.setActualizadoEn(c.getActualizadoEn());
        return r;
    }

    private static PresupuestoDTORespuesta aRespuesta(ContratoPresupuestoEntity p) {
        PresupuestoDTORespuesta r = new PresupuestoDTORespuesta();
        r.setRubroId(siHay(p.getRubro(), x -> x.getId()));
        r.setRubroCodigo(siHay(p.getRubro(), x -> x.getCodigo()));
        r.setDependenciaAfectacion(siHay(p.getRubro(), x -> x.getNombre()));
        r.setProyectoId(siHay(p.getProyecto(), x -> x.getIdProyecto()));
        r.setProyectoNombre(siHay(p.getProyecto(), x -> x.getNombre()));
        r.setPaa(p.getPaa());
        r.setFuenteFinanciacionId(siHay(p.getFuenteFinanciacion(), CatalogoItemEntity::getId));
        r.setFuenteFinanciacionEtiqueta(siHay(p.getFuenteFinanciacion(), CatalogoItemEntity::getEtiqueta));
        r.setRecursosDeConvenio(p.getRecursosDeConvenio());
        TerceroEntity e = p.getEntidadFinanciadora();
        r.setEntidadFinanciadoraId(siHay(e, TerceroEntity::getId));
        r.setEntidadFinanciadoraNombre(siHay(e, TerceroEntity::getNombreCompleto));
        r.setEntidadFinanciadoraNit(siHay(e, TerceroEntity::getNumeroDocumento));
        r.setEntidadFinanciadoraDv(siHay(e, TerceroEntity::getDv));
        r.setDependenciaSolicitanteId(siHay(p.getDependenciaSolicitante(), x -> x.getId()));
        r.setDependenciaSolicitanteNombre(siHay(p.getDependenciaSolicitante(), x -> x.getNombre()));
        r.setNaturalezaGasto(p.getNaturalezaGasto());
        r.setTipoAnticipoId(siHay(p.getTipoAnticipo(), CatalogoItemEntity::getId));
        r.setTipoAnticipoEtiqueta(siHay(p.getTipoAnticipo(), CatalogoItemEntity::getEtiqueta));
        r.setValorAnticipo(p.getValorAnticipo());
        r.setPorcentajeAnticipo(p.getPorcentajeAnticipo());
        r.setFechaCertificacionPago(p.getFechaCertificacionPago());
        return r;
    }

    private static ContratoResumenDTORespuesta aResumen(ContratoEntity c, long pendientes, ResumenContratoDTORespuesta resumen) {
        ContratoResumenDTORespuesta r = new ContratoResumenDTORespuesta();
        r.setId(c.getId());
        r.setNumero(c.getNumero());
        r.setAnio(c.getAnio());
        r.setEstado(c.getEstado());
        r.setTipoContratoNombre(c.getTipoContrato().getNombre());
        r.setObjeto(c.getObjeto());
        r.setContratistaId(siHay(c.getContratista(), TerceroEntity::getId));
        r.setContratistaNombre(siHay(c.getContratista(), TerceroEntity::getNombreCompleto));
        r.setContratistaDocumento(siHay(c.getContratista(), TerceroEntity::getNumeroDocumento));
        r.setResponsableId(c.getResponsable().getId());
        r.setResponsableNombre(nombre(c.getResponsable()));
        r.setValorInicial(c.getValorInicial());
        r.setPlazoDias(c.getPlazoDias());
        r.setFechaSuscripcion(c.getFechaSuscripcion());
        r.setFechaInicio(c.getFechaInicio());
        r.setFechaTerminacion(c.getFechaTerminacion());
        r.setObservacionesPendientes(pendientes);
        r.setValorFinal(resumen.getValorFinal());
        r.setSaldo(resumen.getSaldo());
        r.setAvanceFisicoReal(resumen.getAvanceFisicoReal());
        r.setAvancePresupuestalReal(resumen.getAvancePresupuestalReal());
        return r;
    }
}
