package cl.duoc.dsy1107.ae1.solicitudes;

public class SolicitudNoEncontradaException extends RuntimeException {

    private final long id;

    public SolicitudNoEncontradaException(long id) {
        super("No existe una solicitud con id " + id);
        this.id = id;
    }

    public long id() {
        return id;
    }
}