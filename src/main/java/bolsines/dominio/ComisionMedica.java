package bolsines.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Entidad ComisionMedica (CM). */
@Entity
@Table(name = "comision_medica")
public class ComisionMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private String nombre;
    private String direccion;
    private String email;
    private String telefono;

    /** Constructor vacio: lo necesita JPA para reconstruir el objeto desde la base. */
    protected ComisionMedica() {
    }

    public ComisionMedica(String codigo, String nombre, String direccion, String email, String telefono) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.email = email;
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    /**
     * ¿Soy la Comision Medica Central? AGREGADO para el alternativo A5: la regla de negocio dice
     * que la documentacion solo se redirige a otra area cuando la CM destino es la CMC.
     */
    public boolean esCMC() {
        return codigo.equals("CMC");
    }

    /** ¿Soy la CM destino que me pasan? Dos CM son la misma si tienen el mismo codigo. */
    public boolean esTuCmDestino(ComisionMedica cm) {
        return this.codigo.equals(cm.codigo);
    }
}
