package bolsines.dominio.estados;

/**
 * PATRON STATE - ESTADO CONCRETO.
 * No participa del CU 28: queda vacio y hereda la excepcion por defecto en todas las operaciones.
 */
public class EnBolsinSaliente extends EstadoDocumentacion {

    public EnBolsinSaliente() {
        super("EnBolsinSaliente", "La documentación está en un bolsín que todavía no fue retirado");
    }
}
