package cl.duoc.dsy1107.ae1.solicitudes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Una fila de la tabla "solicitud".
 *
 * Igual que Producto en el proyecto original: esta clase NO sale por HTTP.
 * Los records de SolicitudController convierten hacia/desde ella.
 */
@Entity
@Table(name = "solicitud")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * El correo del solicitante, tomado del token JWT (claim "email"), NUNCA
     * de lo que envía el cliente en el cuerpo de la petición. Es lo que
     * permite filtrar "mis solicitudes" y evita que alguien cree una
     * solicitud a nombre de otra persona.
     */
    @Column(name = "solicitante_email", nullable = false, length = 255)
    private String solicitanteEmail;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false, length = 500)
    private String motivo;

    /**
     * PENDIENTE, APROBADA o RECHAZADA. Un String simple, no un enum de JPA:
     * mantiene la migración y la entidad igual de legibles sin agregar
     * @Enumerated y sus decisiones de mapeo.
     */
    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "comentario_aprobador", length = 500)
    private String comentarioAprobador;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected Solicitud() {
    }

    public Solicitud(String solicitanteEmail, LocalDate fechaInicio, LocalDate fechaFin,
                     String motivo, Instant creadoEn) {
        this.solicitanteEmail = solicitanteEmail;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.motivo = motivo;
        this.estado = "PENDIENTE";
        this.creadoEn = creadoEn;
    }

    public Long getId() {
        return id;
    }

    public String getSolicitanteEmail() {
        return solicitanteEmail;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getComentarioAprobador() {
        return comentarioAprobador;
    }

    public void setComentarioAprobador(String comentarioAprobador) {
        this.comentarioAprobador = comentarioAprobador;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}