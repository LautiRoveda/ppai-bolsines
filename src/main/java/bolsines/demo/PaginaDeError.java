package bolsines.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import bolsines.pantalla.Plantilla;

/**
 * INFRAESTRUCTURA (no es una clase del modelo).
 * Si algo falla, muestra una pagina con el mensaje en vez del error crudo.
 * Cuando esto pasa la transaccion ya se deshizo (rollback): no se guardo nada.
 */
@RestControllerAdvice
public class PaginaDeError {

    private static final Logger LOG = LoggerFactory.getLogger(PaginaDeError.class);

    /** Otro usuario ya registro la recepcion de ese bolsin (lo detecta @Version en Bolsin). */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<String> bolsinYaRecibido(ObjectOptimisticLockingFailureException error) {
        LOG.warn("Recepcion rechazada: el bolsin ya habia sido modificado por otro usuario");
        return pagina(HttpStatus.CONFLICT,
                "Otro usuario ya registró la recepción de ese bolsín. No se guardó ningún cambio.");
    }

    /** Una transicion que el estado de la documentacion no admite (excepcion por defecto del patron State). */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> operacionNoPermitida(IllegalStateException error) {
        LOG.warn("Operacion rechazada: {}", error.getMessage());
        return pagina(HttpStatus.CONFLICT, error.getMessage() + ". No se guardó ningún cambio.");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> errorInesperado(RuntimeException error) {
        // El error puede venir "envuelto" dentro de otro (por ejemplo, si salto mientras Spring creaba
        // el gestor). Se busca la causa original, y si es un mensaje propio del sistema se muestra.
        Throwable causa = error;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        if (causa instanceof IllegalStateException) {
            return operacionNoPermitida((IllegalStateException) causa);
        }
        LOG.error("Error inesperado", error);
        return pagina(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado (el detalle está en la consola). No se guardó ningún cambio.");
    }

    private ResponseEntity<String> pagina(HttpStatus estado, String mensaje) {
        String html = Plantilla.pagina("No se pudo completar la operación",
                "<h1>No se pudo completar la operación</h1>"
                        + "<p class='error'>" + Plantilla.escapar(mensaje) + "</p>"
                        + "<p class='acciones'><a class='boton' href='/registrar-recepcion'>Volver a empezar</a>"
                        + "<a href='/'>Inicio</a></p>");
        return ResponseEntity.status(estado).contentType(MediaType.parseMediaType("text/html;charset=UTF-8")).body(html);
    }
}
