package bolsines.dominio.estados;

import java.time.LocalDateTime;
import java.util.List;

import bolsines.dominio.CambioEstadoDocumentacion;
import bolsines.dominio.Documentacion;
import bolsines.dominio.Empleado;

/**
 * PATRON STATE - ESTADO ABSTRACTO.
 *
 * Declara las 11 operaciones que dependen del estado de la documentacion, todas con la
 * misma firma (doc, fechaHora, empleado, cambioEstado).
 *
 * Implementacion por defecto: LANZAR UNA EXCEPCION. Cada estado concreto sobrescribe
 * solo las operaciones que admite. Asi, una transicion que no corresponde falla sola,
 * sin ningun if ni switch.
 *
 * Es clase abstracta y no interfaz porque: trae comportamiento por defecto, comparte
 * atributos (nombre, descripcion) y resuelve buscarCEActual() una sola vez para todos.
 */
public abstract class EstadoDocumentacion {

    protected String nombre;
    protected String descripcion;

    protected EstadoDocumentacion(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /** Recorre los cambios de estado y retorna el actual (el que no tiene fecha de fin). */
    protected CambioEstadoDocumentacion buscarCEActual(List<CambioEstadoDocumentacion> cambioEstado) {
        for (CambioEstadoDocumentacion ce : cambioEstado) {
            if (ce.sosActual()) {
                return ce;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Operaciones de transicion. Por defecto: excepcion.
    // ------------------------------------------------------------------

    public void aceptarDocumentacion(Documentacion doc, LocalDateTime fechaHora, Empleado empleado,
                                     List<CambioEstadoDocumentacion> cambioEstado) {
        throw new IllegalStateException("La documentación no puede aceptarse en este estado");
    }

    public void asignarRemito(Documentacion doc, LocalDateTime fechaHora, Empleado empleado,
                              List<CambioEstadoDocumentacion> cambioEstado) {
        throw new IllegalStateException("La documentación no puede asignarse a un remito en este estado");
    }

    public void cancelarRemito(Documentacion doc, LocalDateTime fechaHora, Empleado empleado,
                               List<CambioEstadoDocumentacion> cambioEstado) {
        throw new IllegalStateException("La documentación no puede quitarse de un remito cancelado en este estado");
    }

    public void incorporarEnBolsin(Documentacion doc, LocalDateTime fechaHora, Empleado empleado,
                                   List<CambioEstadoDocumentacion> cambioEstado) {
        throw new IllegalStateException("La documentación no puede incorporarse a un bolsín en este estado");
    }

    public void quitarDeBolsin(Documentacion doc, LocalDateTime fechaHora, Empleado empleado,
                               List<CambioEstadoDocumentacion> cambioEstado) {
        throw new IllegalStateException("La documentación no puede quitarse de un bolsín en este estado");
    }

    public void enviarBolsin(Documentacion doc, LocalDateTime fechaHora, Empleado empleado,
                             List<CambioEstadoDocumentacion> cambioEstado) {
        throw new IllegalStateException("La documentación no puede enviarse en un bolsín en este estado");
    }

    public void registrarDocumentacionNoRecibida(Documentacion doc, LocalDateTime fechaHora, Empleado empleado,
                                                 List<CambioEstadoDocumentacion> cambioEstado) {
        throw new IllegalStateException("La documentación no puede registrarse como no recibida en este estado");
    }

    public void registrarRedireccion(Documentacion doc, LocalDateTime fechaHora, Empleado empleado,
                                     List<CambioEstadoDocumentacion> cambioEstado) {
        throw new IllegalStateException("La documentación no puede redirigirse en este estado");
    }

    public void rechazarDocumentacion(Documentacion doc, LocalDateTime fechaHora, Empleado empleado,
                                      List<CambioEstadoDocumentacion> cambioEstado) {
        throw new IllegalStateException("La documentación no puede rechazarse en este estado");
    }

    public void reenviarDocumentacion(Documentacion doc, LocalDateTime fechaHora, Empleado empleado,
                                      List<CambioEstadoDocumentacion> cambioEstado) {
        throw new IllegalStateException("La documentación no puede reenviarse en este estado");
    }

    public void darDeBaja(Documentacion doc, LocalDateTime fechaHora, Empleado empleado,
                          List<CambioEstadoDocumentacion> cambioEstado) {
        throw new IllegalStateException("La documentación no puede darse de baja en este estado");
    }

    // --- El nombre es lo que EstadoDocumentacionConverter guarda en la base ---

    public String getNombre() {
        return nombre;
    }
}
