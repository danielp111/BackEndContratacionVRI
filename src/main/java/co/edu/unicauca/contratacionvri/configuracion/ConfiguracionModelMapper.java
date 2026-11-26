package co.edu.unicauca.contratacionvri.configuracion;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionModelMapper {

    /**
     * ModelMapper con coincidencia estricta: solo mapea propiedades con el mismo nombre,
     * para evitar asignaciones accidentales entre campos parecidos (p. ej. usuarioId / id).
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(false);
        return modelMapper;
    }
}
