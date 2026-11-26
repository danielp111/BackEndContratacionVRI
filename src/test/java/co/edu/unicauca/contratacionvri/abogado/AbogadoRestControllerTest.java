package co.edu.unicauca.contratacionvri.abogado;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;
import co.edu.unicauca.contratacionvri.abogado.accesoADatos.modelos.TipoVinculacion;
import co.edu.unicauca.contratacionvri.abogado.fachadaServicios.DTOPeticion.AbogadoDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.Catalogo;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.dependencia.accesoADatos.modelos.DependenciaEntity;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.Rol;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.UsuarioEntity;

class AbogadoRestControllerTest extends PruebaIntegracionBase {

    private static final String URL = "/api/abogados";

    private CatalogoItemEntity cedula;
    private DependenciaEntity juridica;

    @BeforeEach
    void datosBase() {
        cedula = item(Catalogo.TIPO_IDENTIFICACION, "3", "CÉDULA DE CIUDADANÍA");
        juridica = dependencia("OFICINA JURÍDICA");
    }

    private AbogadoDTOPeticion peticion(UsuarioEntity usuario, String documento) {
        return AbogadoDTOPeticion.builder()
                .usuarioId(usuario.getId())
                .tipoIdentificacionId(cedula.getId())
                .numeroDocumento(documento)
                .nombres("  Ana   María ")
                .apellidos("Pérez Gómez")
                .correoInstitucional("AnaPerez@Unicauca.edu.co")
                .telefono("+57 300 123 4567")
                .tarjetaProfesional("123456-T")
                .cargo("Abogada")
                .tipoVinculacion(TipoVinculacion.CONTRATISTA)
                .dependenciaId(juridica.getId())
                .fechaIngreso(LocalDate.of(2025, 2, 1))
                .build();
    }

    private UsuarioEntity abogadoUsuario(String correo) {
        return usuario(correo, Rol.ABOGADO);
    }

