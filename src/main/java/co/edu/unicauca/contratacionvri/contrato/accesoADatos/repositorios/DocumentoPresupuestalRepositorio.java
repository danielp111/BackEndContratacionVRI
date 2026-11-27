package co.edu.unicauca.contratacionvri.contrato.accesoADatos.repositorios;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos.DocumentoPresupuestalEntity;

/** Consultas comunes de CDP y RP. */
@NoRepositoryBean
public interface DocumentoPresupuestalRepositorio<T extends DocumentoPresupuestalEntity> extends JpaRepository<T, Long> {

    List<T> findByContratoIdOrderByIdAsc(Long contratoId);

    Optional<T> findByIdAndContratoId(Long id, Long contratoId);

    boolean existsByContratoIdAndNumero(Long contratoId, String numero);

    boolean existsByContratoIdAndNumeroAndIdNot(Long contratoId, String numero, Long id);

    /** Suma de los valores registrados (los que no tienen valor no cuentan); nulo si no hay ninguno con valor. */
    @Query("SELECT SUM(d.valor) FROM #{#entityName} d WHERE d.contrato.id = :contratoId")
    BigDecimal sumarValor(@Param("contratoId") Long contratoId);
}
