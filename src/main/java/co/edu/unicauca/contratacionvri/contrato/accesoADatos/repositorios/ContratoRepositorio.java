package co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.ContratoEntity;
import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.EstadoContrato;

@Repository
public interface ContratoRepositorio extends JpaRepository<ContratoEntity, Long>, JpaSpecificationExecutor<ContratoEntity> {

    /** Búsquedas paginadas: trae en la misma consulta lo que muestra cada fila del listado. */
    @Override
    @EntityGraph(attributePaths = {"serie", "tipoContrato", "contratista", "responsable"})
    Page<ContratoEntity> findAll(Specification<ContratoEntity> especificacion, Pageable pagina);

    /** Detalle del contrato con sus relaciones. */
    @EntityGraph(attributePaths = {"serie", "tipoContrato", "responsable", "claseContrato", "contratista",
            "ordenadorGasto", "tipoSeguimiento", "presupuesto"})
    Optional<ContratoEntity> findConDetalleById(Long id);

    /** Otros contratos (no anulados) del contratista en el año: aviso de posible fraccionamiento. */
    @Query("""
            SELECT c FROM ContratoEntity c JOIN FETCH c.serie
            WHERE c.contratista.id = :contratistaId AND c.anio = :anio AND c.estado <> :anulado
              AND (:idExcluir IS NULL OR c.id <> :idExcluir)
            ORDER BY c.consecutivo
            """)
    List<ContratoEntity> buscarOtrosDelContratista(@Param("contratistaId") Long contratistaId,
                                                  @Param("anio") Integer anio,
                                                  @Param("idExcluir") Long idExcluir,
                                                  @Param("anulado") EstadoContrato anulado);

    boolean existsByTipoContratoId(Long tipoContratoId);

    boolean existsBySerieId(Long serieId);

    boolean existsByOrdenadorGastoId(Long ordenadorGastoId);
}
