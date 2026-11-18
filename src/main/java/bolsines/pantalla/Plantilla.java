package bolsines.pantalla;

/**
 * INFRAESTRUCTURA de pantalla (no es una clase del modelo).
 * Arma el HTML comun a todas las paginas (encabezado, estilos, indicador de pasos)
 * y escapa el texto que se muestra.
 *
 * IDENTIDAD VISUAL: los colores, el nombre y el lema del sistema estan juntos aca abajo,
 * para poder ajustarlos al MoodBoard del grupo cambiando solo estas lineas.
 */
public final class Plantilla {

    /** Nombre y lema que se muestran en el encabezado de todas las paginas. */
    private static final String NOMBRE_SISTEMA = "Bolsines";
    private static final String LEMA = "Cada documento, en su destino";

    /** Paleta de colores (variables CSS). */
    private static final String COLORES = ":root{"
            + "--principal:#1f4e79;"        // encabezado, botones y titulos
            + "--principal-oscuro:#163a5c;"
            + "--fondo:#f4f6f9;"
            + "--superficie:#ffffff;"
            + "--texto:#1c2733;"
            + "--texto-suave:#55626f;"
            + "--borde:#c9d2dc;"
            + "--ok:#1e7a3c;--ok-fondo:#e4f5e8;"
            + "--aviso:#8a5a00;--aviso-fondo:#fff3d1;"
            + "--error:#a12622;--error-fondo:#fde7e5;"
            + "}";

    private static final String ESTILOS = COLORES
            + "*{box-sizing:border-box}"
            + "body{font-family:'Segoe UI',system-ui,Arial,sans-serif;margin:0;background:var(--fondo);color:var(--texto);line-height:1.45}"
            + "header{background:var(--principal);color:#fff}"
            + "header .in{max-width:980px;margin:0 auto;padding:12px 16px;display:flex;align-items:baseline;gap:12px;flex-wrap:wrap}"
            + "header .marca{font-size:20px;font-weight:700;color:#fff;text-decoration:none}"
            + "header .lema{font-size:13px;opacity:.85}"
            + "header .grupo{margin-left:auto;font-size:12px;opacity:.8}"
            + "main{max-width:980px;margin:0 auto;padding:20px 16px 48px}"
            + "h1{font-size:24px;margin:0 0 12px}h2{font-size:18px;margin:24px 0 8px}h3{font-size:15px;margin:20px 0 6px}"
            // Indicador de pasos
            + ".pasos{display:flex;gap:6px;list-style:none;padding:0;margin:0 0 20px;flex-wrap:wrap}"
            + ".pasos li{flex:1 1 150px;padding:8px 10px;border-radius:6px;background:#e3e9f0;color:var(--texto-suave);font-size:13px}"
            + ".pasos li b{display:block;font-size:11px;font-weight:600;text-transform:uppercase;letter-spacing:.04em}"
            + ".pasos li.hecho{background:var(--ok-fondo);color:var(--ok)}"
            + ".pasos li.actual{background:var(--principal);color:#fff}"
            // Tarjetas y tablas
            + ".tarjeta{background:var(--superficie);border:1px solid var(--borde);border-radius:8px;padding:16px 18px;margin:0 0 16px}"
            + ".ancho{overflow-x:auto}"
            + "table{border-collapse:collapse;width:100%;margin:8px 0 12px;background:var(--superficie)}"
            + "th,td{border:1px solid var(--borde);padding:8px 10px;text-align:left;font-size:14px}"
            + "th{background:#e8eef5;font-weight:600}"
            + "tbody tr:hover,tr.fila:hover{background:#f1f5fa}"
            + "td.centro,th.centro{text-align:center;width:1%;white-space:nowrap}"
            // Formularios
            + "label{cursor:pointer}"
            + "input[type=radio],input[type=checkbox]{width:18px;height:18px;accent-color:var(--principal);vertical-align:middle}"
            + "input[type=text]{padding:8px 10px;border:1px solid var(--borde);border-radius:4px;font-size:14px;font-family:inherit}"
            + ".filtro{display:flex;gap:10px;align-items:flex-end;flex-wrap:wrap;margin:0 0 12px}"
            + ".filtro label{display:flex;flex-direction:column;font-size:13px;color:var(--texto-suave);gap:4px}"
            + ".opcion{display:block;border:1px solid var(--borde);border-radius:6px;padding:10px 12px;margin:0 0 8px;background:var(--superficie)}"
            + ".opcion:hover{border-color:var(--principal)}"
            + ".acciones{display:flex;gap:10px;align-items:center;flex-wrap:wrap;margin-top:16px}"
            + "button,.boton{background:var(--principal);color:#fff;border:1px solid var(--principal);padding:9px 18px;border-radius:5px;font-size:14px;"
            + "font-family:inherit;cursor:pointer;text-decoration:none;display:inline-block}"
            + "button:hover,.boton:hover{background:var(--principal-oscuro)}"
            + "button.secundario,.boton.secundario{background:var(--superficie);color:var(--principal)}"
            + "button.secundario:hover,.boton.secundario:hover{background:#e8eef5}"
            + "button:focus-visible,a:focus-visible,input:focus-visible{outline:3px solid #f0a500;outline-offset:2px}"
            + "a{color:var(--principal)}"
            // Mensajes
            + ".dato{background:#e8eef5;padding:8px 12px;border-radius:5px;margin:0 0 12px}"
            + ".ok,.aviso,.error{padding:10px 12px;border-radius:5px;margin:0 0 12px;border-left:5px solid}"
            + ".ok{background:var(--ok-fondo);border-color:var(--ok);color:#12421f}"
            + ".aviso{background:var(--aviso-fondo);border-color:var(--aviso);color:#4d3200}"
            + ".error{background:var(--error-fondo);border-color:var(--error);color:#5c1512}"
            + ".ayuda{font-size:13px;color:var(--texto-suave);margin:0 0 10px}"
            + ".actual{background:var(--ok-fondo)}";

