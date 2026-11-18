package bolsines.dominio.estados;

/**
 * PATRON STATE - ESTADO CONCRETO.
 * Destino del alternativo A3 del CU 28. Lo crea EnBolsinEnviado.registrarDocumentacionNoRecibida().
 */
public class NoRecibida extends EstadoDocumentacion {

    public NoRecibida() {
        super("NoRecibida", "La documentación figuraba en el remito pero no llegó");
    }
}
