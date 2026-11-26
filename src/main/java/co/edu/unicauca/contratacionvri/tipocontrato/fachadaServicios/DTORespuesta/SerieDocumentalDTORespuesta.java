package co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SerieDocumentalDTORespuesta {

    private Long id;
    private String codigo;
    private Boolean esConvenio;
    private Boolean activo;
    /** Falso si la serie ya tiene contratos (números emitidos): el código queda fijo. */
    private boolean codigoEditable;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
