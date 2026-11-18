package bolsines;

import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada. Arranca Spring Boot (servidor web + JPA).
 *
 * Abrir en el navegador: http://localhost:8080
 */
@SpringBootApplication
public class BolsinesApplication {

    public static void main(String[] args) {
        // Zona horaria del sistema. Se fija con su nombre actual porque algunas instalaciones de
        // PostgreSQL rechazan el nombre viejo que usa Windows ("America/Buenos_Aires") al conectarse.
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));

        try {
            SpringApplication.run(BolsinesApplication.class, args);
        } catch (RuntimeException error) {
            avisarQueNoArranco(error);
            System.exit(1);
        }
        System.out.println();
        System.out.println("PPAI Bolsines listo. Abrir en el navegador: http://localhost:8080");
    }

    /** En un servidor los datos de conexion llegan por variable de entorno; si no hay, vale application.properties. */
    private static String configurado(String variableDeEntorno, String valorDelArchivo) {
        String valor = System.getenv(variableDeEntorno);
        return valor == null || valor.isBlank() ? valorDelArchivo : valor;
    }

    /** Si el programa no puede arrancar, deja al final de la consola la causa en limpio. */
    private static void avisarQueNoArranco(RuntimeException error) {
        Throwable causa = error;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        String motivo = causa.getMessage();

        // Lo mas comun es que falle la conexion a PostgreSQL: se prueba la conexion directamente
        // para mostrar el mensaje del propio PostgreSQL, que es el que dice que pasa.
        try (InputStream archivo = BolsinesApplication.class.getResourceAsStream("/application.properties")) {
            Properties configuracion = new Properties();
            configuracion.load(archivo);
            DriverManager.getConnection(
                    configurado("SPRING_DATASOURCE_URL", configuracion.getProperty("spring.datasource.url")),
                    configurado("SPRING_DATASOURCE_USERNAME", configuracion.getProperty("spring.datasource.username")),
                    configurado("SPRING_DATASOURCE_PASSWORD", configuracion.getProperty("spring.datasource.password")))
                    .close();
        } catch (SQLException errorDeConexion) {
            motivo = "no se pudo conectar a PostgreSQL: " + errorDeConexion.getMessage();
        } catch (IOException | RuntimeException noSePudoProbar) {
            // se muestra el motivo original
        }

        System.out.println();
        System.out.println("==================================================================");
        System.out.println(" PPAI Bolsines NO PUDO ARRANCAR");
        System.out.println(" Motivo: " + motivo);
        System.out.println();
        System.out.println(" Ver README.md, seccion 'Problemas frecuentes'.");
        System.out.println("==================================================================");
    }
}
