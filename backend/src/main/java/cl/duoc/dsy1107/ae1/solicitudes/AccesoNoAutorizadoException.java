package cl.duoc.dsy1107.ae1.solicitudes;

/** Un solicitante intentó modificar/eliminar una solicitud que no es suya. */
public class AccesoNoAutorizadoException extends RuntimeException {
    public AccesoNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}