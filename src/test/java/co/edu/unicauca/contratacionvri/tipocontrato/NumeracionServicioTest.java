package co.edu.unicauca.contratacionvri.tipocontrato;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import co.edu.unicauca.contratacionvri.PruebaIntegracionBase;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.ConsecutivoId;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.SerieDocumentalEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.TipoContratoEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.NumeroContrato;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios.INumeracionServicio;

class NumeracionServicioTest extends PruebaIntegracionBase {

    @Autowired
    private INumeracionServicio numeracion;

    @Autowired
    private TransactionTemplate transaccion;

    private SerieDocumentalEntity serieOps;
    private SerieDocumentalEntity serieFormacion;
    private TipoContratoEntity ops;
    private TipoContratoEntity aprendizaje;
    private TipoContratoEntity pasantia;
    private TipoContratoEntity judicatura;

    @BeforeEach
    void datos() {
        serieOps = serie("6.1-17.13");
        serieFormacion = serie("6.1-17.3");
        ops = tipoContrato("OPS", serieOps);
        aprendizaje = tipoContrato("Aprendizaje", serieFormacion);
        pasantia = tipoContrato("Pasantía", serieFormacion);
        judicatura = tipoContrato("Judicatura", serieFormacion);
    }

    private int ultimoNumero(SerieDocumentalEntity serie, int anio) {
        return consecutivoRepositorio.findById(new ConsecutivoId(serie.getId(), anio)).orElseThrow().getUltimoNumero();
    }

    // ------------------------------------------------------------------ reglas básicas

    @Test
    void elPrimerNumeroDelAnioEs001YCreaLaFila() {
        NumeroContrato primero = numeracion.generarNumero(ops.getId(), 2027);
        assertThat(primero.numero()).isEqualTo("6.1-17.13/001");
        assertThat(primero.consecutivo()).isEqualTo(1);
        assertThat(primero.anio()).isEqualTo(2027);
        assertThat(primero.serieCodigo()).isEqualTo("6.1-17.13");

        assertThat(numeracion.generarNumero(ops.getId(), 2027).numero()).isEqualTo("6.1-17.13/002");
        assertThat(ultimoNumero(serieOps, 2027)).isEqualTo(2);
    }

    @Test
    void continuaDesdeElUltimoNumeroExistente() {
        consecutivo(serieOps, 2026, 108);
        assertThat(numeracion.generarNumero(ops.getId(), 2026).numero()).isEqualTo("6.1-17.13/109");
    }

    @Test
    void losTiposQueCompartenSerieNoRepitenNumeros() {
        assertThat(numeracion.generarNumero(aprendizaje.getId(), 2026).numero()).isEqualTo("6.1-17.3/001");
        assertThat(numeracion.generarNumero(pasantia.getId(), 2026).numero()).isEqualTo("6.1-17.3/002");
        assertThat(numeracion.generarNumero(judicatura.getId(), 2026).numero()).isEqualTo("6.1-17.3/003");
        assertThat(numeracion.generarNumero(aprendizaje.getId(), 2026).numero()).isEqualTo("6.1-17.3/004");
        assertThat(ultimoNumero(serieFormacion, 2026)).isEqualTo(4);
    }

    @Test
    void cadaSerieYCadaAnioTienenSuPropioConsecutivo() {
        numeracion.generarNumero(ops.getId(), 2026);
        numeracion.generarNumero(ops.getId(), 2026);
        assertThat(numeracion.generarNumero(ops.getId(), 2027).numero()).isEqualTo("6.1-17.13/001");
        assertThat(numeracion.generarNumero(pasantia.getId(), 2026).numero()).isEqualTo("6.1-17.3/001");
    }

    @Test
    void pasadoEl999CreceACuatroDigitos() {
        consecutivo(serieOps, 2026, 999);
        assertThat(numeracion.generarNumero(ops.getId(), 2026).numero()).isEqualTo("6.1-17.13/1000");
    }

    @Test
    void siLaTransaccionQueLlamaFallaElNumeroNoSeConsume() {
        consecutivo(serieOps, 2026, 108);
        transaccion.executeWithoutResult(estado -> {
            assertThat(numeracion.generarNumero(ops.getId(), 2026).numero()).isEqualTo("6.1-17.13/109");
            estado.setRollbackOnly(); // p. ej., falla la validación del contrato después de pedir el número
        });
        assertThat(ultimoNumero(serieOps, 2026)).isEqualTo(108);
        assertThat(numeracion.generarNumero(ops.getId(), 2026).numero()).isEqualTo("6.1-17.13/109");
    }

