package cl.duoc.dsy1107.ae1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Segunda capa de validación del JWT, esta vez dentro del propio backend.
 *
 * El API Gateway ya valida firma, issuer, expiración y scope por ruta
 * (authorization_scopes en terraform/main.tf) — eso sigue siendo la
 * autorización real del sistema. Esta capa es defensa en profundidad: si
 * alguien alcanzara el backend saltándose el gateway (el puerto 8080 está
 * abierto a internet, ver la nota en terraform/ecs.tf), el backend ahora
 * también rechaza cualquier token que no sea válido y firmado por este
 * user pool, en vez de confiar ciegamente en el header Authorization.
 */
@Configuration
public class SecurityConfig {

    private final BackendProperties props;

    public SecurityConfig(BackendProperties props) {
        this.props = props;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        return http.build();
    }

    private CorsConfigurationSource corsSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(props.cors().origenes());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("authorization", "content-type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}