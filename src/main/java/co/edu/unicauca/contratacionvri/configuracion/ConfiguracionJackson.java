package co.edu.unicauca.contratacionvri.configuracion;

import java.io.IOException;
import java.util.regex.Pattern;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class ConfiguracionJackson {

    /** Espacios horizontales repetidos (incluye tabulador y espacio duro de Excel); no toca saltos de línea. */
    private static final Pattern ESPACIOS_REPETIDOS = Pattern.compile("\\h+");

    /**
     * Normaliza todo texto que llega en el JSON ANTES de la validación (@Valid):
     * <ul>
     *   <li>recorta espacios al inicio y al final, así " 3 " no falla un @Pattern;</li>
     *   <li>colapsa espacios internos repetidos ("Ana    María" → "Ana María");</li>
     *   <li>convierte el texto vacío en null, así "" en un campo opcional equivale a no enviarlo
     *       y en uno obligatorio lo rechaza @NotBlank / @NotNull.</li>
     * </ul>
     * Lo que depende del campo (minúsculas en correos, mayúsculas en códigos) lo hace cada servicio.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer normalizarTextos() {
        SimpleModule modulo = new SimpleModule("NormalizarTextos");
        modulo.addDeserializer(String.class, new StringDeserializer() {
            @Override
            public String deserialize(JsonParser parser, DeserializationContext contexto) throws IOException {
                String valor = super.deserialize(parser, contexto);
                if (valor == null) {
                    return null;
                }
                String limpio = ESPACIOS_REPETIDOS.matcher(valor.strip()).replaceAll(" ");
                return limpio.isEmpty() ? null : limpio;
            }
        });
        return builder -> builder.modulesToInstall(modulo);
    }
}
