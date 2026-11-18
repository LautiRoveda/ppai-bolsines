package bolsines.dominio;

import java.time.LocalDateTime;

import bolsines.dominio.estados.EstadoDocumentacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Historial de estados de una documentacion.
 * En el diseño apunta a EstadoDocumentacion (patron State) y no a Estado.
 * El cambio de estado actual es el que no tiene fecha de fin.
 */
@Entity
@Table(name = "cambio_estado_documentacion")
public class CambioEstadoDocumentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;

    /**
     * PATRON STATE: en la base es una columna de texto con el nombre del estado.
     * EstadoDocumentacionConverter convierte entre el objeto estado y ese texto.
     */
    @Column(name = "estado")
    private EstadoDocumentacion estado;

    /** Trazabilidad: el empleado responsable del cambio de estado. */
    @ManyToOne
    @JoinColumn(name = "responsable_ce_id")
    private Empleado responsableCE;

    /** Constructor vacio: lo necesita JPA. */
    protected CambioEstadoDocumentacion() {
    }

    /** new(fechaHoraInicio, estado, responsable): nace sin fecha de fin (es el actual). */
    public CambioEstadoDocumentacion(LocalDateTime fechaHoraInicio, EstadoDocumentacion estado, Empleado responsable) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = null;
        this.estado = estado;
        this.responsableCE = responsable;
    }

    public boolean sosActual() {
        return fechaHoraFin == null;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    // --- Consultas de solo lectura (no son comportamiento del diseño; las usan los tests) ---

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public EstadoDocumentacion getEstado() {
        return estado;
    }

    public Empleado getResponsableCE() {
        return responsableCE;
    }
}
