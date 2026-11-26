package co.edu.unicauca.contratacionvri.usuario.accesoADatos.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.Rol;
import co.edu.unicauca.contratacionvri.usuario.accesoADatos.modelos.UsuarioEntity;

@Repository
public interface UsuarioRepositorio extends JpaRepository<UsuarioEntity, Long> {

    @Query("""
            SELECT u FROM UsuarioEntity u
            WHERE (:rol IS NULL OR u.rol = :rol)
              AND (:activo IS NULL OR u.activo = :activo)
            ORDER BY u.nombre
            """)
    List<UsuarioEntity> buscar(@Param("rol") Rol rol, @Param("activo") Boolean activo);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoAndIdNot(String correo, Long id);

    /** Para el inicio de sesión con Google. */
    Optional<UsuarioEntity> findByGoogleId(String googleId);

    /** Para el primer inicio de sesión, cuando aún no se ha guardado el google_id. */
    Optional<UsuarioEntity> findByCorreo(String correo);
}
