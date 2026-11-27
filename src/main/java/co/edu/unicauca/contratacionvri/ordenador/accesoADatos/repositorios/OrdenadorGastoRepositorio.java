package co.edu.unicauca.contratacionvri.ordenador.accesoADatos.repositorios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.ordenador.accesoADatos.modelos.OrdenadorGastoEntity;

@Repository
public interface OrdenadorGastoRepositorio extends JpaRepository<OrdenadorGastoEntity, Long> {

    /**
     * Filtros opcionales. {@code texto}: contenido del nombre o del cargo, o prefijo de la cédula (llega
     * escapado con '!'). {@code vigenteEn}: solo periodos ACTIVOS que cubren esa fecha (para los
     * selectores del contrato). Orden: por nombre y el periodo más reciente primero.
     */
    @Query("""
            SELECT o FROM OrdenadorGastoEntity o JOIN FETCH o.tercero t
            WHERE (:texto IS NULL
                   OR LOWER(t.nombreCompleto) LIKE CONCAT('%', LOWER(:texto), '%') ESCAPE '!'
                   OR LOWER(o.cargo) LIKE CONCAT('%', LOWER(:texto), '%') ESCAPE '!'
                   OR t.numeroDocumento LIKE CONCAT(:texto, '%') ESCAPE '!')
              AND (:activo IS NULL OR o.activo = :activo)
              AND (:vigenteEn IS NULL
                   OR (o.activo = true AND o.fechaDesde <= :vigenteEn
                       AND (o.fechaHasta IS NULL OR o.fechaHasta >= :vigenteEn)))
            ORDER BY t.nombreCompleto, o.fechaDesde DESC
            """)
    List<OrdenadorGastoEntity> buscar(@Param("texto") String texto,
                                      @Param("activo") Boolean activo,
                                      @Param("vigenteEn") LocalDate vigenteEn);

    /**
     * ¿Hay otro periodo ACTIVO del mismo tercero con el mismo cargo que se cruce con [desde, hasta]?
     * El cargo se compara sin mayúsculas (y en MySQL sin tildes, por la collation). {@code hasta}
     * nunca es nulo aquí: el servicio pasa una fecha máxima para un periodo abierto.
     */
    @Query("""
            SELECT COUNT(o) FROM OrdenadorGastoEntity o
            WHERE o.tercero.id = :terceroId
              AND LOWER(o.cargo) = LOWER(:cargo)
              AND o.activo = true
              AND (:idExcluir IS NULL OR o.id <> :idExcluir)
              AND o.fechaDesde <= :hasta
              AND (o.fechaHasta IS NULL OR o.fechaHasta >= :desde)
            """)
    long contarCruces(@Param("terceroId") Long terceroId,
                      @Param("cargo") String cargo,
                      @Param("desde") LocalDate desde,
                      @Param("hasta") LocalDate hasta,
                      @Param("idExcluir") Long idExcluir);
}
