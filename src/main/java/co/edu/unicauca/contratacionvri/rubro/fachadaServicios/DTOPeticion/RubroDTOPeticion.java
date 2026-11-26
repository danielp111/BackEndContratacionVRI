package co.edu.unicauca.contratacionvri.rubro.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Crear o actualizar un rubro. Los textos ya llegan recortados y sin espacios dobles (ConfiguracionJackson). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RubroDTOPeticion {

    /** Tal como lo entrega el sistema financiero: se conservan ceros a la izquierda, puntos y guiones. */
    @NotBlank(message = "El código del rubro es obligatorio")
    @Size(max = 40, message = "El código del rubro admite máximo 40 caracteres")
    @Pattern(regexp = "^[0-9A-Za-z]+([.-][0-9A-Za-z]+)*$",
            message = "El código del rubro solo admite números y letras separados por puntos o guiones, sin espacios")
    private String codigo;

    @NotBlank(message = "El nombre del rubro es obligatorio")
    @Size(max = 300, message = "El nombre admite máximo 300 caracteres")
    private String nombre;
}
