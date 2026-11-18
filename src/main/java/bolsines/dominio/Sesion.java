package bolsines.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Entidad Sesion. La sesion activa indica quien es el usuario logueado. */
@Entity
@Table(name = "sesion")
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean estaActiva;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /** Constructor vacio: lo necesita JPA. */
    protected Sesion() {
    }

    public Sesion(boolean estaActiva, Usuario usuario) {
        this.estaActiva = estaActiva;
        this.usuario = usuario;
    }

    /** Secuencia: la sesion le pide el empleado a su usuario (getEmpleado) y lo retorna. */
    public Empleado getUsuarioLogueado() {
        return usuario.getEmpleado();
    }
}