    private Plantilla() {
    }

    /** Pagina completa: encabezado comun + el cuerpo que arma cada pantalla. */
    public static String pagina(String titulo, String cuerpo) {
        return "<!DOCTYPE html><html lang='es'><head><meta charset='utf-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1'>"
                + "<title>" + escapar(titulo) + " · " + NOMBRE_SISTEMA + "</title>"
                + "<style>" + ESTILOS + "</style></head><body>"
                + "<header><div class='in'>"
                + "<a class='marca' href='/'>" + NOMBRE_SISTEMA + "</a>"
                + "<span class='lema'>" + LEMA + "</span>"
                + "<span class='grupo'>PPAI · Grupo 10 · 3K3 · CU 28 con patrón State</span>"
                + "</div></header>"
                + "<main>" + cuerpo + "</main>"
                + "</body></html>";
    }

    /**
     * Indicador de pasos del caso de uso: muestra en que paso esta el EB,
     * cuales ya hizo y cuales faltan. El primer paso es el numero 1.
     */
    public static String pasos(int pasoActual) {
        String[] nombres = {"Elegir el bolsín", "Indicar cómo llegó", "Confirmar", "Recepción registrada"};
        StringBuilder h = new StringBuilder("<ol class='pasos'>");
        for (int i = 0; i < nombres.length; i++) {
            int numero = i + 1;
            String clase = numero < pasoActual ? "hecho" : (numero == pasoActual ? "actual" : "");
            h.append("<li class='").append(clase).append("'><b>Paso ").append(numero).append(" de ")
                    .append(nombres.length).append("</b>").append(nombres[i]).append("</li>");
        }
        return h.append("</ol>").toString();
    }

    /** Evita que un texto de la base rompa el HTML. */
    public static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
