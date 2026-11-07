package sv.edu.ues.ppi115.cafefe.boundary.rest;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.ppi115.cafefe.control.PagoService;
import sv.edu.ues.ppi115.cafefe.control.FacturaService;
import sv.edu.ues.ppi115.cafefe.entity.Factura;
import sv.edu.ues.ppi115.cafefe.entity.Pago;

public class PagoResourceTest {

    private UriInfo mockUriInfo;
    private PagoService mockCS;
    private FacturaService mockFS;
    private PagoResource cut;

    @BeforeEach
    public void setup() {
        mockUriInfo = Mockito.mock(UriInfo.class);
        UriBuilder mockUriBuilder = Mockito.mock(UriBuilder.class);
        mockCS = Mockito.mock(PagoService.class);
        mockFS = Mockito.mock(FacturaService.class);

        Mockito.when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.path(Mockito.anyString())).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.build())
                .thenReturn(URI.create("http://localhost:8080/v1/pago/1"));

        cut = new PagoResource();
        cut.pagoService = mockCS;
        cut.facturaService = mockFS;
    }

    @Test
    public void crearExitosoTest() {
        Pago nuevo = new Pago();
        UUID facturaId = UUID.randomUUID();
        Factura factura = new Factura(facturaId);
        nuevo.setIdFactura(new Factura(facturaId));
        Mockito.when(mockFS.findById(facturaId)).thenReturn(factura);
        Mockito.doNothing().when(mockCS).create(nuevo);

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(201, resultado.getStatus());
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearConExcepcionTest() {
        Pago nuevo = new Pago();
        UUID facturaId = UUID.randomUUID();
        nuevo.setIdFactura(new Factura(facturaId));
        Mockito.when(mockFS.findById(facturaId)).thenReturn(new Factura(facturaId));
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).create(nuevo);

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearNullTest() {
        Response resultado = cut.create(null, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearConIdTest() {
        Pago nuevo = new Pago(UUID.randomUUID());

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearSinFacturaTest() {
        Response resultado = cut.create(new Pago(), mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
        Mockito.verifyNoInteractions(mockFS);
    }

    @Test
    public void crearFacturaSinIdTest() {
        Pago nuevo = new Pago();
        nuevo.setIdFactura(new Factura());

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
        Mockito.verifyNoInteractions(mockFS);
    }

    @Test
    public void crearFacturaNoEncontradaTest() {
        Pago nuevo = new Pago();
        UUID facturaId = UUID.randomUUID();
        nuevo.setIdFactura(new Factura(facturaId));
        Mockito.when(mockFS.findById(facturaId)).thenReturn(null);

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verify(mockFS).findById(facturaId);
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void eliminarExitosoTest() {
        UUID id = UUID.randomUUID();
        Pago encontrado = new Pago(id);
        Mockito.when(mockCS.findById(id)).thenReturn(encontrado);
        Mockito.doNothing().when(mockCS).delete(encontrado);

        Response resultado = cut.delete(id);

        assertEquals(204, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS).delete(encontrado);
    }

    @Test
    public void eliminarNoEncontradoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findById(id)).thenReturn(null);

        Response resultado = cut.delete(id);

        assertEquals(404, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS, Mockito.never()).delete(Mockito.any());
    }

    @Test
    public void eliminarConExcepcionTest() {
        UUID id = UUID.randomUUID();
        Pago encontrado = new Pago(id);
        Mockito.when(mockCS.findById(id)).thenReturn(encontrado);
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).delete(encontrado);

        Response resultado = cut.delete(id);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS).delete(encontrado);
    }

    @Test
    public void eliminarIdNullTest() {
        Response resultado = cut.delete(null);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoExitosoTest() {
        Mockito.when(mockCS.findRange(0, 50))
                .thenReturn(List.of(new Pago(UUID.randomUUID()), new Pago(UUID.randomUUID())));
        Mockito.when(mockCS.contar()).thenReturn(2L);

        Response resultado = cut.findRange(0, 50);

        assertEquals(200, resultado.getStatus());
        assertNotNull(resultado.getEntity());
        Mockito.verify(mockCS).findRange(0, 50);
        Mockito.verify(mockCS).contar();
    }

    @Test
    public void buscarPorRangoParametrosInvalidosTest() {
        Response resultado = cut.findRange(-1, 0);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoMaxCeroTest() {
        Response resultado = cut.findRange(0, 0);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoMaxMayorACincuentaTest() {
        Response resultado = cut.findRange(0, 51);

        assertEquals(400, resultado.getStatus());
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
        Pago encontrado = new Pago(id);
        Mockito.when(mockCS.findById(id)).thenReturn(encontrado);

        Response resultado = cut.findById(id);

        assertNotNull(resultado.getEntity());
        assertEquals(200, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
    }

    @Test
    public void buscarPorIdNoEncontradoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findById(id)).thenReturn(null);

        Response resultado = cut.findById(id);

        assertEquals(404, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
    }

    @Test
    public void buscarPorIdConExcepcionTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findById(id))
                .thenThrow(new RuntimeException("Error en base de datos"));

        Response resultado = cut.findById(id);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
    }

    @Test
    public void buscarPorIdNullTest() {
        Response resultado = cut.findById(null);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void actualizarExitosoTest() {
        UUID id = UUID.randomUUID();
        Pago pago = new Pago(id);
        UUID facturaId = UUID.randomUUID();
        pago.setIdFactura(new Factura(facturaId));
        Mockito.when(mockCS.findById(id)).thenReturn(pago);
        Mockito.when(mockFS.findById(facturaId)).thenReturn(new Factura(facturaId));
        Mockito.when(mockCS.update(pago)).thenReturn(pago);

        Response resultado = cut.update(id, pago);

        assertEquals(200, resultado.getStatus());
        assertNotNull(resultado.getEntity());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS).update(pago);
    }

    @Test
    public void actualizarNoEncontradoTest() {
        UUID id = UUID.randomUUID();
        Pago pago = new Pago(id);
        Mockito.when(mockCS.findById(id)).thenReturn(null);

        Response resultado = cut.update(id, pago);

        assertEquals(404, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarSinFacturaTest() {
        UUID id = UUID.randomUUID();
        Pago pago = new Pago();
        Mockito.when(mockCS.findById(id)).thenReturn(new Pago(id));

        Response resultado = cut.update(id, pago);

        assertEquals(400, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verifyNoInteractions(mockFS);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarFacturaSinIdTest() {
        UUID id = UUID.randomUUID();
        Pago pago = new Pago();
        pago.setIdFactura(new Factura());
        Mockito.when(mockCS.findById(id)).thenReturn(new Pago(id));

        Response resultado = cut.update(id, pago);

        assertEquals(400, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verifyNoInteractions(mockFS);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarFacturaNoEncontradaTest() {
        UUID id = UUID.randomUUID();
        UUID facturaId = UUID.randomUUID();
        Pago pago = new Pago();
        pago.setIdFactura(new Factura(facturaId));
        Mockito.when(mockCS.findById(id)).thenReturn(new Pago(id));
        Mockito.when(mockFS.findById(facturaId)).thenReturn(null);

        Response resultado = cut.update(id, pago);

        assertEquals(400, resultado.getStatus());
        Mockito.verify(mockFS).findById(facturaId);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarConExcepcionTest() {
        UUID id = UUID.randomUUID();
        Pago pago = new Pago(id);
        UUID facturaId = UUID.randomUUID();
        pago.setIdFactura(new Factura(facturaId));
        Mockito.when(mockCS.findById(id)).thenReturn(pago);
        Mockito.when(mockFS.findById(facturaId)).thenReturn(new Factura(facturaId));
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).update(pago);

        Response resultado = cut.update(id, pago);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS).update(pago);
    }

    @Test
    public void actualizarNullTest() {
        Response resultado = cut.update(null, null);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void actualizarSinIdTest() {
        Pago pago = new Pago();

        Response resultado = cut.update(pago.getIdPago(), pago);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }
}
