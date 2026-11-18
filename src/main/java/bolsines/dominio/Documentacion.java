package bolsines.dominio;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import bolsines.dominio.estados.EstadoDocumentacion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * PATRON STATE - CONTEXTO.
 *
 * La documentacion tiene un estadoActual y le delega todo lo que depende del estado,
 * pasandose a si misma (this) y su coleccion cambioEstado.
 * Esta clase NO tiene ningun if ni switch sobre su estado.
 */
@Entity
@Table(name = "documentacion")
public class Documentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String asunto;
    private LocalDateTime fechaPase;
    private int numero;

    @ManyToOne
    @JoinColumn(name = "tipo_documento_id")
    private TipoDocumento tipoDocumento;

    @ManyToOne
    @JoinColumn(name = "cm_origen_id")
    private ComisionMedica cmOrigen;

    /** Historial de estados. Se guarda junto con la documentacion (cascade). */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "documentacion_id", nullable = false)
    @OrderBy("fechaHoraInicio, id")
    @Fetch(FetchMode.SUBSELECT)
    private List<CambioEstadoDocumentacion> cambioEstado;

    /**
     * PATRON STATE: el estado actual es un OBJETO (EnBolsinEnviado, RecibidoYAceptado, ...).
     * En la base es una columna de texto con el nombre del estado; la conversion la hace
     * EstadoDocumentacionConverter. No hay tabla de estados de documentacion.
     */
    @Column(name = "estado_actual")
    private EstadoDocumentacion estadoActual;

    /** Constructor vacio: lo necesita JPA. */
    protected Documentacion() {
    }

    /** new() */
    public Documentacion(int numero, String asunto, LocalDateTime fechaPase, TipoDocumento tipoDocumento,
                         ComisionMedica cmOrigen, EstadoDocumentacion estadoActual,
                         List<CambioEstadoDocumentacion> cambioEstado) {
        this.numero = numero;
        this.asunto = asunto;
        this.fechaPase = fechaPase;
        this.tipoDocumento = tipoDocumento;
        this.cmOrigen = cmOrigen;
        this.estadoActual = estadoActual;
        this.cambioEstado = cambioEstado;
    }

    public String getTipoDocumentacion() {
        return tipoDocumento.getNombre();
    }

    public String getAsunto() {
        return asunto;
    }

    // ------------------------------------------------------------------
    // Operaciones que dependen del estado: todas DELEGAN en estadoActual
    // ------------------------------------------------------------------

    /** CU 28, flujo basico. */
    public void aceptarDocumentacion(LocalDateTime fechaHora, Empleado empleado) {
        estadoActual.aceptarDocumentacion(this, fechaHora, empleado, cambioEstado);
    }

    /** CU 28, alternativo A3: no se recibio esta documentacion. */
    public void registrarDocumentacionNoRecibida(LocalDateTime fechaHora, Empleado empleado) {
        estadoActual.registrarDocumentacionNoRecibida(this, fechaHora, empleado, cambioEstado);
    }

    /** CU 28, alternativo A4: esta documentacion no corresponde a la CM destino. */
    public void rechazarDocumentacion(LocalDateTime fechaHora, Empleado empleado) {
        estadoActual.rechazarDocumentacion(this, fechaHora, empleado, cambioEstado);
    }

    /** CU 28, alternativo A5: la CMC debe redirigir esta documentacion a otra area. */
    public void registrarRedireccion(LocalDateTime fechaHora, Empleado empleado) {
        estadoActual.registrarRedireccion(this, fechaHora, empleado, cambioEstado);
    }

    public void asignarRemito(LocalDateTime fechaHora, Empleado empleado) {
        estadoActual.asignarRemito(this, fechaHora, empleado, cambioEstado);
    }

    public void cancelarRemito(LocalDateTime fechaHora, Empleado empleado) {
        estadoActual.cancelarRemito(this, fechaHora, empleado, cambioEstado);
    }

    public void incorporarEnBolsin(LocalDateTime fechaHora, Empleado empleado) {
        estadoActual.incorporarEnBolsin(this, fechaHora, empleado, cambioEstado);
    }

    public void quitarDeBolsin(LocalDateTime fechaHora, Empleado empleado) {
        estadoActual.quitarDeBolsin(this, fechaHora, empleado, cambioEstado);
    }

    public void enviarBolsin(LocalDateTime fechaHora, Empleado empleado) {
        estadoActual.enviarBolsin(this, fechaHora, empleado, cambioEstado);
    }

    public void reenviarDocumentacion(LocalDateTime fechaHora, Empleado empleado) {
        estadoActual.reenviarDocumentacion(this, fechaHora, empleado, cambioEstado);
    }

    public void darDeBaja(LocalDateTime fechaHora, Empleado empleado) {
        estadoActual.darDeBaja(this, fechaHora, empleado, cambioEstado);
    }

    // ------------------------------------------------------------------
    // Los invoca el estado concreto al resolver la transicion
    // ------------------------------------------------------------------

    public void agregarCambioEstado(CambioEstadoDocumentacion ce) {
        cambioEstado.add(ce);
    }

    public void setEstado(EstadoDocumentacion estado) {
        this.estadoActual = estado;
    }

    // --- Consultas de solo lectura (no son comportamiento del diseño; las usan los tests) ---

    public EstadoDocumentacion getEstadoActual() {
        return estadoActual;
    }

    public List<CambioEstadoDocumentacion> getCambioEstado() {
        return cambioEstado;
    }
}
