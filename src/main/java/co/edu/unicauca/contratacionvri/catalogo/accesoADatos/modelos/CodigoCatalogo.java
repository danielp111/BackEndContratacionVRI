package co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos;

/**
 * Códigos de los catálogos que el programa usa. Un ADMIN puede crear otros catálogos por la API,
 * pero el código solo conoce estos (por eso catalogo.codigo no se puede cambiar).
 */
public final class CodigoCatalogo {

    public static final String TIPO_IDENTIFICACION = "TIPO_IDENTIFICACION";
    public static final String NATURALEZA = "NATURALEZA";
    public static final String TIPO_SEGUIMIENTO = "TIPO_SEGUIMIENTO";
    public static final String TIPO_ANTICIPO = "TIPO_ANTICIPO";
    public static final String TIPO_ADICION = "TIPO_ADICION";
    public static final String FUENTE_FINANCIACION = "FUENTE_FINANCIACION";
    public static final String ASEGURADORA = "ASEGURADORA";

    private CodigoCatalogo() {
    }
}
