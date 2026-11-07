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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import sv.edu.ues.ppi115.cafefe.control.DescuentoProductoService;
import sv.edu.ues.ppi115.cafefe.control.DescuentoService;
import sv.edu.ues.ppi115.cafefe.control.ProductoService;
import sv.edu.ues.ppi115.cafefe.entity.Descuento;
import sv.edu.ues.ppi115.cafefe.entity.DescuentoProducto;
import sv.edu.ues.ppi115.cafefe.entity.Producto;

public class DescuentoProductoResourceTest {

    private UriInfo mockUriInfo;
    private DescuentoProductoService mockCS;
    private DescuentoService mockDS;
    private ProductoService mockPS;
    private DescuentoProductoResource cut;

    @BeforeEach
    public void setup() {
        mockUriInfo = Mockito.mock(UriInfo.class);
        UriBuilder mockUriBuilder = Mockito.mock(UriBuilder.class);
        mockCS = Mockito.mock(DescuentoProductoService.class);
        mockDS = Mockito.mock(DescuentoService.class);
        mockPS = Mockito.mock(ProductoService.class);

        Mockito.when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.path(Mockito.anyString())).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.build()).thenReturn(URI.create("http://localhost:8080/v1/descuento/1/producto/1"));

        cut = new DescuentoProductoResource();
        cut.descuentoProductoService = mockCS;
        cut.descuentoService = mockDS;
        cut.productoService = mockPS;
    }

    @Test
    public void crearExitosoTest() {
        UUID descuentoId = UUID.randomUUID();
        Descuento descuento = new Descuento(descuentoId);
        DescuentoProducto nuevo = new DescuentoProducto();
        UUID productoId = UUID.randomUUID();
        nuevo.setIdProducto(new Producto(productoId));
        Mockito.when(mockDS.findById(descuentoId)).thenReturn(descuento);
        Mockito.when(mockPS.findById(productoId)).thenReturn(new Producto(productoId));

        Response resultado = cut.crear(descuentoId, nuevo, mockUriInfo);

        assertEquals(201, resultado.getStatus());
        assertNotNull(nuevo.getIdDescuentoProducto());
        assertEquals(descuento, nuevo.getIdDescuento());
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearConIdTest() {
        UUID descuentoId = UUID.randomUUID();
        DescuentoProducto nuevo = new DescuentoProducto(UUID.randomUUID());

        assertEquals(400, cut.crear(descuentoId, nuevo, mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockCS, mockDS, mockPS);
    }

    @Test
    public void crearParametrosInvalidosTest() {
        UUID descuentoId = UUID.randomUUID();
        DescuentoProducto sinProducto = new DescuentoProducto();
        DescuentoProducto sinIdProducto = new DescuentoProducto();
        sinIdProducto.setIdProducto(new Producto());

        assertEquals(400, cut.crear(null, sinProducto, mockUriInfo).getStatus());
        assertEquals(400, cut.crear(descuentoId, null, mockUriInfo).getStatus());
        assertEquals(400, cut.crear(descuentoId, sinProducto, mockUriInfo).getStatus());
        assertEquals(400, cut.crear(descuentoId, sinIdProducto, mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockCS, mockDS, mockPS);
    }

    @Test
    public void crearDescuentoNoEncontradoTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        DescuentoProducto nuevo = new DescuentoProducto();
        nuevo.setIdProducto(new Producto(productoId));
        Mockito.when(mockDS.findById(descuentoId)).thenReturn(null);

        assertEquals(404, cut.crear(descuentoId, nuevo, mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockPS, mockCS);
    }

    @Test
    public void crearProductoNoEncontradoTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        DescuentoProducto nuevo = new DescuentoProducto();
        nuevo.setIdProducto(new Producto(productoId));
        Mockito.when(mockDS.findById(descuentoId)).thenReturn(new Descuento(descuentoId));
        Mockito.when(mockPS.findById(productoId)).thenReturn(null);

        assertEquals(404, cut.crear(descuentoId, nuevo, mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearConExcepcionTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        DescuentoProducto nuevo = new DescuentoProducto();
        nuevo.setIdProducto(new Producto(productoId));
        Mockito.when(mockDS.findById(descuentoId))
                .thenThrow(new RuntimeException("Error en base de datos"));

        assertEquals(500, cut.crear(descuentoId, nuevo, mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockCS, mockPS);
    }

    @Test
    public void buscarPorRangoExitosoTest() {
        UUID descuentoId = UUID.randomUUID();
        Mockito.when(mockCS.findByDescuento(descuentoId, 0, 50))
                .thenReturn(List.of(new DescuentoProducto()));
        Mockito.when(mockCS.contarPorDescuento(descuentoId)).thenReturn(1L);

        Response resultado = cut.buscarPorRango(descuentoId, 0, 50);

        assertEquals(200, resultado.getStatus());
        assertEquals("1", resultado.getHeaderString(ResponseHeaders.TOTAL_REGISTROS.toString()));
    }

    @Test
    public void buscarPorRangoParametrosInvalidosTest() {
        UUID descuentoId = UUID.randomUUID();
        assertEquals(400, cut.buscarPorRango(descuentoId, 0, 0).getStatus());
        assertEquals(400, cut.buscarPorRango(descuentoId, 0, 51).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoConExcepcionTest() {
        UUID descuentoId = UUID.randomUUID();
        Mockito.when(mockCS.findByDescuento(descuentoId, 0, 50))
                .thenThrow(new RuntimeException("Error en base de datos"));

        assertEquals(500, cut.buscarPorRango(descuentoId, 0, 50).getStatus());
        Mockito.verify(mockCS).findByDescuento(descuentoId, 0, 50);
    }

    @Test
    public void buscarPorRangoConteoConExcepcionTest() {
        UUID descuentoId = UUID.randomUUID();
        Mockito.when(mockCS.findByDescuento(descuentoId, 0, 50))
                .thenReturn(List.of(new DescuentoProducto()));
        Mockito.when(mockCS.contarPorDescuento(descuentoId))
                .thenThrow(new RuntimeException("Error en base de datos"));

        assertEquals(500, cut.buscarPorRango(descuentoId, 0, 50).getStatus());
        Mockito.verify(mockCS).contarPorDescuento(descuentoId);
    }

    @Test
    public void buscarPorIdPerteneceAlDescuentoTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        DescuentoProducto encontrado = new DescuentoProducto(id);
        encontrado.setIdDescuento(new Descuento(descuentoId));
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(encontrado);

        assertEquals(200, cut.buscarPorId(descuentoId, id).getStatus());
    }

    @Test
    public void buscarPorIdDeOtroDescuentoTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        DescuentoProducto encontrado = new DescuentoProducto(id);
        encontrado.setIdDescuento(new Descuento(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(encontrado);

        assertEquals(404, cut.buscarPorId(descuentoId, id).getStatus());
    }

    @Test
    public void buscarPorIdNoEncontradoTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);

        assertEquals(404, cut.buscarPorId(descuentoId, id).getStatus());
    }

    @Test
    public void buscarPorIdSinDescuentoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new DescuentoProducto(id));

        assertEquals(404, cut.buscarPorId(UUID.randomUUID(), id).getStatus());
    }

    @Test
    public void buscarPorIdConExcepcionTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class)))
                .thenThrow(new RuntimeException("Error en base de datos"));

        assertEquals(500, cut.buscarPorId(UUID.randomUUID(), id).getStatus());
    }

    @Test
    public void actualizarExitosoTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        DescuentoProducto existente = new DescuentoProducto(id);
        Descuento descuento = new Descuento(descuentoId);
        existente.setIdDescuento(descuento);
        DescuentoProducto nuevo = new DescuentoProducto();
        nuevo.setIdProducto(new Producto(productoId));
        Producto producto = new Producto(productoId);
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        Mockito.when(mockPS.findById(productoId)).thenReturn(producto);

        assertEquals(200, cut.actualizar(descuentoId, id, nuevo).getStatus());
        assertEquals(id, nuevo.getIdDescuentoProducto());
        assertEquals(descuento, nuevo.getIdDescuento());
        assertEquals(producto, nuevo.getIdProducto());
        Mockito.verify(mockCS).update(nuevo);
    }

    @Test
    public void actualizarParametrosInvalidosTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        assertEquals(400, cut.actualizar(null, id, new DescuentoProducto()).getStatus());
        assertEquals(400, cut.actualizar(descuentoId, null, new DescuentoProducto()).getStatus());
        assertEquals(400, cut.actualizar(descuentoId, id, null).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void actualizarNoEncontradoTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);

        assertEquals(404, cut.actualizar(descuentoId, id, new DescuentoProducto()).getStatus());
        Mockito.verifyNoInteractions(mockPS);
    }

    @Test
    public void actualizarDescuentoDiferenteTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        DescuentoProducto existente = new DescuentoProducto(id);
        existente.setIdDescuento(new Descuento(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);

        assertEquals(404, cut.actualizar(descuentoId, id, new DescuentoProducto()).getStatus());
    }

    @Test
    public void actualizarSinProductoTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        DescuentoProducto existente = new DescuentoProducto(id);
        existente.setIdDescuento(new Descuento(descuentoId));
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);

        assertEquals(400, cut.actualizar(descuentoId, id, new DescuentoProducto()).getStatus());
    }

    @Test
    public void actualizarProductoNoEncontradoTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        DescuentoProducto existente = new DescuentoProducto(id);
        existente.setIdDescuento(new Descuento(descuentoId));
        DescuentoProducto nuevo = new DescuentoProducto();
        nuevo.setIdProducto(new Producto(productoId));
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        Mockito.when(mockPS.findById(productoId)).thenReturn(null);

        assertEquals(404, cut.actualizar(descuentoId, id, nuevo).getStatus());
    }

    @Test
    public void actualizarConExcepcionTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        DescuentoProducto existente = new DescuentoProducto(id);
        existente.setIdDescuento(new Descuento(descuentoId));
        DescuentoProducto nuevo = new DescuentoProducto();
        nuevo.setIdProducto(new Producto(productoId));
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        Mockito.when(mockPS.findById(productoId)).thenReturn(new Producto(productoId));
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).update(nuevo);

        assertEquals(500, cut.actualizar(descuentoId, id, nuevo).getStatus());
    }

    @Test
    public void eliminarExitosoTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        DescuentoProducto existente = new DescuentoProducto(id);
        existente.setIdDescuento(new Descuento(descuentoId));
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);

        assertEquals(204, cut.eliminar(descuentoId, id).getStatus());
        Mockito.verify(mockCS).delete(existente);
    }

    @Test
    public void eliminarNoEncontradoTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);

        assertEquals(404, cut.eliminar(descuentoId, id).getStatus());
    }

    @Test
    public void eliminarDeOtroDescuentoTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        DescuentoProducto existente = new DescuentoProducto(id);
        existente.setIdDescuento(new Descuento(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);

        assertEquals(404, cut.eliminar(descuentoId, id).getStatus());
    }

    @Test
    public void eliminarSinDescuentoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new DescuentoProducto(id));

        assertEquals(404, cut.eliminar(UUID.randomUUID(), id).getStatus());
    }

    @Test
    public void eliminarConExcepcionTest() {
        UUID descuentoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        DescuentoProducto existente = new DescuentoProducto(id);
        existente.setIdDescuento(new Descuento(descuentoId));
        Mockito.when(mockCS.findByIdYDescuento(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).delete(existente);

        assertEquals(500, cut.eliminar(descuentoId, id).getStatus());
    }

    @Test
    public void parametrosInvalidosTest() {
        assertEquals(400, cut.buscarPorRango(null, 0, 50).getStatus());
        assertEquals(400, cut.buscarPorRango(UUID.randomUUID(), -1, 50).getStatus());
        assertEquals(400, cut.buscarPorId(null, UUID.randomUUID()).getStatus());
        assertEquals(400, cut.buscarPorId(UUID.randomUUID(), null).getStatus());
        assertEquals(400, cut.eliminar(null, UUID.randomUUID()).getStatus());
        assertEquals(400, cut.eliminar(UUID.randomUUID(), null).getStatus());
    }
}
