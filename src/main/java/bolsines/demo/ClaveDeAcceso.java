package bolsines.demo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Clave de acceso para cuando el programa se publica en un servidor (no es parte del CU 28).
 *
 * Si la variable de entorno BOLSINES_CLAVE esta definida, el navegador pide usuario y clave
 * antes de mostrar cualquier pagina (usuario: grupo10). Si no esta definida, como en la PC
 * o en la facultad, no se pide nada y el programa funciona igual que siempre.
 */
@Component
public class ClaveDeAcceso extends OncePerRequestFilter {

    private static final String USUARIO = "grupo10";

    private final String clave;

    public ClaveDeAcceso(@Value("${BOLSINES_CLAVE:}") String clave) {
        this.clave = clave.trim();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest pedido, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        if (clave.isEmpty() || traeLaClave(pedido)) {
            cadena.doFilter(pedido, respuesta);
            return;
        }
        respuesta.setHeader("WWW-Authenticate", "Basic realm=\"PPAI Bolsines\", charset=\"UTF-8\"");
        respuesta.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private boolean traeLaClave(HttpServletRequest pedido) {
        String encabezado = pedido.getHeader("Authorization");
        if (encabezado == null || !encabezado.startsWith("Basic ")) {
            return false;
        }
        try {
            String datos = new String(Base64.getDecoder().decode(encabezado.substring(6).trim()), StandardCharsets.UTF_8);
            return datos.equals(USUARIO + ":" + clave);
        } catch (IllegalArgumentException malFormado) {
            return false;
        }
    }
}
