package co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.servicios;

import java.time.LocalDate;
import java.util.List;

import co.edu.unicauca.contratacionvri.ordenador.accesoADatos.modelos.OrdenadorGastoEntity;
import co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.DTOPeticion.OrdenadorGastoDTOPeticion;
import co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.DTORespuesta.OrdenadorGastoDTORespuesta;

public interface IOrdenadorGastoServicio {

    /**
     * Filtros opcionales: {@code texto} (nombre, cédula o cargo), {@code activo}, y {@code vigenteEn}
     * para los selectores (periodos activos que cubren esa fecha).
     */
    List<OrdenadorGastoDTORespuesta> listar(String texto, Boolean activo, LocalDate vigenteEn);

    OrdenadorGastoDTORespuesta consultarPorId(Long id);

    /** Falla con 409 si se cruza con otro periodo activo del mismo tercero y cargo. */
    OrdenadorGastoDTORespuesta crear(OrdenadorGastoDTOPeticion peticion);

    /** Si ya hay contratos, el tercero, el cargo y la fecha desde no se pueden cambiar (400). */
    OrdenadorGastoDTORespuesta actualizar(Long id, OrdenadorGastoDTOPeticion peticion);

    /** Pone fecha_hasta (≥ fecha_desde). Se permite aunque haya contratos. */
    OrdenadorGastoDTORespuesta cerrarPeriodo(Long id, LocalDate fechaHasta);

    /** Activa o inactiva. Activar falla con 409 si el periodo se cruza con otro activo. */
    OrdenadorGastoDTORespuesta cambiarEstado(Long id, boolean activo);

    /**
     * Para el dominio de contratos. Devuelve el periodo si existe; si cambia respecto al que ya tenía
     * el contrato ({@code actual}), debe estar activo. Si se da {@code fechaSuscripcion}, debe caer
     * dentro del periodo (siempre, aunque sea el mismo). Lanza 400 si no cumple.
     */
    OrdenadorGastoEntity obtenerOrdenadorValido(Long id, OrdenadorGastoEntity actual, LocalDate fechaSuscripcion);
}
