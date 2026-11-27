package co.edu.unicauca.contratacionvri.contrato;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;
import co.edu.unicauca.contratacionvri.abogado.accesoADatos.modelos.AbogadoEntity;
import co.edu.unicauca.contratacionvri.abogado.accesoADatos.modelos.TipoVinculacion;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CodigoCatalogo;
import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.AmbitoClase;
import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.ClaseContratoEntity;
import co.edu.unicauca.contratacionvri.dependencia.accesoADatos.modelos.DependenciaEntity;
import co.edu.unicauca.contratacionvri.ordenador.accesoADatos.modelos.OrdenadorGastoEntity;
import co.edu.unicauca.contratacionvri.proyecto.accesoADatos.modelos.ProyectoEntity;
import co.edu.unicauca.contratacionvri.rubro.accesoADatos.modelos.RubroEntity;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.PersonaJuridicaEntity;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.PersonaNaturalEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.SerieDocumentalEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.TipoContratoEntity;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.Rol;

/**
 * Maestros que necesita un contrato y utilidades para armar las peticiones. El año de las pruebas es
 * el actual, porque solo se crean contratos del año actual o del anterior.
 */
abstract class ContratoPruebaBase extends PruebaIntegracionBase {

    protected static final String URL = "/api/contratos";
    protected static final int ANIO = LocalDate.now(ZoneId.of("America/Bogota")).getYear();

    protected SerieDocumentalEntity serieOps;
    protected SerieDocumentalEntity serieAprendizaje;
    protected TipoContratoEntity ops;
    protected TipoContratoEntity pasantia;
    protected TipoContratoEntity judicatura;
    protected AbogadoEntity laura;
    protected AbogadoEntity carlos;
    protected ClaseContratoEntity servicios;
    protected ClaseContratoEntity otros;
    protected CatalogoItemEntity cedula;
    protected CatalogoItemEntity supervisor;
    protected CatalogoItemEntity inversion;
    protected CatalogoItemEntity noPactado;
    protected CatalogoItemEntity anticipo;
    protected PersonaNaturalEntity ana;
    protected PersonaNaturalEntity pedro;
    protected PersonaJuridicaEntity dian;
    protected OrdenadorGastoEntity vicerrector;
    protected RubroEntity rubro;
    protected DependenciaEntity vri;
    protected ProyectoEntity proyecto;

    @BeforeEach
    void maestros() {
        serieOps = serie("6.1-17.13");
        serieAprendizaje = serie("6.1-17.3");
        ops = tipoContrato("Prestación de servicios", serieOps);
        pasantia = tipoContrato("Pasantía", serieAprendizaje);
        judicatura = tipoContrato("Judicatura", serieAprendizaje);

        cedula = item(CodigoCatalogo.TIPO_IDENTIFICACION, "CC", "3", "CÉDULA DE CIUDADANÍA");
        CatalogoItemEntity nit = item(CodigoCatalogo.TIPO_IDENTIFICACION, "NIT", "1", "NIT");
        supervisor = item(CodigoCatalogo.TIPO_SEGUIMIENTO, "SUPERVISOR", "2", "SUPERVISOR");
        inversion = item(CodigoCatalogo.FUENTE_FINANCIACION, "INVERSION", null, "Inversión");
        noPactado = item(CodigoCatalogo.TIPO_ANTICIPO, "NO_PACTADO", "3", "NO PACTADOS");
        anticipo = item(CodigoCatalogo.TIPO_ANTICIPO, "ANTICIPO", "1", "ANTICIPO");

        laura = abogado("laura@unicauca.edu.co", "1061000001", "Laura", "Muñoz");
        carlos = abogado("carlos@unicauca.edu.co", "1061000002", "Carlos", "Ruiz");
        servicios = clase(AmbitoClase.CONTRATO, 23, "PRESTACIÓN DE SERVICIOS");
        otros = clase(AmbitoClase.CONTRATO, 30, "OTROS");

        ana = personaNatural("1061111111", "Ana María", "Gómez López");
        pedro = personaNatural("1062222222", "Pedro", "Pérez");
        dian = new PersonaJuridicaEntity();
        dian.setTipoIdentificacion(nit);
        dian.setNumeroDocumento("800197268");
        dian.setDv("4");
        dian.setRazonSocial("DIAN");
        dian.setActivo(true);
        dian = terceroRepositorio.save(dian);

        vicerrector = new OrdenadorGastoEntity();
        vicerrector.setTercero(personaNatural("76314448", "FRANCISCO JOSE", "PINO CORREA"));
        vicerrector.setCargo("Vicerrector de Investigaciones");
        vicerrector.setFechaDesde(LocalDate.of(ANIO, 1, 1));
        vicerrector = ordenadorGastoRepositorio.save(vicerrector);

        rubro = rubro("1052002", "Museo de Historia Natural");
        vri = dependencia("Vicerrectoría de Investigaciones");
        proyecto = new ProyectoEntity(5019);
        proyecto.setNombre("Proyecto de prueba");
        proyecto.setDirector(ana);
        proyecto = proyectoRepositorio.save(proyecto);
    }

