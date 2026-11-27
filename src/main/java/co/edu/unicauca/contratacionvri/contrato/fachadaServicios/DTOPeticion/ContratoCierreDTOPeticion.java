package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTOPeticion;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Datos del cierre que registra el financiero (los tres; lo que no se envía queda vacío). */
@Getter
@Setter
@NoArgsConstructor
public class ContratoCierreDTOPeticion {

    private LocalDate fechaTerminacion;

    private LocalDate fechaActaFinal;

    private LocalDate fechaCertificacionPago;
}
