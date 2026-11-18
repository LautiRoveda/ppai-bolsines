package bolsines.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad Estado del analisis. Se usa SOLO para bolsines y remitos (segun su ambito).
 * La documentacion no usa esta clase: usa el patron State (EstadoDocumentacion).
 */
@Entity
@Table(name = "estado")
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private String ambito;

    /** Constructor vacio: lo necesita JPA. */
    protected Estado() {
    }

    public Estado(String nombre, String descripcion, String ambito) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ambito = ambito;
    }

    public boolean sosAmbitoBolsin() {
        return ambito.equals("Bolsin");
    }

    public boolean sosAmbitoRemito() {
        return ambito.equals("Remito");
    }

    public boolean sosEnviado() {
        return nombre.equals("Enviado");
    }

    public boolean esRecibidoCmDestino() {
        return nombre.equals("Recibido en CM destino");
    }

    public boolean esRecibidoYAceptado() {
        return nombre.equals("Recibido y Aceptado");
    }

    /** Del modelo de dominio de la catedra. Se usa en los alternativos para encontrar "Recibido y Aceptado Parcial". */
    public String getNombre() {
        return nombre;
    }
}
