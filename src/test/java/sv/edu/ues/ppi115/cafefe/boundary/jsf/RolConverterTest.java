package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.convert.ConverterException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.control.RolRepository;
import sv.edu.ues.ppi115.cafefe.entity.Rol;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RolConverterTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolConverter converter;

    @Test
    public void testGetAsObjectVacioDevuelveNull() {
        assertNull(converter.getAsObject(null, null, null));
        assertNull(converter.getAsObject(null, null, ""));
        assertNull(converter.getAsObject(null, null, "   "));
        verifyNoInteractions(rolRepository);
    }

    @Test
    public void testGetAsObjectUuidValidoBuscaElRol() {
        UUID id = UUID.randomUUID();
        Rol rol = new Rol(id);
        when(rolRepository.findById(id)).thenReturn(rol);

        Rol resultado = converter.getAsObject(null, null, "  " + id + "  ");

        assertSame(rol, resultado);
    }

    @Test
    public void testGetAsObjectSinResultadoEnBaseDeDatosDevuelveNull() {
        when(rolRepository.findById(any())).thenReturn(null);

        assertNull(converter.getAsObject(null, null, UUID.randomUUID().toString()));
    }

    @Test
    public void testGetAsObjectConTextoInvalidoLanzaConverterException() {
        ConverterException error = assertThrows(ConverterException.class,
                () -> converter.getAsObject(null, null, "no-es-uuid"));

        assertEquals("Conversion Error", error.getFacesMessage().getSummary());
        assertEquals("Rol no valido.", error.getFacesMessage().getDetail());
        verifyNoInteractions(rolRepository);
    }

    @Test
    public void testGetAsStringConRolDevuelveElUuid() {
        UUID id = UUID.randomUUID();

        assertEquals(id.toString(), converter.getAsString(null, null, new Rol(id)));
    }

    @Test
    public void testGetAsStringSinRolDevuelveNull() {
        assertNull(converter.getAsString(null, null, null));
    }
}
