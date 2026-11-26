package co.edu.unicauca.contratacionvri.rubro.accesoADatos.repositorios;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.rubro.accesoADatos.modelos.RubroEntity;

@Repository
public interface RubroRepositorio extends JpaRepository<RubroEntity, Long> {

    /**
     * Búsqueda paginada. {@code texto} (opcional) es prefijo del código o contenido del nombre; llega
     * escapado con '!' para que %, _ y ! se busquen literalmente.
     *
     * Orden por código "numérico": primero por longitud y luego por valor, así 1031 va antes que
     * 1027008, como la lista ordenada del Excel (columna C de la hoja CONTROL). El orden va en la
     * consulta (no en el Pageable) porque usa una función.
     */
    @Query(value = """
            SELECT r FROM RubroEntity r
            WHERE (:texto IS NULL
                   OR r.codigo LIKE CONCAT(:texto, '%') ESCAPE '!'
                   OR LOWER(r.nombre) LIKE CONCAT('%', LOWER(:texto), '%') ESCAPE '!')
              AND (:activo IS NULL OR r.activo = :activo)
            ORDER BY LENGTH(r.codigo), r.codigo
            """,
            countQuery = """
            SELECT COUNT(r) FROM RubroEntity r
            WHERE (:texto IS NULL
                   OR r.codigo LIKE CONCAT(:texto, '%') ESCAPE '!'
                   OR LOWER(r.nombre) LIKE CONCAT('%', LOWER(:texto), '%') ESCAPE '!')
              AND (:activo IS NULL OR r.activo = :activo)
            """)
    Page<RubroEntity> buscar(@Param("texto") String texto, @Param("activo") Boolean activo, Pageable paginacion);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
}
