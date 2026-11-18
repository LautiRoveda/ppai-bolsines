package bolsines.pantalla;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.SessionScope;

import bolsines.dominio.Bolsin;
import bolsines.gestor.GestorRegistrarRecepcion;

/**
 * «Boundary» del CU 28 Registrar recepcion de bolsin.
 *
 * Solo muestra y captura datos, y le pasa todo al gestor. No tiene logica del caso de uso
 * ni acceso a la base.
 *
 * @RestController: los metodos con @GetMapping / @PostMapping reciben lo que el EB hace en el navegador.
 * @SessionScope: Spring crea una pantalla por navegador (por sesion web).
 *
 * Como es una aplicacion web, "mostrar" significa guardar el dato en un atributo de la
 * pantalla; despues html() arma la pagina con lo que haya en esos atributos.
 * Los metodos son synchronized para que dos pedidos del mismo navegador no se pisen.
 */
@RestController
@SessionScope
public class PantallaRegistrarRecepcion {

    private static final String HTML = "text/html;charset=UTF-8";

    // ---- Atributos del analisis ----
    private String cmUsuario;
    private List<Bolsin> listadoBolsinEnviados = new ArrayList<>();
    private Bolsin bolsinSeleccionado;
    private List<String> nroRemito = new ArrayList<>();
    private List<String> tipoDocumentacion = new ArrayList<>();
    private List<String> asuntoDocumentacion = new ArrayList<>();
    private List<String> opcionesRecepcion = new ArrayList<>();
    private String opcionSeleccionada;

    // ---- Datos de apoyo para dibujar la pagina (no son del analisis) ----
    private List<String> cmOrigenBolsines = new ArrayList<>();
    private List<Integer> nroPrecintoBolsines = new ArrayList<>();
    private String cmOrigenSeleccionado;
    private int nroPrecintoSeleccionado;
    /** A3, A4 y A5: posiciones (filas de la tabla del paso 6) de la documentacion que marco el EB. */
    private List<Integer> documentacionMarcada = new ArrayList<>();
    private String mensajeError;
    /** Paso 4: lo que el EB escribio para filtrar el listado (vacio = sin filtro). */
    private String filtroPrecinto = "";
    private String filtroCmOrigen = "";
    /** Que pagina toca mostrar: SIN_INICIAR, BOLSINES, OPCIONES, CONFIRMACION, FIN o CANCELADO. */
    private String paso = "SIN_INICIAR";

    /** El gestor del caso de uso en curso. */
    private GestorRegistrarRecepcion gestor;
    /** Le pide a Spring un gestor nuevo cada vez que empieza el caso de uso. */
    private final ObjectProvider<GestorRegistrarRecepcion> nuevoGestor;

    public PantallaRegistrarRecepcion(ObjectProvider<GestorRegistrarRecepcion> nuevoGestor) {
        this.nuevoGestor = nuevoGestor;
    }

    // ------------------------------------------------------------------
    // Paso 1: el EB selecciona la opcion "Registrar recepcion de bolsin"
    // ------------------------------------------------------------------

    /**
     * Es el «create» new() de la secuencia. En una aplicacion web con Spring la pantalla
     * ya existe (una por navegador): este metodo la deja en blanco y crea un gestor nuevo,
     * que en su new() busca la CM del usuario y los bolsines enviados (pasos 2 a 4).
     */
    @GetMapping(value = "/registrar-recepcion", produces = HTML)
    public synchronized String opcionRegistrarRecepcion() {
        bolsinSeleccionado = null;
        opcionSeleccionada = null;
        documentacionMarcada = new ArrayList<>();
        mensajeError = null;
        filtroPrecinto = "";
        filtroCmOrigen = "";
        paso = "BOLSINES";
        gestor = nuevoGestor.getObject();   // «create» new() del gestor
        return html();
    }

    // ------------------------------------------------------------------
    // Mensajes que recibe del gestor (mostrar / solicitar)
    // ------------------------------------------------------------------

    public void mostrarCmEmpleado(String nombreCm) {
        this.cmUsuario = nombreCm;
    }

