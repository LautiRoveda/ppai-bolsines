package bolsines.dominio.estados;

import java.time.LocalDateTime;
import java.util.List;

import bolsines.dominio.CambioEstadoDocumentacion;
import bolsines.dominio.Documentacion;
import bolsines.dominio.Empleado;

/**
 * PATRON STATE - ESTADO CONCRETO.
 *
 * Es el estado de la documentacion cuando el bolsin ya fue retirado por el correo.
 * Sobrescribe las 4 operaciones del CU 28. Para las otras 7 hereda la excepcion por defecto.
 */
public class EnBolsinEnviado extends EstadoDocumentacion {

    public EnBolsinEnviado() {
        super("EnBolsinEnviado", "La documentación viaja en un bolsín retirado por el correo");
    }

    /** CU 28, flujo basico: EnBolsinEnviado -> RecibidoYAceptado. */
    @Override
    public void aceptarDocumentacion(Documentacion doc, LocalDateTime fechaHora, Empleado empleado,
                                     List<CambioEstadoDocumentacion> cambioEstado) {
        CambioEstadoDocumentacion actual = this.buscarCEActual(cambioEstado);   // heredado de EstadoDocumentacion
        actual.setFechaHoraFin(fechaHora);                                      // cierra el cambio de estado anterior
        EstadoDocumentacion nuevoEstado = this.crearProximoEstado();            // encapsula new RecibidoYAceptado()
        CambioEstadoDocumentacion nuevoCE = this.crearCambioEstado(fechaHora, nuevoEstado, empleado);
        doc.agregarCambioEstado(nuevoCE);
        doc.setEstado(nuevoEstado);
    }

    /** CU 28, alternativo A3: EnBolsinEnviado -> NoRecibida. */
    @Override
    public void registrarDocumentacionNoRecibida(Documentacion doc, LocalDateTime fechaHora, Empleado empleado,
                                                 List<CambioEstadoDocumentacion> cambioEstado) {
        CambioEstadoDocumentacion actual = this.buscarCEActual(cambioEstado);
        actual.setFechaHoraFin(fechaHora);
        // crearProximoEstado() no tiene parametros y retorna RecibidoYAceptado (asi esta en el diseño).
        // En los alternativos el estado destino se crea con su new directo («create» del diagrama de clases).
        EstadoDocumentacion nuevoEstado = new NoRecibida();
        CambioEstadoDocumentacion nuevoCE = this.crearCambioEstado(fechaHora, nuevoEstado, empleado);
        doc.agregarCambioEstado(nuevoCE);
        doc.setEstado(nuevoEstado);
    }

    /** CU 28, alternativo A4: EnBolsinEnviado -> RecibidoYRechazado. */
    @Override
    public void rechazarDocumentacion(Documentacion doc, LocalDateTime fechaHora, Empleado empleado,
                                      List<CambioEstadoDocumentacion> cambioEstado) {
        CambioEstadoDocumentacion actual = this.buscarCEActual(cambioEstado);
        actual.setFechaHoraFin(fechaHora);
        // Igual que en A3: el estado destino se crea con su new directo.
        EstadoDocumentacion nuevoEstado = new RecibidoYRechazado();
        CambioEstadoDocumentacion nuevoCE = this.crearCambioEstado(fechaHora, nuevoEstado, empleado);
        doc.agregarCambioEstado(nuevoCE);
        doc.setEstado(nuevoEstado);
    }

    /** CU 28, alternativo A5: EnBolsinEnviado -> ParaRedirigir. */
    @Override
    public void registrarRedireccion(Documentacion doc, LocalDateTime fechaHora, Empleado empleado,
                                     List<CambioEstadoDocumentacion> cambioEstado) {
        CambioEstadoDocumentacion actual = this.buscarCEActual(cambioEstado);
        actual.setFechaHoraFin(fechaHora);
        // Igual que en A3: el estado destino se crea con su new directo.
        EstadoDocumentacion nuevoEstado = new ParaRedirigir();
        CambioEstadoDocumentacion nuevoCE = this.crearCambioEstado(fechaHora, nuevoEstado, empleado);
        doc.agregarCambioEstado(nuevoCE);
        doc.setEstado(nuevoEstado);
    }

    private EstadoDocumentacion crearProximoEstado() {
        return new RecibidoYAceptado();
    }

    private CambioEstadoDocumentacion crearCambioEstado(LocalDateTime fechaHora, EstadoDocumentacion estado,
                                                        Empleado empleado) {
        return new CambioEstadoDocumentacion(fechaHora, estado, empleado);
    }
}
