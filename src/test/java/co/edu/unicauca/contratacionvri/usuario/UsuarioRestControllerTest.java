package co.edu.unicauca.contratacionvri.usuario;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;
import co.edu.unicauca.contratacionvri.abogado.accesoADatos.modelos.TipoVinculacion;
import co.edu.unicauca.contratacionvri.abogado.fachadaServicios.DTOPeticion.AbogadoDTOPeticion;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.Catalogo;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.Rol;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.UsuarioEntity;
import co.edu.unicauca.contratacionvri.usuario.fachadaServicios.DTOPeticion.UsuarioDTOPeticion;

class UsuarioRestControllerTest extends PruebaIntegracionBase {

    private static final String URL = "/api/usuarios";

    @Test
    void creaUsuarioConCorreoEnMinusculasYSinVincularAGoogle() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new UsuarioDTOPeticion(" JPerez@Unicauca.edu.co ", "Juan Pérez", Rol.ABOGADO))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correo").value("jperez@unicauca.edu.co"))
                .andExpect(jsonPath("$.rol").value("ABOGADO"))
                .andExpect(jsonPath("$.vinculadoConGoogle").value(false))
                .andExpect(jsonPath("$.googleId").doesNotExist());
    }

    @Test
    void soloAceptaCorreosDeLaUniversidad() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new UsuarioDTOPeticion("juan@gmail.com", "Juan", Rol.CONSULTA))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correo").value("El correo debe ser del dominio @unicauca.edu.co"));

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new UsuarioDTOPeticion("juan@unicauca.edu.co.evil.com", "Juan", Rol.CONSULTA))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaCorreoRepetidoSinImportarMayusculas() throws Exception {
        crear(URL, new UsuarioDTOPeticion("ana@unicauca.edu.co", "Ana", Rol.FINANCIERO));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new UsuarioDTOPeticion("ANA@unicauca.edu.co", "Ana 2", Rol.CONSULTA))))
                .andExpect(status().isConflict());
    }

    @Test
    void noPermiteQuitarRolAbogadoSiTieneDatosDeAbogado() throws Exception {
        long id = crear(URL, new UsuarioDTOPeticion("abog@unicauca.edu.co", "Abogado", Rol.ABOGADO));
        long cedula = item(Catalogo.TIPO_IDENTIFICACION, "3", "CÉDULA DE CIUDADANÍA").getId();
        crear("/api/abogados", AbogadoDTOPeticion.builder()
                .usuarioId(id).tipoIdentificacionId(cedula).numeroDocumento("123")
                .nombres("A").apellidos("B").tipoVinculacion(TipoVinculacion.PLANTA)
                .fechaIngreso(LocalDate.of(2024, 1, 1)).build());

        mvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new UsuarioDTOPeticion("abog@unicauca.edu.co", "Abogado", Rol.CONSULTA))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0004"));

        // Cambiar el nombre manteniendo el rol sí se permite.
        mvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new UsuarioDTOPeticion("abog@unicauca.edu.co", "Abogado Nuevo", Rol.ABOGADO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Abogado Nuevo"));
    }

    @Test
    void noPermiteCambiarCorreoDeCuentaYaVinculadaAGoogle() throws Exception {
        UsuarioEntity u = usuario("vinculado@unicauca.edu.co", Rol.CONSULTA);
        u.setGoogleId("1234567890");
        usuarioRepositorio.save(u);

        mvc.perform(put(URL + "/" + u.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new UsuarioDTOPeticion("otro@unicauca.edu.co", "X", Rol.CONSULTA))))
                .andExpect(status().isBadRequest());

        mvc.perform(get(URL + "/" + u.getId())).andExpect(jsonPath("$.vinculadoConGoogle").value(true));
    }

    @Test
    void filtraPorRolYEstado() throws Exception {
        usuario("a@unicauca.edu.co", Rol.ABOGADO);
        usuario("b@unicauca.edu.co", Rol.ABOGADO);
        usuario("c@unicauca.edu.co", Rol.ADMIN);

        mvc.perform(get(URL).param("rol", "ABOGADO")).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get(URL).param("rol", "ADMIN").param("activo", "true")).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get(URL)).andExpect(jsonPath("$", hasSize(3)));
    }
}