    @Test
    void rechazaTipoInexistenteOInactivoSerieInactivaYAnioFueraDeRango() {
        assertThatThrownBy(() -> numeracion.generarNumero(999_999L, 2026))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("no existe");

        ops.setActivo(false);
        tipoContratoRepositorio.save(ops);
        assertThatThrownBy(() -> numeracion.generarNumero(ops.getId(), 2026))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("OPS está inactivo");

        serieFormacion.setActivo(false);
        serieRepositorio.save(serieFormacion);
        assertThatThrownBy(() -> numeracion.generarNumero(pasantia.getId(), 2026))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("6.1-17.3 está inactiva");

        serieFormacion.setActivo(true);
        serieRepositorio.save(serieFormacion);
        assertThatThrownBy(() -> numeracion.generarNumero(pasantia.getId(), 1999)).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> numeracion.generarNumero(pasantia.getId(), 2101)).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> numeracion.generarNumero(pasantia.getId(), null)).isInstanceOf(ReglaNegocioException.class);

        assertThat(consecutivoRepositorio.count()).isZero();
    }

    // ------------------------------------------------------------------ concurrencia

    /** Lanza {@code tareas} llamadas a la vez (todas esperan la misma señal de salida) y devuelve los números. */
    private List<String> generarEnParalelo(int hilos, List<Long> tipos, int anio) throws Exception {
        ExecutorService ejecutor = Executors.newFixedThreadPool(hilos);
        CountDownLatch salida = new CountDownLatch(1);
        try {
            List<Future<String>> resultados = new ArrayList<>();
            for (Long tipo : tipos) {
                Callable<String> tarea = () -> {
                    salida.await();
                    return numeracion.generarNumero(tipo, anio).numero();
                };
                resultados.add(ejecutor.submit(tarea));
            }
            salida.countDown();
            List<String> numeros = new ArrayList<>();
            for (Future<String> r : resultados) {
                numeros.add(r.get(60, TimeUnit.SECONDS));
            }
            return numeros;
        } finally {
            ejecutor.shutdownNow();
        }
    }

    private static List<String> esperados(String serie, int desde, int hasta) {
        return IntStream.rangeClosed(desde, hasta).mapToObj(n -> INumeracionServicio.formatear(serie, n)).toList();
    }

    @Test
    void veinteLlamadasSimultaneasConLaFilaExistenteRecibenNumerosDistintosYSeguidos() throws Exception {
        consecutivo(serieOps, 2026, 108);

        List<String> numeros = generarEnParalelo(20, java.util.Collections.nCopies(20, ops.getId()), 2026);

        assertThat(numeros).doesNotHaveDuplicates()
                .containsExactlyInAnyOrderElementsOf(esperados("6.1-17.13", 109, 128));
        assertThat(ultimoNumero(serieOps, 2026)).isEqualTo(128);
    }

    @Test
    void veinteLlamadasSimultaneasEnUnAnioSinFilaLaCreanUnaSolaVez() throws Exception {
        List<String> numeros = generarEnParalelo(20, java.util.Collections.nCopies(20, ops.getId()), 2030);

        assertThat(numeros).doesNotHaveDuplicates()
                .containsExactlyInAnyOrderElementsOf(esperados("6.1-17.13", 1, 20));
        assertThat(ultimoNumero(serieOps, 2030)).isEqualTo(20);
        assertThat(consecutivoRepositorio.count()).isEqualTo(1);
    }

    @Test
    void tiposQueCompartenSerieEnParaleloTampocoRepiten() throws Exception {
        List<Long> tipos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tipos.add(aprendizaje.getId());
            tipos.add(pasantia.getId());
            tipos.add(judicatura.getId());
        }
        List<String> numeros = generarEnParalelo(15, tipos, 2026);

        assertThat(numeros).doesNotHaveDuplicates()
                .containsExactlyInAnyOrderElementsOf(esperados("6.1-17.3", 1, 30));
        assertThat(ultimoNumero(serieFormacion, 2026)).isEqualTo(30);
    }

    // ------------------------------------------------------------------ consultar el consecutivo

    @Test
    void consultaElConsecutivoActualDeUnaSerieYAnio() throws Exception {
        String url = "/api/series-documentales/" + serieOps.getId() + "/consecutivos/";

        // Sin números emitidos: 0 y el siguiente es /001 (no es un 404).
        mvc.perform(get(url + "2027"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ultimoNumero").value(0))
                .andExpect(jsonPath("$.siguienteNumero").value("6.1-17.13/001"));

        consecutivo(serieOps, 2026, 108);
        mvc.perform(get(url + "2026"))
                .andExpect(jsonPath("$.serieCodigo").value("6.1-17.13"))
                .andExpect(jsonPath("$.anio").value(2026))
                .andExpect(jsonPath("$.ultimoNumero").value(108))
                .andExpect(jsonPath("$.siguienteNumero").value("6.1-17.13/109"));

        // Consultar no reserva ni consume números.
        mvc.perform(get(url + "2026")).andExpect(jsonPath("$.ultimoNumero").value(108));

        mvc.perform(get("/api/series-documentales/999999/consecutivos/2026")).andExpect(status().isNotFound());
        mvc.perform(get(url + "1999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.anio").value("El año debe ser 2000 o posterior"));
    }
}
