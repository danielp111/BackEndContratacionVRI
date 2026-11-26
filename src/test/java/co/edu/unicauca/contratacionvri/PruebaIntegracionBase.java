package co.edu.unicauca.contratacionvri;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.contratacionvri.abogado.accesoADatos.repositorios.AbogadoRepositorio;
import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.AmbitoClase;
import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.ClaseContratoEntity;
import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.repositorios.ClaseContratoRepositorio;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.Catalogo;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.repositorios.CatalogoItemRepositorio;
import co.edu.unicauca.contratacionvri.dependencia.accesoADatos.modelos.DependenciaEntity;
import co.edu.unicauca.contratacionvri.dependencia.accesoADatos.repositorios.DependenciaRepositorio;
import co.edu.unicauca.contratacionvri.proyecto.accesoADatos.repositorios.ProyectoRepositorio;
import co.edu.unicauca.contratacionvri.rubro.accesoADatos.modelos.RubroEntity;
import co.edu.unicauca.contratacionvri.rubro.accesoADatos.repositorios.RubroRepositorio;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.PersonaJuridicaEntity;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.repositorios.TerceroRepositorio;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.ConsecutivoEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.ConsecutivoId;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.SerieDocumentalEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.TipoContratoEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios.ConsecutivoRepositorio;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios.SerieDocumentalRepositorio;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios.TipoContratoRepositorio;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.Rol;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.UsuarioEntity;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.repositorios.UsuarioRepositorio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Base de las pruebas de integración: deja la base H2 vacía antes de cada prueba y trae utilidades. */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class PruebaIntegracionBase {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    @Autowired
    protected AbogadoRepositorio abogadoRepositorio;

    @Autowired
    protected UsuarioRepositorio usuarioRepositorio;

    @Autowired
    protected CatalogoItemRepositorio catalogoRepositorio;

    @Autowired
    protected DependenciaRepositorio dependenciaRepositorio;

    @Autowired
    protected TerceroRepositorio terceroRepositorio;

    @Autowired
    protected ProyectoRepositorio proyectoRepositorio;

    @Autowired
    protected RubroRepositorio rubroRepositorio;

    @Autowired
    protected ClaseContratoRepositorio claseContratoRepositorio;

    @Autowired
    protected ConsecutivoRepositorio consecutivoRepositorio;

    @Autowired
    private TransactionTemplate transaccionPruebas;

    @Autowired
    protected TipoContratoRepositorio tipoContratoRepositorio;

    @Autowired
    protected SerieDocumentalRepositorio serieRepositorio;

    @BeforeEach
    void limpiarBase() {
        // Primero las tablas que referencian a las demás: consecutivo y tipo_contrato apuntan a la serie,
        // proyecto a persona_natural (director) y las personas jurídicas a las naturales (representante).
        rubroRepositorio.deleteAll();
        claseContratoRepositorio.deleteAll();
        consecutivoRepositorio.deleteAll();
        tipoContratoRepositorio.deleteAll();
        serieRepositorio.deleteAll();
        proyectoRepositorio.deleteAll();
        terceroRepositorio.deleteAll(terceroRepositorio.findAll().stream()
                .filter(PersonaJuridicaEntity.class::isInstance).toList());
        terceroRepositorio.deleteAll();
        abogadoRepositorio.deleteAll();
        usuarioRepositorio.deleteAll();
        catalogoRepositorio.deleteAll();
        dependenciaRepositorio.deleteAll();
    }

    protected UsuarioEntity usuario(String correo, Rol rol) {
        UsuarioEntity u = new UsuarioEntity();
        u.setCorreo(correo);
        u.setNombre("Usuario " + correo);
        u.setRol(rol);
        u.setActivo(true);
        return usuarioRepositorio.save(u);
    }

    protected CatalogoItemEntity item(Catalogo catalogo, String codigo, String nombre) {
        CatalogoItemEntity i = new CatalogoItemEntity();
        i.setCatalogo(catalogo);
        i.setCodigoSireci(codigo);
        i.setNombre(nombre);
        i.setActivo(true);
        return catalogoRepositorio.save(i);
    }

    protected RubroEntity rubro(String codigo, String nombre) {
        RubroEntity r = new RubroEntity();
        r.setCodigo(codigo);
        r.setNombre(nombre);
        r.setActivo(true);
        return rubroRepositorio.save(r);
    }

    protected ClaseContratoEntity clase(AmbitoClase ambito, int codigo, String nombre) {
        return claseContratoRepositorio.save(new ClaseContratoEntity(ambito, codigo, nombre));
    }

    protected SerieDocumentalEntity serie(String codigo) {
        SerieDocumentalEntity s = new SerieDocumentalEntity();
        s.setCodigo(codigo);
        s.setActivo(true);
        return serieRepositorio.save(s);
    }

    protected TipoContratoEntity tipoContrato(String nombre, SerieDocumentalEntity serie) {
        TipoContratoEntity t = new TipoContratoEntity();
        t.setNombre(nombre);
        t.setSerie(serie);
        t.setActivo(true);
        return tipoContratoRepositorio.save(t);
    }

    /**
     * Deja el consecutivo de la serie en el año con el último número indicado. Dentro de una
     * transacción y con getReferenceById: una serie cargada fuera de la transacción llegaría
     * "detached" y Hibernate rechazaría guardar el consecutivo.
     */
    protected void consecutivo(SerieDocumentalEntity serie, int anio, int ultimoNumero) {
        transaccionPruebas.executeWithoutResult(estado -> {
            ConsecutivoEntity c = consecutivoRepositorio.findById(new ConsecutivoId(serie.getId(), anio))
                    .orElseGet(() -> new ConsecutivoEntity(serieRepositorio.getReferenceById(serie.getId()), anio));
            c.setUltimoNumero(ultimoNumero);
            consecutivoRepositorio.save(c);
        });
    }

    protected DependenciaEntity dependencia(String nombre) {
        DependenciaEntity d = new DependenciaEntity();
        d.setNombre(nombre);
        d.setActivo(true);
        return dependenciaRepositorio.save(d);
    }

    /** Hace POST, espera 201 y devuelve el id creado ("id", o "idProyecto" en proyectos). */
    protected long crear(String url, Object peticion) throws Exception {
        String cuerpo = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(peticion)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var respuesta = json.readTree(cuerpo);
        return (respuesta.has("id") ? respuesta.get("id") : respuesta.get("idProyecto")).asLong();
    }
}
