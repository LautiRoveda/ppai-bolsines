package bolsines.gestor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import bolsines.demo.InicializadorBaseDeDatos;
import bolsines.dominio.Bolsin;
import bolsines.dominio.DetalleRemito;
import bolsines.dominio.Documentacion;
import bolsines.dominio.Remito;
import bolsines.dominio.estados.NoRecibida;
import bolsines.dominio.estados.ParaRedirigir;
import bolsines.dominio.estados.RecibidoYAceptado;
import bolsines.dominio.estados.RecibidoYRechazado;
import bolsines.persistencia.BolsinRepositorio;

/**
 * Prueba el CU 28 de punta a punta, como lo usa el EB desde el navegador:
 * pedido web -> pantalla -> gestor -> entidades -> State -> JPA -> base.
 * Cada resultado se verifica consultando la base.
 *
 * Los tests usan una base H2 en memoria (ver src/test/resources/application.properties).
 * Cada MockHttpSession es un navegador distinto.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RegistrarRecepcionTest {

    private static final String OPCION_1 = GestorRegistrarRecepcion.OPCION_CONTENIDO_IGUAL;
    private static final String OPCION_2 = GestorRegistrarRecepcion.OPCION_NO_RECIBIDA;
    private static final String OPCION_3 = GestorRegistrarRecepcion.OPCION_NO_CORRESPONDE;
    private static final String OPCION_4 = GestorRegistrarRecepcion.OPCION_REDIRIGIR;

    @Autowired
    private MockMvc web;
    @Autowired
    private JdbcTemplate base;
    @Autowired
    private InicializadorBaseDeDatos inicializador;
    @Autowired
    private BolsinRepositorio bolsinRepositorio;

    private MockHttpSession navegador;

    @BeforeEach
    void datosDeDemoYNavegadorNuevo() {
        inicializador.reiniciar();
        navegador = new MockHttpSession();
    }

    // ---------------- Pasos 1 a 4: listado ----------------

    @Test
    void elListadoMuestraSoloLosBolsinesEnviadosALaCmDelUsuario() throws Exception {
        String html = iniciar(navegador);

        assertTrue(html.contains("CM Rosario"));
        assertTrue(html.contains("<td>4471</td>") && html.contains("<td>4472</td>") && html.contains("<td>4473</td>"));
        assertFalse(html.contains("4480"), "El bolsin 108 va a Mendoza: no debe aparecer");
        assertFalse(html.contains("4490"), "El bolsin 109 esta Cerrado: no debe aparecer");
        assertFalse(html.contains("4500"), "El bolsin 110 va a la CMC: no debe aparecer");
    }

    // ---------------- Paso 4: filtro del listado. Alternativos A1 y A2 ----------------

    @Test
    void elListadoSePuedeFiltrarPorPrecintoOPorCmOrigen() throws Exception {
        iniciar(navegador);

        String porPrecinto = filtrar(navegador, "4472", "");
        assertTrue(porPrecinto.contains("<td>4472</td>"));
        assertFalse(porPrecinto.contains("<td>4471</td>"));

        String porCm = filtrar(navegador, "", "salta");   // sin importar mayusculas
        assertTrue(porCm.contains("<td>4473</td>"));
        assertFalse(porCm.contains("<td>4472</td>"));

        // Filtrado o no, la fila elegida es la del listado completo: el 4473 es la posicion 2.
        assertTrue(seleccionarBolsin(navegador, 2).contains("Estudio radiológico"));
    }

    @Test
    void a1NoSeEncuentraBolsinConElPrecintoIngresado() throws Exception {
        iniciar(navegador);
        String html = filtrar(navegador, "9999", "");

        assertTrue(html.contains("No se encuentra bolsín con el número de precinto ingresado"));
        assertFalse(html.contains("name='posicion'"));
        // y se puede volver a ver todo el listado
        assertTrue(filtrar(navegador, "", "").contains("<td>4471</td>"));
    }

    @Test
    void a2NoSeEncuentraBolsinConLaCmOrigenIngresada() throws Exception {
        iniciar(navegador);
        String html = filtrar(navegador, "", "Tucumán");

        assertTrue(html.contains("No se encuentra bolsín con la CM de origen ingresada"));
        assertFalse(html.contains("name='posicion'"));
    }

    @Test
    void elFiltroValidaLoQueSeEscribe() throws Exception {
        iniciar(navegador);

        // Precinto con letras o simbolos: se avisa y no se filtra (se ve el listado completo).
        for (String precinto : new String[] {"44a1", "-4471", "4471;DROP", "12345678901"}) {
            String html = filtrar(navegador, precinto, "");
            assertTrue(html.contains("El número de precinto tiene que ser un número"), precinto);
            assertTrue(html.contains("<td>4471</td>") && html.contains("<td>4473</td>"), precinto);
            assertFalse(html.contains("No se encuentra bolsín"), precinto);
        }

        // CM origen con numeros o simbolos: lo mismo.
        String html = filtrar(navegador, "", "C0rd0ba<script>");
        assertTrue(html.contains("La CM de origen solo puede tener letras y espacios"));
        assertTrue(html.contains("<td>4471</td>"));
        assertFalse(html.contains("<script>"), "lo que se escribio se muestra escapado");

        // Espacios de mas y acentos se aceptan.
        assertTrue(filtrar(navegador, "  4472  ", "").contains("<td>4472</td>"));
        assertTrue(filtrar(navegador, "", " córdoba ").contains("<td>4471</td>"));

        // Los dos datos a la vez: tienen que coincidir los dos.
        assertTrue(filtrar(navegador, "4472", "Mendoza").contains("<td>4472</td>"));
        assertTrue(filtrar(navegador, "4472", "Salta")
                .contains("No se encuentra bolsín con el número de precinto y la CM de origen ingresados"));
    }

    // ---------------- Flujo basico ----------------

    @Test
    void flujoBasicoDejaBolsinRemitosYDocumentacionRecibidosConElEmpleadoYLaMismaFecha() throws Exception {
        iniciar(navegador);
        String paso6 = seleccionarBolsin(navegador, 0);   // bolsin 105, precinto 4471, desde CM Cordoba
        assertTrue(paso6.contains("Dictamen médico") && paso6.contains("<td>301</td>"));
        elegirOpcion(navegador, OPCION_1);
        String fin = confirmar(navegador, "si");
        assertTrue(fin.contains("se registró correctamente"));

        // Bolsin: se cerro "Enviado" y el actual es "Recibido en CM destino", con Ana (empleado 1) como responsable
        assertEquals("Recibido en CM destino", estadoActualBolsin(105));
        assertEquals(1, numero("SELECT responsable_ce_id FROM cambio_estado_bolsin WHERE bolsin_id = 105 AND fecha_hora_fin IS NULL"));
        assertEquals(4, numero("SELECT COUNT(*) FROM cambio_estado_bolsin WHERE bolsin_id = 105"));
        assertEquals(1, numero("SELECT COUNT(*) FROM cambio_estado_bolsin WHERE bolsin_id = 105 AND fecha_hora_fin IS NULL"));
        // la fecha de fin de "Enviado" es la misma que la de inicio de "Recibido en CM destino"
        assertEquals(1, numero("SELECT COUNT(*) FROM cambio_estado_bolsin viejo JOIN cambio_estado_bolsin nuevo "
                + "ON nuevo.bolsin_id = viejo.bolsin_id AND nuevo.fecha_hora_fin IS NULL "
                + "WHERE viejo.bolsin_id = 105 AND viejo.estado_id = 3 AND viejo.fecha_hora_fin = nuevo.fecha_hora_inicio"));

        // Remitos: los dos en "Recibido y Aceptado"
        assertEquals("Recibido y Aceptado", estadoRemito(301));
        assertEquals("Recibido y Aceptado", estadoRemito(302));

        // Documentacion: las tres en RecibidoYAceptado (via State), con 5 CE cada una
        assertEquals(3, numero("SELECT COUNT(*) FROM documentacion WHERE id IN (9001, 9002, 9010) AND estado_actual = 'RecibidoYAceptado'"));
        assertEquals(15, numero("SELECT COUNT(*) FROM cambio_estado_documentacion WHERE documentacion_id IN (9001, 9002, 9010)"));
        assertEquals(3, numero("SELECT COUNT(*) FROM cambio_estado_documentacion WHERE documentacion_id IN (9001, 9002, 9010) "
                + "AND fecha_hora_fin IS NULL AND estado = 'RecibidoYAceptado' AND responsable_ce_id = 1"));
        assertEquals(0, numero("SELECT COUNT(*) FROM cambio_estado_documentacion WHERE documentacion_id IN (9001, 9002, 9010) "
                + "AND estado = 'EnBolsinEnviado' AND fecha_hora_fin IS NULL"));
        // bolsin y documentacion quedaron con la misma fecha y hora (el gestor la toma una sola vez)
        assertEquals(1, numero("SELECT COUNT(DISTINCT inicio) FROM ("
                + "SELECT fecha_hora_inicio AS inicio FROM cambio_estado_bolsin WHERE bolsin_id = 105 AND fecha_hora_fin IS NULL "
                + "UNION ALL SELECT fecha_hora_inicio FROM cambio_estado_documentacion "
                + "WHERE documentacion_id IN (9001, 9002, 9010) AND fecha_hora_fin IS NULL) t"));

        // Los otros bolsines no se tocaron, y la base quedo consistente
        assertEquals(13, numero("SELECT COUNT(*) FROM documentacion WHERE estado_actual = 'EnBolsinEnviado'"));
        verificarIntegridad();
    }

    @Test
    void elBolsinRecibidoYaNoApareceEnElListado() throws Exception {
        recibir(navegador, 0, OPCION_1);

        String html = iniciar(navegador);
        assertFalse(html.contains("4471"));
        assertTrue(html.contains("<td>4472</td>"));
    }

    // ---------------- A3: no se recibe toda la documentacion ----------------

    @Test
    void a3LaDocumentacionMarcadaQuedaNoRecibidaYSuRemitoParcial() throws Exception {
        iniciar(navegador);
        seleccionarBolsin(navegador, 1);                              // bolsin 106, precinto 4472, desde CM Mendoza
        String confirmacion = elegirOpcion(navegador, OPCION_2, 1);   // fila 1: D-9021 (remito 303)
        assertTrue(confirmacion.contains("No recibida"));
        confirmar(navegador, "si");

        assertEquals("NoRecibida", estadoActualDocumentacion(9021));
        assertEquals("RecibidoYAceptado", estadoActualDocumentacion(9020));
        assertEquals("RecibidoYAceptado", estadoActualDocumentacion(9022));
        assertEquals(1, numero("SELECT COUNT(*) FROM cambio_estado_documentacion WHERE documentacion_id = 9021 "
                + "AND estado = 'NoRecibida' AND fecha_hora_fin IS NULL AND responsable_ce_id = 1"));
        assertEquals(15, numero("SELECT COUNT(*) FROM cambio_estado_documentacion WHERE documentacion_id IN (9020, 9021, 9022)"));

        // Remitos: el 303 (tiene la no recibida) queda Parcial; el 304 queda Recibido y Aceptado.
        assertEquals("Recibido y Aceptado Parcial", estadoRemito(303));
        assertEquals("Recibido y Aceptado", estadoRemito(304));
        assertEquals("Recibido en CM destino", estadoActualBolsin(106));
        verificarIntegridad();
    }

    // ---------------- A4: documentacion que no corresponde al destino ----------------

    @Test
    void a4LaDocumentacionMarcadaQuedaRecibidoYRechazadoYSusRemitosParciales() throws Exception {
        iniciar(navegador);
        seleccionarBolsin(navegador, 2);                                 // bolsin 107, precinto 4473, desde CM Salta
        String confirmacion = elegirOpcion(navegador, OPCION_3, 0, 3);   // D-9030 (remito 305) y D-9033 (remito 306)
        assertTrue(confirmacion.contains("No corresponde al destino"));
        confirmar(navegador, "si");

        assertEquals("RecibidoYRechazado", estadoActualDocumentacion(9030));
        assertEquals("RecibidoYAceptado", estadoActualDocumentacion(9031));
        assertEquals("RecibidoYAceptado", estadoActualDocumentacion(9032));
        assertEquals("RecibidoYRechazado", estadoActualDocumentacion(9033));
        assertEquals(2, numero("SELECT COUNT(*) FROM cambio_estado_documentacion WHERE documentacion_id IN (9030, 9033) "
                + "AND estado = 'RecibidoYRechazado' AND fecha_hora_fin IS NULL AND responsable_ce_id = 1"));
        assertEquals(0, numero("SELECT COUNT(*) FROM cambio_estado_documentacion WHERE documentacion_id IN (9030, 9031, 9032, 9033) "
                + "AND estado = 'EnBolsinEnviado' AND fecha_hora_fin IS NULL"));

        // Los dos remitos tienen una documentacion rechazada: los dos quedan Parcial.
        assertEquals("Recibido y Aceptado Parcial", estadoRemito(305));
        assertEquals("Recibido y Aceptado Parcial", estadoRemito(306));
        assertEquals("Recibido en CM destino", estadoActualBolsin(107));
        verificarIntegridad();
    }

    // ---------------- A5: la documentacion se debe redirigir a otra area (solo la CMC) ----------------

    @Test
    void a5LaCmcRedirigeLaDocumentacionMarcadaYSuRemitoQuedaParcial() throws Exception {
        loguearUsuarioDeLaCmc();
        String listado = iniciar(navegador);
        assertTrue(listado.contains("Comisión Médica Central"));
        assertTrue(listado.contains("<td>4500</td>"));
        assertFalse(listado.contains("4471"), "Los bolsines de Rosario no son de la CMC");

        String opciones = seleccionarBolsin(navegador, 0);            // bolsin 110, precinto 4500, desde CM Cordoba
        assertTrue(opciones.contains(OPCION_4), "A la CMC si se le ofrece la opcion de redirigir");
        String confirmacion = elegirOpcion(navegador, OPCION_4, 0);   // fila 0: 9060 (remito 311)
        assertTrue(confirmacion.contains("Para redirigir a otra área"));
        confirmar(navegador, "si");

        assertEquals("ParaRedirigir", estadoActualDocumentacion(9060));
        assertEquals("RecibidoYAceptado", estadoActualDocumentacion(9061));
        assertEquals("RecibidoYAceptado", estadoActualDocumentacion(9062));
        // el responsable es Elena Sosa (empleado 5), la usuaria de la CMC
        assertEquals(1, numero("SELECT COUNT(*) FROM cambio_estado_documentacion WHERE documentacion_id = 9060 "
                + "AND estado = 'ParaRedirigir' AND fecha_hora_fin IS NULL AND responsable_ce_id = 5"));
        assertEquals("Recibido y Aceptado Parcial", estadoRemito(311));
        assertEquals("Recibido y Aceptado", estadoRemito(312));
        assertEquals("Recibido en CM destino", estadoActualBolsin(110));
        assertEquals(5, numero("SELECT responsable_ce_id FROM cambio_estado_bolsin WHERE bolsin_id = 110 AND fecha_hora_fin IS NULL"));
        verificarIntegridad();

        // y al leerla de la base vuelve como objeto ParaRedirigir
        Bolsin bolsin110 = bolsinRepositorio.findAllByOrderByNumeroBolsin().get(5);
        assertInstanceOf(ParaRedirigir.class, documentacionDe(bolsin110).get(0).getEstadoActual());
    }

    @Test
    void a5ExigeMarcarAlMenosUnaDocumentacion() throws Exception {
        loguearUsuarioDeLaCmc();
        iniciar(navegador);
        seleccionarBolsin(navegador, 0);

        assertTrue(elegirOpcion(navegador, OPCION_4).contains("marcar al menos una documentación"));
    }

    // ---------------- Persistencia del State: lo guardado se lee como la subclase correcta ----------------

    @Test
    void alLeerDeLaBaseElEstadoVuelveComoElObjetoDeSuSubclase() throws Exception {
        iniciar(navegador);
        seleccionarBolsin(navegador, 1);          // bolsin 106
        elegirOpcion(navegador, OPCION_2, 1);     // D-9021 no recibida
        confirmar(navegador, "si");
        iniciar(navegador);
        seleccionarBolsin(navegador, 1);          // ahora el segundo del listado es el 107
        elegirOpcion(navegador, OPCION_3, 0);     // D-9030 rechazada
        confirmar(navegador, "si");

        List<Documentacion> del106 = documentacionDe(bolsinNumero(1));
        assertInstanceOf(RecibidoYAceptado.class, del106.get(0).getEstadoActual());
        assertInstanceOf(NoRecibida.class, del106.get(1).getEstadoActual());
        assertEquals(5, del106.get(1).getCambioEstado().size());
        assertInstanceOf(NoRecibida.class, del106.get(1).getCambioEstado().get(4).getEstado());
        assertTrue(del106.get(1).getCambioEstado().get(4).sosActual());

        List<Documentacion> del107 = documentacionDe(bolsinNumero(2));
        assertInstanceOf(RecibidoYRechazado.class, del107.get(0).getEstadoActual());
        assertFalse(bolsinNumero(2).sosEnviado());
    }

    // ---------------- A6: no confirma ----------------

    @Test
    void a6SiElEbNoConfirmaNoSeModificaNada() throws Exception {
        List<Map<String, Object>> antes = volcado();

        iniciar(navegador);
        seleccionarBolsin(navegador, 1);
        elegirOpcion(navegador, OPCION_2, 0);
        String html = confirmar(navegador, "no");

        assertTrue(html.contains("No se registró ningún cambio"));
        assertEquals(antes, volcado());
        // y despues de cancelar ya no se puede confirmar
        confirmar(navegador, "si");
        assertEquals(antes, volcado());
    }

    // ---------------- Validaciones de la pantalla ----------------

    @Test
    void lasOpciones2Y3ExigenMarcarAlMenosUnaDocumentacion() throws Exception {
        List<Map<String, Object>> antes = volcado();
        iniciar(navegador);
        seleccionarBolsin(navegador, 1);

        assertTrue(elegirOpcion(navegador, OPCION_2).contains("marcar al menos una documentación"));
        // marcas que no son filas de la tabla: se descartan, asi que es como no haber marcado nada
        assertTrue(elegirOpcionConMarcas(navegador, OPCION_3, "99", "-1", "abc").contains("marcar al menos una documentación"));

        confirmar(navegador, "si");   // todavia no esta en el paso de confirmacion: no hace nada
        assertEquals(antes, volcado());
    }

    @Test
    void noSeAceptaUnaOpcionQueNoEsDeLaLista() throws Exception {
        List<Map<String, Object>> antes = volcado();
        iniciar(navegador);
        seleccionarBolsin(navegador, 1);

        assertTrue(elegirOpcion(navegador, "una opción inventada", 0).contains("Seleccioná una opción"));
        assertTrue(elegirOpcion(navegador, "", 0).contains("Seleccioná una opción"));
        // La opcion 4 (redirigir) no se le ofrece a la CM Rosario: mandarla a mano no sirve.
        assertFalse(ver(navegador).contains(OPCION_4));
        assertTrue(elegirOpcion(navegador, OPCION_4, 0).contains("Seleccioná una opción"));

        confirmar(navegador, "si");
        assertEquals(antes, volcado());
    }

    @Test
    void conLaOpcion1LasMarcasSeIgnoranYSeAceptaTodo() throws Exception {
        iniciar(navegador);
        seleccionarBolsin(navegador, 1);
        elegirOpcion(navegador, OPCION_1, 1);
        confirmar(navegador, "si");

        assertEquals(3, numero("SELECT COUNT(*) FROM documentacion WHERE id IN (9020, 9021, 9022) AND estado_actual = 'RecibidoYAceptado'"));
        assertEquals("Recibido y Aceptado", estadoRemito(303));
    }

    @Test
    void unaPosicionDeBolsinInvalidaMuestraUnAvisoYNoRompe() throws Exception {
        iniciar(navegador);
        for (String posicion : new String[] {"3", "-1", "abc", ""}) {
            MvcResult respuesta = web.perform(post("/registrar-recepcion/bolsin").session(navegador).param("posicion", posicion)).andReturn();
            assertEquals(303, respuesta.getResponse().getStatus());
            assertTrue(ver(navegador).contains("Seleccioná un bolsín del listado"));
        }
        // y el caso de uso sigue vivo
        assertTrue(seleccionarBolsin(navegador, 0).contains("Dictamen médico"));
    }

    @Test
    void sePuedeVolverAtrasYCambiarLaOpcionAntesDeConfirmar() throws Exception {
        iniciar(navegador);
        seleccionarBolsin(navegador, 1);
        elegirOpcion(navegador, OPCION_1);                      // llega a la confirmacion con la opcion 1
        String html = elegirOpcion(navegador, OPCION_2, 2);     // vuelve atras y elige la 2, marcando D-9022
        assertTrue(html.contains(OPCION_2) && html.contains("No recibida"));
        confirmar(navegador, "si");

        assertEquals("NoRecibida", estadoActualDocumentacion(9022));
        assertEquals("Recibido y Aceptado Parcial", estadoRemito(304));
        assertEquals("Recibido y Aceptado", estadoRemito(303));
    }

    @Test
    void reenviarLaConfirmacionNoRegistraDosVeces() throws Exception {
        recibir(navegador, 0, OPCION_1);
        confirmar(navegador, "si");
        confirmar(navegador, "si");

        assertEquals(4, numero("SELECT COUNT(*) FROM cambio_estado_bolsin WHERE bolsin_id = 105"));
        verificarIntegridad();
    }

    @Test
    void sinHaberEmpezadoElCasoDeUsoLasAccionesNoHacenNada() throws Exception {
        List<Map<String, Object>> antes = volcado();

        web.perform(post("/registrar-recepcion/bolsin").session(navegador).param("posicion", "0"));
        web.perform(post("/registrar-recepcion/opcion").session(navegador).param("opcion", OPCION_1));
        web.perform(post("/registrar-recepcion/confirmacion").session(navegador).param("confirma", "si"));

        assertEquals(302, web.perform(get("/registrar-recepcion/ver").session(navegador)).andReturn().getResponse().getStatus());
        assertEquals(antes, volcado());
    }

    @Test
    void soloConfirmaConSiYSoloCancelaConNo() throws Exception {
        List<Map<String, Object>> antes = volcado();
        iniciar(navegador);
        seleccionarBolsin(navegador, 0);
        elegirOpcion(navegador, OPCION_1);

        // Un valor que no es ni "si" ni "no" no hace nada: sigue en la confirmacion.
        assertTrue(confirmar(navegador, "quizas").contains("¿Confirma el registro"));
        assertEquals(antes, volcado());

        assertTrue(confirmar(navegador, "si").contains("se registró correctamente"));
    }

    @Test
    void siNoHaySesionActivaSeMuestraElMotivo() throws Exception {
        base.update("UPDATE sesion SET esta_activa = FALSE");

        MvcResult respuesta = web.perform(get("/registrar-recepcion").session(navegador)).andReturn();

        assertEquals(409, respuesta.getResponse().getStatus());
        assertTrue(respuesta.getResponse().getContentAsString().contains("No hay ninguna sesión activa en la base"));
    }

    // ---------------- Dos usuarios sobre el mismo bolsin ----------------

    @Test
    void siDosUsuariosRecibenElMismoBolsinElSegundoEsRechazadoYNoSeCorrompeNada() throws Exception {
        MockHttpSession otroNavegador = new MockHttpSession();
        // Los dos abren el listado y eligen el bolsin 105 antes de que ninguno confirme.
        iniciar(navegador);
        iniciar(otroNavegador);
        seleccionarBolsin(navegador, 0);
        seleccionarBolsin(otroNavegador, 0);
        elegirOpcion(navegador, OPCION_1);
        elegirOpcion(otroNavegador, OPCION_2, 0);

        confirmar(navegador, "si");
        List<Map<String, Object>> despuesDelPrimero = volcado();

        MvcResult segundo = web.perform(post("/registrar-recepcion/confirmacion").session(otroNavegador).param("confirma", "si")).andReturn();

        assertEquals(409, segundo.getResponse().getStatus());
        assertTrue(segundo.getResponse().getContentAsString().contains("Otro usuario ya registró la recepción"));
        assertEquals(despuesDelPrimero, volcado());   // el segundo no cambio nada
        assertEquals("RecibidoYAceptado", estadoActualDocumentacion(9001));
        verificarIntegridad();
    }

    // ---------------- Transicion invalida: la excepcion del State deshace todo ----------------

    @Test
    void siUnaDocumentacionNoEstaEnBolsinEnviadoElStateLanzaLaExcepcionYNoSeGuardaNada() throws Exception {
        // Se fuerza por SQL una documentacion del bolsin 105 a un estado que no admite aceptarDocumentacion.
        base.update("UPDATE documentacion SET estado_actual = 'Registrada' WHERE id = 9002");
        List<Map<String, Object>> antes = volcado();

        iniciar(navegador);
        seleccionarBolsin(navegador, 0);
        elegirOpcion(navegador, OPCION_1);
        MvcResult respuesta = web.perform(post("/registrar-recepcion/confirmacion").session(navegador).param("confirma", "si")).andReturn();

        assertEquals(409, respuesta.getResponse().getStatus());
        assertTrue(respuesta.getResponse().getContentAsString().contains("La documentación no puede aceptarse en este estado"));
        assertEquals(antes, volcado());   // rollback: ni el bolsin, ni los remitos, ni la otra documentacion cambiaron
    }

    // ---------------- Datos raros ----------------

    @Test
    void unAsuntoConPuntoYComaSeMuestraEntero() throws Exception {
        base.update("UPDATE documentacion SET asunto = 'Pericia; anexo II' WHERE id = 9002");
        iniciar(navegador);
        String html = seleccionarBolsin(navegador, 0);

        assertTrue(html.contains("<td>Pericia; anexo II</td><td>Expediente</td>"));
    }

    // ---------------- Ayudas ----------------

    /** Deja como sesion activa la de Elena Sosa, de la Comision Medica Central (sesion 2 de los datos de demo). */
    private void loguearUsuarioDeLaCmc() {
        base.update("UPDATE sesion SET esta_activa = (id = 2)");
    }

    /** Paso 1: el EB elige "Registrar recepcion de bolsin". Devuelve el HTML del listado. */
    private String iniciar(MockHttpSession sesion) throws Exception {
        return web.perform(get("/registrar-recepcion").session(sesion)).andReturn().getResponse().getContentAsString();
    }

    /** Paso 4: el EB filtra el listado por numero de precinto y/o por CM origen. */
    private String filtrar(MockHttpSession sesion, String precinto, String cmOrigen) throws Exception {
        return web.perform(get("/registrar-recepcion/ver").session(sesion)
                .param("precinto", precinto).param("cmOrigen", cmOrigen)).andReturn().getResponse().getContentAsString();
    }

    private String seleccionarBolsin(MockHttpSession sesion, int posicion) throws Exception {
        web.perform(post("/registrar-recepcion/bolsin").session(sesion).param("posicion", String.valueOf(posicion)));
        return ver(sesion);
    }

    private String elegirOpcion(MockHttpSession sesion, String opcion, int... marcadas) throws Exception {
        String[] textos = new String[marcadas.length];
        for (int i = 0; i < marcadas.length; i++) {
            textos[i] = String.valueOf(marcadas[i]);
        }
        return elegirOpcionConMarcas(sesion, opcion, textos);
    }

    private String elegirOpcionConMarcas(MockHttpSession sesion, String opcion, String... marcadas) throws Exception {
        var pedido = post("/registrar-recepcion/opcion").session(sesion).param("opcion", opcion);
        for (String marcada : marcadas) {
            pedido = pedido.param("marcadas", marcada);
        }
        web.perform(pedido);
        return ver(sesion);
    }

    private String confirmar(MockHttpSession sesion, String confirma) throws Exception {
        web.perform(post("/registrar-recepcion/confirmacion").session(sesion).param("confirma", confirma));
        return ver(sesion);
    }

    private void recibir(MockHttpSession sesion, int posicion, String opcion) throws Exception {
        iniciar(sesion);
        seleccionarBolsin(sesion, posicion);
        elegirOpcion(sesion, opcion);
        confirmar(sesion, "si");
    }

    private String ver(MockHttpSession sesion) throws Exception {
        return web.perform(get("/registrar-recepcion/ver").session(sesion)).andReturn().getResponse().getContentAsString();
    }

    private Bolsin bolsinNumero(int posicion) {
        return bolsinRepositorio.findAllByOrderByNumeroBolsin().get(posicion);
    }

    private List<Documentacion> documentacionDe(Bolsin bolsin) {
        List<Documentacion> documentaciones = new ArrayList<>();
        for (Remito remito : bolsin.getRemito()) {
            for (DetalleRemito detalle : remito.getDetalleRemito()) {
                documentaciones.add(detalle.getDocumentacion());
            }
        }
        return documentaciones;
    }

    private String estadoActualBolsin(int bolsinId) {
        return base.queryForObject("SELECT e.nombre FROM cambio_estado_bolsin ce JOIN estado e ON e.id = ce.estado_id "
                + "WHERE ce.bolsin_id = ? AND ce.fecha_hora_fin IS NULL", String.class, bolsinId);
    }

    private String estadoRemito(int numeroRemito) {
        return base.queryForObject("SELECT e.nombre FROM remito r JOIN estado e ON e.id = r.estado_id WHERE r.numero = ?",
                String.class, numeroRemito);
    }

    private String estadoActualDocumentacion(int documentacionId) {
        return base.queryForObject("SELECT estado_actual FROM documentacion WHERE id = ?", String.class, documentacionId);
    }

    private int numero(String sql) {
        return base.queryForObject(sql, Integer.class);
    }

    /** Todo lo que el CU puede modificar, fila por fila, para comparar "antes" y "despues". */
    private List<Map<String, Object>> volcado() {
        List<Map<String, Object>> filas = new ArrayList<>();
        filas.addAll(base.queryForList("SELECT bolsin_id, estado_id, fecha_hora_inicio, fecha_hora_fin, responsable_ce_id "
                + "FROM cambio_estado_bolsin ORDER BY bolsin_id, fecha_hora_inicio, id"));
        filas.addAll(base.queryForList("SELECT id, estado_id FROM remito ORDER BY id"));
        filas.addAll(base.queryForList("SELECT id, estado_actual FROM documentacion ORDER BY id"));
        filas.addAll(base.queryForList("SELECT documentacion_id, estado, fecha_hora_inicio, fecha_hora_fin, responsable_ce_id "
                + "FROM cambio_estado_documentacion ORDER BY documentacion_id, fecha_hora_inicio, id"));
        return filas;
    }

    /** Reglas que la base tiene que cumplir siempre. */
    private void verificarIntegridad() {
        assertEquals(0, numero("SELECT COUNT(*) FROM bolsin b WHERE (SELECT COUNT(*) FROM cambio_estado_bolsin ce "
                + "WHERE ce.bolsin_id = b.id AND ce.fecha_hora_fin IS NULL) <> 1"), "cada bolsin tiene un solo CE actual");
        assertEquals(0, numero("SELECT COUNT(*) FROM documentacion d WHERE (SELECT COUNT(*) FROM cambio_estado_documentacion ce "
                + "WHERE ce.documentacion_id = d.id AND ce.fecha_hora_fin IS NULL) <> 1"), "cada documentacion tiene un solo CE actual");
        assertEquals(0, numero("SELECT COUNT(*) FROM documentacion d JOIN cambio_estado_documentacion ce "
                + "ON ce.documentacion_id = d.id AND ce.fecha_hora_fin IS NULL WHERE ce.estado <> d.estado_actual"),
                "estado_actual coincide con el CE actual");
        assertEquals(0, numero("SELECT COUNT(*) FROM cambio_estado_documentacion WHERE fecha_hora_fin < fecha_hora_inicio"));
        assertEquals(0, numero("SELECT COUNT(*) FROM cambio_estado_bolsin WHERE fecha_hora_fin < fecha_hora_inicio"));
    }
}
