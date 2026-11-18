package bolsines.dominio;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Entidad DetalleRemito: une un remito con una documentacion. */
@Entity
@Table(name = "detalle_remito")
public class DetalleRemito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "area_cmc_destino")
    private String areaCMCDestino;

    /** Al guardar el detalle se guarda tambien su documentacion (cascade). */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "documentacion_id")
    private Documentacion documentacion;

    /** Constructor vacio: lo necesita JPA. */
    protected DetalleRemito() {
    }

    public DetalleRemito(String areaCMCDestino, Documentacion documentacion) {
        this.areaCMCDestino = areaCMCDestino;
        this.documentacion = documentacion;
    }

    /**
     * Secuencia (paso 6): le pide a su documentacion el tipo (getTipoDocumentacion) y el asunto (getAsunto).
     * Retorna los dos datos en un solo String: "tipo;asunto".
     */
    public String getTipoDocumentacion() {
        String tipo = documentacion.getTipoDocumentacion();
        String asunto = documentacion.getAsunto();
        return tipo + ";" + asunto;
    }

    /** Cadena de delegacion: el detalle le pasa el pedido a su documentacion. */
    public void aceptarDocumentacion(LocalDateTime fechaHora, Empleado empleado) {
        documentacion.aceptarDocumentacion(fechaHora, empleado);
    }

    /** Getter de la asociacion (modelo de dominio de la catedra). Lo usa el gestor en los alternativos. */
    public Documentacion getDocumentacion() {
        return documentacion;
    }
}
