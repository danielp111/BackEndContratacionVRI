package co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoObservacionEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.EstadoContrato;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.TerceroEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.Builder;

/**
 * Consulta dinámica de los listados de contratos. Cada criterio nulo se ignora. Los textos llegan
 * limpios y con los comodines de LIKE ya escapados con '!'.
 */
public final class ContratoEspecificaciones {

    private static final char ESCAPE = '!';

    private ContratoEspecificaciones() {
    }

    /** Criterios ya interpretados por el servicio (el número separado en serie y consecutivo). */
    @Builder
    public record Criterios(
            String serieCodigo,
            Integer consecutivo,
            Integer anio,
            EstadoContrato estado,
            Long tipoContratoId,
            Long claseContratoId,
            Long responsableId,
            String contratista,
            String texto,
            LocalDate inicioDesde,
            LocalDate inicioHasta,
            LocalDate terminacionDesde,
            LocalDate terminacionHasta,
            Boolean conObservaciones) {
    }

    public static Specification<ContratoEntity> de(Criterios c) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            igual(p, cb, root.get("anio"), c.anio());
            igual(p, cb, root.get("estado"), c.estado());
            igual(p, cb, root.get("consecutivo"), c.consecutivo());
            igual(p, cb, root.get("tipoContrato").get("id"), c.tipoContratoId());
            igual(p, cb, root.get("claseContrato").get("id"), c.claseContratoId());
            igual(p, cb, root.get("responsable").get("id"), c.responsableId());
            if (c.serieCodigo() != null) {
                p.add(cb.equal(cb.lower(root.get("serie").get("codigo")), c.serieCodigo().toLowerCase(Locale.ROOT)));
            }
            if (c.contratista() != null) {
                Join<ContratoEntity, TerceroEntity> t = root.join("contratista", JoinType.INNER);
                p.add(cb.or(
                        cb.like(t.get("numeroDocumento"), c.contratista() + "%", ESCAPE),
                        cb.like(cb.lower(t.get("nombreCompleto")), "%" + c.contratista().toLowerCase(Locale.ROOT) + "%", ESCAPE)));
            }
            if (c.texto() != null) {
                p.add(cb.like(cb.lower(root.get("objeto")), "%" + c.texto().toLowerCase(Locale.ROOT) + "%", ESCAPE));
            }
            rango(p, cb, root.get("fechaInicio"), c.inicioDesde(), c.inicioHasta());
            rango(p, cb, root.get("fechaTerminacion"), c.terminacionDesde(), c.terminacionHasta());
            if (c.conObservaciones() != null) {
                Subquery<Long> pendientes = query.subquery(Long.class);
                Root<ContratoObservacionEntity> o = pendientes.from(ContratoObservacionEntity.class);
                pendientes.select(o.get("id")).where(cb.equal(o.get("contrato"), root), cb.isFalse(o.get("resuelta")));
                p.add(c.conObservaciones() ? cb.exists(pendientes) : cb.not(cb.exists(pendientes)));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    private static void igual(List<Predicate> p, CriteriaBuilder cb, Path<?> campo, Object valor) {
        if (valor != null) {
            p.add(cb.equal(campo, valor));
        }
    }

    private static void rango(List<Predicate> p, CriteriaBuilder cb, Path<LocalDate> campo, LocalDate desde, LocalDate hasta) {
        if (desde != null) {
            p.add(cb.greaterThanOrEqualTo(campo, desde));
        }
        if (hasta != null) {
            p.add(cb.lessThanOrEqualTo(campo, hasta));
        }
    }
}
