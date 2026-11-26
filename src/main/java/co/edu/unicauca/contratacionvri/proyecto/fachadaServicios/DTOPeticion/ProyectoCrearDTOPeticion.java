package co.edu.unicauca.contratacionvri.proyecto.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Crear un proyecto: el código lo digita el usuario. */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ProyectoCrearDTOPeticion extends ProyectoDTOPeticion {

    @NotNull(message = "El código del proyecto es obligatorio")
    @Positive(message = "El código del proyecto debe ser un número positivo")
    private Integer idProyecto;
}
