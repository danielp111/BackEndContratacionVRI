package co.edu.unicauca.contratacionvri.tercero.accesoADatos.repositorios;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.Naturaleza;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.TerceroEntity;

/**
 * Repositorio polimórfico: findById devuelve la subclase real (PersonaNaturalEntity o
 * PersonaJuridicaEntity).
 */
@Repository
public interface TerceroRepositorio extends JpaRepository<TerceroEntity, Long> {

    boolean existsByTipoIdentificacionIdAndNumeroDocumento(Long tipoIdentificacionId, String numeroDocumento);

    boolean existsByTipoIdentificacionIdAndNumeroDocumentoAndIdNot(Long tipoIdentificacionId,
                                                                  String numeroDocumento, Long id);

    /**
     * Búsqueda con filtros opcionales (un parámetro nulo no filtra).
     * {@code documento} es prefijo y {@code nombre} es contenido; ambos llegan ya escapados
     * con '!' como carácter de escape para que %, _ y ! del usuario se busquen literalmente.
     */
    @Query("""
            SELECT t FROM TerceroEntity t
            WHERE (:documento IS NULL OR t.numeroDocumento LIKE CONCAT(:documento, '%') ESCAPE '!')
              AND (:nombre IS NULL OR LOWER(t.nombreCompleto) LIKE CONCAT('%', LOWER(:nombre), '%') ESCAPE '!')
              AND (:naturaleza IS NULL OR t.naturaleza = :naturaleza)
              AND (:activo IS NULL OR t.activo = :activo)
            """)
    Page<TerceroEntity> buscar(@Param("documento") String documento,
                               @Param("nombre") String nombre,
                               @Param("naturaleza") Naturaleza naturaleza,
                               @Param("activo") Boolean activo,
                               Pageable paginacion);
}
