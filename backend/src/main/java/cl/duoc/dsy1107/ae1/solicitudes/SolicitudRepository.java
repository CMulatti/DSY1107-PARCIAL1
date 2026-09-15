package cl.duoc.dsy1107.ae1.solicitudes;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    /** Lo que ve un solicitante: solo lo suyo. */
    List<Solicitud> findBySolicitanteEmailOrderByCreadoEnDesc(String solicitanteEmail);

    /** Lo que ve un aprobador: toodo, sin filtrar por dueño. */
    List<Solicitud> findAllByOrderByCreadoEnDesc();
}