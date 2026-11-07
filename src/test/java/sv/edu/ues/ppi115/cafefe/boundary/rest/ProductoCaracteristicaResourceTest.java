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
import sv.edu.ues.ppi115.cafefe.control.ProductoCaracteristicaService;
import sv.edu.ues.ppi115.cafefe.control.ProductoService;
import sv.edu.ues.ppi115.cafefe.control.CaracteristicaService;
import sv.edu.ues.ppi115.cafefe.entity.Producto;
import sv.edu.ues.ppi115.cafefe.entity.ProductoCaracteristica;
import sv.edu.ues.ppi115.cafefe.entity.Caracteristica;

public class ProductoCaracteristicaResourceTest {

    private UriInfo mockUriInfo;
    private ProductoCaracteristicaService mockCS;
    private ProductoService mockPS;
    private CaracteristicaService mockCS2;
    private ProductoCaracteristicaResource cut;

    @BeforeEach
    public void setup() {
        mockUriInfo = Mockito.mock(UriInfo.class);
        UriBuilder mockUriBuilder = Mockito.mock(UriBuilder.class);
        mockCS = Mockito.mock(ProductoCaracteristicaService.class);
        mockPS = Mockito.mock(ProductoService.class);
        mockCS2 = Mockito.mock(CaracteristicaService.class);

        Mockito.when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.path(Mockito.anyString())).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.build())
                .thenReturn(URI.create("http://localhost:8080/v1/producto/1/caracteristica/1"));

        cut = new ProductoCaracteristicaResource();
        cut.productoCaracteristicaService = mockCS;
        cut.productoService = mockPS;
        cut.caracteristicaService = mockCS2;
    }

    @Test
    public void crearExitosoTest() {
        UUID productoId = UUID.randomUUID();
        Producto producto = new Producto(productoId);
        ProductoCaracteristica nuevo = new ProductoCaracteristica();
        UUID caracteristicaId = UUID.randomUUID();
        nuevo.setIdCaracteristica(new Caracteristica(caracteristicaId));
        Mockito.when(mockPS.findById(productoId)).thenReturn(producto);
        Mockito.when(mockCS2.findById(caracteristicaId)).thenReturn(new Caracteristica(caracteristicaId));

        Response resultado = cut.crear(productoId, nuevo, mockUriInfo);

        assertEquals(201, resultado.getStatus());
        assertNotNull(nuevo.getIdProductoCaracteristica());
        assertEquals(producto, nuevo.getIdProducto());
        Mockito.verify(mockPS).findById(productoId);
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearConIdTest() {
        UUID productoId = UUID.randomUUID();
        UUID productoCaracteristicaId = UUID.randomUUID();
        ProductoCaracteristica nuevo = new ProductoCaracteristica(productoCaracteristicaId);
        UUID caracteristicaId = UUID.randomUUID();
        nuevo.setIdCaracteristica(new Caracteristica(caracteristicaId));
        Mockito.when(mockPS.findById(productoId)).thenReturn(new Producto(productoId));
        Mockito.when(mockCS2.findById(caracteristicaId)).thenReturn(new Caracteristica(caracteristicaId));

        Response resultado = cut.crear(productoId, nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS, mockCS2);
    }

    @Test
    public void crearNullTest() {
        assertEquals(400, cut.crear(UUID.randomUUID(), null, mockUriInfo).getStatus());
        assertEquals(400, cut.crear(null, new ProductoCaracteristica(), mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockCS, mockPS);
    }

    @Test
    public void crearProductoNoEncontradoTest() {
        UUID productoId = UUID.randomUUID();
        ProductoCaracteristica nuevo = new ProductoCaracteristica();
        UUID caracteristicaId = UUID.randomUUID();
        nuevo.setIdCaracteristica(new Caracteristica(caracteristicaId));
        Mockito.when(mockPS.findById(productoId)).thenReturn(null);

        Response resultado = cut.crear(productoId, nuevo, mockUriInfo);

        assertEquals(404, resultado.getStatus());
        Mockito.verify(mockPS).findById(productoId);
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearConExcepcionTest() {
        UUID productoId = UUID.randomUUID();
        ProductoCaracteristica nuevo = new ProductoCaracteristica();
        UUID caracteristicaId = UUID.randomUUID();
        nuevo.setIdCaracteristica(new Caracteristica(caracteristicaId));
        Mockito.when(mockPS.findById(productoId))
                .thenThrow(new RuntimeException("Error en base de datos"));

        assertEquals(500, cut.crear(productoId, nuevo, mockUriInfo).getStatus());
        Mockito.verify(mockPS).findById(productoId);
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoExitosoTest() {
        UUID productoId = UUID.randomUUID();
        Mockito.when(mockCS.findByProducto(productoId, 0, 50)).thenReturn(List.of(new ProductoCaracteristica()));
        Mockito.when(mockCS.contarPorProducto(productoId)).thenReturn(1L);

        Response resultado = cut.buscarPorRango(productoId, 0, 50);

        assertEquals(200, resultado.getStatus());
        assertNotNull(resultado.getEntity());
        assertEquals("1", resultado.getHeaderString(ResponseHeaders.TOTAL_REGISTROS.toString()));
        Mockito.verify(mockCS).findByProducto(productoId, 0, 50);
        Mockito.verify(mockCS).contarPorProducto(productoId);
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
        UUID productoId = UUID.randomUUID();
        Mockito.when(mockCS.findByProducto(productoId, 0, 50))
                .thenThrow(new RuntimeException("Error en base de datos"));

        assertEquals(500, cut.buscarPorRango(productoId, 0, 50).getStatus());
        Mockito.verify(mockCS).findByProducto(productoId, 0, 50);
    }

    @Test
    public void buscarPorIdExitosoTest() {
        UUID productoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        ProductoCaracteristica encontrado = new ProductoCaracteristica(id);
        encontrado.setIdProducto(new Producto(productoId));
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(encontrado);

        Response resultado = cut.buscarPorId(productoId, id);

        assertEquals(200, resultado.getStatus());
        assertNotNull(resultado.getEntity());
        Mockito.verify(mockCS).findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class));
    }

    @Test
    public void buscarPorIdNoEncontradoTest() {
        UUID productoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.buscarPorId(productoId, id).getStatus());

        ProductoCaracteristica deOtroProducto = new ProductoCaracteristica(id);
        deOtroProducto.setIdProducto(new Producto(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(deOtroProducto);
        assertEquals(404, cut.buscarPorId(productoId, id).getStatus());
    }

    @Test
    public void buscarPorIdSinProductoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new ProductoCaracteristica(id));
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
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class)))
                .thenThrow(new RuntimeException("Error en base de datos"));
        assertEquals(500, cut.buscarPorId(UUID.randomUUID(), id).getStatus());
        Mockito.verify(mockCS).findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class));
    }

    @Test
    public void actualizarExitosoTest() {
        UUID productoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Producto producto = new Producto(productoId);
        ProductoCaracteristica existente = new ProductoCaracteristica(id);
        existente.setIdProducto(producto);
        ProductoCaracteristica nuevo = new ProductoCaracteristica();
        UUID caracteristicaId = UUID.randomUUID();
        nuevo.setIdCaracteristica(new Caracteristica(caracteristicaId));
        Mockito.when(mockCS2.findById(caracteristicaId)).thenReturn(new Caracteristica(caracteristicaId));
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);

        Response resultado = cut.actualizar(productoId, id, nuevo);

        assertEquals(200, resultado.getStatus());
        assertEquals(id, nuevo.getIdProductoCaracteristica());
        assertEquals(producto, nuevo.getIdProducto());
        assertEquals(caracteristicaId, nuevo.getIdCaracteristica().getIdCaracteristica());
        Mockito.verify(mockCS).update(nuevo);
    }

    @Test
    public void actualizarNoEncontradoTest() {
        UUID productoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.actualizar(productoId, id, new ProductoCaracteristica()).getStatus());

        ProductoCaracteristica deOtroProducto = new ProductoCaracteristica(id);
        deOtroProducto.setIdProducto(new Producto(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(deOtroProducto);
        assertEquals(404, cut.actualizar(productoId, id, new ProductoCaracteristica()).getStatus());
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarSinProductoExistenteTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new ProductoCaracteristica(id));
        assertEquals(404, cut.actualizar(UUID.randomUUID(), id, new ProductoCaracteristica()).getStatus());
    }

    @Test
    public void actualizarNullTest() {
        assertEquals(400, cut.actualizar(null, UUID.randomUUID(), new ProductoCaracteristica()).getStatus());
        assertEquals(400, cut.actualizar(UUID.randomUUID(), null, new ProductoCaracteristica()).getStatus());
        assertEquals(400, cut.actualizar(UUID.randomUUID(), UUID.randomUUID(), null).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void actualizarConExcepcionTest() {
        UUID productoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        ProductoCaracteristica existente = new ProductoCaracteristica(id);
        existente.setIdProducto(new Producto(productoId));
        ProductoCaracteristica nuevo = new ProductoCaracteristica();
        UUID caracteristicaId = UUID.randomUUID();
        nuevo.setIdCaracteristica(new Caracteristica(caracteristicaId));
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        Mockito.when(mockCS2.findById(caracteristicaId)).thenReturn(new Caracteristica(caracteristicaId));
        Mockito.doThrow(new RuntimeException("Error en base de datos")).when(mockCS).update(nuevo);

        assertEquals(500, cut.actualizar(productoId, id, nuevo).getStatus());
        Mockito.verify(mockCS).update(nuevo);
    }

    @Test
    public void eliminarExitosoTest() {
        UUID productoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        ProductoCaracteristica existente = new ProductoCaracteristica(id);
        existente.setIdProducto(new Producto(productoId));
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);

        assertEquals(204, cut.eliminar(productoId, id).getStatus());
        Mockito.verify(mockCS).delete(existente);
    }

    @Test
    public void eliminarNoEncontradoTest() {
        UUID productoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.eliminar(productoId, id).getStatus());

        ProductoCaracteristica deOtroProducto = new ProductoCaracteristica(id);
        deOtroProducto.setIdProducto(new Producto(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(deOtroProducto);
        assertEquals(404, cut.eliminar(productoId, id).getStatus());
        Mockito.verify(mockCS, Mockito.never()).delete(Mockito.any());
    }

    @Test
    public void eliminarSinProductoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new ProductoCaracteristica(id));
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
        UUID productoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        ProductoCaracteristica existente = new ProductoCaracteristica(id);
        existente.setIdProducto(new Producto(productoId));
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        Mockito.doThrow(new RuntimeException("Error en base de datos")).when(mockCS).delete(existente);

        assertEquals(500, cut.eliminar(productoId, id).getStatus());
        Mockito.verify(mockCS).delete(existente);
    }
}
