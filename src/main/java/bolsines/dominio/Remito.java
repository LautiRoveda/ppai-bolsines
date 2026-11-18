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

/** Entidad Remito. No usa State: guarda su estado con la entidad Estado (ambito Remito). */
@Entity
@Table(name = "remito")
public class Remito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;
    private int numero;

    /** Los detalles se guardan junto con el remito (cascade). */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "remito_id", nullable = false)
    @OrderBy("id")
    @Fetch(FetchMode.SUBSELECT)
    private List<DetalleRemito> detalleRemito;

    @ManyToOne
    @JoinColumn(name = "estado_id")
    private Estado estado;

    @ManyToOne
    @JoinColumn(name = "cm_origen_id")
    private ComisionMedica cmOrigen;

    @ManyToOne
    @JoinColumn(name = "cm_destino_id")
    private ComisionMedica cmDestino;

    /** Constructor vacio: lo necesita JPA. */
    protected Remito() {
    }

    public Remito(int numero, LocalDateTime fecha, Estado estado, ComisionMedica cmOrigen, ComisionMedica cmDestino,
                  List<DetalleRemito> detalleRemito) {
        this.numero = numero;
        this.fecha = fecha;
        this.estado = estado;
        this.cmOrigen = cmOrigen;
        this.cmDestino = cmDestino;
        this.detalleRemito = detalleRemito;
    }

    public int getNumero() {
        return numero;
    }

    /** Secuencia (paso 6): un String "tipo;asunto" por cada detalle del remito. */
    public List<String> getTipoDocumentacion() {
        List<String> datos = new ArrayList<>();
        for (DetalleRemito detalle : detalleRemito) {
            datos.add(detalle.getTipoDocumentacion());
        }
        return datos;
    }

    public void actualizarRecibidoYAceptado(Estado estado) {
        setEstadoRecibidoYAceptado(estado);
    }

    private void setEstadoRecibidoYAceptado(Estado estado) {
        this.estado = estado;
    }

    /** Cadena de delegacion: el remito recorre sus detalles. */
    public void aceptarDocumentacion(LocalDateTime fechaHora, Empleado empleado) {
        for (DetalleRemito detalle : detalleRemito) {
            detalle.aceptarDocumentacion(fechaHora, empleado);
        }
    }

    /** Getter de la asociacion detalleRemito. Lo usa el gestor en los alternativos (ver docs/DECISIONES.md). */
    public List<DetalleRemito> getDetalleRemito() {
        return detalleRemito;
    }
}
