package co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.servicios;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.ConsecutivoEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.ConsecutivoId;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.SerieDocumentalEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.TipoContratoEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios.ConsecutivoRepositorio;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios.SerieDocumentalRepositorio;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios.TipoContratoRepositorio;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.ConsecutivoDTORespuesta;
import co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta.NumeroContrato;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NumeracionServicioImpl implements INumeracionServicio {

    private final TipoContratoRepositorio tipoRepositorio;
    private final SerieDocumentalRepositorio serieRepositorio;
    private final ConsecutivoRepositorio consecutivoRepositorio;

    /**
     * Todo en una sola transacción (una sola conexión):
     * <ol>
     *   <li>Validar tipo, serie y año.</li>
     *   <li>Asegurar que exista la fila (serie, año): INSERT ... ON DUPLICATE KEY UPDATE, que no cambia
     *       nada si ya existía. Ver ConsecutivoRepositorio.asegurarFila.</li>
     *   <li>Bloquear la fila con SELECT ... FOR UPDATE: las demás peticiones de la misma serie y año
     *       esperan aquí hasta que esta transacción termine.</li>
     *   <li>Incrementar y devolver el número. Se confirma al terminar la transacción; si la
     *       transacción que llama falla, el número no se consume.</li>
     * </ol>
     */
    @Override
    @Transactional
    public NumeroContrato generarNumero(Long tipoContratoId, Integer anio) {
        TipoContratoEntity tipo = tipoRepositorio.findById(tipoContratoId).orElseThrow(() ->
                new ReglaNegocioException("El tipo de contrato con id " + tipoContratoId + " no existe"));
        if (!Boolean.TRUE.equals(tipo.getActivo())) {
            throw new ReglaNegocioException("El tipo de contrato " + tipo.getNombre()
                    + " está inactivo y no se puede usar en contratos nuevos");
        }
        SerieDocumentalEntity serie = tipo.getSerie();
        if (!Boolean.TRUE.equals(serie.getActivo())) {
            throw new ReglaNegocioException("La serie documental " + serie.getCodigo()
                    + " está inactiva; el tipo " + tipo.getNombre() + " no puede generar números");
        }
        validarAnio(anio);

        consecutivoRepositorio.asegurarFila(serie.getId(), anio);
        ConsecutivoEntity consecutivo = consecutivoRepositorio.bloquear(new ConsecutivoId(serie.getId(), anio))
                .orElseThrow(() -> new IllegalStateException("No se pudo crear el consecutivo " + serie.getCodigo() + "/" + anio));

        int numero = consecutivo.incrementar();
        return new NumeroContrato(INumeracionServicio.formatear(serie.getCodigo(), numero),
                serie.getId(), serie.getCodigo(), anio, numero);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsecutivoDTORespuesta consultarConsecutivo(Long serieId, Integer anio) {
        SerieDocumentalEntity serie = serieRepositorio.findById(serieId)
                .orElseThrow(() -> new EntidadNoExisteException("No existe una serie documental con id " + serieId));
        validarAnio(anio);
        // Lectura sin bloqueo: consultar no reserva ni consume números.
        int ultimo = consecutivoRepositorio.findById(new ConsecutivoId(serieId, anio))
                .map(ConsecutivoEntity::getUltimoNumero).orElse(0);
        return new ConsecutivoDTORespuesta(serieId, serie.getCodigo(), anio, ultimo,
                INumeracionServicio.formatear(serie.getCodigo(), ultimo + 1));
    }

    private void validarAnio(Integer anio) {
        if (anio == null || anio < ANIO_MINIMO || anio > ANIO_MAXIMO) {
            throw new ReglaNegocioException("El año debe estar entre " + ANIO_MINIMO + " y " + ANIO_MAXIMO);
        }
    }
}
