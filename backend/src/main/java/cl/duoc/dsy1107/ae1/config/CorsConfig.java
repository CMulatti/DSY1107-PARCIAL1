package cl.duoc.dsy1107.ae1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS para el modo local.
 *
 * Cuando el front pasa por el API Gateway, el CORS lo resuelve allá el
 * cors_configuration de main.tf y estas reglas ni se consultan: el
 * navegador nunca habla con este servicio directamente. Esto existe para
 * poder apuntar "npm run dev" directamente al :8080.
 *
 * Ojo con el detalle: el origen va SIN barra final, porque un header
 * Origin nunca la lleva.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final BackendProperties props;

    public CorsConfig(BackendProperties props) {
        this.props = props;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(props.cors().origenes().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("authorization", "content-type")
                .maxAge(300);
    }
}