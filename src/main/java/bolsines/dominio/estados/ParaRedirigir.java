package bolsines.dominio.estados;

/**
 * PATRON STATE - ESTADO CONCRETO.
 * Destino del alternativo A5 del CU 28. Lo crea EnBolsinEnviado.registrarRedireccion(). El diseño no le define operaciones propias.
 */
public class ParaRedirigir extends EstadoDocumentacion {

    public ParaRedirigir() {
        super("ParaRedirigir", "La CMC debe redirigir la documentación a un área interna");
    }
}
