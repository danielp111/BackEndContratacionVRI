package co.edu.unicauca.contratacionvri.tipocontrato.fachadaServicios.DTORespuesta;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TipoContratoDTORespuesta {

    private Long id;
    private String nombre;
    private Long serieId;
    private String serieCodigo;
    private Boolean esConvenio;
    private Boolean activo;
    /** Falso si ya hay contratos de este tipo: el nombre y la serie quedan fijos. */
    private boolean nombreYSerieEditables;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