    public void mostrarCmOrigen(List<String> cmOrigenBolsines) {
        this.cmOrigenBolsines = cmOrigenBolsines;
    }

    public void mostrarNroPrecinto(List<Integer> nroPrecintoBolsines) {
        this.nroPrecintoBolsines = nroPrecintoBolsines;
    }

    public void solicitarSeleccionarBolsin(List<Bolsin> bolsinesEnviados) {
        this.listadoBolsinEnviados = bolsinesEnviados;
        this.paso = "BOLSINES";
    }

    /** Recibe una linea por documentacion: "numeroRemito;tipo;asunto". */
    public void mostrarDatosBolsin(List<String> datosBolsin) {
        nroRemito = new ArrayList<>();
        tipoDocumentacion = new ArrayList<>();
        asuntoDocumentacion = new ArrayList<>();
        for (String linea : datosBolsin) {
            String[] partes = linea.split(";", 3);   // como maximo 3 partes: el asunto puede tener ";"
            nroRemito.add(partes[0]);
            tipoDocumentacion.add(partes[1]);
            asuntoDocumentacion.add(partes[2]);
        }
    }

    public void solicitarSeleccionarOpcionRecepcion(List<String> opciones) {
        this.opcionesRecepcion = opciones;
        this.paso = "OPCIONES";
    }

    public void solicitarConfirmacion() {
        this.paso = "CONFIRMACION";
    }

    // ------------------------------------------------------------------
    // Acciones del Encargado de Bolsines (EB)
    // ------------------------------------------------------------------

    /** Paso 5: el EB selecciona un bolsin del listado (por su posicion en la lista). */
    @PostMapping("/registrar-recepcion/bolsin")
    public synchronized ResponseEntity<Void> seleccionarBolsin(@RequestParam(required = false) String posicion) {
        // Se puede elegir (o cambiar) el bolsin mientras no se haya confirmado ni cancelado.
        if (paso.equals("BOLSINES") || paso.equals("OPCIONES") || paso.equals("CONFIRMACION")) {
            int fila = aNumero(posicion);
            if (fila < 0 || fila >= listadoBolsinEnviados.size()) {
                mensajeError = "Seleccioná un bolsín del listado.";
                paso = "BOLSINES";
            } else {
                mensajeError = null;
                documentacionMarcada = new ArrayList<>();
                bolsinSeleccionado = listadoBolsinEnviados.get(fila);
                cmOrigenSeleccionado = cmOrigenBolsines.get(fila);
                nroPrecintoSeleccionado = nroPrecintoBolsines.get(fila);
                gestor.tomarSeleccionBolsin(bolsinSeleccionado);
            }
        }
        return verPagina();
    }

    /**
     * Paso 8: el EB selecciona una opcion de recepcion.
     * En A3, A4 y A5, ademas, marca en la lista del paso 6 la documentacion que no llego, que no
     * corresponde o que hay que redirigir: llega en el mismo formulario y se le pasa al gestor
     * antes que la opcion.
     */
    @PostMapping("/registrar-recepcion/opcion")
    public synchronized ResponseEntity<Void> opcionSeleccionada(
            @RequestParam(required = false) String opcion,
            @RequestParam(required = false) List<String> marcadas) {
        // Tambien se acepta desde la confirmacion, por si el EB vuelve atras y cambia la opcion.
        if (paso.equals("OPCIONES") || paso.equals("CONFIRMACION")) {
            paso = "OPCIONES";
            seleccionarDocumentacion(marcadas);

            // Validacion de la entrada.
            if (opcion == null || !opcionesRecepcion.contains(opcion)) {
                mensajeError = "Seleccioná una opción de recepción.";
            } else if (requiereMarcarDocumentacion(opcion) && documentacionMarcada.isEmpty()) {
                mensajeError = "Para esa opción tenés que marcar al menos una documentación en la lista.";
            } else {
                mensajeError = null;
                if (!requiereMarcarDocumentacion(opcion)) {
                    seleccionarDocumentacion(null);   // con la opcion 1 las marcas no cuentan
                }
                this.opcionSeleccionada = opcion;
                gestor.tomarOpcionSeleccionada(opcion);
            }
        }
        return verPagina();
    }

