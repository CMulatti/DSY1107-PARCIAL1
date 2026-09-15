package cl.duoc.dsy1107.ae1.solicitudes;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class SolicitudService {

    private final SolicitudRepository repositorio;
    private final Clock reloj;

    public SolicitudService(SolicitudRepository repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<Solicitud> listar(JwtClaims claims) {
        if (claims.esAprobador()) {
            return repositorio.findAllByOrderByCreadoEnDesc();
        }
        return repositorio.findBySolicitanteEmailOrderByCreadoEnDesc(claims.email());
    }

    @Transactional(readOnly = true)
    public Solicitud obtener(long id, JwtClaims claims) {
        Solicitud solicitud = buscarOFallar(id);
        verificarPropietarioOAprobador(solicitud, claims);
        return solicitud;
    }

    @Transactional
    public Solicitud crear(JwtClaims claims, LocalDate fechaInicio, LocalDate fechaFin, String motivo) {
        Solicitud solicitud = new Solicitud(claims.email(), fechaInicio, fechaFin, motivo, reloj.instant());
        return repositorio.save(solicitud);
    }

    @Transactional
    public Solicitud actualizar(long id, JwtClaims claims, LocalDate fechaInicio, LocalDate fechaFin, String motivo) {
        Solicitud solicitud = buscarOFallar(id);
        verificarPropietario(solicitud, claims);
        verificarPendiente(solicitud);

        solicitud.setFechaInicio(fechaInicio);
        solicitud.setFechaFin(fechaFin);
        solicitud.setMotivo(motivo);
        return solicitud;
    }

    @Transactional
    public void eliminar(long id, JwtClaims claims) {
        Solicitud solicitud = buscarOFallar(id);
        verificarPropietario(solicitud, claims);
        verificarPendiente(solicitud);
        repositorio.delete(solicitud);
    }

    /**
     * La decisión del aprobador. No comprueba el grupo del usuario: eso ya lo
     * exigió authorization_scopes en la ruta (solicitudes/aprobar). Si esta
     * función corre, es porque el API Gateway ya autorizó a un aprobador.
     */
    @Transactional
    public Solicitud decidir(long id, String nuevoEstado, String comentario) {
        if (!nuevoEstado.equals("APROBADA") && !nuevoEstado.equals("RECHAZADA")) {
            throw new EstadoInvalidoException("El estado de la decisión debe ser APROBADA o RECHAZADA");
        }
        Solicitud solicitud = buscarOFallar(id);
        verificarPendiente(solicitud);

        solicitud.setEstado(nuevoEstado);
        solicitud.setComentarioAprobador(comentario);
        return solicitud;
    }

    private Solicitud buscarOFallar(long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new SolicitudNoEncontradaException(id));
    }

    private void verificarPropietario(Solicitud solicitud, JwtClaims claims) {
        if (!solicitud.getSolicitanteEmail().equals(claims.email())) {
            throw new AccesoNoAutorizadoException("Esta solicitud no te pertenece");
        }
    }

    private void verificarPropietarioOAprobador(Solicitud solicitud, JwtClaims claims) {
        if (!claims.esAprobador() && !solicitud.getSolicitanteEmail().equals(claims.email())) {
            throw new AccesoNoAutorizadoException("Esta solicitud no te pertenece");
        }
    }

    private void verificarPendiente(Solicitud solicitud) {
        if (!solicitud.getEstado().equals("PENDIENTE")) {
            throw new EstadoInvalidoException("Solo se puede modificar una solicitud PENDIENTE");
        }
    }
}