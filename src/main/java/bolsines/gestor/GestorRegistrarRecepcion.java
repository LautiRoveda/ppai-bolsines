package bolsines.gestor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bolsines.dominio.Bolsin;
import bolsines.dominio.ComisionMedica;
import bolsines.dominio.DetalleRemito;
import bolsines.dominio.Documentacion;
import bolsines.dominio.Empleado;
import bolsines.dominio.Estado;
import bolsines.dominio.Remito;
import bolsines.dominio.Sesion;
import bolsines.pantalla.PantallaRegistrarRecepcion;
import bolsines.persistencia.BolsinRepositorio;
import bolsines.persistencia.EstadoRepositorio;
import bolsines.persistencia.SesionRepositorio;

/**
 * «Control» del CU 28 Registrar recepcion de bolsin.
 *
 * Coordina el caso de uso con los metodos de la secuencia. La logica de dominio
 * esta en las entidades: el gestor solo les pide que hagan su parte. No decide
 * las transiciones de la documentacion (eso lo hace el patron State).
 *
 * @Scope("prototype"): Spring crea un gestor NUEVO cada vez que la pantalla lo pide,
 * o sea, uno por cada ejecucion del caso de uso (es el «create» new() de la secuencia).
 * El gestor guarda en sus atributos lo que el EB va eligiendo entre una pagina y la siguiente.
 */
@Service
@Scope("prototype")
public class GestorRegistrarRecepcion {

    /** Opciones de recepcion del paso 7 de la ERS. */
    public static final String OPCION_CONTENIDO_IGUAL = "El contenido del bolsín es igual al registrado";
    public static final String OPCION_NO_RECIBIDA = "No se recibe toda la documentación asociada a los remitos que contiene el bolsín";
    public static final String OPCION_NO_CORRESPONDE = "Existe documentación que no corresponde al destino";
    public static final String OPCION_REDIRIGIR = "La documentación se debe redirigir a otra área";

    private static final Logger LOG = LoggerFactory.getLogger(GestorRegistrarRecepcion.class);

    // ---- Atributos del diseño ----
    private Empleado empleadoLog;
    private ComisionMedica cmEmpleadoLog;
    private List<Bolsin> bolsinesEnviados;
    private Bolsin bolsinSelec;
    private Remito remito;                 // en los alternativos: el remito que se esta recorriendo
    private Documentacion documentacion;   // en los alternativos: la documentacion que se esta recorriendo
    private String cmOrigen;
    private String opcionRegistroSeleccionada;
    private LocalDateTime fechaHoraActual;
    private Estado estadoRecibidoCmDestino;
    private Estado estadoRecibidoYAceptado;

    // ---- Agregado para A3, A4 y A5: la documentacion que marco el EB (ver docs/DECISIONES.md) ----
    private List<Documentacion> documentacionSeleccionada = new ArrayList<>();

    // ---- Colaboradores que inyecta Spring: la pantalla (boundary) y los repositorios (persistencia) ----
    private final PantallaRegistrarRecepcion pantalla;
    private final SesionRepositorio sesionRepositorio;
    private final BolsinRepositorio bolsinRepositorio;
    private final EstadoRepositorio estadoRepositorio;

    /** new(): se ejecuta cuando la pantalla pide un gestor nuevo (paso 1). Hace los pasos 2 a 4. */
    public GestorRegistrarRecepcion(PantallaRegistrarRecepcion pantalla, SesionRepositorio sesionRepositorio,
                                    BolsinRepositorio bolsinRepositorio, EstadoRepositorio estadoRepositorio) {
        this.pantalla = pantalla;
        this.sesionRepositorio = sesionRepositorio;
        this.bolsinRepositorio = bolsinRepositorio;
        this.estadoRepositorio = estadoRepositorio;

        buscarCmEmpleado();
        buscarBolsinesEnviados();
        mostrarBolsinEncontrado();
    }

    /** Paso 2: busca y muestra la CM del usuario logueado. */
    private void buscarCmEmpleado() {
        Sesion sesion = sesionRepositorio.findByEstaActivaTrue()
                .orElseThrow(() -> new IllegalStateException("No hay ninguna sesión activa en la base"));
        empleadoLog = sesion.getUsuarioLogueado();
        cmEmpleadoLog = empleadoLog.getCM();
        pantalla.mostrarCmEmpleado(cmEmpleadoLog.getNombre());
    }

    /** Paso 3: busca los bolsines en estado Enviado cuya CM destino es la del usuario logueado. */
    private void buscarBolsinesEnviados() {
        bolsinesEnviados = new ArrayList<>();
        for (Bolsin bolsin : bolsinRepositorio.findAllByOrderByNumeroBolsin()) {
            boolean esDeMiCm = bolsin.esTuCM(cmEmpleadoLog);
            boolean estaEnviado = bolsin.sosEnviado();
            if (esDeMiCm && estaEnviado) {
                bolsinesEnviados.add(bolsin);
            }
        }
    }

