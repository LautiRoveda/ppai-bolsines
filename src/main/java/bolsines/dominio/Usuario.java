package bolsines.dominio;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Entidad Usuario. Cada usuario pertenece a un empleado. */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String password;
    private LocalDateTime fechaAlta;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    /** Constructor vacio: lo necesita JPA. */
    protected Usuario() {
    }

    public Usuario(String nombre, String password, LocalDateTime fechaAlta, Empleado empleado) {
        this.nombre = nombre;
        this.password = password;
        this.fechaAlta = fechaAlta;
        this.empleado = empleado;
    }

    public Empleado getEmpleado() {
        return empleado;
    }
}
