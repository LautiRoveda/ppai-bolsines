package bolsines.dominio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.Version;

/** Entidad Bolsin. No usa State: su historial apunta a la entidad Estado (ambito Bolsin). */
@Entity
@Table(name = "bolsin")
public class Bolsin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Control de concurrencia de JPA (no es del modelo): si dos usuarios intentan registrar
     * la recepcion del mismo bolsin, el segundo falla en vez de pisar al primero.
     */
    @Version
    private int version;

    private LocalDateTime fecha;
    private int numeroBolsin;
    private int numeroPrecinto;
    private double peso;

    /** Los remitos se guardan junto con el bolsin (cascade). */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "bolsin_id")
    @OrderBy("numero")
    @Fetch(FetchMode.SUBSELECT)
    private List<Remito> remito;

    @ManyToOne
    @JoinColumn(name = "cm_origen_id")
    private ComisionMedica origen;

    @ManyToOne
    @JoinColumn(name = "cm_destino_id")
    private ComisionMedica destino;

    /** Historial de estados. Se guarda junto con el bolsin (cascade). */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "bolsin_id", nullable = false)
    @OrderBy("fechaHoraInicio, id")
    @Fetch(FetchMode.SUBSELECT)
    private List<CambioEstadoBolsin> cambioEstadoBolsin;

    /** Constructor vacio: lo necesita JPA. */
    protected Bolsin() {
    }

    public Bolsin(int numeroBolsin, LocalDateTime fecha, int numeroPrecinto, double peso,
                  ComisionMedica origen, ComisionMedica destino,
                  List<Remito> remito, List<CambioEstadoBolsin> cambioEstadoBolsin) {
        this.numeroBolsin = numeroBolsin;
        this.fecha = fecha;
        this.numeroPrecinto = numeroPrecinto;
        this.peso = peso;
        this.origen = origen;
        this.destino = destino;
        this.remito = remito;
        this.cambioEstadoBolsin = cambioEstadoBolsin;
    }

    /** ¿La CM que me pasan es mi CM destino? */
    public boolean esTuCM(ComisionMedica cm) {
        return destino.esTuCmDestino(cm);
    }

    /** ¿Mi cambio de estado actual es "Enviado"? */
    public boolean sosEnviado() {
        CambioEstadoBolsin actual = buscarCEActual();
        return actual.sosEnviado();
    }

    public String getCmOrigen() {
        return origen.getNombre();
    }

    public int getNroPrecinto() {
        return numeroPrecinto;
    }

    /**
     * Secuencia (paso 6): por cada remito pide el numero (getNumero) y los datos de su
     * documentacion (getTipoDocumentacion). Retorna una linea por documentacion:
     * "numeroRemito;tipo;asunto".
     */
    public List<String> getDatosBolsin() {
        List<String> datos = new ArrayList<>();
        for (Remito r : remito) {
            int numeroRemito = r.getNumero();
            for (String tipoYAsunto : r.getTipoDocumentacion()) {
                datos.add(numeroRemito + ";" + tipoYAsunto);
            }
        }
        return datos;
    }

    /** Paso 11: cierra el cambio de estado actual y crea el nuevo con el empleado responsable. */
    public void actualizarRecibido(LocalDateTime fechaHora, Estado estado, Empleado empleado) {
        CambioEstadoBolsin actual = buscarCEActual();
        actual.setFechaHoraFin(fechaHora);
        crearCEBolsin(fechaHora, estado, empleado);
    }

    private CambioEstadoBolsin buscarCEActual() {
        for (CambioEstadoBolsin ce : cambioEstadoBolsin) {
            if (ce.sosActual()) {
                return ce;
            }
        }
        return null;
    }

    private void crearCEBolsin(LocalDateTime fechaHora, Estado estado, Empleado empleado) {
        CambioEstadoBolsin nuevo = new CambioEstadoBolsin(fechaHora, estado, empleado);
        cambioEstadoBolsin.add(nuevo);
    }

    /** Paso 11: todos los remitos pasan al estado recibido. */
    public void actualizarRecibidoYAceptado(Estado estado) {
        for (Remito r : remito) {
            r.actualizarRecibidoYAceptado(estado);
        }
    }

    /** Cadena de delegacion del tramo rediseñado: el bolsin recorre sus remitos. */
    public void aceptarDocumentacion(LocalDateTime fechaHora, Empleado empleado) {
        for (Remito r : remito) {
            r.aceptarDocumentacion(fechaHora, empleado);
        }
    }

    /** Getter de la asociacion remito. Lo usa el gestor en los alternativos (ver docs/DECISIONES.md). */
    public List<Remito> getRemito() {
        return remito;
    }
}
