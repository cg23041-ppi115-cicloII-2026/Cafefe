package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.PrimeFaces;
import sv.edu.ues.ppi115.cafefe.control.DescuentoProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.OrdenProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.OrdenRepository;
import sv.edu.ues.ppi115.cafefe.control.ProductoRepository;
import sv.edu.ues.ppi115.cafefe.entity.Descuento;
import sv.edu.ues.ppi115.cafefe.entity.DescuentoProducto;
import sv.edu.ues.ppi115.cafefe.entity.Orden;
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;
import sv.edu.ues.ppi115.cafefe.entity.Producto;
import sv.edu.ues.ppi115.cafefe.entity.TipoDescuento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdenProductoModelTest {

    @Mock
    private OrdenProductoRepository ordenProductoRepository;

    @Mock
    private OrdenRepository ordenRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private DescuentoProductoRepository descuentoProductoRepository;

    @InjectMocks
    private OrdenProductoModel modelo;

    private ContextoPrueba contexto;
    private PrimeFaces.Ajax ajax;

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

    private void conPrimeFaces(Runnable prueba) {
        try (MockedStatic<PrimeFaces> estatica = mockStatic(PrimeFaces.class)) {
            PrimeFaces faces = mock(PrimeFaces.class);
            ajax = mock(PrimeFaces.Ajax.class);
            when(faces.ajax()).thenReturn(ajax);
            estatica.when(PrimeFaces::current).thenReturn(faces);
            prueba.run();
        }
    }

    private Producto producto(String nombre) {
        Producto p = new Producto(UUID.randomUUID());
        p.setNombre(nombre);
        p.setActivo(true);
        p.setPrecioSugerido(new BigDecimal("2.50"));
        return p;
    }

    private Orden ordenDeHoy(UUID idOrden) {
        Orden o = new Orden(idOrden);
        o.setFechaCreacion(new Date());
        return o;
    }

    private DescuentoProducto descuentoDe(Producto producto) {
        DescuentoProducto dp = new DescuentoProducto(UUID.randomUUID());
        dp.setValor(20);
        dp.setIdProducto(producto);
        TipoDescuento tipo = new TipoDescuento(UUID.randomUUID());
        tipo.setNombre("Por fecha");
        tipo.setActivo(true);
        Descuento descuento = new Descuento(UUID.randomUUID());
        descuento.setNombre("Platinum");
        descuento.setIdTipoDescuento(tipo);
        dp.setIdDescuento(descuento);
        return dp;
    }

    @Test
    public void testInstanciarRegistroConIdNuevoyObservacionesVacias() {
        OrdenProducto r = modelo.instanciarRegistro();

        assertNotNull(r.getIdOrdenProducto());
        assertEquals("", r.getObservaciones());
        assertNull(r.getIdProducto());
        assertNull(r.getPrecio());
    }

    @Test
    public void testCompletarProductosSinTextoDevuelveTodos() {
        when(productoRepository.findActivos()).thenReturn(
                new ArrayList<>(List.of(producto("Café"), producto("Té"))));

        assertEquals(List.of("Café", "Té"), modelo.completarProductos(""));
        assertEquals(List.of("Café", "Té"), modelo.completarProductos(null));
    }

    @Test
    public void testCompletarProductosFiltraPorTexto() {
        when(productoRepository.findActivos()).thenReturn(
                new ArrayList<>(List.of(producto("Café Molido"), producto("Té Verde"))));

        assertEquals(List.of("Café Molido"), modelo.completarProductos("CAF"));
        assertEquals(List.of("Té Verde"), modelo.completarProductos("  verde  "));
        assertTrue(modelo.completarProductos("Xuxu").isEmpty());
    }

    @Test
    public void testSeleccionarProductoVacioAvisa() {
        modelo.limpiar();
        modelo.setProductoBusqueda("   ");

        modelo.btnSeleccionarProductoHandler();

        assertEquals("Escriba el nombre del producto", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verifyNoInteractions(productoRepository);
    }

    @Test
    public void testSeleccionarProductoInexistenteAvisa() {
        modelo.limpiar();
        modelo.setProductoBusqueda("Xuxu");
        when(productoRepository.findActivos()).thenReturn(
                new ArrayList<>(List.of(producto("Café"))));

        modelo.btnSeleccionarProductoHandler();

        assertEquals("No se encontró el producto indicado (o está inactivo)",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        assertNull(modelo.getRegistro().getIdProducto());
    }

    @Test
    public void testSeleccionarProductoAplicaElProductoYElPrecioSugerido() {
        modelo.limpiar();
        assertEquals("(ninguno)", modelo.getProductoElegido());
        Producto cafecito = producto("Café");
        when(productoRepository.findActivos()).thenReturn(
                new ArrayList<>(List.of(cafecito)));
        modelo.setProductoBusqueda("  café  ");

        modelo.btnSeleccionarProductoHandler();

        assertSame(cafecito, modelo.getRegistro().getIdProducto());
        assertEquals(new BigDecimal("2.50"), modelo.getRegistro().getPrecio());
        assertEquals("Café", modelo.getProductoElegido());
        assertTrue(contexto.mensajes.isEmpty());
    }

    @Test
    public void testGuardarLineaSinOrdenGuardadaAvisa() {
        modelo.limpiar();

        modelo.btnGuardarLinea(null);
        assertEquals("Primero guarde la orden en la pestaña Tomar Orden",
                unicoMensaje().getSummary());

        contexto.mensajes.clear();
        modelo.btnGuardarLinea(UUID.randomUUID());
        assertEquals("Primero guarde la orden en la pestaña Tomar Orden",
                unicoMensaje().getSummary());
        verifyNoInteractions(ordenProductoRepository);
    }

    @Test
    public void testGuardarLineaSinProductoAvisa() {
        modelo.limpiar();
        UUID idOrden = UUID.randomUUID();
        when(ordenRepository.findById(idOrden)).thenReturn(ordenDeHoy(idOrden));

        modelo.btnGuardarLinea(idOrden);

        assertEquals("Seleccione un producto", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(ordenProductoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarLineaConPrecioInvalidoAvisa() {
        modelo.limpiar();
        UUID idOrden = UUID.randomUUID();
        when(ordenRepository.findById(idOrden)).thenReturn(ordenDeHoy(idOrden));
        modelo.getRegistro().setIdProducto(producto("Café"));

        modelo.btnGuardarLinea(idOrden);
        assertEquals("El precio debe ser mayor a 0", unicoMensaje().getSummary());

        contexto.mensajes.clear();
        modelo.getRegistro().setPrecio(BigDecimal.ZERO);
        modelo.btnGuardarLinea(idOrden);
        assertEquals("El precio debe ser mayor a 0", unicoMensaje().getSummary());
        verify(ordenProductoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarLineaValidaCreaLaLinea() {
        modelo.limpiar();
        UUID idOrden = UUID.randomUUID();
        Orden orden = ordenDeHoy(idOrden);
        when(ordenRepository.findById(idOrden)).thenReturn(orden);
        modelo.getRegistro().setIdProducto(producto("Café"));
        modelo.getRegistro().setPrecio(new BigDecimal("15.99"));
        OrdenProducto linea = modelo.getRegistro();

        modelo.btnGuardarLinea(idOrden);

        assertSame(orden, linea.getIdOrden());
        verify(ordenProductoRepository, times(1)).crear(linea);
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
        assertFalse(modelo.isFormVisible());
    }

    @Test
    public void testGuardarLineaCuandoElDaoFallaAvisaAlUsuario() {
        modelo.limpiar();
        UUID idOrden = UUID.randomUUID();
        when(ordenRepository.findById(idOrden)).thenReturn(ordenDeHoy(idOrden));
        modelo.getRegistro().setIdProducto(producto("Café"));
        modelo.getRegistro().setPrecio(new BigDecimal("15.99"));
        doThrow(new RuntimeException("boom")).when(ordenProductoRepository).crear(any());

        modelo.btnGuardarLinea(idOrden);

        assertEquals("No se pudo guardar: verifique los datos e intente de nuevo",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testAbrirDescuentosSinOrdenAvisa() {
        conPrimeFaces(() -> {
            modelo.btnAbrirDescuentos(null);

            assertEquals("Primero guarde la orden en la pestaña Tomar Orden",
                    unicoMensaje().getSummary());
            assertFalse(modelo.isDescuentosListos());
        });
        verify(ajax).addCallbackParam("listos", false);
    }

    @Test
    public void testAbrirDescuentosSinAplicablesAvisa() {
        Producto cafecito = producto("Café");
        OrdenProducto linea = new OrdenProducto(UUID.randomUUID());
        linea.setIdProducto(cafecito);
        when(ordenProductoRepository.findByOrden(any()))
                .thenReturn(new ArrayList<>(List.of(linea)));
        when(descuentoProductoRepository.findAll()).thenReturn(new ArrayList<>());

        conPrimeFaces(() -> {
            UUID idOrden = UUID.randomUUID();
            when(ordenRepository.findById(idOrden)).thenReturn(ordenDeHoy(idOrden));

            modelo.btnAbrirDescuentos(idOrden);

            assertEquals("No hay descuentos aplicables a los productos de la orden",
                    unicoMensaje().getSummary());
            assertFalse(modelo.isDescuentosListos());
        });
        verify(ajax).addCallbackParam("listos", false);
    }

    @Test
    public void testAbrirDescuentosConAplicablesAbreLaVentana() {
        Producto cafecito = producto("Café");
        OrdenProducto linea = new OrdenProducto(UUID.randomUUID());
        linea.setIdProducto(cafecito);
        DescuentoProducto dp = descuentoDe(cafecito);
        when(ordenProductoRepository.findByOrden(any()))
                .thenReturn(new ArrayList<>(List.of(linea)));
        when(descuentoProductoRepository.findAll())
                .thenReturn(new ArrayList<>(List.of(dp)));

        conPrimeFaces(() -> {
            UUID idOrden = UUID.randomUUID();
            when(ordenRepository.findById(idOrden)).thenReturn(ordenDeHoy(idOrden));

            modelo.btnAbrirDescuentos(idOrden);

            assertTrue(modelo.isDescuentosListos());
            assertEquals(1, modelo.getListaDescuentosAplicables().size());
            assertEquals("Platinum (20)%",
                    modelo.getListaDescuentosAplicables().get(0).getLabel());
        });
        verify(ajax).addCallbackParam("listos", true);
    }

    @Test
    public void testAplicarSinDescuentoSeleccionadoAvisa() {
        modelo.btnAplicarDescuentoHandler();

        assertEquals("Seleccione un descuento", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verifyNoInteractions(descuentoProductoRepository);
    }

    @Test
    public void testAplicarDescuentoInexistenteAvisa() {
        modelo.setDescuentoAplicableSeleccionado(UUID.randomUUID());

        modelo.btnAplicarDescuentoHandler();

        assertEquals("El descuento seleccionado ya no está disponible",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testAplicarFueraDeFechaDeLaOrdenAvisa() {
        Producto cafecito = producto("Café");
        OrdenProducto linea = new OrdenProducto(UUID.randomUUID());
        linea.setIdProducto(cafecito);
        DescuentoProducto dp = descuentoDe(cafecito);
        dp.setFechaDesde(Date.from(LocalDate.now().plusDays(30)
                .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        when(ordenProductoRepository.findByOrden(any()))
                .thenReturn(new ArrayList<>(List.of(linea)));
        when(descuentoProductoRepository.findAll())
                .thenReturn(new ArrayList<>(List.of(dp)));
        when(descuentoProductoRepository.findById(dp.getIdDescuentoProducto()))
                .thenReturn(dp);

        conPrimeFaces(() -> {
            UUID idOrden = UUID.randomUUID();
            when(ordenRepository.findById(idOrden)).thenReturn(ordenDeHoy(idOrden));
            modelo.btnAbrirDescuentos(idOrden);
            contexto.mensajes.clear();
            modelo.setDescuentoAplicableSeleccionado(dp.getIdDescuentoProducto());

            modelo.btnAplicarDescuentoHandler();

            assertEquals("El descuento ya no aplica a la fecha de la orden",
                    unicoMensaje().getSummary());
        });
    }

    @Test
    public void testAplicarConTipoInactivoAvisa() {
        Producto cafecito = producto("Café");
        OrdenProducto linea = new OrdenProducto(UUID.randomUUID());
        linea.setIdProducto(cafecito);
        DescuentoProducto dp = descuentoDe(cafecito);
        dp.getIdDescuento().getIdTipoDescuento().setActivo(false);
        when(ordenProductoRepository.findByOrden(any()))
                .thenReturn(new ArrayList<>(List.of(linea)));
        when(descuentoProductoRepository.findAll())
                .thenReturn(new ArrayList<>(List.of(dp)));
        when(descuentoProductoRepository.findById(dp.getIdDescuentoProducto()))
                .thenReturn(dp);

        conPrimeFaces(() -> {
            UUID idOrden = UUID.randomUUID();
            when(ordenRepository.findById(idOrden)).thenReturn(ordenDeHoy(idOrden));
            modelo.btnAbrirDescuentos(idOrden);
            contexto.mensajes.clear();
            modelo.setDescuentoAplicableSeleccionado(dp.getIdDescuentoProducto());

            modelo.btnAplicarDescuentoHandler();

            assertEquals("El tipo de descuento del elegido está inactivo",
                    unicoMensaje().getSummary());
        });
    }

    @Test
    public void testAplicarConValorInvalidoAvisa() {
        Producto cafecito = producto("Café");
        OrdenProducto linea = new OrdenProducto(UUID.randomUUID());
        linea.setIdProducto(cafecito);
        DescuentoProducto dp = descuentoDe(cafecito);
        dp.setValor(0);
        when(ordenProductoRepository.findByOrden(any()))
                .thenReturn(new ArrayList<>(List.of(linea)));
        when(descuentoProductoRepository.findAll())
                .thenReturn(new ArrayList<>(List.of(dp)));
        when(descuentoProductoRepository.findById(dp.getIdDescuentoProducto()))
                .thenReturn(dp);

        conPrimeFaces(() -> {
            UUID idOrden = UUID.randomUUID();
            when(ordenRepository.findById(idOrden)).thenReturn(ordenDeHoy(idOrden));
            modelo.btnAbrirDescuentos(idOrden);
            contexto.mensajes.clear();
            modelo.setDescuentoAplicableSeleccionado(dp.getIdDescuentoProducto());

            modelo.btnAplicarDescuentoHandler();

            assertEquals("El descuento no tiene un valor válido",
                    unicoMensaje().getSummary());
        });
    }

    @Test
    public void testAplicarSinLineasDelProductoAvisa() {
        Producto cafecito = producto("Café");
        Producto te = producto("Té");
        OrdenProducto linea = new OrdenProducto(UUID.randomUUID());
        linea.setIdProducto(cafecito);
        DescuentoProducto dp = descuentoDe(te);
        when(ordenProductoRepository.findByOrden(any()))
                .thenReturn(new ArrayList<>(List.of(linea)));
        when(descuentoProductoRepository.findAll())
                .thenReturn(new ArrayList<>(List.of(dp)));
        when(descuentoProductoRepository.findById(dp.getIdDescuentoProducto()))
                .thenReturn(dp);

        conPrimeFaces(() -> {
            UUID idOrden = UUID.randomUUID();
            when(ordenRepository.findById(idOrden)).thenReturn(ordenDeHoy(idOrden));
            modelo.btnAbrirDescuentos(idOrden);
            contexto.mensajes.clear();
            modelo.setDescuentoAplicableSeleccionado(dp.getIdDescuentoProducto());

            modelo.btnAplicarDescuentoHandler();

            assertEquals("La orden ya no tiene el producto de ese descuento",
                    unicoMensaje().getSummary());
        });
    }

    @Test
    public void testAplicarDescuentoDescuentaElPrecioYAnotaObservaciones() {
        Producto cafecito = producto("Café");
        OrdenProducto linea = new OrdenProducto(UUID.randomUUID());
        linea.setIdProducto(cafecito);
        linea.setPrecio(new BigDecimal("100.00"));
        DescuentoProducto dp = descuentoDe(cafecito);
        when(ordenProductoRepository.findByOrden(any()))
                .thenReturn(new ArrayList<>(List.of(linea)));
        when(descuentoProductoRepository.findAll())
                .thenReturn(new ArrayList<>(List.of(dp)));
        when(descuentoProductoRepository.findById(dp.getIdDescuentoProducto()))
                .thenReturn(dp);

        conPrimeFaces(() -> {
            UUID idOrden = UUID.randomUUID();
            when(ordenRepository.findById(idOrden)).thenReturn(ordenDeHoy(idOrden));
            modelo.btnAbrirDescuentos(idOrden);
            contexto.mensajes.clear();
            modelo.setDescuentoAplicableSeleccionado(dp.getIdDescuentoProducto());

            modelo.btnAplicarDescuentoHandler();

            assertEquals(new BigDecimal("80.00"), linea.getPrecio());
            assertEquals("Platinum (20)%", linea.getObservaciones());
            verify(ordenProductoRepository, times(1)).modificar(linea);
            assertEquals("Registro actualizado con éxito", unicoMensaje().getSummary());
            assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
        });
    }

    @Test
    public void testGetRegistroByIdYGetIdByRegistro() {
        UUID id = UUID.randomUUID();
        OrdenProducto linea = new OrdenProducto(id);
        when(ordenProductoRepository.findById(id)).thenReturn(linea);

        assertSame(linea, modelo.getRegistroById(id));
        assertEquals(id, modelo.getIdByRegistro(linea));
        assertNull(modelo.getIdByRegistro(null));
    }
}
