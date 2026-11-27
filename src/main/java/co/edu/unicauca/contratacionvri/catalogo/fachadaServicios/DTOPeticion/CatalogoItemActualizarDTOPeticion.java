package co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Actualizar un ítem: nombre, código SIRECI y orden. El código del ítem no cambia; si el cuerpo lo
 * trae, se acepta igual al actual y se rechaza con 400 si es distinto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoItemActualizarDTOPeticion {

    @NotBlank(message = "El nombre del ítem es obligatorio")
    @Size(max = 150, message = "El nombre admite máximo 150 caracteres")
    private String nombre;

    @Size(max = 10, message = "El código SIRECI admite máximo 10 caracteres")
    @Pattern(regexp = "^[0-9A-Za-z.-]+$", message = "El código SIRECI solo admite números, letras, puntos y guiones")
    private String codigoSireci;

    @Min(value = 0, message = "El orden no puede ser negativo")
    private Integer orden;

    private String codigo;
}