    /** Paso 4: muestra de cada bolsin su CM origen y su numero de precinto, y pide elegir uno. */
    private void mostrarBolsinEncontrado() {
        List<String> cmOrigenBolsines = new ArrayList<>();
        List<Integer> nroPrecintoBolsines = new ArrayList<>();
        for (Bolsin bolsin : bolsinesEnviados) {
            cmOrigenBolsines.add(bolsin.getCmOrigen());
            nroPrecintoBolsines.add(bolsin.getNroPrecinto());
        }
        pantalla.mostrarCmOrigen(cmOrigenBolsines);
        pantalla.mostrarNroPrecinto(nroPrecintoBolsines);
        pantalla.solicitarSeleccionarBolsin(bolsinesEnviados);
    }

    /** Paso 5: el EB selecciono un bolsin del listado. */
    public void tomarSeleccionBolsin(Bolsin bolsin) {
        bolsinSelec = bolsin;
        cmOrigen = bolsinSelec.getCmOrigen();   // se guarda para la notificacion del paso 12
        documentacionSeleccionada = new ArrayList<>();
        mostrarBolsinSeleccionado();
    }

    /** Pasos 6 y 7: muestra remitos y documentacion del bolsin, y las opciones de recepcion. */
    private void mostrarBolsinSeleccionado() {
        List<String> datosBolsin = bolsinSelec.getDatosBolsin();
        pantalla.mostrarDatosBolsin(datosBolsin);

        List<String> opciones = new ArrayList<>(
                List.of(OPCION_CONTENIDO_IGUAL, OPCION_NO_RECIBIDA, OPCION_NO_CORRESPONDE));
        // Regla de negocio: redirigir a otra area solo ocurre cuando la CM destino es la CMC.
        if (cmEmpleadoLog.esCMC()) {
            opciones.add(OPCION_REDIRIGIR);
        }
        pantalla.solicitarSeleccionarOpcionRecepcion(opciones);
    }

    /**
     * Alternativos A3, A4 y A5: el EB marco documentacion en la lista del paso 6.
     * La pantalla manda las posiciones (filas de la tabla, en el mismo orden que getDatosBolsin())
     * y el gestor las traduce a los objetos Documentacion recorriendo remitos y detalles.
     * AGREGADO respecto de la secuencia del flujo basico: no esta en el diagrama.
     */
    public void tomarSeleccionDocumentacion(List<Integer> posiciones) {
        documentacionSeleccionada = new ArrayList<>();
        int posicion = 0;
        for (Remito r : bolsinSelec.getRemito()) {
            for (DetalleRemito detalle : r.getDetalleRemito()) {
                if (posiciones.contains(posicion)) {
                    documentacionSeleccionada.add(detalle.getDocumentacion());
                }
                posicion++;
            }
        }
    }

    /** Pasos 8 y 9: el EB eligio una opcion de recepcion; se pide confirmacion. */
    public void tomarOpcionSeleccionada(String opcion) {
        opcionRegistroSeleccionada = opcion;
        pantalla.solicitarConfirmacion();
    }

    /**
     * Pasos 10 a 12: el EB confirmo.
     *
     * @Transactional: todo lo que se guarda en este metodo va en UNA transaccion.
     * Los cambios se hacen primero en los objetos y se guardan todos juntos al final:
     * si algo falla antes (por ejemplo, una transicion que el estado no admite) no se llego a
     * escribir nada, y si falla al escribir, la transaccion se deshace (rollback).
     */
    @Transactional
    public void tomarConfirmacion() {
        actualizarBolsinRecibido();
        notificarRegistrarRecepcionCMOrigen();
        finCU();
    }

    /** Paso 11: actualiza el bolsin, sus remitos y su documentacion, y los guarda. */
    private void actualizarBolsinRecibido() {
        // Bolsin y Remito NO usan State: el gestor busca sus estados recorriendo los Estado (como en el analisis).
        List<Estado> estados = estadoRepositorio.findAll();
        for (Estado estado : estados) {
            if (estado.sosAmbitoBolsin() && estado.esRecibidoCmDestino()) {
                estadoRecibidoCmDestino = estado;
            }
        }
        for (Estado estado : estados) {
            if (estado.sosAmbitoRemito() && estado.esRecibidoYAceptado()) {
                estadoRecibidoYAceptado = estado;
            }
        }
        // Con State ya NO hay un tercer loop para buscar el estado de la documentacion.

        fechaHoraActual = getFechaActual();   // una sola vez: todo queda con la misma fecha y hora

        registrarCambioEstadoBolsin();
        registrarEstadoRemito();
        registrarCambioEstadoDocumentacion();

        // Persistencia: un solo guardado. JPA guarda en cascada el historial del bolsin, los remitos
        // y cada documentacion con su historial. saveAndFlush escribe en la base en este momento
        // (antes de notificar). Si otro usuario ya habia recibido este bolsin, JPA lo detecta
        // (@Version) y lanza una excepcion.
        bolsinRepositorio.saveAndFlush(bolsinSelec);
    }

