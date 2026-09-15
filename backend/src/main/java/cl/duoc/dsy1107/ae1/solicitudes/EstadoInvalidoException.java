package cl.duoc.dsy1107.ae1.solicitudes;

/** Ej: editar/eliminar una solicitud que ya fue decidida, o un estado de decisión inválido. */
public class EstadoInvalidoException extends RuntimeException {
    public EstadoInvalidoException(String mensaje) {
        super(mensaje);
    }
}