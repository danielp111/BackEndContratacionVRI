package co.edu.unicauca.contratacionvri;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.contratacionvri.abogado.accesoADatos.repositorios.AbogadoRepositorio;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.Catalogo;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.repositorios.CatalogoItemRepositorio;
import co.edu.unicauca.contratacionvri.dependencia.accesoADatos.modelos.DependenciaEntity;
import co.edu.unicauca.contratacionvri.dependencia.accesoADatos.repositorios.DependenciaRepositorio;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.PersonaJuridicaEntity;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.repositorios.TerceroRepositorio;
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

    @BeforeEach
    void limpiarBase() {
        // Primero las tablas que referencian a las demás. Las personas jurídicas antes que las
        // naturales, porque pueden apuntar a ellas como representante legal.
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

    protected DependenciaEntity dependencia(String nombre) {
        DependenciaEntity d = new DependenciaEntity();
        d.setNombre(nombre);
        d.setActivo(true);
        return dependenciaRepositorio.save(d);
    }

    /** Hace POST, espera 201 y devuelve el id creado. */
    protected long crear(String url, Object peticion) throws Exception {
        String cuerpo = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(peticion)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(cuerpo).get("id").asLong();
    }
}
