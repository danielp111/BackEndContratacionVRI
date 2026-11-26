package co.edu.unicauca.contratacionvri.configuracion;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Llena creado_en/actualizado_en y creado_por/actualizado_por en las entidades auditables.
 * Mientras no exista el inicio de sesión con Google, no hay usuario autenticado y
 * creado_por/actualizado_por quedan nulos. Al agregar Spring Security, este bean debe
 * devolver el id del usuario del token.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "usuarioActual")
public class ConfiguracionAuditoria {

    @Bean
    public AuditorAware<Long> usuarioActual() {
        return Optional::empty;
    }
}
