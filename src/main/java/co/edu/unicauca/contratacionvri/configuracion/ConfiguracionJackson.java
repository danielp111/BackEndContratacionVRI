package co.edu.unicauca.contratacionvri.configuracion;

import java.io.IOException;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class ConfiguracionJackson {

    /**
     * Recorta los espacios al inicio y al final de todo texto que llega en el JSON, antes de
     * la validación. Así un valor pegado con espacios (" 3 ", " correo@unicauca.edu.co")
     * no falla un @Pattern. La limpieza fina (espacios internos, mayúsculas) la hace cada servicio.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer recortarTextos() {
        SimpleModule modulo = new SimpleModule("RecortarTextos");
        modulo.addDeserializer(String.class, new StringDeserializer() {
            @Override
            public String deserialize(JsonParser parser, DeserializationContext contexto) throws IOException {
                String valor = super.deserialize(parser, contexto);
                return valor == null ? null : valor.strip();
            }
        });
        return builder -> builder.modulesToInstall(modulo);
    }
}
