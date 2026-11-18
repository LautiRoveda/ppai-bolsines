package bolsines.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bolsines.dominio.estados.DeBaja;
import bolsines.dominio.estados.EnBolsinEnviado;
import bolsines.dominio.estados.EnBolsinSaliente;
import bolsines.dominio.estados.EnRemito;
import bolsines.dominio.estados.EstadoDocumentacion;
import bolsines.dominio.estados.NoRecibida;
import bolsines.dominio.estados.ParaRedirigir;
import bolsines.dominio.estados.RecibidoYAceptado;
import bolsines.dominio.estados.RecibidoYRechazado;
import bolsines.dominio.estados.Registrada;

/**
 * Tests del patron State sobre Documentacion.
 * Usan el ejemplo de la explicacion del pseudocodigo: D-9001, con el historial CE1 a CE4,
 * y Ana Perez confirmando el 16/11 a las 10:30.
 */
class EstadoDocumentacionTest {

    private static final LocalDateTime CONFIRMACION = LocalDateTime.of(2026, 11, 16, 10, 30);

    private Empleado ana;
    private Empleado luis;

    @BeforeEach
    void crearEmpleados() {
        ComisionMedica rosario = new ComisionMedica("CM-ROS", "CM Rosario", "", "", "");
        ComisionMedica cordoba = new ComisionMedica("CM-CBA", "CM Córdoba", "", "", "");
        ana = new Empleado("Pérez", "Ana", "ana@cm.gob.ar", rosario);
        luis = new Empleado("Gómez", "Luis", "luis@cm.gob.ar", cordoba);
    }

    // ---------------- Flujo basico ----------------

    @Test
    void aceptarDesdeEnBolsinEnviadoCierraElCEActualYCreaRecibidoYAceptadoConElEmpleado() {
        Documentacion doc = documentacionEnBolsinEnviado();
        CambioEstadoDocumentacion ce4 = doc.getCambioEstado().get(3);

        doc.aceptarDocumentacion(CONFIRMACION, ana);

        // 1) cierra el cambio de estado que estaba abierto (CE4)
        assertEquals(CONFIRMACION, ce4.getFechaHoraFin());
        assertFalse(ce4.sosActual());

        // 2) el estado actual pasa a ser un objeto RecibidoYAceptado
        assertInstanceOf(RecibidoYAceptado.class, doc.getEstadoActual());

        // 3) se agrega un CE nuevo (CE5): inicio = fecha de confirmacion, sin fin, con Ana como responsable
        assertEquals(5, doc.getCambioEstado().size());
        CambioEstadoDocumentacion ce5 = doc.getCambioEstado().get(4);
        assertEquals(CONFIRMACION, ce5.getFechaHoraInicio());
        assertNull(ce5.getFechaHoraFin());
        assertTrue(ce5.sosActual());
        assertSame(ana, ce5.getResponsableCE());
        assertSame(doc.getEstadoActual(), ce5.getEstado());

        // 4) queda un solo cambio de estado actual, y el historial anterior no se toca
        assertEquals(1, doc.getCambioEstado().stream().filter(CambioEstadoDocumentacion::sosActual).count());
        assertSame(luis, ce4.getResponsableCE());
    }

    // ---------------- Flujos alternativos ----------------

    @Test
    void registrarNoRecibidaDesdeEnBolsinEnviadoPasaANoRecibida() {
        Documentacion doc = documentacionEnBolsinEnviado();
        CambioEstadoDocumentacion ce4 = doc.getCambioEstado().get(3);

        doc.registrarDocumentacionNoRecibida(CONFIRMACION, ana);

        verificarTransicion(doc, ce4, NoRecibida.class);
    }

    @Test
    void rechazarDesdeEnBolsinEnviadoPasaARecibidoYRechazado() {
        Documentacion doc = documentacionEnBolsinEnviado();
        CambioEstadoDocumentacion ce4 = doc.getCambioEstado().get(3);

        doc.rechazarDocumentacion(CONFIRMACION, ana);

        verificarTransicion(doc, ce4, RecibidoYRechazado.class);
    }

    @Test
    void registrarRedireccionDesdeEnBolsinEnviadoPasaAParaRedirigir() {
        Documentacion doc = documentacionEnBolsinEnviado();
        CambioEstadoDocumentacion ce4 = doc.getCambioEstado().get(3);

        doc.registrarRedireccion(CONFIRMACION, ana);

        verificarTransicion(doc, ce4, ParaRedirigir.class);
    }

    // ---------------- Transiciones invalidas: excepcion por defecto ----------------

    @Test
    void aceptarDesdeRegistradaLanzaLaExcepcionYNoCambiaNada() {
        Documentacion doc = documentacionEn(new Registrada());

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> doc.aceptarDocumentacion(CONFIRMACION, ana));