    private LocalDateTime getFechaActual() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    /** El bolsin pasa a "Recibido en CM destino", registrando el empleado responsable. */
    private void registrarCambioEstadoBolsin() {
        bolsinSelec.actualizarRecibido(fechaHoraActual, estadoRecibidoCmDestino, empleadoLog);
    }

    /**
     * Flujo basico: todos los remitos pasan a "Recibido y Aceptado".
     * A3, A4 y A5: el remito que tiene alguna documentacion marcada pasa a "Recibido y Aceptado Parcial";
     * los demas, a "Recibido y Aceptado".
     */
    private void registrarEstadoRemito() {
        if (opcionRegistroSeleccionada.equals(OPCION_CONTENIDO_IGUAL)) {
            bolsinSelec.actualizarRecibidoYAceptado(estadoRecibidoYAceptado);
            return;
        }

        // Estado no tiene un esRecibidoYAceptadoParcial(): se busca por ambito y nombre.
        Estado estadoRecibidoYAceptadoParcial = null;
        for (Estado estado : estadoRepositorio.findAll()) {
            if (estado.sosAmbitoRemito() && estado.getNombre().equals("Recibido y Aceptado Parcial")) {
                estadoRecibidoYAceptadoParcial = estado;
            }
        }

        for (Remito r : bolsinSelec.getRemito()) {
            remito = r;
            boolean tieneDocumentacionMarcada = false;
            for (DetalleRemito detalle : remito.getDetalleRemito()) {
                if (documentacionSeleccionada.contains(detalle.getDocumentacion())) {
                    tieneDocumentacionMarcada = true;
                }
            }
            // Remito.actualizarRecibidoYAceptado(estado) recibe cualquier Estado de ambito Remito.
            if (tieneDocumentacionMarcada) {
                remito.actualizarRecibidoYAceptado(estadoRecibidoYAceptadoParcial);
            } else {
                remito.actualizarRecibidoYAceptado(estadoRecibidoYAceptado);
            }
        }
    }

    /**
     * METODO ENGANCHE DEL PATRON STATE (tramo rediseñado).
     * El gestor no busca ningun estado de documentacion: solo le pide a la documentacion que
     * haga la operacion y le pasa la fecha y el empleado. El estado siguiente lo crea el estado concreto.
     *
     * Flujo basico: el pedido baja por Bolsin -> Remito -> DetalleRemito -> Documentacion -> estadoActual.
     *
     * A3, A4 y A5: el diagrama solo tiene esa cadena para aceptarDocumentacion, asi que el gestor recorre
     * remitos y detalles y le envia a cada Documentacion la operacion publica que le corresponde.
     * Los if son sobre lo que eligio y marco el EB, NO sobre el estado de la documentacion.
     */
    private void registrarCambioEstadoDocumentacion() {
        if (opcionRegistroSeleccionada.equals(OPCION_CONTENIDO_IGUAL)) {
            bolsinSelec.aceptarDocumentacion(fechaHoraActual, empleadoLog);
            return;
        }

        for (Remito r : bolsinSelec.getRemito()) {
            remito = r;
            for (DetalleRemito detalle : remito.getDetalleRemito()) {
                documentacion = detalle.getDocumentacion();
                if (!documentacionSeleccionada.contains(documentacion)) {
                    documentacion.aceptarDocumentacion(fechaHoraActual, empleadoLog);                  // llego bien
                } else if (opcionRegistroSeleccionada.equals(OPCION_NO_RECIBIDA)) {
                    documentacion.registrarDocumentacionNoRecibida(fechaHoraActual, empleadoLog);     // A3
                } else if (opcionRegistroSeleccionada.equals(OPCION_NO_CORRESPONDE)) {
                    documentacion.rechazarDocumentacion(fechaHoraActual, empleadoLog);                // A4
                } else if (opcionRegistroSeleccionada.equals(OPCION_REDIRIGIR)) {
                    documentacion.registrarRedireccion(fechaHoraActual, empleadoLog);                 // A5
                } else {
                    throw new IllegalStateException("Opción de recepción no válida: " + opcionRegistroSeleccionada);
                }
            }
        }
    }

    /**
     * Paso 12: «include» CU 29 Notificar recepcion de bolsin.
     * STUB: no envia un mail real, registra la notificacion en el log de la consola.
     */
    private void notificarRegistrarRecepcionCMOrigen() {
        LOG.info("CU 29 (stub): notificacion enviada a la CM origen {} - bolsin con precinto {} recibido en {} el {}. "
                        + "Opcion registrada: {}. Documentacion marcada por el EB: {}",
                cmOrigen, bolsinSelec.getNroPrecinto(), cmEmpleadoLog.getNombre(), fechaHoraActual,
                opcionRegistroSeleccionada, documentacionSeleccionada.size());
    }

    private void finCU() {
        LOG.info("Fin del CU 28 Registrar recepcion de bolsin");
    }
}
