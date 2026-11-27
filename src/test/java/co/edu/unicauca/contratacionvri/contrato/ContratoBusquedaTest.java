package co.edu.unicauca.contratacionvri.contrato;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Listados paginados. Datos: tres OPS (001 de Laura con Ana y en ejecución; 002 de Laura con Pedro y
 * terminado; 003 de Carlos en trámite) y una pasantía de Carlos.
 */
class ContratoBusquedaTest extends ContratoPruebaBase {

    private long ops1;
    private long ops2;
    private long ops3;

    @BeforeEach
    void contratos() throws Exception {
        Map<String, Object> p = completo();
        p.put("objeto", "Apoyo al laboratorio de genética");
        p.put("fechaSuscripcion", fecha(2, 1));
        p.put("fechaInicio", fecha(2, 3));
        ops1 = crearContratoId(p);

        p = completo();
        p.put("contratistaId", pedro.getId());
        p.put("objeto", "Diseño de material divulgativo");
        p.put("fechaSuscripcion", fecha(3, 1));
        p.put("fechaInicio", fecha(3, 5));
        p.put("fechaTerminacion", fecha(6, 30));
        ops2 = crearContratoId(p);

        p = minimo();
        p.put("responsableId", carlos.getId());
        ops3 = crearContratoId(p);

        p = minimo();
        p.put("tipoContratoId", pasantia.getId());
        p.put("responsableId", carlos.getId());
        crearContratoId(p);
    }

