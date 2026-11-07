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
import sv.edu.ues.ppi115.cafefe.control.PagoDetalleService;
import sv.edu.ues.ppi115.cafefe.control.PagoService;
import sv.edu.ues.ppi115.cafefe.entity.Pago;
import sv.edu.ues.ppi115.cafefe.entity.PagoDetalle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PagoDetalleResourceTest {

    private UriInfo mockUriInfo;
    private PagoDetalleService mockCS;
    private PagoService mockPS;
    private PagoDetalleResource cut;

    @BeforeEach
    public void setup() {
        mockUriInfo = Mockito.mock(UriInfo.class);
        UriBuilder mockUriBuilder = Mockito.mock(UriBuilder.class);
        mockCS = Mockito.mock(PagoDetalleService.class);
        mockPS = Mockito.mock(PagoService.class);

        Mockito.when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.path(Mockito.anyString())).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.build()).thenReturn(URI.create("http://localhost:8080/v1/pago/1/detalle/1"));

        cut = new PagoDetalleResource();
        cut.pagoDetalleService = mockCS;
        cut.pagoService = mockPS;
    }

    @Test
    public void crearExitosoTest() {
        UUID pagoId = UUID.randomUUID();
        Pago pago = new Pago(pagoId);
        PagoDetalle nuevo = new PagoDetalle();
        Mockito.when(mockPS.findById(pagoId)).thenReturn(pago);

        Response resultado = cut.crear(pagoId, nuevo, mockUriInfo);

        assertEquals(201, resultado.getStatus());
        assertNotNull(nuevo.getIdPagoDetalle());
        assertEquals(pago, nuevo.getIdPago());
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearConIdTest() {
        UUID pagoId = UUID.randomUUID();
        PagoDetalle nuevo = new PagoDetalle(UUID.randomUUID());

        assertEquals(400, cut.crear(pagoId, nuevo, mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockCS, mockPS);
    }

    @Test
    public void crearParametrosInvalidosTest() {
        UUID pagoId = UUID.randomUUID();
        assertEquals(400, cut.crear(null, new PagoDetalle(), mockUriInfo).getStatus());
        assertEquals(400, cut.crear(pagoId, null, mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockCS, mockPS);
    }

    @Test
    public void crearPagoNoEncontradoTest() {
        UUID pagoId = UUID.randomUUID();
        Mockito.when(mockPS.findById(pagoId)).thenReturn(null);

        assertEquals(404, cut.crear(pagoId, new PagoDetalle(), mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearConExcepcionTest() {
        UUID pagoId = UUID.randomUUID();
        Mockito.when(mockPS.findById(pagoId))
                .thenThrow(new RuntimeException("Error en base de datos"));

        assertEquals(500, cut.crear(pagoId, new PagoDetalle(), mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoExitosoTest() {
        UUID pagoId = UUID.randomUUID();
        Mockito.when(mockCS.findByPago(pagoId, 0, 50)).thenReturn(List.of(new PagoDetalle()));
        Mockito.when(mockCS.contarPorPago(pagoId)).thenReturn(1L);

        assertEquals(200, cut.buscarPorRango(pagoId, 0, 50).getStatus());
    }

    @Test
    public void buscarPorRangoParametrosInvalidosTest() {
        UUID pagoId = UUID.randomUUID();
        assertEquals(400, cut.buscarPorRango(pagoId, -1, 50).getStatus());
        assertEquals(400, cut.buscarPorRango(pagoId, 0, 0).getStatus());
        assertEquals(400, cut.buscarPorRango(pagoId, 0, 51).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoConExcepcionTest() {
        UUID pagoId = UUID.randomUUID();
        Mockito.when(mockCS.findByPago(pagoId, 0, 50))
                .thenThrow(new RuntimeException("Error en base de datos"));

        assertEquals(500, cut.buscarPorRango(pagoId, 0, 50).getStatus());
    }

    @Test
    public void buscarPorRangoConteoConExcepcionTest() {
        UUID pagoId = UUID.randomUUID();
        Mockito.when(mockCS.findByPago(pagoId, 0, 50)).thenReturn(List.of(new PagoDetalle()));
        Mockito.when(mockCS.contarPorPago(pagoId))
                .thenThrow(new RuntimeException("Error en base de datos"));

        assertEquals(500, cut.buscarPorRango(pagoId, 0, 50).getStatus());
    }

    @Test
    public void buscarPorIdPerteneceAlPagoTest() {
        UUID pagoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        PagoDetalle encontrado = new PagoDetalle(id);
        encontrado.setIdPago(new Pago(pagoId));
        Mockito.when(mockCS.findByIdYPago(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(encontrado);

        assertEquals(200, cut.buscarPorId(pagoId, id).getStatus());
    }

    @Test
    public void buscarPorIdNoEncontradoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYPago(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.buscarPorId(UUID.randomUUID(), id).getStatus());
    }

    @Test
    public void buscarPorIdSinPagoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYPago(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new PagoDetalle(id));
        assertEquals(404, cut.buscarPorId(UUID.randomUUID(), id).getStatus());
    }

    @Test
    public void buscarPorIdDeOtroPagoTest() {
        UUID id = UUID.randomUUID();
        PagoDetalle encontrado = new PagoDetalle(id);
        encontrado.setIdPago(new Pago(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYPago(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(encontrado);
        assertEquals(404, cut.buscarPorId(UUID.randomUUID(), id).getStatus());
    }

    @Test
    public void buscarPorIdConExcepcionTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYPago(Mockito.eq(id), Mockito.any(UUID.class)))
                .thenThrow(new RuntimeException("Error en base de datos"));
        assertEquals(500, cut.buscarPorId(UUID.randomUUID(), id).getStatus());
    }

    @Test
    public void actualizarExitosoTest() {
        UUID pagoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Pago pago = new Pago(pagoId);
        PagoDetalle existente = new PagoDetalle(id);
        existente.setIdPago(pago);
        PagoDetalle nuevo = new PagoDetalle();
        Mockito.when(mockCS.findByIdYPago(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);

        assertEquals(200, cut.actualizar(pagoId, id, nuevo).getStatus());
        assertEquals(id, nuevo.getIdPagoDetalle());
        assertEquals(pago, nuevo.getIdPago());
        Mockito.verify(mockCS).update(nuevo);
    }

    @Test
    public void actualizarParametrosInvalidosTest() {
        UUID pagoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        assertEquals(400, cut.actualizar(null, id, new PagoDetalle()).getStatus());
        assertEquals(400, cut.actualizar(pagoId, null, new PagoDetalle()).getStatus());
        assertEquals(400, cut.actualizar(pagoId, id, null).getStatus());
    }

    @Test
    public void actualizarNoEncontradoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYPago(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.actualizar(UUID.randomUUID(), id, new PagoDetalle()).getStatus());
    }

    @Test
    public void actualizarPagoDiferenteTest() {
        UUID id = UUID.randomUUID();
        PagoDetalle existente = new PagoDetalle(id);
        existente.setIdPago(new Pago(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYPago(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        assertEquals(404, cut.actualizar(UUID.randomUUID(), id, new PagoDetalle()).getStatus());
    }

    @Test
    public void actualizarSinPagoExistenteTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYPago(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new PagoDetalle(id));
        assertEquals(404, cut.actualizar(UUID.randomUUID(), id, new PagoDetalle()).getStatus());
    }

    @Test
    public void actualizarConExcepcionTest() {
        UUID pagoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        PagoDetalle existente = new PagoDetalle(id);
        existente.setIdPago(new Pago(pagoId));
        PagoDetalle nuevo = new PagoDetalle();
        Mockito.when(mockCS.findByIdYPago(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        Mockito.doThrow(new RuntimeException("Error en base de datos")).when(mockCS).update(nuevo);
        assertEquals(500, cut.actualizar(pagoId, id, nuevo).getStatus());
    }

    @Test
    public void eliminarExitosoTest() {
        UUID pagoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        PagoDetalle existente = new PagoDetalle(id);
        existente.setIdPago(new Pago(pagoId));
        Mockito.when(mockCS.findByIdYPago(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        assertEquals(204, cut.eliminar(pagoId, id).getStatus());
        Mockito.verify(mockCS).delete(existente);
    }

    @Test
    public void eliminarNoEncontradoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYPago(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.eliminar(UUID.randomUUID(), id).getStatus());
    }

    @Test
    public void eliminarConExcepcionTest() {
        UUID pagoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        PagoDetalle existente = new PagoDetalle(id);
        existente.setIdPago(new Pago(pagoId));
        Mockito.when(mockCS.findByIdYPago(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        Mockito.doThrow(new RuntimeException("Error en base de datos")).when(mockCS).delete(existente);
        assertEquals(500, cut.eliminar(pagoId, id).getStatus());
    }

    @Test
    public void parametrosInvalidosTest() {
        assertEquals(400, cut.buscarPorRango(null, 0, 50).getStatus());
        assertEquals(400, cut.buscarPorId(UUID.randomUUID(), null).getStatus());
        assertEquals(400, cut.buscarPorId(null, UUID.randomUUID()).getStatus());
        assertEquals(400, cut.eliminar(null, UUID.randomUUID()).getStatus());
        assertEquals(400, cut.eliminar(UUID.randomUUID(), null).getStatus());
    }
}