    /**
     * Alternativos A3, A4 y A5: el EB marca documentacion en la lista del paso 6.
     * AGREGADO respecto de la secuencia del flujo basico. Las marcas que no son
     * una fila valida de la tabla se descartan.
     */
    private void seleccionarDocumentacion(List<String> marcadas) {
        documentacionMarcada = new ArrayList<>();
        if (marcadas != null) {
            for (String marcada : marcadas) {
                int fila = aNumero(marcada);
                if (fila >= 0 && fila < nroRemito.size() && !documentacionMarcada.contains(fila)) {
                    documentacionMarcada.add(fila);
                }
            }
        }
        gestor.tomarSeleccionDocumentacion(documentacionMarcada);
    }

    /** Paso 10: el EB confirma la seleccion. Alternativo A6: no confirma (no se modifica nada). */
    @PostMapping("/registrar-recepcion/confirmacion")
    public synchronized ResponseEntity<Void> confirmacionSeleccion(@RequestParam(required = false) String confirma) {
        if (paso.equals("CONFIRMACION")) {   // evita registrar dos veces si se reenvia el formulario
            if ("si".equals(confirma)) {
                paso = "FIN";
                try {
                    gestor.tomarConfirmacion();
                } catch (RuntimeException error) {
                    paso = "SIN_INICIAR";   // el caso de uso se descarta; el error se muestra en PaginaDeError
                    throw error;
                }
            } else if ("no".equals(confirma)) {
                paso = "CANCELADO";         // A6: no se llama al gestor
            }
        }
        return verPagina();
    }

    /**
     * Muestra la pagina que corresponde al paso en el que esta el caso de uso.
     * En el listado (paso 4) tambien recibe el filtro por numero de precinto o por CM origen.
     */
    @GetMapping(value = "/registrar-recepcion/ver", produces = HTML)
    public synchronized ResponseEntity<String> ver(@RequestParam(required = false) String precinto,
                                                   @RequestParam(required = false) String cmOrigen) {
        if (paso.equals("SIN_INICIAR")) {
            // No hay un caso de uso en curso (por ejemplo, se reinicio el programa): vuelve al inicio.
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/?sinCasoDeUso=1")).build();
        }
        if (paso.equals("BOLSINES") && (precinto != null || cmOrigen != null)) {
            filtroPrecinto = precinto == null ? "" : precinto.trim();
            filtroCmOrigen = cmOrigen == null ? "" : cmOrigen.trim();

            // Validacion de lo que se escribio en el filtro. Un dato mal escrito no se usa para filtrar.
            if (!precintoValido()) {
                mensajeError = "El número de precinto tiene que ser un número, sin letras ni símbolos (hasta 9 dígitos).";
            } else if (!cmOrigenValida()) {
                mensajeError = "La CM de origen solo puede tener letras y espacios (hasta 60 caracteres).";
            } else {
                mensajeError = null;
            }
        }
        return ResponseEntity.ok(html());
    }

    // ------------------------------------------------------------------
    // Ayudas
    // ------------------------------------------------------------------

    /** Despues de cada accion el navegador vuelve a pedir la pagina (asi "actualizar" no reenvia el formulario). */
    private ResponseEntity<Void> verPagina() {
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/registrar-recepcion/ver")).build();
    }

