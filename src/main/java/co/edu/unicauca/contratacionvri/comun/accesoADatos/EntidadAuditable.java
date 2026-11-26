package co.edu.unicauca.contratacionvri.comun.accesoADatos;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Campos de auditoría que llevan todas las tablas de negocio. Los llena JPA Auditing
 * (ver ConfiguracionAuditoria); ningún servicio debe asignarlos a mano.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class EntidadAuditable {

    @CreatedDate
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @CreatedBy
    @Column(name = "creado_por", updatable = false)
    private Long creadoPor;

    @LastModifiedDate
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @LastModifiedBy
    @Column(name = "actualizado_por")
    private Long actualizadoPor;
}
