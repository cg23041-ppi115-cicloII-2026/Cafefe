package sv.edu.ues.ppi115.cafefe.boundary.rest;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.ppi115.cafefe.control.DescuentoService;
import sv.edu.ues.ppi115.cafefe.entity.Descuento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DescuentoResourceTest {

    private UriInfo mockUriInfo;
    private DescuentoService mockCS;
    private DescuentoResource cut;

    @BeforeEach
    public void setup() {
        mockUriInfo = Mockito.mock(UriInfo.class);
        UriBuilder mockUriBuilder = Mockito.mock(UriBuilder.class);
        mockCS = Mockito.mock(DescuentoService.class);

        Mockito.when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.path(Mockito.anyString())).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.build())
                .thenReturn(URI.create("http://localhost:8080/v1/descuento/1"));

        cut = new DescuentoResource();
        cut.descuentoService = mockCS;
    }

    @Test
    public void crearExitosoTest() {
        Descuento nuevo = new Descuento();

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(201, resultado.getStatus());
        assertNotNull(nuevo.getIdDescuento());
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearConIdTest() {
        Descuento nuevo = new Descuento(UUID.randomUUID());

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearNullTest() {
        Response resultado = cut.create(null, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearConExcepcionTest() {
        Descuento nuevo = new Descuento();
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).create(nuevo);

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void buscarPorRangoExitosoTest() {
        Mockito.when(mockCS.findRange(0, 50))
                .thenReturn(List.of(new Descuento(), new Descuento()));
        Mockito.when(mockCS.contar()).thenReturn(2L);

        Response resultado = cut.findRange(0, 50);

        assertEquals(200, resultado.getStatus());
        assertNotNull(resultado.getEntity());
        Mockito.verify(mockCS).findRange(0, 50);
        Mockito.verify(mockCS).contar();
    }

    @Test
    public void buscarPorRangoParametrosInvalidosTest() {
        assertEquals(400, cut.findRange(-1, 50).getStatus());
        assertEquals(400, cut.findRange(0, 0).getStatus());
        assertEquals(400, cut.findRange(0, 51).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoConExcepcionTest() {
        Mockito.when(mockCS.findRange(0, 50))
                .thenThrow(new RuntimeException("Error en base de datos"));

        Response resultado = cut.findRange(0, 50);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findRange(0, 50);
    }

    @Test
    public void buscarPorIdExitosoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findById(id)).thenReturn(new Descuento(id));

        Response resultado = cut.findById(id);

        assertEquals(200, resultado.getStatus());
        assertNotNull(resultado.getEntity());
        Mockito.verify(mockCS).findById(id);
    }

    @Test
    public void buscarPorIdNoEncontradoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findById(id)).thenReturn(null);

        assertEquals(404, cut.findById(id).getStatus());
        Mockito.verify(mockCS).findById(id);
    }

    @Test
    public void buscarPorIdNullTest() {
        assertEquals(400, cut.findById(null).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorIdConExcepcionTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findById(id))
                .thenThrow(new RuntimeException("Error en base de datos"));

        assertEquals(500, cut.findById(id).getStatus());
        Mockito.verify(mockCS).findById(id);
    }

    @Test
    public void actualizarExitosoTest() {
        UUID id = UUID.randomUUID();
        Descuento nuevo = new Descuento();
        Mockito.when(mockCS.findById(id)).thenReturn(new Descuento(id));

        Response resultado = cut.update(id, nuevo);

        assertEquals(200, resultado.getStatus());
        assertEquals(id, nuevo.getIdDescuento());
        Mockito.verify(mockCS).update(nuevo);
    }

    @Test
    public void actualizarNoEncontradoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findById(id)).thenReturn(null);

        assertEquals(404, cut.update(id, new Descuento()).getStatus());
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarParametrosInvalidosTest() {
        assertEquals(400, cut.update(null, new Descuento()).getStatus());
        assertEquals(400, cut.update(UUID.randomUUID(), null).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void eliminarExitosoTest() {
        UUID id = UUID.randomUUID();
        Descuento encontrado = new Descuento(id);
        Mockito.when(mockCS.findById(id)).thenReturn(encontrado);

        assertEquals(204, cut.delete(id).getStatus());
        Mockito.verify(mockCS).delete(encontrado);
    }

    @Test
    public void eliminarNoEncontradoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findById(id)).thenReturn(null);

        assertEquals(404, cut.delete(id).getStatus());
        Mockito.verify(mockCS, Mockito.never()).delete(Mockito.any());
    }

    @Test
    public void eliminarParametrosInvalidosTest() {
        assertEquals(400, cut.delete(null).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }
}
