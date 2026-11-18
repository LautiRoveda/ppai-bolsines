package bolsines.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Entidad Empleado. Esta asignado a una comision medica. */
@Entity
@Table(name = "empleado")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String apellido;
    private String nombre;
    private String email;

    @ManyToOne
    @JoinColumn(name = "comision_medica_id")
    private ComisionMedica comisionMedica;

    /** Constructor vacio: lo necesita JPA. */
    protected Empleado() {
    }

    public Empleado(String apellido, String nombre, String email, ComisionMedica comisionMedica) {
        this.apellido = apellido;
        this.nombre = nombre;
        this.email = email;
        this.comisionMedica = comisionMedica;
    }

    public ComisionMedica getCM() {
        return comisionMedica;
    }
}
