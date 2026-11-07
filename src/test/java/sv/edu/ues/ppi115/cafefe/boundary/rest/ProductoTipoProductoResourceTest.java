package sv.edu.ues.ppi115.cafefe.boundary.rest;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.ppi115.cafefe.control.ProductoService;
import sv.edu.ues.ppi115.cafefe.control.ProductoTipoProductoService;
import sv.edu.ues.ppi115.cafefe.control.TipoProductoService;
import sv.edu.ues.ppi115.cafefe.entity.Producto;
import sv.edu.ues.ppi115.cafefe.entity.ProductoTipoProducto;
import sv.edu.ues.ppi115.cafefe.entity.TipoProducto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProductoTipoProductoResourceTest {

    private UriInfo mockUriInfo;
    private ProductoTipoProductoService mockCS;
    private ProductoService mockPS;
    private TipoProductoService mockTPS;
    private ProductoTipoProductoResource cut;

    @BeforeEach
    public void setup() {
        mockUriInfo = Mockito.mock(UriInfo.class);
        UriBuilder mockUriBuilder = Mockito.mock(UriBuilder.class);
        mockCS = Mockito.mock(ProductoTipoProductoService.class);
        mockPS = Mockito.mock(ProductoService.class);
        mockTPS = Mockito.mock(TipoProductoService.class);

        Mockito.when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.path(Mockito.anyString())).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.build()).thenReturn(URI.create("http://localhost:8080/v1/producto/1/tipoproducto/1"));

        cut = new ProductoTipoProductoResource();
        cut.productoTipoProductoService = mockCS;
        cut.productoService = mockPS;
        cut.tipoProductoService = mockTPS;
    }

    @Test
    public void crearExitosoTest() {
        UUID productoId = UUID.randomUUID();
        Producto producto = new Producto(productoId);
        ProductoTipoProducto nuevo = new ProductoTipoProducto();
        UUID tipoProductoId = UUID.randomUUID();
        nuevo.setIdTipoProducto(new TipoProducto(tipoProductoId));
        Mockito.when(mockPS.findById(productoId)).thenReturn(producto);
        Mockito.when(mockTPS.findById(tipoProductoId)).thenReturn(new TipoProducto(tipoProductoId));

        assertEquals(201, cut.crear(productoId, nuevo, mockUriInfo).getStatus());
        assertNotNull(nuevo.getIdProductoTipoProducto());
        assertEquals(producto, nuevo.getIdProducto());
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearConIdTest() {
        UUID productoId = UUID.randomUUID();
        ProductoTipoProducto nuevo = new ProductoTipoProducto(UUID.randomUUID());

        assertEquals(400, cut.crear(productoId, nuevo, mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockCS, mockPS, mockTPS);
    }

    @Test
    public void crearParametrosInvalidosTest() {
        UUID productoId = UUID.randomUUID();
        ProductoTipoProducto sinTipo = new ProductoTipoProducto();
        ProductoTipoProducto sinId = new ProductoTipoProducto();
        sinId.setIdTipoProducto(new TipoProducto());
        assertEquals(400, cut.crear(null, sinTipo, mockUriInfo).getStatus());
        assertEquals(400, cut.crear(productoId, null, mockUriInfo).getStatus());
        assertEquals(400, cut.crear(productoId, sinTipo, mockUriInfo).getStatus());
        assertEquals(400, cut.crear(productoId, sinId, mockUriInfo).getStatus());
    }

    @Test
    public void crearProductoNoEncontradoTest() {
        UUID productoId = UUID.randomUUID();
        ProductoTipoProducto nuevo = new ProductoTipoProducto();
        nuevo.setIdTipoProducto(new TipoProducto(UUID.randomUUID()));
        Mockito.when(mockPS.findById(productoId)).thenReturn(null);
        assertEquals(404, cut.crear(productoId, nuevo, mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockTPS, mockCS);
    }

    @Test
    public void crearTipoNoEncontradoTest() {
        UUID productoId = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();
        ProductoTipoProducto nuevo = new ProductoTipoProducto();
        nuevo.setIdTipoProducto(new TipoProducto(tipoId));
        Mockito.when(mockPS.findById(productoId)).thenReturn(new Producto(productoId));
        Mockito.when(mockTPS.findById(tipoId)).thenReturn(null);
        assertEquals(404, cut.crear(productoId, nuevo, mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearConExcepcionTest() {
        UUID productoId = UUID.randomUUID();
        ProductoTipoProducto nuevo = new ProductoTipoProducto();
        nuevo.setIdTipoProducto(new TipoProducto(UUID.randomUUID()));
        Mockito.when(mockPS.findById(productoId))
                .thenThrow(new RuntimeException("Error en base de datos"));
        assertEquals(500, cut.crear(productoId, nuevo, mockUriInfo).getStatus());
    }

    @Test
    public void buscarPorRangoExitosoTest() {
        UUID productoId = UUID.randomUUID();
        Mockito.when(mockCS.findByProducto(productoId, 0, 50))
                .thenReturn(List.of(new ProductoTipoProducto()));
        Mockito.when(mockCS.contarPorProducto(productoId)).thenReturn(1L);

        assertEquals(200, cut.buscarPorRango(productoId, 0, 50).getStatus());
    }

    @Test
    public void buscarPorRangoErroresTest() {
        UUID productoId = UUID.randomUUID();
        assertEquals(400, cut.buscarPorRango(productoId, -1, 50).getStatus());
        assertEquals(400, cut.buscarPorRango(productoId, 0, 0).getStatus());
        assertEquals(400, cut.buscarPorRango(productoId, 0, 51).getStatus());
        Mockito.when(mockCS.findByProducto(productoId, 0, 50))
                .thenThrow(new RuntimeException("Error en base de datos"));
        assertEquals(500, cut.buscarPorRango(productoId, 0, 50).getStatus());
    }

    @Test
    public void buscarPorRangoConteoConExcepcionTest() {
        UUID productoId = UUID.randomUUID();
        Mockito.when(mockCS.findByProducto(productoId, 0, 50))
                .thenReturn(List.of(new ProductoTipoProducto()));
        Mockito.when(mockCS.contarPorProducto(productoId))
                .thenThrow(new RuntimeException("Error en base de datos"));
        assertEquals(500, cut.buscarPorRango(productoId, 0, 50).getStatus());
    }

    @Test
    public void buscarPorIdDeOtroProductoTest() {
        UUID productoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        ProductoTipoProducto encontrado = new ProductoTipoProducto(id);
        encontrado.setIdProducto(new Producto(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(encontrado);

        assertEquals(404, cut.buscarPorId(productoId, id).getStatus());
    }

    @Test
    public void buscarPorIdCasosTest() {
        UUID productoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        assertEquals(400, cut.buscarPorId(null, id).getStatus());
        assertEquals(400, cut.buscarPorId(productoId, null).getStatus());
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.buscarPorId(productoId, id).getStatus());
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new ProductoTipoProducto(id));
        assertEquals(404, cut.buscarPorId(productoId, id).getStatus());
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenThrow(new RuntimeException("Error en base de datos"));
        assertEquals(500, cut.buscarPorId(productoId, id).getStatus());
    }

    @Test
    public void actualizarExitosoTest() {
        UUID productoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();
        Producto producto = new Producto(productoId);
        ProductoTipoProducto existente = new ProductoTipoProducto(id);
        existente.setIdProducto(producto);
        ProductoTipoProducto nuevo = new ProductoTipoProducto();
        nuevo.setIdTipoProducto(new TipoProducto(tipoId));
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        Mockito.when(mockTPS.findById(tipoId)).thenReturn(new TipoProducto(tipoId));
        assertEquals(200, cut.actualizar(productoId, id, nuevo).getStatus());
        assertEquals(id, nuevo.getIdProductoTipoProducto());
        assertEquals(producto, nuevo.getIdProducto());
        Mockito.verify(mockCS).update(nuevo);
    }

    @Test
    public void actualizarErroresTest() {
        UUID productoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        assertEquals(400, cut.actualizar(null, id, new ProductoTipoProducto()).getStatus());
        assertEquals(400, cut.actualizar(productoId, null, new ProductoTipoProducto()).getStatus());
        assertEquals(400, cut.actualizar(productoId, id, null).getStatus());
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.actualizar(productoId, id, new ProductoTipoProducto()).getStatus());
        ProductoTipoProducto otro = new ProductoTipoProducto(id);
        otro.setIdProducto(new Producto(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(otro);
        assertEquals(404, cut.actualizar(productoId, id, new ProductoTipoProducto()).getStatus());
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new ProductoTipoProducto(id));
        assertEquals(404, cut.actualizar(productoId, id, new ProductoTipoProducto()).getStatus());
    }

    @Test
    public void actualizarDependenciaErroresTest() {
        UUID productoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        ProductoTipoProducto existente = new ProductoTipoProducto(id);
        existente.setIdProducto(new Producto(productoId));
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        assertEquals(400, cut.actualizar(productoId, id, new ProductoTipoProducto()).getStatus());
        ProductoTipoProducto nuevo = new ProductoTipoProducto();
        nuevo.setIdTipoProducto(new TipoProducto(UUID.randomUUID()));
        Mockito.when(mockTPS.findById(Mockito.any())).thenReturn(null);
        assertEquals(404, cut.actualizar(productoId, id, nuevo).getStatus());
        Mockito.when(mockTPS.findById(Mockito.any())).thenThrow(new RuntimeException("Error en base de datos"));
        assertEquals(500, cut.actualizar(productoId, id, nuevo).getStatus());
    }

    @Test
    public void eliminarCasosTest() {
        UUID productoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        assertEquals(400, cut.eliminar(null, id).getStatus());
        assertEquals(400, cut.eliminar(productoId, null).getStatus());
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.eliminar(productoId, id).getStatus());
        ProductoTipoProducto existente = new ProductoTipoProducto(id);
        existente.setIdProducto(new Producto(productoId));
        Mockito.when(mockCS.findByIdYProducto(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        assertEquals(204, cut.eliminar(productoId, id).getStatus());
        Mockito.doThrow(new RuntimeException("Error en base de datos")).when(mockCS).delete(existente);
        assertEquals(500, cut.eliminar(productoId, id).getStatus());
    }

    @Test
    public void parametrosInvalidosTest() {
        assertEquals(400, cut.buscarPorRango(null, 0, 50).getStatus());
        assertEquals(400, cut.buscarPorId(UUID.randomUUID(), null).getStatus());
    }
}
