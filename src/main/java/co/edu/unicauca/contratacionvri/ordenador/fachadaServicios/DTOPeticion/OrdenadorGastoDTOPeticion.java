package co.edu.unicauca.contratacionvri.ordenador.fachadaServicios.DTOPeticion;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Crear o actualizar un periodo de ordenador del gasto. Los datos personales no van aquí: se toman
 * del tercero. Las reglas que cruzan campos (fecha_hasta ≥ fecha_desde, acto de delegación, periodos
 * que se cruzan) las valida el servicio.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenadorGastoDTOPeticion {

    @NotNull(message = "El tercero (persona natural) del ordenador es obligatorio")
    @Positive(message = "El tercero debe ser un identificador válido")
    private Long terceroId;

    @NotBlank(message = "El cargo es obligatorio")
    @Size(max = 120, message = "El cargo admite máximo 120 caracteres")
    private String cargo;

    /** Si no se envía, se toma como falso. */
    private Boolean esDelegado;

    @Size(max = 200, message = "El acto de delegación admite máximo 200 caracteres")
    private String actoDelegacion;

    @NotNull(message = "La fecha desde es obligatoria")
    private LocalDate fechaDesde;

    /** Opcional: vacía mientras el periodo sigue abierto. */
    private LocalDate fechaHasta;
}
