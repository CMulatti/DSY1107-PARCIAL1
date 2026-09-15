package cl.duoc.dsy1107.ae1.web;

import cl.duoc.dsy1107.ae1.solicitudes.AccesoNoAutorizadoException;
import cl.duoc.dsy1107.ae1.solicitudes.EstadoInvalidoException;
import cl.duoc.dsy1107.ae1.solicitudes.SolicitudNoEncontradaException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ManejadorDeErrores {

    public record ErrorHttp(String error, String detalle, Instant momento) {
    }

    /** El id pedido no está en la tabla. */
    @ExceptionHandler(SolicitudNoEncontradaException.class)
    public ResponseEntity<ErrorHttp> solicitudNoEncontrada(SolicitudNoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorHttp("Solicitud no encontrada", e.getMessage(), Instant.now()));
    }

    /**
     * Un solicitante intentó tocar una solicitud que no es suya.
     *
     * 403, no 404: la solicitud existe, solo que quien pregunta no es su
     * dueño. Ocultarla como un 404 sería más "seguro" en un sistema real,
     * pero aquí el punto didáctico es distinguir autenticación (401),
     * autorización a nivel de ruta (403 del propio API Gateway) y
     * autorización a nivel de dato (este 403, decidido en el backend).
     */
    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<ErrorHttp> accesoNoAutorizado(AccesoNoAutorizadoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorHttp("Acceso no autorizado", e.getMessage(), Instant.now()));
    }

    /** Ej: editar/eliminar una solicitud ya decidida, o un estado de decisión inválido. */
    @ExceptionHandler(EstadoInvalidoException.class)
    public ResponseEntity<ErrorHttp> estadoInvalido(EstadoInvalidoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorHttp("Operación no permitida en el estado actual", e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Cuerpo400> invalido(MethodArgumentNotValidException e) {
        Map<String, String> campos = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        f -> f.getDefaultMessage() == null ? "valor invalido" : f.getDefaultMessage(),
                        (primero, segundo) -> primero));

        return ResponseEntity.badRequest()
                .body(new Cuerpo400("El cuerpo de la petición no es válido", campos, Instant.now()));
    }

    public record Cuerpo400(String error, Map<String, String> campos, Instant momento) {
    }
}