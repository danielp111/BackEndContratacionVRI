package co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.AmbitoClase;
import co.edu.unicauca.contratacionvri.clasecontrato.accesoADatos.modelos.ClaseContratoEntity;

@Repository
public interface ClaseContratoRepositorio extends JpaRepository<ClaseContratoEntity, Long> {

    /** Filtros opcionales; orden: primero contratos, luego convenios, cada uno por código. */
    @Query("""
            SELECT c FROM ClaseContratoEntity c
            WHERE (:ambito IS NULL OR c.ambito = :ambito)
              AND (:activo IS NULL OR c.activo = :activo)
            ORDER BY c.ambito, c.codigoSireci
            """)
    List<ClaseContratoEntity> buscar(@Param("ambito") AmbitoClase ambito, @Param("activo") Boolean activo);

    boolean existsByAmbitoAndCodigoSireci(AmbitoClase ambito, Integer codigoSireci);
}
