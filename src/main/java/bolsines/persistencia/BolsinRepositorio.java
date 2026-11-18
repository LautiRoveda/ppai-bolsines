package bolsines.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import bolsines.dominio.Bolsin;

/**
 * Repositorio JPA de Bolsin. Spring Data genera la implementacion: no se escribe SQL.
 * Al guardar un bolsin (save) se guardan en cascada su historial, sus remitos,
 * los detalles y la documentacion con su historial.
 */
public interface BolsinRepositorio extends JpaRepository<Bolsin, Long> {

    /** Todos los bolsines, ordenados por numero. */
    List<Bolsin> findAllByOrderByNumeroBolsin();
}
