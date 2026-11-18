package bolsines.dominio;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Historial de estados de un bolsin. El cambio de estado actual es el que no tiene fecha de fin. */
@Entity
@Table(name = "cambio_estado_bolsin")
public class CambioEstadoBolsin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;

    @ManyToOne
    @JoinColumn(name = "estado_id")
    private Estado estado;

    /** Trazabilidad: el empleado responsable del cambio de estado. */
    @ManyToOne
    @JoinColumn(name = "responsable_ce_id")
    private Empleado responsableCE;

    /** Constructor vacio: lo necesita JPA. */
    protected CambioEstadoBolsin() {
    }

    /** new(): nace sin fecha de fin, o sea, como el cambio de estado actual. */
    public CambioEstadoBolsin(LocalDateTime fechaHoraInicio, Estado estado, Empleado responsable) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = null;
        this.estado = estado;
        this.responsableCE = responsable;
    }

    public boolean sosActual() {
        return fechaHoraFin == null;
    }

    public boolean sosEnviado() {
        return estado.sosEnviado();
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }
}
