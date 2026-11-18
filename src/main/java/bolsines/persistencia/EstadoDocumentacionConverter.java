package bolsines.persistencia;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.hibernate.annotations.Immutable;

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
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * PERSISTENCIA DEL PATRON STATE.
 *
 * Los estados concretos son clases Java comunes: no son @Entity y no tienen tabla.
 * En la base se guarda el NOMBRE del estado en una columna de texto
 * (documentacion.estado_actual y cambio_estado_documentacion.estado).
 *
 * Este conversor es el unico lugar del programa que conoce la correspondencia
 * nombre <-> clase. JPA lo aplica solo (autoApply = true) a todo atributo de tipo
 * EstadoDocumentacion. El dominio no tiene ningun switch sobre el nombre.
 */
@Converter(autoApply = true)
@Immutable   // un objeto estado no cambia: Hibernate no necesita copiarlo para detectar cambios
public class EstadoDocumentacionConverter implements AttributeConverter<EstadoDocumentacion, String> {

    /** Para cada nombre guardado en la base, como crear el objeto de la subclase que corresponde. */
    private static final Map<String, Supplier<EstadoDocumentacion>> ESTADO_POR_NOMBRE = new HashMap<>();

    static {
        ESTADO_POR_NOMBRE.put("Registrada", () -> new Registrada());
        ESTADO_POR_NOMBRE.put("EnRemito", () -> new EnRemito());
        ESTADO_POR_NOMBRE.put("EnBolsinSaliente", () -> new EnBolsinSaliente());
        ESTADO_POR_NOMBRE.put("EnBolsinEnviado", () -> new EnBolsinEnviado());
        ESTADO_POR_NOMBRE.put("RecibidoYAceptado", () -> new RecibidoYAceptado());
        ESTADO_POR_NOMBRE.put("RecibidoYRechazado", () -> new RecibidoYRechazado());
        ESTADO_POR_NOMBRE.put("NoRecibida", () -> new NoRecibida());
        ESTADO_POR_NOMBRE.put("ParaRedirigir", () -> new ParaRedirigir());
        ESTADO_POR_NOMBRE.put("DeBaja", () -> new DeBaja());
    }

    /** Desmaterializacion (objeto -> base): se guarda el nombre del estado. */
    @Override
    public String convertToDatabaseColumn(EstadoDocumentacion estado) {
        if (estado == null) {
            return null;
        }
        return estado.getNombre();
    }

    /** Materializacion (base -> objeto): con el nombre guardado se crea el objeto de la subclase. */
    @Override
    public EstadoDocumentacion convertToEntityAttribute(String nombre) {
        if (nombre == null) {
            return null;
        }
        Supplier<EstadoDocumentacion> constructor = ESTADO_POR_NOMBRE.get(nombre);
        if (constructor == null) {
            throw new IllegalArgumentException("Estado de documentación desconocido en la base: " + nombre);
        }
        return constructor.get();
    }
}
