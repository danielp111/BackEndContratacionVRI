package co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTOPeticion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Actualizar un proyecto. El código viene en la URL y no se puede cambiar. {@code idProyecto}
 * es opcional: se acepta si coincide con el de la URL (hay clientes que reenvían el objeto
 * completo) y se rechaza con 400 si es distinto.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ProyectoActualizarDTOPeticion extends ProyectoDTOPeticion {

    private Integer idProyecto;
}
