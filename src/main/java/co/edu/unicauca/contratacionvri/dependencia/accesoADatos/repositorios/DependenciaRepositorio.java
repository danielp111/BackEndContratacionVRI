package co.edu.unicauca.contratacionvri.dependencia.accesoADatos.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.dependencia.accesoADatos.modelos.DependenciaEntity;

@Repository
public interface DependenciaRepositorio extends JpaRepository<DependenciaEntity, Long> {

    @Query("""
            SELECT d FROM DependenciaEntity d
            WHERE (:activo IS NULL OR d.activo = :activo)
            ORDER BY d.nombre
            """)
    List<DependenciaEntity> buscar(@Param("activo") Boolean activo);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
