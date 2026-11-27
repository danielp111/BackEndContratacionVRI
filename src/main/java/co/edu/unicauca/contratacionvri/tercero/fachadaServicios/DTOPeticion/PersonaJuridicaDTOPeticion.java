package co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PersonaJuridicaDTOPeticion extends TerceroDTOPeticion {

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 200, message = "La razón social admite máximo 200 caracteres")
    private String razonSocial;

    /** Opcional. Si se envía, debe ser un tercero existente, activo y persona natural. */
    @Positive(message = "El representante legal debe ser un identificador válido")
    private Long representanteLegalId;

    /** Unión temporal o consorcio (para el SIRECI). Si no se envía, falso. */
    private Boolean esConsorcioUnionTemporal;
}