    protected AbogadoEntity abogado(String correo, String documento, String nombres, String apellidos) {
        AbogadoEntity a = new AbogadoEntity();
        a.setUsuario(usuario(correo, Rol.ABOGADO));
        a.setTipoIdentificacion(cedula);
        a.setNumeroDocumento(documento);
        a.setNombres(nombres);
        a.setApellidos(apellidos);
        a.setTipoVinculacion(TipoVinculacion.PLANTA);
        a.setActivo(true);
        return abogadoRepositorio.save(a);
    }

    protected PersonaNaturalEntity personaNatural(String documento, String nombres, String apellidos) {
        PersonaNaturalEntity p = new PersonaNaturalEntity();
        p.setTipoIdentificacion(cedula);
        p.setNumeroDocumento(documento);
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setActivo(true);
        return terceroRepositorio.save(p);
    }

    // ------------------------------------------------------------------ peticiones

    /** Lo mínimo para obtener un número: tipo OPS, año actual, Laura y un objeto. */
    protected Map<String, Object> minimo() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("tipoContratoId", ops.getId());
        m.put("anio", ANIO);
        m.put("responsableId", laura.getId());
        m.put("objeto", "Prestar servicios profesionales de apoyo al proyecto 5019");
        return m;
    }

    /** Todo lo que exige la suscripción (sin la fecha de suscripción). */
    protected Map<String, Object> completo() {
        Map<String, Object> m = minimo();
        m.put("claseContratoId", servicios.getId());
        m.put("contratistaId", ana.getId());
        m.put("ordenadorGastoId", vicerrector.getId());
        m.put("tipoSeguimientoId", supervisor.getId());
        m.put("valorSinIva", 10000000);
        m.put("iva", 0);
        m.put("plazoDias", 120);
        m.put("numeroPagos", 4);
        m.put("presupuesto", presupuesto());
        return m;
    }

    protected Map<String, Object> presupuesto() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("rubroId", rubro.getId());
        p.put("proyectoId", proyecto.getIdProyecto());
        p.put("paa", "PAA-2026-15");
        p.put("fuenteFinanciacionId", inversion.getId());
        p.put("recursosDeConvenio", false);
        p.put("dependenciaSolicitanteId", vri.getId());
        p.put("naturalezaGasto", "SPI");
        p.put("tipoAnticipoId", noPactado.getId());
        return p;
    }

    @SuppressWarnings("unchecked")
    protected static Map<String, Object> presupuestoDe(Map<String, Object> contrato) {
        return (Map<String, Object>) contrato.get("presupuesto");
    }

    protected static String fecha(int mes, int dia) {
        return LocalDate.of(ANIO, mes, dia).toString();
    }

    protected ResultActions enviar(String metodo, String url, Object cuerpo) throws Exception {
        var peticion = switch (metodo) {
            case "POST" -> post(url);
            case "PUT" -> put(url);
            default -> patch(url);
        };
        String texto = cuerpo instanceof String s ? s : json.writeValueAsString(cuerpo);
        return mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(texto));
    }

    /** Crea el contrato (espera 201) y devuelve la respuesta. */
    protected JsonNode crearContrato(Map<String, Object> peticion) throws Exception {
        String cuerpo = enviar("POST", URL, peticion).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(cuerpo);
    }

    protected long crearContratoId(Map<String, Object> peticion) throws Exception {
        return crearContrato(peticion).get("id").asLong();
    }

    /** Designa a Pedro como supervisor desde el 1 de enero (la suscripción lo exige). */
    protected void designarSupervisor(long contratoId) throws Exception {
        enviar("POST", URL + "/" + contratoId + "/seguimientos",
                Map.of("rol", "SUPERVISOR", "terceroId", pedro.getId(), "desde", fecha(1, 1)))
                .andExpect(status().isCreated());
    }

    /**
     * Crea el contrato sin fechas, designa el supervisor y luego registra las fechas con un PUT: es el
     * orden real, porque la suscripción exige el supervisor. Devuelve el resultado del PUT.
     */
    protected ResultActions crearYSuscribir(Map<String, Object> peticion) throws Exception {
        Map<String, Object> sinFechas = new LinkedHashMap<>(peticion);
        sinFechas.remove("fechaSuscripcion");
        sinFechas.remove("fechaInicio");
        sinFechas.remove("fechaTerminacion");
        long id = crearContratoId(sinFechas);
        designarSupervisor(id);
        return enviar("PUT", URL + "/" + id, peticion);
    }

    protected long crearSuscritoId(Map<String, Object> peticion) throws Exception {
        String cuerpo = crearYSuscribir(peticion).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(cuerpo).get("id").asLong();
    }

    protected void pagar(long contratoId, int valor, String fecha) throws Exception {
        enviar("POST", URL + "/" + contratoId + "/pagos", Map.of("valor", valor, "fecha", fecha)).andExpect(status().isCreated());
    }
}
