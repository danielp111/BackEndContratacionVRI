package co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Campos comunes a crear y actualizar un proyecto. */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class ProyectoDTOPeticion {

    @NotBlank(message = "El nombre del proyecto es obligatorio")
    @Size(max = 400, message = "El nombre admite máximo 400 caracteres")
    private String nombre;

    @NotNull(message = "El director del proyecto es obligatorio")
    @Positive(message = "El director debe ser un identificador válido")
    private Long directorTerceroId;

    /** Opcional. Solo dígitos; los espacios internos se quitan al recibirlo. */
    @Size(max = 20, message = "El BPIN admite máximo 20 dígitos")
    @Pattern(regexp = "^\\d+$", message = "El BPIN solo admite dígitos")
    private String bpin;

    /**
     * Jackson usa este setter, así los espacios internos ("2023 0001 0012") se quitan antes de la
     * validación. El texto vacío ya llega como null (ver ConfiguracionJackson).
     */
    public void setBpin(String bpin) {
        this.bpin = sinEspacios(bpin);
    }

    static String sinEspacios(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.replaceAll("\\s+", "");
        return limpio.isEmpty() ? null : limpio;
    }
}
