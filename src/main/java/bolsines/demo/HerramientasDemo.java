package bolsines.demo;

import java.net.URI;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bolsines.pantalla.Plantilla;
import jakarta.servlet.http.HttpSession;

/**
 * INFRAESTRUCTURA de la demo (no es parte del CU 28 ni del modelo).
 * Menu de inicio, pagina para ver lo que hay guardado en la base, y botones para reiniciar los datos
 * y para cambiar el usuario logueado.
 * Las consultas SQL de esta clase son solo para MOSTRAR la base en la defensa.
 */
@RestController
public class HerramientasDemo {

    private static final String HTML = "text/html;charset=UTF-8";
    private static final String SQL_NOMBRE = "e.nombre || ' ' || e.apellido || ' (' || cm.nombre || ')'";
    private static final String SQL_SESIONES = "SELECT " + SQL_NOMBRE + " FROM sesion s "
            + "JOIN usuario u ON u.id = s.usuario_id JOIN empleado e ON e.id = u.empleado_id "
            + "JOIN comision_medica cm ON cm.id = e.comision_medica_id";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbc;
    private final InicializadorBaseDeDatos inicializador;

    public HerramientasDemo(JdbcTemplate jdbc, InicializadorBaseDeDatos inicializador) {
        this.jdbc = jdbc;
        this.inicializador = inicializador;
    }

    @GetMapping(value = "/", produces = HTML)
    public String inicio(@RequestParam(required = false) String reiniciado,
                         @RequestParam(required = false) String sinCasoDeUso,
                         @RequestParam(required = false) String usuarioCambiado) {
        String aviso = "";
        if (reiniciado != null) {
            aviso = "<p class='ok'>Los datos de demo se reiniciaron.</p>";
        }
        if (sinCasoDeUso != null) {
            aviso = "<p class='aviso'>No hay una recepción en curso. Empezá de nuevo desde acá.</p>";
        }
        if (usuarioCambiado != null) {
            aviso = "<p class='ok'>Se cambió el usuario logueado.</p>";
        }
        return Plantilla.pagina("PPAI Bolsines",
                "<h1>Sistema de Bolsines</h1>" + aviso
                        + "<p class='dato'>Usuario logueado: <b>" + Plantilla.escapar(usuarioLogueado()) + "</b></p>"
                        + "<div class='tarjeta'><h2 style='margin-top:0'>Encargado de Bolsines</h2>"
                        + "<p class='ayuda'>Registrá los bolsines que llegaron a tu comisión médica.</p>"
                        + "<p class='acciones'><a class='boton' href='/registrar-recepcion'>Registrar recepción de bolsín</a></p></div>"
                        + "<div class='tarjeta'><h2 style='margin-top:0'>Herramientas de la demo</h2>"
                        + "<p class='ayuda'>No son parte del caso de uso: sirven para mostrar y ensayar la defensa.</p>"
                        + "<p class='acciones'><a class='boton secundario' href='/base'>Ver la base de datos</a></p>"
                        + htmlCambiarUsuario()
                        + "<form method='post' action='/reiniciar'>"
                        + "<button type='submit' class='secundario'>Reiniciar datos de demo</button></form></div>");
    }

    /** Nombre y CM del empleado de la sesion activa. */
    private String usuarioLogueado() {
        List<String> nombres = jdbc.queryForList(SQL_SESIONES + " WHERE s.esta_activa = TRUE", String.class);
        return nombres.isEmpty() ? "(ninguno)" : nombres.get(0);
    }

    /** Un boton por cada sesion que no es la activa, para simular que se loguea otro usuario. */
    private String htmlCambiarUsuario() {
        StringBuilder h = new StringBuilder();
        for (Map<String, Object> sesion : jdbc.queryForList(
                "SELECT s.id AS id, (" + SQL_NOMBRE + ") AS nombre FROM sesion s JOIN usuario u ON u.id = s.usuario_id "
                        + "JOIN empleado e ON e.id = u.empleado_id JOIN comision_medica cm ON cm.id = e.comision_medica_id "
                        + "WHERE s.esta_activa = FALSE ORDER BY s.id")) {
            h.append("<form method='post' action='/cambiar-usuario' style='margin-bottom:10px'>")
                    .append("<input type='hidden' name='sesion' value='").append(sesion.get("id")).append("'>")
                    .append("<button type='submit' class='secundario'>Cambiar usuario a ")
                    .append(Plantilla.escapar(String.valueOf(sesion.get("nombre")))).append("</button></form>");
        }
        return h.toString();
    }

    /**
     * Simula que se loguea otro usuario: deja activa solo la sesion elegida.
     * (No hay pantalla de login: el usuario logueado es la sesion activa de la tabla sesion.)
     */
    @PostMapping("/cambiar-usuario")
    public ResponseEntity<Void> cambiarUsuario(@RequestParam(required = false) String sesion, HttpSession sesionWeb) {
        Integer existe = null;
        try {
            existe = jdbc.queryForObject("SELECT COUNT(*) FROM sesion WHERE id = ?", Integer.class, Long.parseLong(sesion));
        } catch (NumberFormatException noEsNumero) {
            // se ignora: no cambia nada
        }
        if (existe != null && existe == 1) {
            jdbc.update("UPDATE sesion SET esta_activa = (id = ?)", Long.parseLong(sesion));
            sesionWeb.invalidate();   // descarta el caso de uso que estuviera en curso en este navegador
        }
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/?usuarioCambiado=1")).build();
    }

