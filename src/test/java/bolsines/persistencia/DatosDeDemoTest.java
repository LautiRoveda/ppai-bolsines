package bolsines.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import bolsines.demo.InicializadorBaseDeDatos;

/** Verifica que el esquema y los datos de demo cargan, y que reiniciar() deja la base como al principio. */
@SpringBootTest
class DatosDeDemoTest {

    @Autowired
    private JdbcTemplate base;
    @Autowired
    private InicializadorBaseDeDatos inicializador;

    @BeforeEach
    void datosDeDemo() {
        inicializador.reiniciar();
    }

    @Test
    void losDatosDeDemoEstanCargados() {
        assertEquals(5, contar("SELECT COUNT(*) FROM comision_medica"));
        assertEquals(6, contar("SELECT COUNT(*) FROM bolsin"));
        assertEquals(12, contar("SELECT COUNT(*) FROM remito"));
        assertEquals(18, contar("SELECT COUNT(*) FROM documentacion"));
        assertEquals(1, contar("SELECT COUNT(*) FROM sesion WHERE esta_activa = TRUE"));
    }

    @Test
    void hayTresBolsinesEnviadosParaLaCmRosario() {
        assertEquals(3, contar("SELECT COUNT(*) FROM bolsin b "
                + "JOIN cambio_estado_bolsin ce ON ce.bolsin_id = b.id AND ce.fecha_hora_fin IS NULL "
                + "JOIN estado e ON e.id = ce.estado_id WHERE b.cm_destino_id = 1 AND e.nombre = 'Enviado'"));
    }

    @Test
    void cadaDocumentacionTieneUnSoloCambioDeEstadoActualYCoincideConSuEstado() {
        assertEquals(18, contar("SELECT COUNT(*) FROM cambio_estado_documentacion WHERE fecha_hora_fin IS NULL"));
        assertEquals(0, contar("SELECT COUNT(*) FROM documentacion d "
                + "JOIN cambio_estado_documentacion ce ON ce.documentacion_id = d.id AND ce.fecha_hora_fin IS NULL "
                + "WHERE ce.estado <> d.estado_actual"));
        // Bolsines 105 a 108 y 110 (enviados): 16 documentaciones con el historial completo (4 CE).
        // Bolsin 109 (cerrado): 2 con 3 CE.
        assertEquals(16, contar("SELECT COUNT(*) FROM documentacion WHERE estado_actual = 'EnBolsinEnviado'"));
        assertEquals(16 * 4 + 2 * 3, contar("SELECT COUNT(*) FROM cambio_estado_documentacion"));
    }

    @Test
    void hayUnBolsinEnviadoALaComisionMedicaCentralParaElAlternativoA5() {
        assertEquals(1, contar("SELECT COUNT(*) FROM bolsin b JOIN comision_medica cm ON cm.id = b.cm_destino_id "
                + "WHERE cm.codigo = 'CMC'"));
        assertEquals(1, contar("SELECT COUNT(*) FROM sesion s JOIN usuario u ON u.id = s.usuario_id "
                + "JOIN empleado e ON e.id = u.empleado_id JOIN comision_medica cm ON cm.id = e.comision_medica_id "
                + "WHERE cm.codigo = 'CMC' AND s.esta_activa = FALSE"));
    }

    @Test
    void reiniciarDejaLaBaseComoAlPrincipio() {
        base.update("UPDATE documentacion SET estado_actual = 'RecibidoYAceptado'");
        base.update("DELETE FROM cambio_estado_bolsin");

        inicializador.reiniciar();

        assertEquals(0, contar("SELECT COUNT(*) FROM documentacion WHERE estado_actual = 'RecibidoYAceptado'"));
        assertEquals(17, contar("SELECT COUNT(*) FROM cambio_estado_bolsin"));
    }

    private int contar(String sql) {
        return base.queryForObject(sql, Integer.class);
    }
}
