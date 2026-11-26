package co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos;

/**
 * Naturaleza del tercero. Es además la columna discriminadora de la herencia JPA:
 * NATURAL → persona_natural, JURIDICA → persona_juridica. No cambia después de crear el tercero.
 */
public enum Naturaleza {
    NATURAL,
    JURIDICA;

    /** Valores para @DiscriminatorValue (deben ser constantes de compilación). */
    public static final String VALOR_NATURAL = "NATURAL";
    public static final String VALOR_JURIDICA = "JURIDICA";
}
