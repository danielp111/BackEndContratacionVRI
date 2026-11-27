package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.servicios;

import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;

/** CDP o RP: comparten forma y reglas; la ruta (/cdp, /rp) dice cuál. */
public enum TipoDocumentoPresupuestal {
    CDP("cdp", "el CDP"),
    RP("rp", "el RP");

    private final String ruta;
    private final String nombre;

    TipoDocumentoPresupuestal(String ruta, String nombre) {
        this.ruta = ruta;
        this.nombre = nombre;
    }

    /** "el CDP", "el RP", para los mensajes. */
    public String getNombre() {
        return nombre;
    }

    public static TipoDocumentoPresupuestal desdeRuta(String ruta) {
        for (TipoDocumentoPresupuestal t : values()) {
            if (t.ruta.equalsIgnoreCase(ruta)) {
                return t;
            }
        }
        throw new EntidadNoExisteException("La ruta /" + ruta + " no existe");
    }
}
