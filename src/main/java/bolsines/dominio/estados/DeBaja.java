package bolsines.dominio.estados;

/**
 * PATRON STATE - ESTADO CONCRETO.
 * No participa del CU 28: queda vacio y hereda la excepcion por defecto en todas las operaciones.
 */
public class DeBaja extends EstadoDocumentacion {

    public DeBaja() {
        super("DeBaja", "La documentación fue dada de baja");
    }
}
