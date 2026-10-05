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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoModelTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private TipoProductoRepository tipoProductoRepository;

    @Mock
    private ProductoTipoProductoRepository productoTipoProductoRepository;

    @InjectMocks
    private ProductoModel modelo;

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
    public void testInstanciarRegistroConValoresPorDefecto() {
        Producto p = modelo.instanciarRegistro();

        assertNotNull(p.getIdProducto());
        assertEquals(Boolean.TRUE, p.getActivo());
        assertEquals("", p.getComentarios());
        assertEquals(BigDecimal.ZERO, p.getPrecioSugerido());
    }

    @Test
    public void testGetRegistroByIdBuscaEnLaListaCargada() {
        UUID id1 = UUID.randomUUID();
        Producto p1 = new Producto(id1);
        Producto p2 = new Producto(UUID.randomUUID());
        modelo.setRegistros(new ArrayList<>(List.of(p1, p2)));

        assertSame(p1, modelo.getRegistroById(id1));
        assertNull(modelo.getRegistroById(UUID.randomUUID()));
        assertNull(modelo.getRegistroById(null));

        modelo.setRegistros(null);
        assertNull(modelo.getRegistroById(id1));
    }

    @Test
    public void testLimpiarReiniciaElTipoSeleccionado() {
        modelo.setTipoSeleccionado(UUID.randomUUID());

        modelo.limpiar();

        assertNull(modelo.getTipoSeleccionado());
    }

    @Test
    public void testGuardarSinNombreAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("   ");

        modelo.btnGuardarHandler();

        assertEquals("El producto necesita nombre", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(productoRepository, never()).crear(any());
        verify(productoRepository, never()).modificar(any());
    }

    @Test
    public void testGuardarSinPrecioAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("Café");
        modelo.getRegistro().setPrecioSugerido(null);

        modelo.btnGuardarHandler();

        assertEquals("El producto necesita precio sugerido", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(productoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarConPrecioCeroAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("Café");
        modelo.getRegistro().setPrecioSugerido(BigDecimal.ZERO);

        modelo.btnGuardarHandler();

        assertEquals("El precio sugerido debe ser mayor a 0", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(productoRepository, never()).crear(any());
        verify(productoRepository, never()).modificar(any());
    }

    @Test
    public void testGuardarCreaElProductoYSuRelacionConTipo() {
        modelo.limpiar();
        Producto p = modelo.getRegistro();
        p.setNombre("Café");
        p.setPrecioSugerido(new BigDecimal("2.50"));
        UUID idTipo = UUID.randomUUID();
        modelo.setTipoSeleccionado(idTipo);
        TipoProducto tipo = new TipoProducto(idTipo);
        when(tipoProductoRepository.findById(idTipo)).thenReturn(tipo);
        when(productoTipoProductoRepository.findByProducto(p.getIdProducto()))
                .thenReturn(new ArrayList<>());

        modelo.btnGuardarHandler();

        verify(productoRepository, times(1)).crear(p);
        ArgumentCaptor<ProductoTipoProducto> captor =
                ArgumentCaptor.forClass(ProductoTipoProducto.class);
        verify(productoTipoProductoRepository, times(1)).crear(captor.capture());
        assertSame(tipo, captor.getValue().getIdTipoProducto());
        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testGuardarNoDuplicaLaRelacionSiYaExiste() {
        modelo.limpiar();
        Producto p = modelo.getRegistro();
        p.setNombre("Café");
        p.setPrecioSugerido(new BigDecimal("2.50"));
        UUID idTipo = UUID.randomUUID();
        modelo.setTipoSeleccionado(idTipo);
        ProductoTipoProducto existente = new ProductoTipoProducto(UUID.randomUUID());
        existente.setIdTipoProducto(new TipoProducto(idTipo));
        when(productoTipoProductoRepository.findByProducto(p.getIdProducto()))
                .thenReturn(new ArrayList<>(List.of(existente)));

        modelo.btnGuardarHandler();

        verify(productoRepository, times(1)).crear(p);
        verify(productoTipoProductoRepository, never()).crear(any());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
    }

    @Test
    public void testGuardarSinTipoNoCreaRelacion() {
        modelo.limpiar();
        Producto p = modelo.getRegistro();
        p.setNombre("Café");
        p.setPrecioSugerido(new BigDecimal("2.50"));

        modelo.btnGuardarHandler();

        verify(productoRepository, times(1)).crear(p);
        verify(productoTipoProductoRepository, never()).crear(any());
        verify(productoTipoProductoRepository, never()).findByProducto(any());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
    }

    @Test
    public void testGuardarRegistroExistenteEnEstadoCrearUsaModificar() {
        modelo.limpiar();
        Producto p = modelo.getRegistro();
        p.setNombre("Café");
        p.setPrecioSugerido(new BigDecimal("2.50"));
        when(productoRepository.findById(p.getIdProducto())).thenReturn(p);

        modelo.btnGuardarHandler();

        verify(productoRepository, never()).crear(any());
        verify(productoRepository, times(1)).modificar(p);
        assertEquals("Registro actualizado con éxito", unicoMensaje().getSummary());
    }

    @Test
    public void testGuardarCuandoElDaoFallaAvisaSinRomper() {
        modelo.limpiar();
        Producto p = modelo.getRegistro();
        p.setNombre("Café");
        p.setPrecioSugerido(new BigDecimal("2.50"));
        doThrow(new RuntimeException("boom")).when(productoRepository).crear(any());

        modelo.btnGuardarHandler();

        assertEquals("No se pudo guardar: verifique los datos e intente de nuevo",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testGuardadoFallidoDejaUnSoloMensaje() {
        modelo.limpiar();
        Producto p = modelo.getRegistro();
        p.setNombre("Café");
        p.setPrecioSugerido(new BigDecimal("2.50"));
        UUID idTipo = UUID.randomUUID();
        modelo.setTipoSeleccionado(idTipo);
        when(productoTipoProductoRepository.findByProducto(p.getIdProducto()))
                .thenThrow(new RuntimeException("boom"));

        modelo.btnGuardarHandler();

        assertEquals("No se pudo guardar: verifique los datos e intente de nuevo",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testEditarPrecargaElTipoDelRegistro() {
        UUID idProducto = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        Producto p = new Producto(idProducto);
        ProductoTipoProducto fila = new ProductoTipoProducto(UUID.randomUUID());
        fila.setIdTipoProducto(new TipoProducto(idTipo));
        when(productoTipoProductoRepository.findByProducto(idProducto))
                .thenReturn(new ArrayList<>(List.of(fila)));
        modelo.setSeleccion(p);

        modelo.btnEditarHandler();

        assertEquals(idTipo, modelo.getTipoSeleccionado());
        assertTrue(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.MODIFICAR, modelo.getEstado());
    }

    @Test
    public void testEditarSinRelacionDejaElComboVacio() {
        UUID idProducto = UUID.randomUUID();
        Producto p = new Producto(idProducto);
        when(productoTipoProductoRepository.findByProducto(idProducto))
                .thenReturn(new ArrayList<>());
        modelo.setTipoSeleccionado(UUID.randomUUID());
        modelo.setSeleccion(p);

        modelo.btnEditarHandler();

        assertNull(modelo.getTipoSeleccionado());
    }

    @Test
    public void testEliminarConRestricionLimpiaYAvisa() {
        Producto p = new Producto(UUID.randomUUID());
        modelo.setRegistro(p);
        modelo.setFormVisible(true);
        doThrow(new RuntimeException("RESTRICT")).when(productoRepository).eliminar(p);

        modelo.btnEliminarHandler();

        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertNotSame(p, modelo.getRegistro());
        assertEquals("No se puede eliminar: quite primero las asignaciones del producto",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testListaTiposMarcaInactivosEnGris() {
        TipoProducto activo = new TipoProducto();
        activo.setNombre("Bebida");
        activo.setActivo(true);
        TipoProducto inactivo = new TipoProducto();
        inactivo.setNombre("Comida");
        inactivo.setActivo(false);
        when(tipoProductoRepository.findAll())
                .thenReturn(new ArrayList<>(List.of(activo, inactivo)));

        List<SelectItem> lista = modelo.getListaTipos();

        assertEquals(2, lista.size());
        assertFalse(lista.get(0).isDisabled());
        assertTrue(lista.get(1).isDisabled());
    }

    @Test
    public void testBuscarPorNombreUsaElRepositorioYVacioRecargaTodo() {
        Producto p = new Producto(UUID.randomUUID());
        when(productoRepository.findByNombreLike("Caf")).thenReturn(new ArrayList<>(List.of(p)));
        modelo.setFiltroNombre("Caf");

        modelo.buscarPorNombre();

        assertEquals(1, modelo.getRegistros().size());
        verify(productoRepository, never()).findAll();

        modelo.setFiltroNombre(null);
        modelo.buscarPorNombre();

        verify(productoRepository, times(1)).findAll();
    }

    @Test
    public void testBuscarActivosUsaElRepositorio() {
        when(productoRepository.findActivos()).thenReturn(new ArrayList<>());

        modelo.buscarActivos();

        assertNotNull(modelo.getRegistros());
        verify(productoRepository, times(1)).findActivos();
    }

    @Test
    public void testBuscarPorPrecioConRangoYSinRangoRecargaTodo() {
        BigDecimal min = new BigDecimal("1.00");
        BigDecimal max = new BigDecimal("10.00");
        when(productoRepository.findByPrecioRange(min, max)).thenReturn(new ArrayList<>());
        modelo.setFiltroPrecioMin(min);
        modelo.setFiltroPrecioMax(max);

        modelo.buscarPorPrecio();

        verify(productoRepository, times(1)).findByPrecioRange(min, max);
        verify(productoRepository, never()).findAll();

        modelo.setFiltroPrecioMin(null);
        modelo.setFiltroPrecioMax(null);
        modelo.buscarPorPrecio();

        verify(productoRepository, times(1)).findAll();
    }
}