    @PostMapping("/reiniciar")
    public ResponseEntity<Void> reiniciar(HttpSession sesionWeb) {
        inicializador.reiniciar();
        sesionWeb.invalidate();   // descarta el caso de uso que estuviera en curso en este navegador
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/?reiniciado=1")).build();
    }

    @GetMapping(value = "/base", produces = HTML)
    public String base() {
        StringBuilder h = new StringBuilder();
        h.append("<h1>Base de datos (PostgreSQL)</h1>");
        h.append("<p class='ayuda'>Las filas resaltadas son los cambios de estado actuales (sin fecha de fin).</p>");
        h.append("<p class='acciones'><a class='boton secundario' href='/'>Inicio</a>")
                .append("<a class='boton secundario' href='/registrar-recepcion'>Registrar recepción de bolsín</a></p>");

        h.append(tabla("Bolsines: historial de estados (cambio_estado_bolsin)",
                "SELECT b.numero_bolsin AS bolsin, b.numero_precinto AS precinto, o.nombre AS cm_origen, "
                        + "d.nombre AS cm_destino, e.nombre AS estado, ce.fecha_hora_inicio AS inicio, "
                        + "ce.fecha_hora_fin AS fin, emp.apellido || ', ' || emp.nombre AS responsable "
                        + "FROM cambio_estado_bolsin ce "
                        + "JOIN bolsin b ON b.id = ce.bolsin_id "
                        + "JOIN comision_medica o ON o.id = b.cm_origen_id "
                        + "JOIN comision_medica d ON d.id = b.cm_destino_id "
                        + "JOIN estado e ON e.id = ce.estado_id "
                        + "JOIN empleado emp ON emp.id = ce.responsable_ce_id "
                        + "ORDER BY b.numero_bolsin, ce.fecha_hora_inicio, ce.id"));

        h.append(tabla("Remitos: estado actual (remito)",
                "SELECT b.numero_bolsin AS bolsin, r.numero AS remito, e.nombre AS estado "
                        + "FROM remito r JOIN bolsin b ON b.id = r.bolsin_id JOIN estado e ON e.id = r.estado_id "
                        + "ORDER BY b.numero_bolsin, r.numero"));

        h.append(tabla("Documentación: estado actual, guardado por nombre (documentacion.estado_actual)",
                "SELECT b.numero_bolsin AS bolsin, r.numero AS remito, doc.numero AS documentacion, doc.asunto, "
                        + "doc.estado_actual "
                        + "FROM documentacion doc "
                        + "JOIN detalle_remito dr ON dr.documentacion_id = doc.id "
                        + "JOIN remito r ON r.id = dr.remito_id JOIN bolsin b ON b.id = r.bolsin_id "
                        + "ORDER BY b.numero_bolsin, r.numero, doc.numero"));

        h.append(tabla("Documentación: historial de estados (cambio_estado_documentacion)",
                "SELECT doc.numero AS documentacion, ce.estado, ce.fecha_hora_inicio AS inicio, "
                        + "ce.fecha_hora_fin AS fin, emp.apellido || ', ' || emp.nombre AS responsable "
                        + "FROM cambio_estado_documentacion ce "
                        + "JOIN documentacion doc ON doc.id = ce.documentacion_id "
                        + "JOIN empleado emp ON emp.id = ce.responsable_ce_id "
                        + "ORDER BY doc.numero, ce.fecha_hora_inicio, ce.id"));

        return Plantilla.pagina("Base de datos", h.toString());
    }

    /** Texto de una celda. Las fechas se muestran como 2026-11-17 10:30:00. */
    private String aTexto(Object valor) {
        if (valor == null) {
            return "";
        }
        if (valor instanceof Timestamp fecha) {
            return fecha.toLocalDateTime().format(FORMATO_FECHA);
        }
        return valor.toString();
    }

    /** Ejecuta una consulta y la muestra como tabla HTML, con los nombres de las columnas. */
    private String tabla(String titulo, String sql) {
        List<Map<String, Object>> filas = jdbc.queryForList(sql);
        StringBuilder h = new StringBuilder("<h3>" + Plantilla.escapar(titulo) + "</h3><div class='ancho'><table>");
        if (!filas.isEmpty()) {
            h.append("<tr>");
            for (String columna : filas.get(0).keySet()) {
                h.append("<th>").append(Plantilla.escapar(columna.toLowerCase())).append("</th>");
            }
            h.append("</tr>");
        }
        for (Map<String, Object> fila : filas) {
            boolean esActual = false;
            for (Map.Entry<String, Object> celda : fila.entrySet()) {
                if (celda.getKey().equalsIgnoreCase("fin") && celda.getValue() == null) {
                    esActual = true;
                }
            }
            h.append(esActual ? "<tr class='actual'>" : "<tr>");
            for (Object valor : fila.values()) {
                h.append("<td>").append(Plantilla.escapar(aTexto(valor))).append("</td>");
            }
            h.append("</tr>");
        }
        return h.append("</table></div>").toString();
    }
}
