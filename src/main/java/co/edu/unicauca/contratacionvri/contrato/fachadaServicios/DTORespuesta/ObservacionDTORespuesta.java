package co.edu.unicauca.contratacionvri.contrato.fachadaServicios.DTORespuesta;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ObservacionDTORespuesta {

    private Long id;
    private Long contratoId;
    /** Código del campo, p. ej. VALOR_SIN_IVA. */
    private String campo;
    /** "Valor sin IVA". */
    private String campoEtiqueta;
    private String texto;
    private Long autorId;
    private LocalDateTime creadoEn;
    private Boolean resuelta;
    private String respuesta;
    private Long resueltaPorId;
    private LocalDateTime resueltaEn;
}
