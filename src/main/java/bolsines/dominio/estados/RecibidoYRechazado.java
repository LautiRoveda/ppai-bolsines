package bolsines.dominio.estados;

/**
 * PATRON STATE - ESTADO CONCRETO.
 * Destino del alternativo A4 del CU 28. Lo crea EnBolsinEnviado.rechazarDocumentacion().
 */
public class RecibidoYRechazado extends EstadoDocumentacion {

    public RecibidoYRechazado() {
        super("RecibidoYRechazado", "La documentación llegó pero no corresponde a la CM destino");
    }
}
