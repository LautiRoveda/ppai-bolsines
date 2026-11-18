package bolsines.demo;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/**
 * INFRAESTRUCTURA de la demo (no es una clase del modelo).
 * Crea las tablas (db/schema.sql) y carga los datos de demo (db/data.sql).
 *
 * - Al arrancar: solo si la base todavia no tiene las tablas (primera vez).
 *   Si ya las tiene no toca nada, asi lo registrado se conserva entre un arranque y otro.
 * - Con el boton "Reiniciar datos de demo": deja la base como al principio.
 */
@Component
public class InicializadorBaseDeDatos implements CommandLineRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbc;

    public InicializadorBaseDeDatos(DataSource dataSource, JdbcTemplate jdbc) {
        this.dataSource = dataSource;
        this.jdbc = jdbc;
    }

    /** Lo ejecuta Spring Boot una vez, cuando la aplicacion termina de arrancar. */
    @Override
    public void run(String... args) {
        if (!existenLasTablas()) {
            reiniciar();
        }
    }

    /** Borra todo, crea las tablas y carga los datos de demo. */
    public void reiniciar() {
        ResourceDatabasePopulator scripts = new ResourceDatabasePopulator();
        scripts.setSqlScriptEncoding("UTF-8");
        scripts.addScript(new ClassPathResource("db/schema.sql"));
        scripts.addScript(new ClassPathResource("db/data.sql"));
        scripts.execute(dataSource);
    }

    private boolean existenLasTablas() {
        Integer cantidad = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE LOWER(table_name) = 'bolsin'", Integer.class);
        return cantidad != null && cantidad > 0;
    }
}
