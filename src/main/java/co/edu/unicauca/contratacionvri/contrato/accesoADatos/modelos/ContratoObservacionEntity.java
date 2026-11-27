package co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.edu.unicauca.contratacionvri.comun.accesoADatos.EntidadAuditable;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.UsuarioEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Observación sobre un campo del contrato, escrita por un abogado o el financiero. No se edita ni se
 * borra: se resuelve (una sola vez) y, si el problema sigue, se agrega otra. Así queda el historial.
 */
@Entity
@Table(name = "contrato_observacion",
        indexes = {
                @Index(name = "ix_contrato_observacion_campo", columnList = "contrato_id, campo"),
                @Index(name = "ix_contrato_observacion_resuelta", columnList = "contrato_id, resuelta")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContratoObservacionEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_contrato_observacion_contrato"))
    private ContratoEntity contrato;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "campo", nullable = false, updatable = false, length = 40)
    private CampoContrato campo;

    @Column(name = "texto", nullable = false, updatable = false, length = 390)
    private String texto;

    /** Quién la escribió. Nulo hasta que exista el inicio de sesión. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", updatable = false, foreignKey = @ForeignKey(name = "fk_contrato_observacion_autor"))
    private UsuarioEntity autor;

    @Column(name = "resuelta", nullable = false)
    private Boolean resuelta = Boolean.FALSE;

    @Column(name = "resuelta_en")
    private LocalDateTime resueltaEn;

    /** Cómo se atendió (opcional). */
    @Column(name = "respuesta", length = 390)
    private String respuesta;

    /** Quién la resolvió. Nulo hasta que exista el inicio de sesión. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resuelta_por_id", foreignKey = @ForeignKey(name = "fk_contrato_observacion_resuelta_por"))
    private UsuarioEntity resueltaPor;

    public ContratoObservacionEntity(ContratoEntity contrato, CampoContrato campo, String texto, UsuarioEntity autor) {
        this.contrato = contrato;
        this.campo = campo;
        this.texto = texto;
        this.autor = autor;
    }

    public void resolver(String respuesta, UsuarioEntity usuario, LocalDateTime cuando) {
        this.resuelta = Boolean.TRUE;
        this.respuesta = respuesta;
        this.resueltaPor = usuario;
        this.resueltaEn = cuando;
    }
}
