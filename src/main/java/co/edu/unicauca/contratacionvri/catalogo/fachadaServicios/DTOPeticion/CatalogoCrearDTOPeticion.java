package co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoCrearDTOPeticion {

    /** Se guarda en mayúsculas y no se puede cambiar después: el programa lo usa. */
    @NotBlank(message = "El código del catálogo es obligatorio")
    @Size(max = 40, message = "El código del catálogo admite máximo 40 caracteres")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9]*(_[A-Za-z0-9]+)*$",
            message = "El código del catálogo solo admite letras, números y guion bajo, por ejemplo TIPO_IDENTIFICACION")
    private String codigo;

    @NotBlank(message = "El nombre del catálogo es obligatorio")
    @Size(max = 120, message = "El nombre admite máximo 120 caracteres")
    private String nombre;

    @Size(max = 300, message = "La descripción admite máximo 300 caracteres")
    private String descripcion;
}