    @Test
    void creaAbogadoConSusRelacionesYDatosNormalizados() throws Exception {
        UsuarioEntity u = abogadoUsuario("ana@unicauca.edu.co");

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(peticion(u, "1061000111"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", notNullValue()))
                .andExpect(jsonPath("$.usuarioId").value(u.getId()))
                .andExpect(jsonPath("$.usuarioCorreo").value("ana@unicauca.edu.co"))
                .andExpect(jsonPath("$.tipoIdentificacionId").value(cedula.getId()))
                .andExpect(jsonPath("$.tipoIdentificacionEtiqueta").value("3 CÉDULA DE CIUDADANÍA"))
                .andExpect(jsonPath("$.dependenciaId").value(juridica.getId()))
                .andExpect(jsonPath("$.dependenciaNombre").value("OFICINA JURÍDICA"))
                .andExpect(jsonPath("$.nombres").value("Ana María"))
                .andExpect(jsonPath("$.correoInstitucional").value("anaperez@unicauca.edu.co"))
                .andExpect(jsonPath("$.fechaIngreso").value("2025-02-01"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.creadoEn").isNotEmpty());
    }

    @Test
    void rechazaRelacionesInvalidas() throws Exception {
        UsuarioEntity financiero = usuario("fin@unicauca.edu.co", Rol.FINANCIERO);
        UsuarioEntity u = abogadoUsuario("ana@unicauca.edu.co");
        CatalogoItemEntity naturaleza = item(Catalogo.NATURALEZA, "1", "PERSONA NATURAL");

        // El usuario no tiene rol ABOGADO.
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(peticion(financiero, "1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0004"));

        // El usuario no existe.
        AbogadoDTOPeticion sinUsuario = peticion(u, "2");
        sinUsuario.setUsuarioId(999_999L);
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(sinUsuario)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El usuario con id 999999 no existe"));

        // El ítem es de otro catálogo.
        AbogadoDTOPeticion otroCatalogo = peticion(u, "3");
        otroCatalogo.setTipoIdentificacionId(naturaleza.getId());
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(otroCatalogo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0004"));

        // La dependencia está inactiva.
        juridica.setActivo(false);
        dependenciaRepositorio.save(juridica);
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(peticion(u, "4"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La dependencia OFICINA JURÍDICA está inactiva"));

        assertThat(abogadoRepositorio.count()).isZero();
    }

    @Test
    void rechazaDatosInvalidosConDetallePorCampo() throws Exception {
        AbogadoDTOPeticion p = peticion(abogadoUsuario("a@unicauca.edu.co"), "");
        p.setCorreoInstitucional("ana@gmail.com");
        p.setTipoVinculacion(null);

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(p)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0005"))
                .andExpect(jsonPath("$.errores.numeroDocumento").exists())
                .andExpect(jsonPath("$.errores.correoInstitucional").exists())
                .andExpect(jsonPath("$.errores.tipoVinculacion").exists());
    }

    @Test
    void rechazaUsuarioODocumentoRepetido() throws Exception {
        UsuarioEntity u1 = abogadoUsuario("u1@unicauca.edu.co");
        UsuarioEntity u2 = abogadoUsuario("u2@unicauca.edu.co");
        crear(URL, peticion(u1, "1061000200"));

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(peticion(u1, "1061000201"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigoError").value("GC-0002"));

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(peticion(u2, "1061000200"))))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizaSinAlterarLasEntidadesRelacionadas() throws Exception {
        UsuarioEntity u1 = abogadoUsuario("u1@unicauca.edu.co");
        UsuarioEntity u2 = abogadoUsuario("u2@unicauca.edu.co");
        DependenciaEntity investigaciones = dependencia("VICERRECTORÍA DE INVESTIGACIONES");
        long id = crear(URL, peticion(u1, "1061000300"));

        AbogadoDTOPeticion cambio = peticion(u2, "1061000300");
        cambio.setCargo("Profesional especializado");
        cambio.setTipoVinculacion(TipoVinculacion.PLANTA);
        cambio.setDependenciaId(investigaciones.getId());

        mvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(cambio)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.usuarioId").value(u2.getId()))
                .andExpect(jsonPath("$.dependenciaNombre").value("VICERRECTORÍA DE INVESTIGACIONES"))
                .andExpect(jsonPath("$.cargo").value("Profesional especializado"))
                .andExpect(jsonPath("$.tipoVinculacion").value("PLANTA"))
                .andExpect(jsonPath("$.activo").value(true));

        // Las entidades relacionadas no deben haberse modificado (ids, nombres).
        assertThat(usuarioRepositorio.findById(u1.getId()).orElseThrow().getCorreo()).isEqualTo("u1@unicauca.edu.co");
        assertThat(dependenciaRepositorio.findById(juridica.getId()).orElseThrow().getNombre()).isEqualTo("OFICINA JURÍDICA");
        assertThat(usuarioRepositorio.count()).isEqualTo(2);
        assertThat(dependenciaRepositorio.count()).isEqualTo(2);
    }

    @Test
    void permiteEditarAunqueSuDependenciaSeHayaInactivado() throws Exception {
        long id = crear(URL, peticion(abogadoUsuario("u@unicauca.edu.co"), "1061000500"));
        UsuarioEntity mismoUsuario = usuarioRepositorio.findAll().get(0);
        juridica.setActivo(false);
        dependenciaRepositorio.save(juridica);

        AbogadoDTOPeticion cambio = peticion(mismoUsuario, "1061000500");
        cambio.setTelefono("3001112233");
        mvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(cambio)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefono").value("3001112233"));
    }

    @Test
    void inactivaYFiltraPorEstado() throws Exception {
        long id = crear(URL, peticion(abogadoUsuario("a@unicauca.edu.co"), "1061000400"));
        crear(URL, peticion(abogadoUsuario("b@unicauca.edu.co"), "1061000401"));

        mvc.perform(patch(URL + "/" + id + "/estado").param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mvc.perform(get(URL).param("activo", "true")).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get(URL).param("activo", "false")).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get(URL)).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void respondeErroresConFormatoUniforme() throws Exception {
        mvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoError").value("GC-0003"))
                .andExpect(jsonPath("$.mensaje").value("No existe un abogado con id 999999"))
                .andExpect(jsonPath("$.metodo").value("GET"));

        mvc.perform(get(URL + "/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0006"));

        mvc.perform(get(URL + "/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.id").value("El id debe ser positivo"));

        mvc.perform(patch(URL + "/1/estado"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0006"));
    }
}
