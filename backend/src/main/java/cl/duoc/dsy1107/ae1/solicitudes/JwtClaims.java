package cl.duoc.dsy1107.ae1.solicitudes;

import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Lee el payload del access token que ya validó el JWT authorizer del API
 * Gateway. NO verifica la firma: eso ya lo hizo el gateway antes de que la
 * petición llegara hasta aquí. Aquí solo se lee para saber quién es el
 * solicitante y a qué grupos pertenece.
 *
 * Mismo principio que decodificarJwt() en el frontend, solo que del lado del
 * servidor: el token viaja igual, solo cambia quién lo lee.
 */
public class JwtClaims {

    private final Map<String, Object> payload;

    private JwtClaims(Map<String, Object> payload) {
        this.payload = payload;
    }

    public static JwtClaims desdeHeader(String authorizationHeader) {
        String token = authorizationHeader.replaceFirst("(?i)^Bearer ", "");
        String[] partes = token.split("\\.");
        byte[] decodificado = Base64.getUrlDecoder().decode(partes[1]);
        try {
            Map<String, Object> payload = new ObjectMapper()
                    .readValue(decodificado, Map.class);
            return new JwtClaims(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Token JWT inválido o mal formado", e);
        }
    }

    public String email() {
        return (String) payload.get("email");
    }

    @SuppressWarnings("unchecked")
    public List<String> grupos() {
        Object valor = payload.get("cognito:groups");
        return valor == null ? List.of() : (List<String>) valor;
    }

    public boolean esAprobador() {
        return grupos().contains("aprobadores");
    }
}