    /** Convierte el texto del formulario en numero. Si no es un numero, devuelve -1. */
    private int aNumero(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException noEsNumero) {
            return -1;
        }
    }

    private boolean requiereMarcarDocumentacion(String opcion) {
        return opcion.equals(GestorRegistrarRecepcion.OPCION_NO_RECIBIDA)
                || opcion.equals(GestorRegistrarRecepcion.OPCION_NO_CORRESPONDE)
                || opcion.equals(GestorRegistrarRecepcion.OPCION_REDIRIGIR);
    }

    // ------------------------------------------------------------------
    // Armado del HTML
    // ------------------------------------------------------------------

    private String html() {
        StringBuilder h = new StringBuilder();
        h.append("<h1>Registrar recepción de bolsín</h1>");
        h.append("<p class='dato'>Comisión médica del usuario logueado: <b>")
                .append(Plantilla.escapar(cmUsuario)).append("</b></p>");

        if (paso.equals("BOLSINES")) {
            h.append(Plantilla.pasos(1)).append(htmlBolsines());
        } else if (paso.equals("OPCIONES")) {
            h.append(Plantilla.pasos(2)).append(htmlOpciones());
        } else if (paso.equals("CONFIRMACION")) {
            h.append(Plantilla.pasos(3)).append(htmlConfirmacion());
        } else if (paso.equals("FIN")) {
            h.append(Plantilla.pasos(4)).append(htmlFin());
        } else {
            h.append(htmlCancelado());
        }
        return Plantilla.pagina("Registrar recepción de bolsín", h.toString());
    }

    /**
     * Paso 4: listado de bolsines enviados a la CM del usuario, con el filtro por precinto o por
     * CM origen. Alternativos A1 y A2: el filtro no encuentra ningun bolsin.
     */
    private String htmlBolsines() {
        if (listadoBolsinEnviados.isEmpty()) {
            return "<p class='aviso'>No hay bolsines en estado Enviado para esta comisión médica.</p>"
                    + "<p class='acciones'><a class='boton secundario' href='/'>Volver al inicio</a></p>";
        }
        StringBuilder h = new StringBuilder();
        h.append("<h2>Bolsines enviados a esta comisión médica</h2>");
        h.append("<p class='ayuda'>Elegí el bolsín que recibiste. Lo identificás por el número de precinto.</p>");

        // Filtro (paso 4 de la ERS: "filtrando por alguno de estos datos")
        h.append("<form class='filtro' method='get' action='/registrar-recepcion/ver'>")
                .append("<label>N° de precinto<input type='text' name='precinto' inputmode='numeric' ")
                .append("pattern='[0-9]*' maxlength='9' title='Solo números, hasta 9 dígitos' value='")
                .append(Plantilla.escapar(filtroPrecinto)).append("'></label>")
                .append("<label>CM origen<input type='text' name='cmOrigen' maxlength='60' ")
                .append("title='Nombre o parte del nombre de la comisión médica' value='")
                .append(Plantilla.escapar(filtroCmOrigen)).append("'></label>")
                .append("<button type='submit' class='secundario'>Filtrar</button>");
        if (!filtroPrecinto.isEmpty() || !filtroCmOrigen.isEmpty()) {
            h.append("<a href='/registrar-recepcion/ver?precinto=&amp;cmOrigen='>Ver todos</a>");
        }
        h.append("</form>");

        if (mensajeError != null) {
            h.append("<p class='error'>").append(Plantilla.escapar(mensajeError)).append("</p>");
        }

        // Filas que pasan el filtro. El valor de cada fila es su posicion en el listado completo.
        StringBuilder filas = new StringBuilder();
        for (int i = 0; i < listadoBolsinEnviados.size(); i++) {
            if (!pasaElFiltro(i)) {
                continue;
            }
            filas.append("<tr class='fila'><td class='centro'><input type='radio' name='posicion' id='bolsin").append(i)
                    .append("' value='").append(i).append("' required></td>")
                    .append("<td><label for='bolsin").append(i).append("'>")
                    .append(Plantilla.escapar(cmOrigenBolsines.get(i))).append("</label></td>")
                    .append("<td>").append(nroPrecintoBolsines.get(i)).append("</td></tr>");
        }

        if (filas.length() == 0) {
            // A1 y A2
            if (!filtroPrecinto.isEmpty() && !filtroCmOrigen.isEmpty()) {
                h.append("<p class='aviso'>No se encuentra bolsín con el número de precinto y la CM de origen ingresados.</p>");
            } else if (!filtroPrecinto.isEmpty()) {
                h.append("<p class='aviso'>No se encuentra bolsín con el número de precinto ingresado.</p>");
            } else {
                h.append("<p class='aviso'>No se encuentra bolsín con la CM de origen ingresada.</p>");
            }
            h.append("<p class='acciones'><a class='boton secundario' href='/'>Cancelar</a></p>");
            return h.toString();
        }

        h.append("<form method='post' action='/registrar-recepcion/bolsin'>");
        h.append("<div class='ancho'><table><tr><th class='centro'>Elegir</th><th>CM origen</th><th>N° de precinto</th></tr>");
        h.append(filas).append("</table></div>");
        h.append("<p class='acciones'><button type='submit'>Seleccionar bolsín</button>")
                .append("<a class='boton secundario' href='/'>Cancelar</a></p>");
        h.append("</form>");
        return h.toString();
    }

    /** El precinto del filtro es valido si esta vacio o si son solo digitos (hasta 9). */
    private boolean precintoValido() {
        return filtroPrecinto.isEmpty() || filtroPrecinto.matches("[0-9]{1,9}");
    }

    /** La CM origen del filtro es valida si esta vacia o si son solo letras, espacios y puntos (hasta 60). */
    private boolean cmOrigenValida() {
        return filtroCmOrigen.isEmpty() || filtroCmOrigen.matches("[\\p{L} .]{1,60}");
    }

    /**
     * ¿El bolsin de esa posicion coincide con lo que el EB escribio en el filtro?
     * El precinto tiene que ser igual; de la CM origen alcanza con una parte del nombre,
     * sin importar mayusculas. Un dato vacio o mal escrito no filtra.
     */
    private boolean pasaElFiltro(int posicion) {
        boolean coincidePrecinto = filtroPrecinto.isEmpty() || !precintoValido()
                || String.valueOf(nroPrecintoBolsines.get(posicion)).equals(filtroPrecinto);
        boolean coincideCmOrigen = filtroCmOrigen.isEmpty() || !cmOrigenValida()
                || cmOrigenBolsines.get(posicion).toLowerCase().contains(filtroCmOrigen.toLowerCase());
        return coincidePrecinto && coincideCmOrigen;
    }

    /**
     * Paso 6: remitos del bolsin y, de cada documentacion, el asunto y el tipo.
     * Segun el paso se agrega una columna: casilla para marcar al principio (paso 7) o situacion al final (paso 9).
     */
    private String htmlDatosBolsin(boolean conCasillas) {
        boolean conSituacion = !conCasillas && requiereMarcarDocumentacion(opcionSeleccionada);
        StringBuilder h = new StringBuilder();
        h.append("<h2>Bolsín seleccionado</h2>");
        h.append("<p class='dato'>CM origen: <b>").append(Plantilla.escapar(cmOrigenSeleccionado))
                .append("</b> &middot; N° de precinto: <b>").append(nroPrecintoSeleccionado).append("</b></p>");
        h.append("<div class='ancho'><table><tr>");
        if (conCasillas) {
            h.append("<th class='centro'>Marcar</th>");
        }
        h.append("<th>N° de remito</th><th>Asunto</th><th>Tipo de documentación</th>");
        if (conSituacion) {
            h.append("<th>Situación</th>");
        }
        h.append("</tr>");
        for (int i = 0; i < nroRemito.size(); i++) {
            h.append("<tr class='fila'>");
            if (conCasillas) {
                h.append("<td class='centro'><input type='checkbox' name='marcadas' value='").append(i).append("'")
                        .append(" aria-label='Marcar ").append(Plantilla.escapar(asuntoDocumentacion.get(i))).append("'")
                        .append(documentacionMarcada.contains(i) ? " checked" : "").append("></td>");
            }
            h.append("<td>").append(Plantilla.escapar(nroRemito.get(i))).append("</td>")
                    .append("<td>").append(Plantilla.escapar(asuntoDocumentacion.get(i))).append("</td>")
                    .append("<td>").append(Plantilla.escapar(tipoDocumentacion.get(i))).append("</td>");
            if (conSituacion) {
                h.append("<td>").append(situacion(i)).append("</td>");
            }
            h.append("</tr>");
        }
        h.append("</table></div>");
        return h.toString();
    }

    /** Texto de la columna "Situacion" en la pagina de confirmacion (A3, A4 y A5). */
    private String situacion(int posicion) {
        if (!documentacionMarcada.contains(posicion)) {
            return "Recibida y aceptada";
        }
        if (opcionSeleccionada.equals(GestorRegistrarRecepcion.OPCION_NO_RECIBIDA)) {
            return "<b>No recibida</b>";
        }
        if (opcionSeleccionada.equals(GestorRegistrarRecepcion.OPCION_REDIRIGIR)) {
            return "<b>Para redirigir a otra área</b>";
        }
        return "<b>No corresponde al destino (rechazada)</b>";
    }

    /** Paso 7: opciones de recepcion. En el mismo formulario va la lista con las casillas para marcar. */
    private String htmlOpciones() {
        StringBuilder h = new StringBuilder();
        h.append("<form method='post' action='/registrar-recepcion/opcion'>");
        h.append(htmlDatosBolsin(true));
        h.append("<h2>¿Cómo llegó el bolsín?</h2>");
        if (mensajeError != null) {
            h.append("<p class='error'>").append(Plantilla.escapar(mensajeError)).append("</p>");
        }
        h.append("<p class='ayuda'>Si llegó todo bien, elegí la primera opción. Para las demás, marcá antes en la ")
                .append("lista la documentación que no llegó, que no corresponde a esta comisión médica o que hay ")
                .append("que redirigir.</p>");
        for (int i = 0; i < opcionesRecepcion.size(); i++) {
            String opcion = opcionesRecepcion.get(i);
            h.append("<label class='opcion'><input type='radio' name='opcion' required value='")
                    .append(Plantilla.escapar(opcion)).append("'")
                    .append(opcion.equals(opcionSeleccionada) ? " checked" : "").append("> ")
                    .append(i + 1).append(". ").append(Plantilla.escapar(opcion))
                    .append("</label>");
        }
        h.append("<p class='acciones'><button type='submit'>Continuar</button>")
                .append("<a class='boton secundario' href='/registrar-recepcion'>Elegir otro bolsín</a>")
                .append("<a href='/'>Cancelar</a></p>");
        h.append("</form>");
        return h.toString();
    }

    /** Paso 9: pedido de confirmacion, con el resumen de lo que se va a registrar. */
    private String htmlConfirmacion() {
        return htmlDatosBolsin(false)
                + "<h2>Confirmación</h2>"
                + "<div class='tarjeta'>"
                + "<p>Opción seleccionada: <b>" + Plantilla.escapar(opcionSeleccionada) + "</b></p>"
                + "<p>¿Confirma el registro de la recepción del bolsín?</p>"
                + "<p class='ayuda'>Al confirmar, el bolsín queda como recibido en esta comisión médica "
                + "y se notifica a la CM origen. Esto no se puede deshacer.</p>"
                + "<form method='post' action='/registrar-recepcion/confirmacion'>"
                + "<p class='acciones'><button type='submit' name='confirma' value='si'>Confirmar</button>"
                + "<button type='submit' name='confirma' value='no' class='secundario'>No confirmar</button></p>"
                + "</form></div>";
    }

    /** Paso 12: informa la ejecucion exitosa. */
    private String htmlFin() {
        return "<p class='ok'>La recepción del bolsín con precinto " + nroPrecintoSeleccionado
                + " se registró correctamente.</p>"
                + "<div class='tarjeta'>"
                + "<p>Opción registrada: <b>" + Plantilla.escapar(opcionSeleccionada) + "</b></p>"
                + "<p>Se notificó la recepción a la CM origen <b>" + Plantilla.escapar(cmOrigenSeleccionado)
                + "</b> (CU 29: ver el log de la consola).</p></div>"
                + "<p class='acciones'><a class='boton' href='/registrar-recepcion'>Registrar otra recepción</a>"
                + "<a class='boton secundario' href='/base'>Ver cómo quedó la base de datos</a>"
                + "<a href='/'>Inicio</a></p>";
    }

    private String htmlCancelado() {
        return "<p class='aviso'>No se confirmó la selección. No se registró ningún cambio.</p>"
                + "<p class='acciones'><a class='boton' href='/registrar-recepcion'>Volver a empezar</a>"
                + "<a href='/'>Inicio</a></p>";
    }
}
