package bolsines.dominio.estados;

/**
 * PATRON STATE - ESTADO CONCRETO.
 * No participa del CU 28: queda vacio y hereda la excepcion por defecto en todas las operaciones.
 */
public class EnRemito extends EstadoDocumentacion {

    public EnRemito() {
        super("EnRemito", "La documentación está incluida en un remito");
    }
}