        assertEquals("La documentación no puede aceptarse en este estado", error.getMessage());
        assertInstanceOf(Registrada.class, doc.getEstadoActual());
        assertEquals(1, doc.getCambioEstado().size());
        assertTrue(doc.getCambioEstado().get(0).sosActual());
    }

    @Test
    void aceptarLanzaExcepcionEnTodosLosEstadosQueNoSonEnBolsinEnviado() {
        List<EstadoDocumentacion> estados = List.of(new Registrada(), new EnRemito(), new EnBolsinSaliente(),
                new RecibidoYAceptado(), new RecibidoYRechazado(), new NoRecibida(), new ParaRedirigir(), new DeBaja());

        for (EstadoDocumentacion estado : estados) {
            Documentacion doc = documentacionEn(estado);
            assertThrows(IllegalStateException.class, () -> doc.aceptarDocumentacion(CONFIRMACION, ana),
                    "Deberia fallar en " + estado.getNombre());
        }
    }

    @Test
    void noSePuedeAceptarDosVecesLaMismaDocumentacion() {
        Documentacion doc = documentacionEnBolsinEnviado();
        doc.aceptarDocumentacion(CONFIRMACION, ana);

        // Ahora responde RecibidoYAceptado, que hereda la excepcion por defecto.
        assertThrows(IllegalStateException.class, () -> doc.aceptarDocumentacion(CONFIRMACION, ana));
        assertEquals(5, doc.getCambioEstado().size());
    }

    @Test
    void enBolsinEnviadoHeredaLaExcepcionEnLasOperacionesQueNoSonDelCU28() {
        Documentacion doc = documentacionEnBolsinEnviado();

        assertThrows(IllegalStateException.class, () -> doc.asignarRemito(CONFIRMACION, ana));
        assertThrows(IllegalStateException.class, () -> doc.darDeBaja(CONFIRMACION, ana));
        assertInstanceOf(EnBolsinEnviado.class, doc.getEstadoActual());
    }

    // ---------------- Cadena de delegacion Bolsin -> Remito -> DetalleRemito -> Documentacion ----------------

    @Test
    void elBolsinHaceLlegarLaAceptacionATodasSusDocumentaciones() {
        Documentacion d9001 = documentacionEnBolsinEnviado();
        Documentacion d9002 = documentacionEnBolsinEnviado();
        Documentacion d9010 = documentacionEnBolsinEnviado();
        Remito r301 = new Remito(301, null, null, null, null,
                List.of(new DetalleRemito(null, d9001), new DetalleRemito(null, d9002)));
        Remito r302 = new Remito(302, null, null, null, null, List.of(new DetalleRemito(null, d9010)));
        Bolsin bolsin = new Bolsin(105, null, 4471, 2.4, null, null, List.of(r301, r302), new ArrayList<>());

        bolsin.aceptarDocumentacion(CONFIRMACION, ana);

        for (Documentacion doc : List.of(d9001, d9002, d9010)) {
            assertInstanceOf(RecibidoYAceptado.class, doc.getEstadoActual());
            CambioEstadoDocumentacion ultimo = doc.getCambioEstado().get(4);
            assertEquals(CONFIRMACION, ultimo.getFechaHoraInicio());   // todas con la misma fecha y hora
            assertSame(ana, ultimo.getResponsableCE());
        }
    }

    // ---------------- Ayudas ----------------

    private void verificarTransicion(Documentacion doc, CambioEstadoDocumentacion ceAnterior,
                                     Class<? extends EstadoDocumentacion> estadoEsperado) {
        assertEquals(CONFIRMACION, ceAnterior.getFechaHoraFin());
        assertInstanceOf(estadoEsperado, doc.getEstadoActual());
        assertEquals(5, doc.getCambioEstado().size());
        CambioEstadoDocumentacion nuevo = doc.getCambioEstado().get(4);
        assertEquals(CONFIRMACION, nuevo.getFechaHoraInicio());
        assertTrue(nuevo.sosActual());
        assertSame(ana, nuevo.getResponsableCE());
        assertSame(doc.getEstadoActual(), nuevo.getEstado());
    }

    /** D-9001 con el historial del ejemplo: Registrada -> EnRemito -> EnBolsinSaliente -> EnBolsinEnviado. */
    private Documentacion documentacionEnBolsinEnviado() {
        List<CambioEstadoDocumentacion> historial = new ArrayList<>();
        historial.add(cerrado(new Registrada(), LocalDateTime.of(2026, 11, 1, 8, 0), LocalDateTime.of(2026, 11, 3, 9, 10)));
        historial.add(cerrado(new EnRemito(), LocalDateTime.of(2026, 11, 3, 9, 10), LocalDateTime.of(2026, 11, 5, 11, 0)));
        historial.add(cerrado(new EnBolsinSaliente(), LocalDateTime.of(2026, 11, 5, 11, 0), LocalDateTime.of(2026, 11, 6, 14, 0)));
        EstadoDocumentacion actual = new EnBolsinEnviado();
        historial.add(new CambioEstadoDocumentacion(LocalDateTime.of(2026, 11, 6, 14, 0), actual, luis));
        return new Documentacion(9001, "Dictamen médico", null, new TipoDocumento("Dictamen", ""), null, actual, historial);
    }

    /** Una documentacion con un unico cambio de estado, abierto, en el estado que se pasa. */
    private Documentacion documentacionEn(EstadoDocumentacion estado) {
        List<CambioEstadoDocumentacion> historial = new ArrayList<>();
        historial.add(new CambioEstadoDocumentacion(LocalDateTime.of(2026, 11, 1, 8, 0), estado, luis));
        return new Documentacion(9001, "Dictamen médico", null, new TipoDocumento("Dictamen", ""), null, estado, historial);
    }

    private CambioEstadoDocumentacion cerrado(EstadoDocumentacion estado, LocalDateTime inicio, LocalDateTime fin) {
        CambioEstadoDocumentacion ce = new CambioEstadoDocumentacion(inicio, estado, luis);
        ce.setFechaHoraFin(fin);
        return ce;
    }
}
