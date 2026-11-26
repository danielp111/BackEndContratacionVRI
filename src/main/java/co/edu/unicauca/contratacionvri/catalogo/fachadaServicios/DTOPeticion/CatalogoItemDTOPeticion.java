package co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.DTOPeticion;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.Catalogo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoItemDTOPeticion {

    @NotNull(message = "El catálogo es obligatorio")
    private Catalogo catalogo;

    @NotBlank(message = "El código SIRECI es obligatorio")
    @Size(max = 10, message = "El código SIRECI admite máximo 10 caracteres")
    @Pattern(regexp = "^[0-9A-Za-z.-]+$", message = "El código SIRECI solo admite números, letras, puntos y guiones")
    private String codigoSireci;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre admite máximo 150 caracteres")
    private String nombre;
}
