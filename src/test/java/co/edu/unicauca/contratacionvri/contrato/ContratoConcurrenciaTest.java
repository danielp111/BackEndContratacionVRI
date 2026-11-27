package co.edu.unicauca.contratacionvri.contrato;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.ContratoCrearDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion.PagoDTOPeticion;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.IContratoServicio;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios.PagoServicio;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;

/**
 * Creación simultánea de contratos. En H2 se corre siempre; para MySQL real se puede apuntar a una
 * base temporal con -Dspring.datasource.url=jdbc:mysql://... (ver CLAUDE.md).
 */
class ContratoConcurrenciaTest extends ContratoPruebaBase {

    private static final int HILOS = 30;

    @Autowired
    private IContratoServicio servicio;

    @Autowired
    private PagoServicio pagoServicio;

    private ContratoCrearDTOPeticion peticion(Long tipoId, boolean invalida) {
        ContratoCrearDTOPeticion p = new ContratoCrearDTOPeticion();
        p.setTipoContratoId(tipoId);
        p.setAnio(ANIO);
        p.setResponsableId(laura.getId());
        p.setObjeto("Contrato simultáneo");
        if (invalida) {
            // Otra clase sin ser 30 OTROS: la creación falla y no debe consumir número.
            p.setClaseContratoId(servicios.getId());
            p.setOtraClase("No aplica");
        }
        return p;
    }

    /** Lanza todas las creaciones a la vez y devuelve los consecutivos de las que tuvieron éxito. */
    private List<Integer> crearEnParalelo(List<ContratoCrearDTOPeticion> peticiones) throws Exception {
        ExecutorService ejecutor = Executors.newFixedThreadPool(peticiones.size());
        CountDownLatch salida = new CountDownLatch(1);
        try {
            List<Future<Integer>> futuros = new ArrayList<>();
            for (ContratoCrearDTOPeticion p : peticiones) {
                Callable<Integer> tarea = () -> {
                    salida.await();
                    try {
                        return servicio.crear(p).getConsecutivo();
                    } catch (ReglaNegocioException e) {
                        return null;
                    }
                };
                futuros.add(ejecutor.submit(tarea));
            }
            salida.countDown();
            List<Integer> consecutivos = new ArrayList<>();
            for (Future<Integer> f : futuros) {
                Integer c = f.get(60, TimeUnit.SECONDS);
                if (c != null) {
                    consecutivos.add(c);
                }
            }
            return consecutivos;
        } finally {
            ejecutor.shutdownNow();
        }
    }

    @Test
    void creacionesSimultaneasRecibenNumerosDistintosYSeguidos() throws Exception {
        consecutivo(serieOps, ANIO, 108);
        List<Integer> consecutivos = crearEnParalelo(IntStream.range(0, HILOS).mapToObj(i -> peticion(ops.getId(), false)).toList());
        assertThat(consecutivos).hasSize(HILOS).doesNotHaveDuplicates()
                .containsExactlyInAnyOrderElementsOf(IntStream.rangeClosed(109, 108 + HILOS).boxed().toList());
        assertThat(contratoRepositorio.count()).isEqualTo(HILOS);
    }

    /**
     * Pagos simultáneos del mismo contrato: el bloqueo de la fila del contrato hace que cada uno vea el
     * anterior, así no se repiten números y el pago que excede el saldo se rechaza.
     */
    @Test
    void pagosSimultaneosRecibenNumerosDistintosYNoExcedenElSaldo() throws Exception {
        Map<String, Object> p = completo();
        p.put("valorSinIva", 1000000);
        p.put("fechaSuscripcion", fecha(3, 10));
        p.put("fechaInicio", fecha(3, 12));
        long contratoId = crearSuscritoId(p);

        // 25 pagos de 50.000 sobre un contrato de 1.000.000: caben 20.
        int intentos = 25;
        ExecutorService ejecutor = Executors.newFixedThreadPool(intentos);
        CountDownLatch salida = new CountDownLatch(1);
        try {
            List<Future<Integer>> futuros = new ArrayList<>();
            for (int i = 0; i < intentos; i++) {
                futuros.add(ejecutor.submit(() -> {
                    salida.await();
                    PagoDTOPeticion pago = new PagoDTOPeticion();
                    pago.setValor(BigDecimal.valueOf(50000));
                    pago.setFecha(LocalDate.of(ANIO, 4, 1));
                    try {
                        return pagoServicio.crear(contratoId, pago).numero();
                    } catch (ReglaNegocioException e) {
                        return null;
                    }
                }));
            }
            salida.countDown();
            List<Integer> numeros = new ArrayList<>();
            for (Future<Integer> f : futuros) {
                Integer n = f.get(60, TimeUnit.SECONDS);
                if (n != null) {
                    numeros.add(n);
                }
            }
            assertThat(numeros).containsExactlyInAnyOrderElementsOf(IntStream.rangeClosed(1, 20).boxed().toList());
            assertThat(pagoRepositorio.countByContratoId(contratoId)).isEqualTo(20);
        } finally {
            ejecutor.shutdownNow();
        }
    }

    @Test
    void lasCreacionesFallidasNoDejanHuecosNiLosTiposQueCompartenSerieRepiten() throws Exception {
        List<ContratoCrearDTOPeticion> peticiones = new ArrayList<>();
        for (int i = 0; i < HILOS; i++) {
            peticiones.add(peticion(i % 2 == 0 ? pasantia.getId() : judicatura.getId(), i % 3 == 0));
        }
        long validas = IntStream.range(0, HILOS).filter(i -> i % 3 != 0).count();
        List<Integer> consecutivos = crearEnParalelo(peticiones);
        assertThat(consecutivos).hasSize((int) validas)
                .containsExactlyInAnyOrderElementsOf(IntStream.rangeClosed(1, (int) validas).boxed().toList());
    }
}
