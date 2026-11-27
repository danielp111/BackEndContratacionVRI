package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ContratoCdpRepositorio;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios.ContratoRepositorio;
import co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta.ResumenContratoDTORespuesta;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import lombok.RequiredArgsConstructor;

/** Utilidades comunes de los servicios del detalle del contrato (CDP, RP, pagos...). */
@Component
@RequiredArgsConstructor
public class ContratoDetalleSoporte {

    static final DateTimeFormatter FECHA = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final BigDecimal MITAD = new BigDecimal("0.5");

    private final ContratoRepositorio contratoRepositorio;
    private final ResumenContratoServicio resumenServicio;
    private final ContratoCdpRepositorio cdpRepositorio;

    /** Para leer: 404 si no existe. */
    public ContratoEntity contrato(Long id) {
        return contratoRepositorio.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("No existe un contrato con id " + id));
    }

    /** Para modificar su detalle: 404 si no existe, 400 si está anulado. */
    public ContratoEntity contratoEditable(Long id) {
        return noAnulado(contrato(id));
    }

    /**
     * Igual que {@link #contratoEditable} pero bloquea la fila del contrato hasta que termine la
     * transacción. Para los cambios que tocan el dinero o numeran (pagos, modificaciones).
     */
    public ContratoEntity contratoBloqueado(Long id) {
        return noAnulado(contratoRepositorio.bloquear(id)
                .orElseThrow(() -> new EntidadNoExisteException("No existe un contrato con id " + id)));
    }

    /**
     * El saldo no puede quedar negativo: valor final ≥ pagos acumulados + anticipo. Se llama después
     * del cambio (el resumen consulta la base, y JPA envía antes los cambios pendientes).
     */
    public void validarSaldo(ContratoEntity contrato) {
        ResumenContratoDTORespuesta r = resumenServicio.resumen(contrato);
        if (r.getSaldo() != null && r.getSaldo().signum() < 0) {
            throw new ReglaNegocioException("El saldo del contrato " + contrato.getNumero() + " quedaría negativo ("
                    + r.getSaldo().toPlainString() + "): el valor final es " + r.getValorFinal().toPlainString()
                    + ", los pagos suman " + r.getPagosAcumulados().toPlainString() + " y el anticipo es "
                    + r.getValorAnticipo().toPlainString());
        }
    }

    public ResumenContratoDTORespuesta resumen(ContratoEntity contrato) {
        return resumenServicio.resumen(contrato);
    }

    /** Aviso (no bloquea) si los CDP con valor no alcanzan a cubrir el valor final. */
    public List<String> advertenciaCdp(ContratoEntity contrato, ResumenContratoDTORespuesta resumen) {
        BigDecimal suma = cdpRepositorio.sumarValor(contrato.getId());
        BigDecimal valorFinal = resumen.getValorFinal();
        if (suma == null || valorFinal == null || suma.compareTo(valorFinal) >= 0) {
            return List.of();
        }
        return List.of("La suma de los CDP (" + dinero(suma) + ") es menor que el valor final del contrato ("
                + dinero(valorFinal) + ")");
    }

    /** Aviso (no bloquea) si las adiciones en valor superan el 50 % del valor inicial (límite de la Ley 80). */
    public List<String> advertenciaAdiciones(ResumenContratoDTORespuesta resumen) {
        BigDecimal inicial = resumen.getValorInicial();
        BigDecimal adiciones = resumen.getAdicionesValor();
        if (inicial == null || adiciones == null || adiciones.compareTo(inicial.multiply(MITAD)) <= 0) {
            return List.of();
        }
        return List.of("Las adiciones en valor (" + dinero(adiciones) + ") superan el 50 % del valor inicial ("
                + dinero(inicial) + ")");
    }

    static void noAnteriorA(LocalDate fecha, String nombre, LocalDate limite, String nombreLimite) {
        if (fecha != null && limite != null && fecha.isBefore(limite)) {
            throw new ReglaNegocioException("La fecha " + nombre + " (" + fecha.format(FECHA)
                    + ") no puede ser anterior a la fecha de " + nombreLimite + " del contrato (" + limite.format(FECHA) + ")");
        }
    }

    static String texto(String valor) {
        return Textos.limpiar(valor);
    }

    static String dinero(BigDecimal valor) {
        return valor == null ? "0" : valor.toPlainString();
    }

    private static ContratoEntity noAnulado(ContratoEntity c) {
        if (c.estaAnulado()) {
            throw new ReglaNegocioException("El contrato " + c.getNumero() + " está anulado y no se puede modificar");
        }
        return c;
    }
}
