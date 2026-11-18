package bolsines.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import bolsines.dominio.Estado;

/** Repositorio JPA de Estado (estados de bolsin y de remito). */
public interface EstadoRepositorio extends JpaRepository<Estado, Long> {
}
