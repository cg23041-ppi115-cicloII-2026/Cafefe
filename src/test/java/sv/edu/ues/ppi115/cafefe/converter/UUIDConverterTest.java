package sv.edu.ues.ppi115.cafefe.converter;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UUIDConverterTest {

    private final UUIDConverter converter = new UUIDConverter();

    @Test
    public void testConvertToDatabaseColumnDevuelveElMismoUuid() {
        UUID id = UUID.randomUUID();

        assertSame(id, converter.convertToDatabaseColumn(id));
    }

    @Test
    public void testConvertToEntityAttributeDevuelveElMismoUuid() {
        UUID id = UUID.randomUUID();

        assertSame(id, converter.convertToEntityAttribute(id));
    }

    @Test
    public void testPasoDeNulosEnAmbasDirecciones() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }
}
