package co.edu.unicauca.contratacionvri.proyecto.accesoADatos.repositorios;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.proyecto.accesoADatos.modelos.ProyectoEntity;

@Repository
public interface ProyectoRepositorio extends JpaRepository<ProyectoEntity, Integer> {

    /**
     * Búsqueda con filtros opcionales (un parámetro nulo no filtra). {@code id} es prefijo del código;
     * {@code nombre} es contenido; {@code director} busca en el nombre (contenido) o en el documento
     * (prefijo) del director. Los textos llegan escapados con '!' para que %, _ y ! se busquen literalmente.
     */
    @Query(value = """
            SELECT p FROM ProyectoEntity p JOIN FETCH p.director d
            WHERE (:id IS NULL OR CAST(p.idProyecto AS String) LIKE CONCAT(:id, '%'))
              AND (:nombre IS NULL OR LOWER(p.nombre) LIKE CONCAT('%', LOWER(:nombre), '%') ESCAPE '!')
              AND (:director IS NULL
                   OR LOWER(d.nombreCompleto) LIKE CONCAT('%', LOWER(:director), '%') ESCAPE '!'
                   OR d.numeroDocumento LIKE CONCAT(UPPER(:director), '%') ESCAPE '!')
              AND (:activo IS NULL OR p.activo = :activo)
            """,
            countQuery = """
            SELECT COUNT(p) FROM ProyectoEntity p JOIN p.director d
            WHERE (:id IS NULL OR CAST(p.idProyecto AS String) LIKE CONCAT(:id, '%'))
              AND (:nombre IS NULL OR LOWER(p.nombre) LIKE CONCAT('%', LOWER(:nombre), '%') ESCAPE '!')
              AND (:director IS NULL
                   OR LOWER(d.nombreCompleto) LIKE CONCAT('%', LOWER(:director), '%') ESCAPE '!'
                   OR d.numeroDocumento LIKE CONCAT(UPPER(:director), '%') ESCAPE '!')
              AND (:activo IS NULL OR p.activo = :activo)
            """)
    Page<ProyectoEntity> buscar(@Param("id") String id,
                                @Param("nombre") String nombre,
                                @Param("director") String director,
                                @Param("activo") Boolean activo,
                                Pageable paginacion);

    /** Proyectos activos para los selectores: prefijo del código o contenido del nombre. */
    @Query("""
            SELECT p FROM ProyectoEntity p JOIN FETCH p.director
            WHERE p.activo = true
              AND (:texto IS NULL
                   OR CAST(p.idProyecto AS String) LIKE CONCAT(:texto, '%') ESCAPE '!'
                   OR LOWER(p.nombre) LIKE CONCAT('%', LOWER(:texto), '%') ESCAPE '!')
            ORDER BY p.idProyecto
            """)
    List<ProyectoEntity> buscarActivos(@Param("texto") String texto);
}
