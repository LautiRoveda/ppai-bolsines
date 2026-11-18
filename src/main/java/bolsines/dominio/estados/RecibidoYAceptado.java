package bolsines.dominio.estados;

/**
 * PATRON STATE - ESTADO CONCRETO.
 * Destino del flujo basico del CU 28. Lo crea EnBolsinEnviado.crearProximoEstado(). En el CU 28 es un estado final: no redefine operaciones.
 */
public class RecibidoYAceptado extends EstadoDocumentacion {

    public RecibidoYAceptado() {
        super("RecibidoYAceptado", "La CM destino recibió y aceptó la documentación");
    }
}