    @Test
    void listaDelMasRecienteAlMasAntiguoConPaginacion() throws Exception {
        mvc.perform(get(URL).param("tipoContratoId", ops.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[*].numero", contains("6.1-17.13/003", "6.1-17.13/002", "6.1-17.13/001")))
                .andExpect(jsonPath("$.totalElementos").value(3));
        mvc.perform(get(URL).param("tamanio", "2").param("pagina", "1"))
                .andExpect(jsonPath("$.contenido.length()").value(2))
                .andExpect(jsonPath("$.totalElementos").value(4))
                .andExpect(jsonPath("$.totalPaginas").value(2))
                .andExpect(jsonPath("$.ultima").value(true));
        mvc.perform(get(URL).param("tamanio", "101")).andExpect(status().isBadRequest());
    }

    @Test
    void filaConLosDatosDelListado() throws Exception {
        mvc.perform(get(URL).param("numero", "6.1-17.13/001"))
                .andExpect(jsonPath("$.contenido.length()").value(1))
                .andExpect(jsonPath("$.contenido[0].id").value(ops1))
                .andExpect(jsonPath("$.contenido[0].estado").value("EN_EJECUCION"))
                .andExpect(jsonPath("$.contenido[0].tipoContratoNombre").value("Prestación de servicios"))
                .andExpect(jsonPath("$.contenido[0].contratistaNombre").value("Ana María Gómez López"))
                .andExpect(jsonPath("$.contenido[0].responsableNombre").value("Laura Muñoz"))
                .andExpect(jsonPath("$.contenido[0].valorInicial").value(10000000.0))
                .andExpect(jsonPath("$.contenido[0].fechaInicio").value(fecha(2, 3)))
                .andExpect(jsonPath("$.contenido[0].observacionesPendientes").value(0));
    }

    @Test
    void filtraPorNumeroCompletoConsecutivoOSerie() throws Exception {
        mvc.perform(get(URL).param("numero", "2"))
                .andExpect(jsonPath("$.contenido[*].id", contains((int) ops2)));
        mvc.perform(get(URL).param("numero", "6.1-17.3"))
                .andExpect(jsonPath("$.contenido[*].numero", contains("6.1-17.3/001")));
        mvc.perform(get(URL).param("numero", "/001"))
                .andExpect(jsonPath("$.contenido[*].numero", containsInAnyOrder("6.1-17.13/001", "6.1-17.3/001")));
        mvc.perform(get(URL).param("numero", "6.1-17.13/999")).andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void filtraPorEstadoResponsableContratistaYObjeto() throws Exception {
        mvc.perform(get(URL).param("estado", "TERMINADO")).andExpect(jsonPath("$.contenido[*].id", contains((int) ops2)));
        mvc.perform(get(URL).param("responsableId", carlos.getId().toString())).andExpect(jsonPath("$.totalElementos").value(2));
        mvc.perform(get(URL).param("contratista", "106222")).andExpect(jsonPath("$.contenido[*].id", contains((int) ops2)));
        mvc.perform(get(URL).param("contratista", "gómez")).andExpect(jsonPath("$.contenido[*].id", contains((int) ops1)));
        mvc.perform(get(URL).param("texto", "GENÉTICA")).andExpect(jsonPath("$.contenido[*].id", contains((int) ops1)));
        mvc.perform(get(URL).param("texto", "100%")).andExpect(jsonPath("$.totalElementos").value(0));
        mvc.perform(get(URL).param("anio", String.valueOf(ANIO - 1))).andExpect(jsonPath("$.totalElementos").value(0));
        mvc.perform(get(URL).param("estado", "NO_EXISTE")).andExpect(status().isBadRequest());
    }

    @Test
    void contratosDeUnAbogadoConFiltrosDeFechas() throws Exception {
        String url = "/api/abogados/" + laura.getId() + "/contratos";
        mvc.perform(get(url)).andExpect(jsonPath("$.contenido[*].id", contains((int) ops2, (int) ops1)));
        mvc.perform(get(url).param("inicioDesde", fecha(3, 1)))
                .andExpect(jsonPath("$.contenido[*].id", contains((int) ops2)));
        mvc.perform(get(url).param("inicioDesde", fecha(2, 1)).param("inicioHasta", fecha(2, 28)))
                .andExpect(jsonPath("$.contenido[*].id", contains((int) ops1)));
        mvc.perform(get(url).param("terminacionHasta", fecha(12, 31)))
                .andExpect(jsonPath("$.contenido[*].id", contains((int) ops2)));
        mvc.perform(get("/api/abogados/" + carlos.getId() + "/contratos").param("estado", "EN_TRAMITE"))
                .andExpect(jsonPath("$.totalElementos").value(2));

        mvc.perform(get(url).param("inicioDesde", fecha(3, 1)).param("inicioHasta", fecha(2, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("En el filtro de fecha de inicio, la fecha hasta (" + fecha(2, 1)
                        + ") no puede ser anterior a la fecha desde (" + fecha(3, 1) + ")"));
        mvc.perform(get(url).param("inicioDesde", "01/03/2026")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/abogados/999999/contratos")).andExpect(status().isNotFound());
    }

    @Test
    void filtraPorObservacionesPendientes() throws Exception {
        String url = "/api/abogados/" + laura.getId() + "/contratos";
        enviar("POST", URL + "/" + ops1 + "/observaciones", Map.of("campo", "OBJETO", "texto", "Revisar redacción"))
                .andExpect(status().isCreated());
        long resuelta = json.readTree(enviar("POST", URL + "/" + ops2 + "/observaciones",
                        Map.of("campo", "IVA", "texto", "Confirmar IVA")).andReturn().getResponse().getContentAsString())
                .get("id").asLong();
        enviar("PATCH", URL + "/" + ops2 + "/observaciones/" + resuelta + "/resolver", Map.of()).andExpect(status().isOk());

        mvc.perform(get(url).param("conObservaciones", "true"))
                .andExpect(jsonPath("$.contenido[*].id", contains((int) ops1)))
                .andExpect(jsonPath("$.contenido[0].observacionesPendientes").value(1));
        mvc.perform(get(url).param("conObservaciones", "false"))
                .andExpect(jsonPath("$.contenido[*].id", contains((int) ops2)));
        mvc.perform(get(URL).param("responsableId", carlos.getId().toString()).param("numero", "3"))
                .andExpect(jsonPath("$.contenido[*].id", contains((int) ops3)));
    }
}
