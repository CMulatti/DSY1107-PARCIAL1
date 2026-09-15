package cl.duoc.dsy1107.ae1.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "backend")
public record BackendProperties(Cors cors) {

    public record Cors(List<String> origenes) {
    }
}