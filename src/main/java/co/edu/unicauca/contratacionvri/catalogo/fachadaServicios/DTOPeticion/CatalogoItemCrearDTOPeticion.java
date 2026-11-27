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

/** Crear un ítem. El catálogo viene en la URL (/api/catalogos/{codigo}/items). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoItemCrearDTOPeticion {

    /** Mnemónico que usa el programa (CC, NIT, SUPERVISOR). Se guarda en mayúsculas y no cambia. */
    @NotBlank(message = "El código del ítem es obligatorio")
    @Size(max = 40, message = "El código del ítem admite máximo 40 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9]+(_[A-Za-z0-9]+)*$",
            message = "El código del ítem solo admite letras, números y guion bajo, por ejemplo CC o PAGO_ANTICIPADO")
    private String codigo;

    /** Opcional: el número que pide el SIRECI ("3" de "3 CÉDULA DE CIUDADANÍA"). */
    @Size(max = 10, message = "El código SIRECI admite máximo 10 caracteres")
    @Pattern(regexp = "^[0-9A-Za-z.-]+$", message = "El código SIRECI solo admite números, letras, puntos y guiones")
    private String codigoSireci;

    /** Se guarda tal como se escribe, con tildes. */
    @NotBlank(message = "El nombre del ítem es obligatorio")
    @Size(max = 150, message = "El nombre admite máximo 150 caracteres")
    private String nombre;

    /** Posición en los selectores. Si no se envía, 0 (y entonces el orden lo da el nombre). */
    @Min(value = 0, message = "El orden no puede ser negativo")
    private Integer orden;
}
