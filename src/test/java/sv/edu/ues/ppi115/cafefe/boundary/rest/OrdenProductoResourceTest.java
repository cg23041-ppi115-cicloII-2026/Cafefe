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
import sv.edu.ues.ppi115.cafefe.control.OrdenProductoService;
import sv.edu.ues.ppi115.cafefe.control.OrdenService;
import sv.edu.ues.ppi115.cafefe.control.ProductoService;
import sv.edu.ues.ppi115.cafefe.entity.Orden;
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;
import sv.edu.ues.ppi115.cafefe.entity.Producto;

public class OrdenProductoResourceTest {

    private UriInfo mockUriInfo;
    private OrdenProductoService mockCS;
    private OrdenService mockOS;
    private ProductoService mockPS;
    private OrdenProductoResource cut;

    @BeforeEach
    public void setup() {
        mockUriInfo = Mockito.mock(UriInfo.class);
        UriBuilder mockUriBuilder = Mockito.mock(UriBuilder.class);
        mockCS = Mockito.mock(OrdenProductoService.class);
        mockOS = Mockito.mock(OrdenService.class);
        mockPS = Mockito.mock(ProductoService.class);

        Mockito.when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.path(Mockito.anyString())).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.build())
                .thenReturn(URI.create("http://localhost:8080/v1/orden/1/producto/1"));

        cut = new OrdenProductoResource();
        cut.ordenProductoService = mockCS;
        cut.ordenService = mockOS;
        cut.productoService = mockPS;
    }

    @Test
    public void crearExitosoTest() {
        UUID ordenId = UUID.randomUUID();
        Orden orden = new Orden(ordenId);
        OrdenProducto nuevo = new OrdenProducto();
        UUID productoId = UUID.randomUUID();
        nuevo.setIdProducto(new Producto(productoId));
        Mockito.when(mockOS.findById(ordenId)).thenReturn(orden);
        Mockito.when(mockPS.findById(productoId)).thenReturn(new Producto(productoId));

        Response resultado = cut.crear(ordenId, nuevo, mockUriInfo);

        assertEquals(201, resultado.getStatus());
        assertNotNull(nuevo.getIdOrdenProducto());
        assertEquals(orden, nuevo.getIdOrden());
        Mockito.verify(mockOS).findById(ordenId);
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearConIdTest() {
        UUID ordenId = UUID.randomUUID();
        UUID ordenProductoId = UUID.randomUUID();
        OrdenProducto nuevo = new OrdenProducto(ordenProductoId);
        UUID productoId = UUID.randomUUID();
        nuevo.setIdProducto(new Producto(productoId));
        Mockito.when(mockOS.findById(ordenId)).thenReturn(new Orden(ordenId));
        Mockito.when(mockPS.findById(productoId)).thenReturn(new Producto(productoId));

        Response resultado = cut.crear(ordenId, nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS, mockPS);
    }

    @Test
    public void crearNullTest() {
        assertEquals(400, cut.crear(UUID.randomUUID(), null, mockUriInfo).getStatus());
        assertEquals(400, cut.crear(null, new OrdenProducto(), mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockCS, mockOS);
    }

    @Test
    public void crearOrdenNoEncontradaTest() {
        UUID ordenId = UUID.randomUUID();
        OrdenProducto nuevo = new OrdenProducto();
        UUID productoId = UUID.randomUUID();
        nuevo.setIdProducto(new Producto(productoId));
        Mockito.when(mockOS.findById(ordenId)).thenReturn(null);

        Response resultado = cut.crear(ordenId, nuevo, mockUriInfo);

        assertEquals(404, resultado.getStatus());
        Mockito.verify(mockOS).findById(ordenId);
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearConExcepcionTest() {
        UUID ordenId = UUID.randomUUID();
        OrdenProducto nuevo = new OrdenProducto();
        UUID productoId = UUID.randomUUID();
        nuevo.setIdProducto(new Producto(productoId));
        Mockito.when(mockOS.findById(ordenId))
                .thenThrow(new RuntimeException("Error en base de datos"));

        assertEquals(500, cut.crear(ordenId, nuevo, mockUriInfo).getStatus());
        Mockito.verify(mockOS).findById(ordenId);
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoExitosoTest() {
        UUID ordenId = UUID.randomUUID();
        Mockito.when(mockCS.findByOrden(ordenId, 0, 50)).thenReturn(List.of(new OrdenProducto()));
        Mockito.when(mockCS.contarPorOrden(ordenId)).thenReturn(1L);

        Response resultado = cut.buscarPorRango(ordenId, 0, 50);

        assertEquals(200, resultado.getStatus());
        assertNotNull(resultado.getEntity());
        assertEquals("1", resultado.getHeaderString(ResponseHeaders.TOTAL_REGISTROS.toString()));
        Mockito.verify(mockCS).findByOrden(ordenId, 0, 50);
        Mockito.verify(mockCS).contarPorOrden(ordenId);
    }

    @Test
    public void buscarPorRangoParametrosInvalidosTest() {
        assertEquals(400, cut.buscarPorRango(null, 0, 50).getStatus());
        assertEquals(400, cut.buscarPorRango(UUID.randomUUID(), -1, 50).getStatus());
        assertEquals(400, cut.buscarPorRango(UUID.randomUUID(), 0, 0).getStatus());
        assertEquals(400, cut.buscarPorRango(UUID.randomUUID(), 0, 51).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoConExcepcionTest() {
        UUID ordenId = UUID.randomUUID();
        Mockito.when(mockCS.findByOrden(ordenId, 0, 50))
                .thenThrow(new RuntimeException("Error en base de datos"));

        assertEquals(500, cut.buscarPorRango(ordenId, 0, 50).getStatus());
        Mockito.verify(mockCS).findByOrden(ordenId, 0, 50);
    }

    @Test
    public void buscarPorIdExitosoTest() {
        UUID ordenId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        OrdenProducto encontrado = new OrdenProducto(id);
        encontrado.setIdOrden(new Orden(ordenId));
        Mockito.when(mockCS.findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(encontrado);

        Response resultado = cut.buscarPorId(ordenId, id);

        assertEquals(200, resultado.getStatus());
        assertNotNull(resultado.getEntity());
        Mockito.verify(mockCS).findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class));
    }

    @Test
    public void buscarPorIdNoEncontradoTest() {
        UUID ordenId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.buscarPorId(ordenId, id).getStatus());

        OrdenProducto deOtraOrden = new OrdenProducto(id);
        deOtraOrden.setIdOrden(new Orden(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(deOtraOrden);
        assertEquals(404, cut.buscarPorId(ordenId, id).getStatus());
    }

    @Test
    public void buscarPorIdSinOrdenTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new OrdenProducto(id));
        assertEquals(404, cut.buscarPorId(UUID.randomUUID(), id).getStatus());
    }

    @Test
    public void buscarPorIdNullTest() {
        assertEquals(400, cut.buscarPorId(null, UUID.randomUUID()).getStatus());
        assertEquals(400, cut.buscarPorId(UUID.randomUUID(), null).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorIdConExcepcionTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class)))
                .thenThrow(new RuntimeException("Error en base de datos"));
        assertEquals(500, cut.buscarPorId(UUID.randomUUID(), id).getStatus());
        Mockito.verify(mockCS).findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class));
    }

    @Test
    public void actualizarExitosoTest() {
        UUID ordenId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Orden orden = new Orden(ordenId);
        OrdenProducto existente = new OrdenProducto(id);
        existente.setIdOrden(orden);
        OrdenProducto nuevo = new OrdenProducto();
        UUID productoId = UUID.randomUUID();
        nuevo.setIdProducto(new Producto(productoId));
        Mockito.when(mockPS.findById(productoId)).thenReturn(new Producto(productoId));
        Mockito.when(mockCS.findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);

        Response resultado = cut.actualizar(ordenId, id, nuevo);

        assertEquals(200, resultado.getStatus());
        assertEquals(id, nuevo.getIdOrdenProducto());
        assertEquals(orden, nuevo.getIdOrden());
        assertEquals(productoId, nuevo.getIdProducto().getIdProducto());
        Mockito.verify(mockCS).update(nuevo);
    }

    @Test
    public void actualizarNoEncontradoTest() {
        UUID ordenId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.actualizar(ordenId, id, new OrdenProducto()).getStatus());

        OrdenProducto deOtraOrden = new OrdenProducto(id);
        deOtraOrden.setIdOrden(new Orden(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(deOtraOrden);
        assertEquals(404, cut.actualizar(ordenId, id, new OrdenProducto()).getStatus());
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarSinOrdenExistenteTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new OrdenProducto(id));
        assertEquals(404, cut.actualizar(UUID.randomUUID(), id, new OrdenProducto()).getStatus());
    }

    @Test
    public void actualizarNullTest() {
        assertEquals(400, cut.actualizar(null, UUID.randomUUID(), new OrdenProducto()).getStatus());
        assertEquals(400, cut.actualizar(UUID.randomUUID(), null, new OrdenProducto()).getStatus());
        assertEquals(400, cut.actualizar(UUID.randomUUID(), UUID.randomUUID(), null).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void actualizarConExcepcionTest() {
        UUID ordenId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        OrdenProducto existente = new OrdenProducto(id);
        existente.setIdOrden(new Orden(ordenId));
        OrdenProducto nuevo = new OrdenProducto();
        UUID productoId = UUID.randomUUID();
        nuevo.setIdProducto(new Producto(productoId));
        Mockito.when(mockCS.findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        Mockito.when(mockPS.findById(productoId)).thenReturn(new Producto(productoId));
        Mockito.doThrow(new RuntimeException("Error en base de datos")).when(mockCS).update(nuevo);

        assertEquals(500, cut.actualizar(ordenId, id, nuevo).getStatus());
        Mockito.verify(mockCS).update(nuevo);
    }

    @Test
    public void eliminarExitosoTest() {
        UUID ordenId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        OrdenProducto existente = new OrdenProducto(id);
        existente.setIdOrden(new Orden(ordenId));
        Mockito.when(mockCS.findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);

        assertEquals(204, cut.eliminar(ordenId, id).getStatus());
        Mockito.verify(mockCS).delete(existente);
    }

    @Test
    public void eliminarNoEncontradoTest() {
        UUID ordenId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.eliminar(ordenId, id).getStatus());

        OrdenProducto deOtraOrden = new OrdenProducto(id);
        deOtraOrden.setIdOrden(new Orden(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(deOtraOrden);
        assertEquals(404, cut.eliminar(ordenId, id).getStatus());
        Mockito.verify(mockCS, Mockito.never()).delete(Mockito.any());
    }

    @Test
    public void eliminarSinOrdenTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new OrdenProducto(id));
        assertEquals(404, cut.eliminar(UUID.randomUUID(), id).getStatus());
    }

    @Test
    public void eliminarNullTest() {
        assertEquals(400, cut.eliminar(null, UUID.randomUUID()).getStatus());
        assertEquals(400, cut.eliminar(UUID.randomUUID(), null).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void eliminarConExcepcionTest() {
        UUID ordenId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        OrdenProducto existente = new OrdenProducto(id);
        existente.setIdOrden(new Orden(ordenId));
        Mockito.when(mockCS.findByIdYOrden(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        Mockito.doThrow(new RuntimeException("Error en base de datos")).when(mockCS).delete(existente);

        assertEquals(500, cut.eliminar(ordenId, id).getStatus());
        Mockito.verify(mockCS).delete(existente);
    }
}
