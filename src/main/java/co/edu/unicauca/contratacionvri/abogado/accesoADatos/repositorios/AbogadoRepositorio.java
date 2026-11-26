package co.edu.unicauca.contratacionvri.abogado.accesoADatos.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.abogado.accesoADatos.modelos.AbogadoEntity;

/** Acceso a la tabla abogado en MySQL mediante Spring Data JPA. */
@Repository
public interface AbogadoRepositorio extends JpaRepository<AbogadoEntity, Long> {

    List<AbogadoEntity> findByActivoOrderByApellidosAscNombresAsc(Boolean activo);

    List<AbogadoEntity> findAllByOrderByApellidosAscNombresAsc();

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndIdNot(Long usuarioId, Long id);

    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByNumeroDocumentoAndIdNot(String numeroDocumento, Long id);
}
