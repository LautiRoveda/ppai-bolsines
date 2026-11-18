package bolsines.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import bolsines.dominio.Sesion;

/** Repositorio JPA de Sesion. */
public interface SesionRepositorio extends JpaRepository<Sesion, Long> {

    /** La sesion activa: simula al usuario logueado (en los datos de demo, aperez de la CM Rosario). */
    Optional<Sesion> findByEstaActivaTrue();
}
