package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.model.SelectItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.control.ProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.ProductoTipoProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.TipoProductoRepository;
import sv.edu.ues.ppi115.cafefe.entity.Producto;
import sv.edu.ues.ppi115.cafefe.entity.ProductoTipoProducto;
import sv.edu.ues.ppi115.cafefe.entity.TipoProducto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoTipoProductoModelTest {

    @Mock
    private ProductoTipoProductoRepository productoTipoProductoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private TipoProductoRepository tipoProductoRepository;

    @InjectMocks
    private ProductoTipoProductoModel modelo;

    private ContextoPrueba contexto;

    @BeforeEach
    public void setUp() {
        contexto = new ContextoPrueba();
        ContextoPrueba.colocarComoActual(contexto);
    }

    @AfterEach
    public void quitarContexto() {
        ContextoPrueba.colocarComoActual(null);
    }

    private FacesMessage unicoMensaje() {
        assertEquals(1, contexto.mensajes.size());
        return contexto.mensajes.get(0);
    }

    @Test
    public void testInstanciarRegistroPoneFechaCreacion() {
        ProductoTipoProducto r = modelo.instanciarRegistro();

        assertNotNull(r.getIdProductoTipoProducto());
        assertNotNull(r.getFechaCreacion());
    }

    @Test
    public void testLimpiarReiniciaLasDosSelecciones() {
        modelo.setProductoSeleccionado(UUID.randomUUID());
        modelo.setTipoSeleccionado(UUID.randomUUID());

        modelo.limpiar();

        assertNull(modelo.getProductoSeleccionado());
        assertNull(modelo.getTipoSeleccionado());
    }

    @Test
    public void testGuardarSinNadaAvisaYNoGuarda() {
        modelo.limpiar();

        modelo.btnGuardarHandler();

        assertEquals("Debe elegir el producto y el tipo de producto", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(productoTipoProductoRepository, never()).crear(any());
        verify(productoTipoProductoRepository, never()).modificar(any());
    }

    @Test
    public void testGuardarSoloConProductoAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.setProductoSeleccionado(UUID.randomUUID());

        modelo.btnGuardarHandler();

        assertEquals("Debe elegir el producto y el tipo de producto", unicoMensaje().getSummary());
        verify(productoTipoProductoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarConAmbosCreaLaRelacion() {
        modelo.limpiar();
        ProductoTipoProducto r = modelo.getRegistro();
        UUID idProducto = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        modelo.setProductoSeleccionado(idProducto);
        modelo.setTipoSeleccionado(idTipo);
        Producto producto = new Producto(idProducto);
        TipoProducto tipo = new TipoProducto(idTipo);
        when(productoRepository.findById(idProducto)).thenReturn(producto);
        when(tipoProductoRepository.findById(idTipo)).thenReturn(tipo);

        modelo.btnGuardarHandler();

        verify(productoTipoProductoRepository, times(1)).crear(r);
        ArgumentCaptor<ProductoTipoProducto> captor =
                ArgumentCaptor.forClass(ProductoTipoProducto.class);
        verify(productoTipoProductoRepository, times(1)).crear(captor.capture());
        assertSame(producto, captor.getValue().getIdProducto());
        assertSame(tipo, captor.getValue().getIdTipoProducto());
        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testGuardarEnModificacionConvierteLosCombos() {
        modelo.limpiar();
        ProductoTipoProducto existente = new ProductoTipoProducto(UUID.randomUUID());
        modelo.seleccionarRegistro(existente);
        UUID idProducto = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        modelo.setProductoSeleccionado(idProducto);
        modelo.setTipoSeleccionado(idTipo);
        Producto producto = new Producto(idProducto);
        TipoProducto tipo = new TipoProducto(idTipo);
        when(productoRepository.findById(idProducto)).thenReturn(producto);
        when(tipoProductoRepository.findById(idTipo)).thenReturn(tipo);

        modelo.btnGuardarHandler();

        verify(productoTipoProductoRepository, never()).crear(any());
        verify(productoTipoProductoRepository, times(1)).modificar(existente);
        assertSame(producto, existente.getIdProducto());
        assertSame(tipo, existente.getIdTipoProducto());
        assertEquals("Registro actualizado con éxito", unicoMensaje().getSummary());
    }

    @Test
    public void testEditarPrecargaLosDesplegables() {
        UUID idProducto = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        ProductoTipoProducto r = new ProductoTipoProducto(UUID.randomUUID());
        r.setIdProducto(new Producto(idProducto));
        r.setIdTipoProducto(new TipoProducto(idTipo));
        modelo.setSeleccion(r);

        modelo.btnEditarHandler();

        assertEquals(idProducto, modelo.getProductoSeleccionado());
        assertEquals(idTipo, modelo.getTipoSeleccionado());
        assertTrue(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.MODIFICAR, modelo.getEstado());
    }

    @Test
    public void testEditarSinRelacionesDejaCombosVacios() {
        ProductoTipoProducto r = new ProductoTipoProducto(UUID.randomUUID());
        modelo.setProductoSeleccionado(UUID.randomUUID());
        modelo.setTipoSeleccionado(UUID.randomUUID());
        modelo.setSeleccion(r);

        modelo.btnEditarHandler();

        assertNull(modelo.getProductoSeleccionado());
        assertNull(modelo.getTipoSeleccionado());
    }

    @Test
    public void testEliminarConErrorLimpiaYAvisa() {
        ProductoTipoProducto r = new ProductoTipoProducto(UUID.randomUUID());
        modelo.setRegistro(r);
        modelo.setFormVisible(true);
        doThrow(new RuntimeException("RESTRICT")).when(productoTipoProductoRepository).eliminar(r);

        modelo.btnEliminarHandler();

        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertNotSame(r, modelo.getRegistro());
        assertEquals("No se pudo eliminar la relación producto - tipo de producto",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testOnTabChangeVaciaLasListas() {
        when(productoRepository.findAll()).thenReturn(new ArrayList<>());
        when(tipoProductoRepository.findAll()).thenReturn(new ArrayList<>());

        modelo.getListaProductos();
        modelo.getListaTipos();
        modelo.onTabChange(null);
        modelo.getListaProductos();
        modelo.getListaTipos();

        verify(productoRepository, times(2)).findAll();
        verify(tipoProductoRepository, times(2)).findAll();
    }

    @Test
    public void testListasMarcanInactivosEnGris() {
        Producto activo = new Producto();
        activo.setNombre("Café");
        activo.setActivo(true);
        Producto inactivo = new Producto();
        inactivo.setNombre("Té");
        inactivo.setActivo(false);
        TipoProducto tipoActivo = new TipoProducto();
        tipoActivo.setNombre("Bebida");
        tipoActivo.setActivo(true);
        TipoProducto tipoInactivo = new TipoProducto();
        tipoInactivo.setNombre("Comida");
        tipoInactivo.setActivo(false);
        when(productoRepository.findAll()).thenReturn(new ArrayList<>(List.of(activo, inactivo)));
        when(tipoProductoRepository.findAll())
                .thenReturn(new ArrayList<>(List.of(tipoActivo, tipoInactivo)));

        List<SelectItem> productos = modelo.getListaProductos();
        assertEquals(2, productos.size());
        assertFalse(productos.get(0).isDisabled());
        assertTrue(productos.get(1).isDisabled());

        List<SelectItem> tipos = modelo.getListaTipos();
        assertEquals(2, tipos.size());
        assertFalse(tipos.get(0).isDisabled());
        assertTrue(tipos.get(1).isDisabled());
    }

    @Test
    public void testBuscarPorProductoIdValidoEInvalido() {
        UUID id = UUID.randomUUID();
        modelo.setFiltroProducto(id.toString());

        modelo.buscarPorProducto();

        verify(productoTipoProductoRepository, times(1)).findByProducto(id);
        verify(productoTipoProductoRepository, never()).findAll();

        modelo.setFiltroProducto("no-es-uuid");
        modelo.buscarPorProducto();

        verify(productoTipoProductoRepository, times(1)).findAll();
    }

    @Test
    public void testBuscarPorTipoProductoIdValidoEInvalido() {
        UUID id = UUID.randomUUID();
        modelo.setFiltroTipoProducto(id.toString());

        modelo.buscarPorTipoProducto();

        verify(productoTipoProductoRepository, times(1)).findByTipoProducto(id);
        verify(productoTipoProductoRepository, never()).findAll();

        modelo.setFiltroTipoProducto("no-es-uuid");
        modelo.buscarPorTipoProducto();

        verify(productoTipoProductoRepository, times(1)).findAll();
    }

    @Test
    public void testBuscarPorObservacionesConValorYVacio() {
        modelo.setFiltroObservaciones("  alto azúcar  ");

        modelo.buscarPorObservaciones();

        verify(productoTipoProductoRepository, times(1)).findByObservaciones("alto azúcar");
        verify(productoTipoProductoRepository, never()).findAll();

        modelo.setFiltroObservaciones("   ");
        modelo.buscarPorObservaciones();

        verify(productoTipoProductoRepository, times(1)).findAll();
    }
}
