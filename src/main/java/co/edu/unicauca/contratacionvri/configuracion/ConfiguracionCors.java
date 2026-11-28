package co.edu.unicauca.contratacionvri.configuracion;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS para el frontend Angular, que corre en otro origen (en desarrollo, http://localhost:4200).
 * Los orígenes permitidos salen de {@code vri.cors.origenes-permitidos} (separados por coma).
 * Sin credenciales: la API es sin estado y el token viajará en {@code Authorization: Bearer …}, no en cookies.
 */
@Configuration
public class ConfiguracionCors implements WebMvcConfigurer {

    private final List<String> origenesPermitidos;

    public ConfiguracionCors(@Value("${vri.cors.origenes-permitidos}") List<String> origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos.stream().map(String::trim).filter(o -> !o.isEmpty()).toList();
    }

    @Override
    public void addCorsMappings(CorsRegistry registro) {
        registro.addMapping("/api/**")
                .allowedOrigins(origenesPermitidos.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders(HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION, HttpHeaders.ACCEPT)
                // El 201 de los POST trae la URL del recurso creado.
                .exposedHeaders(HttpHeaders.LOCATION)
                .allowCredentials(false)
                .maxAge(3600);
    }
}
