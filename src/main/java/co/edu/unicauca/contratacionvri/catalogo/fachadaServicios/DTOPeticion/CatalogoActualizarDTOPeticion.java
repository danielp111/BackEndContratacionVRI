package co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Actualizar un catálogo: nombre y descripción. El código va en la URL y no cambia; si el cuerpo lo
 * trae, se acepta igual al actual y se rechaza con 400 si es distinto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoActualizarDTOPeticion {

    @NotBlank(message = "El nombre del catálogo es obligatorio")
    @Size(max = 120, message = "El nombre admite máximo 120 caracteres")
    private String nombre;

    @Size(max = 300, message = "La descripción admite máximo 300 caracteres")
    private String descripcion;

    private String codigo;
}
