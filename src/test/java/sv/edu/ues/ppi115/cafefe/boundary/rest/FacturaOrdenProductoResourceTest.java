package sv.edu.ues.ppi115.cafefe.boundary.rest;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.ppi115.cafefe.control.FacturaOrdenProductoService;
import sv.edu.ues.ppi115.cafefe.control.FacturaService;
import sv.edu.ues.ppi115.cafefe.control.OrdenProductoService;
import sv.edu.ues.ppi115.cafefe.entity.Factura;
import sv.edu.ues.ppi115.cafefe.entity.FacturaOrdenProducto;
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FacturaOrdenProductoResourceTest {

    private UriInfo mockUriInfo;
    private FacturaOrdenProductoService mockCS;
    private FacturaService mockFS;
    private OrdenProductoService mockOS;
    private FacturaOrdenProductoResource cut;

    @BeforeEach
    public void setup() {
        mockUriInfo = Mockito.mock(UriInfo.class);
        UriBuilder mockUriBuilder = Mockito.mock(UriBuilder.class);
        mockCS = Mockito.mock(FacturaOrdenProductoService.class);
        mockFS = Mockito.mock(FacturaService.class);
        mockOS = Mockito.mock(OrdenProductoService.class);

        Mockito.when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.path(Mockito.anyString())).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.build()).thenReturn(URI.create("http://localhost:8080/v1/factura/1/ordenproducto/1"));

        cut = new FacturaOrdenProductoResource();
        cut.facturaOrdenProductoService = mockCS;
        cut.facturaService = mockFS;
        cut.ordenProductoService = mockOS;
    }

    @Test
    public void crearExitosoTest() {
        UUID facturaId = UUID.randomUUID();
        Factura factura = new Factura(facturaId);
        FacturaOrdenProducto nuevo = new FacturaOrdenProducto();
        UUID ordenProductoId = UUID.randomUUID();
        nuevo.setIdOrdenProducto(new OrdenProducto(ordenProductoId));
        Mockito.when(mockFS.findById(facturaId)).thenReturn(factura);
        Mockito.when(mockOS.findById(ordenProductoId)).thenReturn(new OrdenProducto(ordenProductoId));

        assertEquals(201, cut.crear(facturaId, nuevo, mockUriInfo).getStatus());
        assertNotNull(nuevo.getIdFacturaOrdenProducto());
        assertEquals(factura, nuevo.getIdFactura());
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearConIdTest() {
        UUID facturaId = UUID.randomUUID();
        FacturaOrdenProducto nuevo = new FacturaOrdenProducto(UUID.randomUUID());

        assertEquals(400, cut.crear(facturaId, nuevo, mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockCS, mockFS, mockOS);
    }

    @Test
    public void crearParametrosInvalidosTest() {
        UUID facturaId = UUID.randomUUID();
        FacturaOrdenProducto sinOrden = new FacturaOrdenProducto();
        FacturaOrdenProducto sinId = new FacturaOrdenProducto();
        sinId.setIdOrdenProducto(new OrdenProducto());
        assertEquals(400, cut.crear(null, sinOrden, mockUriInfo).getStatus());
        assertEquals(400, cut.crear(facturaId, null, mockUriInfo).getStatus());
        assertEquals(400, cut.crear(facturaId, sinOrden, mockUriInfo).getStatus());
        assertEquals(400, cut.crear(facturaId, sinId, mockUriInfo).getStatus());
    }

    @Test
    public void crearFacturaNoEncontradaTest() {
        UUID facturaId = UUID.randomUUID();
        FacturaOrdenProducto nuevo = new FacturaOrdenProducto();
        nuevo.setIdOrdenProducto(new OrdenProducto(UUID.randomUUID()));
        Mockito.when(mockFS.findById(facturaId)).thenReturn(null);
        assertEquals(404, cut.crear(facturaId, nuevo, mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockOS, mockCS);
    }

    @Test
    public void crearOrdenProductoNoEncontradoTest() {
        UUID facturaId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        FacturaOrdenProducto nuevo = new FacturaOrdenProducto();
        nuevo.setIdOrdenProducto(new OrdenProducto(ordenId));
        Mockito.when(mockFS.findById(facturaId)).thenReturn(new Factura(facturaId));
        Mockito.when(mockOS.findById(ordenId)).thenReturn(null);
        assertEquals(404, cut.crear(facturaId, nuevo, mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearConExcepcionTest() {
        UUID facturaId = UUID.randomUUID();
        FacturaOrdenProducto nuevo = new FacturaOrdenProducto();
        nuevo.setIdOrdenProducto(new OrdenProducto(UUID.randomUUID()));
        Mockito.when(mockFS.findById(facturaId))
                .thenThrow(new RuntimeException("Error en base de datos"));
        assertEquals(500, cut.crear(facturaId, nuevo, mockUriInfo).getStatus());
    }

    @Test
    public void buscarPorRangoExitosoTest() {
        UUID facturaId = UUID.randomUUID();
        Mockito.when(mockCS.findByFactura(facturaId, 0, 50))
                .thenReturn(List.of(new FacturaOrdenProducto()));
        Mockito.when(mockCS.contarPorFactura(facturaId)).thenReturn(1L);

        assertEquals(200, cut.buscarPorRango(facturaId, 0, 50).getStatus());
    }

    @Test
    public void buscarPorRangoErroresTest() {
        UUID facturaId = UUID.randomUUID();
        assertEquals(400, cut.buscarPorRango(facturaId, -1, 50).getStatus());
        assertEquals(400, cut.buscarPorRango(facturaId, 0, 0).getStatus());
        assertEquals(400, cut.buscarPorRango(facturaId, 0, 51).getStatus());
        Mockito.when(mockCS.findByFactura(facturaId, 0, 50))
                .thenThrow(new RuntimeException("Error en base de datos"));
        assertEquals(500, cut.buscarPorRango(facturaId, 0, 50).getStatus());
    }

    @Test
    public void buscarPorRangoConteoConExcepcionTest() {
        UUID facturaId = UUID.randomUUID();
        Mockito.when(mockCS.findByFactura(facturaId, 0, 50)).thenReturn(List.of(new FacturaOrdenProducto()));
        Mockito.when(mockCS.contarPorFactura(facturaId))
                .thenThrow(new RuntimeException("Error en base de datos"));
        assertEquals(500, cut.buscarPorRango(facturaId, 0, 50).getStatus());
    }

    @Test
    public void buscarPorIdPerteneceAFacturaTest() {
        UUID facturaId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        FacturaOrdenProducto encontrado = new FacturaOrdenProducto(id);
        encontrado.setIdFactura(new Factura(facturaId));
        Mockito.when(mockCS.findByIdYFactura(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(encontrado);

        assertEquals(200, cut.buscarPorId(facturaId, id).getStatus());
    }

    @Test
    public void buscarPorIdErroresTest() {
        UUID facturaId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        assertEquals(400, cut.buscarPorId(null, id).getStatus());
        assertEquals(400, cut.buscarPorId(facturaId, null).getStatus());
        Mockito.when(mockCS.findByIdYFactura(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.buscarPorId(facturaId, id).getStatus());
        Mockito.when(mockCS.findByIdYFactura(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new FacturaOrdenProducto(id));
        assertEquals(404, cut.buscarPorId(facturaId, id).getStatus());
        Mockito.when(mockCS.findByIdYFactura(Mockito.eq(id), Mockito.any(UUID.class))).thenThrow(new RuntimeException("Error en base de datos"));
        assertEquals(500, cut.buscarPorId(facturaId, id).getStatus());
    }

    @Test
    public void actualizarExitosoTest() {
        UUID facturaId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        Factura factura = new Factura(facturaId);
        FacturaOrdenProducto existente = new FacturaOrdenProducto(id);
        existente.setIdFactura(factura);
        FacturaOrdenProducto nuevo = new FacturaOrdenProducto();
        nuevo.setIdOrdenProducto(new OrdenProducto(ordenId));
        Mockito.when(mockCS.findByIdYFactura(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        Mockito.when(mockOS.findById(ordenId)).thenReturn(new OrdenProducto(ordenId));
        assertEquals(200, cut.actualizar(facturaId, id, nuevo).getStatus());
        assertEquals(id, nuevo.getIdFacturaOrdenProducto());
        assertEquals(factura, nuevo.getIdFactura());
        Mockito.verify(mockCS).update(nuevo);
    }

    @Test
    public void actualizarErroresTest() {
        UUID facturaId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        assertEquals(400, cut.actualizar(null, id, new FacturaOrdenProducto()).getStatus());
        assertEquals(400, cut.actualizar(facturaId, null, new FacturaOrdenProducto()).getStatus());
        assertEquals(400, cut.actualizar(facturaId, id, null).getStatus());
        Mockito.when(mockCS.findByIdYFactura(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.actualizar(facturaId, id, new FacturaOrdenProducto()).getStatus());
        FacturaOrdenProducto otro = new FacturaOrdenProducto(id);
        otro.setIdFactura(new Factura(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYFactura(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(otro);
        assertEquals(404, cut.actualizar(facturaId, id, new FacturaOrdenProducto()).getStatus());
        FacturaOrdenProducto sinPadre = new FacturaOrdenProducto(id);
        Mockito.when(mockCS.findByIdYFactura(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(sinPadre);
        assertEquals(404, cut.actualizar(facturaId, id, new FacturaOrdenProducto()).getStatus());
    }

    @Test
    public void actualizarDependenciaErroresTest() {
        UUID facturaId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        FacturaOrdenProducto existente = new FacturaOrdenProducto(id);
        existente.setIdFactura(new Factura(facturaId));
        Mockito.when(mockCS.findByIdYFactura(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        assertEquals(400, cut.actualizar(facturaId, id, new FacturaOrdenProducto()).getStatus());
        FacturaOrdenProducto nuevo = new FacturaOrdenProducto();
        nuevo.setIdOrdenProducto(new OrdenProducto(UUID.randomUUID()));
        Mockito.when(mockOS.findById(Mockito.any())).thenReturn(null);
        assertEquals(404, cut.actualizar(facturaId, id, nuevo).getStatus());
        Mockito.when(mockOS.findById(Mockito.any())).thenThrow(new RuntimeException("Error en base de datos"));
        assertEquals(500, cut.actualizar(facturaId, id, nuevo).getStatus());
    }

    @Test
    public void eliminarCasosTest() {
        UUID facturaId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        assertEquals(400, cut.eliminar(null, id).getStatus());
        assertEquals(400, cut.eliminar(facturaId, null).getStatus());
        Mockito.when(mockCS.findByIdYFactura(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.eliminar(facturaId, id).getStatus());
        FacturaOrdenProducto existente = new FacturaOrdenProducto(id);
        existente.setIdFactura(new Factura(facturaId));
        Mockito.when(mockCS.findByIdYFactura(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        assertEquals(204, cut.eliminar(facturaId, id).getStatus());
        Mockito.doThrow(new RuntimeException("Error en base de datos")).when(mockCS).delete(existente);
        assertEquals(500, cut.eliminar(facturaId, id).getStatus());
    }

    @Test
    public void parametrosInvalidosTest() {
        assertEquals(400, cut.buscarPorRango(null, 0, 50).getStatus());
        assertEquals(400, cut.buscarPorId(UUID.randomUUID(), null).getStatus());
    }
}
