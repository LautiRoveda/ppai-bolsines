package bolsines.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import bolsines.dominio.estados.DeBaja;
import bolsines.dominio.estados.EnBolsinEnviado;
import bolsines.dominio.estados.EnBolsinSaliente;
import bolsines.dominio.estados.EnRemito;
import bolsines.dominio.estados.EstadoDocumentacion;
import bolsines.dominio.estados.NoRecibida;
import bolsines.dominio.estados.ParaRedirigir;
import bolsines.dominio.estados.RecibidoYAceptado;
import bolsines.dominio.estados.RecibidoYRechazado;
import bolsines.dominio.estados.Registrada;

/** El conversor del patron State: objeto estado <-> nombre guardado en la base. */
class EstadoDocumentacionConverterTest {

    private final EstadoDocumentacionConverter conversor = new EstadoDocumentacionConverter();

    @Test
    void cadaUnoDeLosNueveEstadosSeGuardaPorNombreYSeReconstruyeComoSuSubclase() {
        List<EstadoDocumentacion> estados = List.of(new Registrada(), new EnRemito(), new EnBolsinSaliente(),
                new EnBolsinEnviado(), new RecibidoYAceptado(), new RecibidoYRechazado(), new NoRecibida(),
                new ParaRedirigir(), new DeBaja());

        for (EstadoDocumentacion estado : estados) {
            String guardado = conversor.convertToDatabaseColumn(estado);
            EstadoDocumentacion leido = conversor.convertToEntityAttribute(guardado);

            assertEquals(estado.getClass().getSimpleName(), guardado);   // en la base va el nombre de la clase
            assertEquals(estado.getClass(), leido.getClass());           // y vuelve como la misma subclase
        }
    }

    @Test
    void unNombreQueNoEsUnEstadoLanzaUnaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> conversor.convertToEntityAttribute("Inventado"));
    }

    @Test
    void nullSeConvierteEnNull() {
        assertNull(conversor.convertToDatabaseColumn(null));
        assertNull(conversor.convertToEntityAttribute(null));
    }
}
