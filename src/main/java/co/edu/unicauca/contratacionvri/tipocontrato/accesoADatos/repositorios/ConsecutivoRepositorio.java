package co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.ConsecutivoEntity;
import co.edu.unicauca.contratacionvri.tipocontrato.accesoADatos.modelos.ConsecutivoId;
import jakarta.persistence.LockModeType;

@Repository
public interface ConsecutivoRepositorio extends JpaRepository<ConsecutivoEntity, ConsecutivoId> {

    /**
     * Garantiza que la fila (serie, año) exista, en la MISMA transacción del número y sin cambiar
     * ultimo_numero si ya existe. Además deja la fila bloqueada en exclusivo hasta el final de la
     * transacción.
     *
     * Por qué así y no de otra forma (probado contra MySQL):
     * <ul>
     *   <li>No se crea en una transacción aparte (REQUIRES_NEW): cada petición necesitaría dos
     *       conexiones, y con más peticiones simultáneas que conexiones del pool todas quedan
     *       esperando la segunda (el pool se agota).</li>
     *   <li>No se usa INSERT IGNORE: ante un duplicado toma un bloqueo COMPARTIDO, y dos peticiones
     *       que luego piden el exclusivo (FOR UPDATE) se bloquean mutuamente (deadlock).
     *       ON DUPLICATE KEY UPDATE toma el bloqueo exclusivo desde el principio.</li>
     *   <li>No se pregunta antes con FOR UPDATE: sobre una fila inexistente InnoDB bloquea el hueco
     *       del índice, y el INSERT de otra petición quedaría esperando ese bloqueo.</li>
     * </ul>
     */
    @Modifying
    @Query(value = """
            INSERT INTO consecutivo (serie_id, anio, ultimo_numero, creado_en, actualizado_en)
            VALUES (:serieId, :anio, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE ultimo_numero = ultimo_numero
            """, nativeQuery = true)
    int asegurarFila(@Param("serieId") Long serieId, @Param("anio") Integer anio);

    /**
     * SELECT ... FOR UPDATE: bloquea la fila hasta que termine la transacción, así dos peticiones
     * de la misma serie y año nunca leen el mismo ultimo_numero. Llamar después de asegurarFila.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ConsecutivoEntity c WHERE c.id = :id")
    Optional<ConsecutivoEntity> bloquear(@Param("id") ConsecutivoId id);

    /** ¿La serie ya emitió algún número en cualquier año? */
    boolean existsByIdSerieIdAndUltimoNumeroGreaterThan(Long serieId, Integer numero);
}
