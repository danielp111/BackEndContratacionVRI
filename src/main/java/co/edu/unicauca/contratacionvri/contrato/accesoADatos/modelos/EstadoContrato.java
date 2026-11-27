package co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos;

/**
 * Estado del contrato. Lo calcula el sistema a partir de las fechas (ver
 * {@link ContratoEntity#recalcularEstado()}); solo ANULADO se pone con una acción explícita.
 */
public enum EstadoContrato {
    /** Tiene número pero aún no se ha suscrito. */
    EN_TRAMITE,
    /** Tiene fecha de suscripción. Desde aquí entra al reporte SIRECI. */
    SUSCRITO,
    /** Tiene fecha de inicio. */
    EN_EJECUCION,
    /** Tiene fecha de terminación. */
    TERMINADO,
    /** Tiene fecha del acta final o de liquidación. */
    LIQUIDADO,
    /** Número asignado que no se usó. Irreversible; el número nunca se reutiliza. */
    ANULADO
}
