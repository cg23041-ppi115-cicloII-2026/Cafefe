package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.model.SelectItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.event.TabChangeEvent;
import sv.edu.ues.ppi115.cafefe.control.DescuentoProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.DescuentoRepository;
import sv.edu.ues.ppi115.cafefe.control.ProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.TipoDescuentoRepository;
import sv.edu.ues.ppi115.cafefe.entity.Descuento;
import sv.edu.ues.ppi115.cafefe.entity.DescuentoProducto;
import sv.edu.ues.ppi115.cafefe.entity.Producto;
import sv.edu.ues.ppi115.cafefe.entity.TipoDescuento;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DescuentoProductoModelTest {

    @Mock
    private DescuentoProductoRepository descuentoProductoRepository;

    @Mock
    private DescuentoRepository descuentoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private TipoDescuentoRepository tipoDescuentoRepository;

    @InjectMocks
    private DescuentoProductoModel modelo;

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

    private Date hace(int dias) {
        return Date.from(LocalDate.now().minusDays(dias)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private TipoDescuento tipo(String nombre, boolean activo, Integer maximo) {
        TipoDescuento t = new TipoDescuento(UUID.randomUUID());
        t.setNombre(nombre);
        t.setActivo(activo);
        t.setDescuentoMaximo(maximo);
        return t;
    }

    private Descuento descuentoDe(TipoDescuento tipo) {
        Descuento d = new Descuento(UUID.randomUUID());
        d.setNombre("Happy");
        d.setIdTipoDescuento(tipo);
        return d;
    }

    private void seleccionarDescuentoYProducto() {
        modelo.limpiar();
        modelo.setDescuentoSeleccionado(UUID.randomUUID());
        modelo.setProductoSeleccionado(UUID.randomUUID());
    }

    private void conDescuento(Descuento descuento) {
        when(descuentoRepository.findById(modelo.getDescuentoSeleccionado()))
                .thenReturn(descuento);
    }

    @Test
    public void testInstanciarRegistroConIdNuevoyValoresPorDefecto() {
        DescuentoProducto r = modelo.instanciarRegistro();

        assertNotNull(r.getIdDescuentoProducto());
        assertEquals(0, r.getValor());
        assertEquals("", r.getObservaciones());
    }

    @Test
    public void testGuardarSinDescuentoAvisaYNoGuarda() {
        modelo.limpiar();

        modelo.btnGuardarHandler();

        assertEquals("Seleccione un descuento (tipo de descuento y descuento)",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(descuentoProductoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarSinProductoAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.setDescuentoSeleccionado(UUID.randomUUID());

        modelo.btnGuardarHandler();

        assertEquals("Seleccione un producto", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(descuentoProductoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarConTipoInactivoAvisaYNoGuarda() {
        seleccionarDescuentoYProducto();
        conDescuento(descuentoDe(tipo("Por fecha", false, null)));

        modelo.btnGuardarHandler();

        assertEquals("El tipo de descuento 'Por fecha' está inactivo",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(descuentoProductoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarConValorNuloAvisaYNoGuarda() {
        seleccionarDescuentoYProducto();
        conDescuento(descuentoDe(tipo("Por fecha", true, 50)));
        modelo.getRegistro().setValor(null);

        modelo.btnGuardarHandler();

        assertEquals("El valor no es válido. El valor no puede ser nulo",
                unicoMensaje().getSummary());
        verify(descuentoProductoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarConValorCeroAvisaYNoGuarda() {
        seleccionarDescuentoYProducto();
        conDescuento(descuentoDe(tipo("Por fecha", true, 50)));
        modelo.getRegistro().setValor(0);

        modelo.btnGuardarHandler();

        assertEquals("El valor no es válido. El valor debe ser mayor a 0",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(descuentoProductoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarConValorSobreElMaximoAvisaYNoGuarda() {
        seleccionarDescuentoYProducto();
        conDescuento(descuentoDe(tipo("Por fecha", true, 30)));
        modelo.getRegistro().setValor(40);

        modelo.btnGuardarHandler();

        assertEquals("El valor no es válido. El valor no cumple la validación de "
                + "Por fecha: debe ser menor o igual a 30", unicoMensaje().getSummary());
        verify(descuentoProductoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarSinFechaInicialAvisaYNoGuarda() {
        seleccionarDescuentoYProducto();
        conDescuento(descuentoDe(tipo("Por fecha", true, null)));
        modelo.getRegistro().setValor(20);

        modelo.btnGuardarHandler();

        assertEquals("La fecha inicial no es válida. La fecha no puede quedar vacía",
                unicoMensaje().getSummary());
        verify(descuentoProductoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarSinFechaFinalGuardaConVigenciaAbierta() {
        seleccionarDescuentoYProducto();
        conDescuento(descuentoDe(tipo("Por fecha", true, null)));
        Producto producto = new Producto(UUID.randomUUID());
        when(productoRepository.findById(modelo.getProductoSeleccionado()))
                .thenReturn(producto);
        DescuentoProducto r = modelo.getRegistro();
        r.setValor(20);
        r.setFechaDesde(hace(1));
        r.setFechaHasta(null);

        modelo.btnGuardarHandler();

        verify(descuentoProductoRepository, times(1)).crear(r);
        assertNull(r.getFechaHasta());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
        assertFalse(modelo.isFormVisible());
    }

    @Test
    public void testGuardarSinFechaFinalNoValidaElRangoDelDescuento() {
        seleccionarDescuentoYProducto();
        Descuento descuento = descuentoDe(tipo("Por fecha", true, null));
        descuento.setFechaHasta(hace(5));
        conDescuento(descuento);
        Producto producto = new Producto(UUID.randomUUID());
        when(productoRepository.findById(modelo.getProductoSeleccionado()))
                .thenReturn(producto);
        DescuentoProducto r = modelo.getRegistro();
        r.setValor(20);
        r.setFechaDesde(hace(3));
        r.setFechaHasta(null);

        modelo.btnGuardarHandler();

        verify(descuentoProductoRepository, times(1)).crear(r);
        assertNull(r.getFechaHasta());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
    }

    @Test
    public void testGuardarConFechasInvertidasAvisaYNoGuarda() {
        seleccionarDescuentoYProducto();
        conDescuento(descuentoDe(tipo("Por fecha", true, null)));
        modelo.getRegistro().setValor(20);
        modelo.getRegistro().setFechaDesde(hace(1));
        modelo.getRegistro().setFechaHasta(hace(5));

        modelo.btnGuardarHandler();

        assertEquals("La fecha final no es válida. La fecha de finalización "
                + "no puede ser anterior a la fecha de inicio", unicoMensaje().getSummary());
        verify(descuentoProductoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarAntesDeLaFechaDelDescuentoAvisaYNoGuarda() {
        seleccionarDescuentoYProducto();
        Descuento descuento = descuentoDe(tipo("Por fecha", true, null));
        descuento.setFechaDesde(hace(10));
        conDescuento(descuento);
        modelo.getRegistro().setValor(20);
        modelo.getRegistro().setFechaDesde(hace(20));
        modelo.getRegistro().setFechaHasta(hace(1));

        modelo.btnGuardarHandler();

        assertEquals("La fecha inicial no es válida. La fecha de inicio debe ser "
                + "después de la fecha inicial aplicable del descuento",
                unicoMensaje().getSummary());
        verify(descuentoProductoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarDespuesDeLaFechaDelDescuentoAvisaYNoGuarda() {
        seleccionarDescuentoYProducto();
        Descuento descuento = descuentoDe(tipo("Por fecha", true, null));
        descuento.setFechaHasta(hace(1));
        conDescuento(descuento);
        modelo.getRegistro().setValor(20);
        modelo.getRegistro().setFechaDesde(hace(3));
        modelo.getRegistro().setFechaHasta(hace(0));

        modelo.btnGuardarHandler();

        assertEquals("La fecha final no es válida. La fecha de finalización no puede "
                + "ser posterior a la de fin del descuento", unicoMensaje().getSummary());
        verify(descuentoProductoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarValidoAplicaLasDosFechasYCreaElRegistro() {
        seleccionarDescuentoYProducto();
        Descuento descuento = descuentoDe(tipo("Por fecha", true, null));
        conDescuento(descuento);
        Producto producto = new Producto(UUID.randomUUID());
        when(productoRepository.findById(modelo.getProductoSeleccionado()))
                .thenReturn(producto);
        DescuentoProducto r = modelo.getRegistro();
        r.setValor(15);
        r.setFechaDesde(hace(1));
        r.setFechaHasta(hace(0));

        modelo.btnGuardarHandler();

        verify(descuentoProductoRepository, times(1)).crear(r);
        assertSame(descuento, r.getIdDescuento());
        assertSame(producto, r.getIdProducto());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
        assertFalse(modelo.isFormVisible());
    }

    @Test
    public void testLimpiarReiniciaLasTresSelecciones() {
        modelo.setTipoSeleccionado(UUID.randomUUID());
        modelo.setDescuentoSeleccionado(UUID.randomUUID());
        modelo.setProductoSeleccionado(UUID.randomUUID());

        modelo.limpiar();

        assertNull(modelo.getTipoSeleccionado());
        assertNull(modelo.getDescuentoSeleccionado());
        assertNull(modelo.getProductoSeleccionado());
    }

    @Test
    public void testGetListaDescuentosSinTipoVaciaSinConsultar() {
        modelo.limpiar();

        assertTrue(modelo.getListaDescuentos().isEmpty());

        verifyNoInteractions(descuentoRepository, tipoDescuentoRepository);
    }

    @Test
    public void testGetListaDescuentosFiltraPorElTipoElegido() {
        UUID idTipo1 = UUID.randomUUID();
        UUID idTipo2 = UUID.randomUUID();
        TipoDescuento tipo1 = tipo("Por fecha", true, null);
        TipoDescuento tipo2 = tipo("Global", true, null);
        Descuento d1 = new Descuento(UUID.randomUUID());
        d1.setNombre("Platinum");
        d1.setIdTipoDescuento(tipo1);
        Descuento d2 = new Descuento(UUID.randomUUID());
        d2.setNombre("Viejo");
        d2.setIdTipoDescuento(tipo2);
        when(tipoDescuentoRepository.findById(idTipo1)).thenReturn(tipo1);
        when(descuentoRepository.findAll()).thenReturn(new ArrayList<>(List.of(d1, d2)));
        modelo.setTipoSeleccionado(idTipo1);

        List<SelectItem> descuentos = modelo.getListaDescuentos();

        assertEquals(1, descuentos.size());
        assertEquals("Platinum", descuentos.get(0).getLabel());
        assertEquals(d1.getIdDescuento(), descuentos.get(0).getValue());
    }

    @Test
    public void testSetTipoSeleccionadoReiniciaElDescuentoElegido() {
        UUID idTipo1 = UUID.randomUUID();
        UUID idTipo2 = UUID.randomUUID();
        TipoDescuento tipo1 = tipo("Por fecha", true, null);
        Descuento d1 = new Descuento(UUID.randomUUID());
        d1.setNombre("Platinum");
        d1.setIdTipoDescuento(tipo1);
        when(tipoDescuentoRepository.findById(idTipo1)).thenReturn(tipo1);
        when(descuentoRepository.findAll()).thenReturn(new ArrayList<>(List.of(d1)));
        modelo.setTipoSeleccionado(idTipo1);
        modelo.getListaDescuentos();
        modelo.setDescuentoSeleccionado(d1.getIdDescuento());

        modelo.setTipoSeleccionado(idTipo2);

        assertNull(modelo.getDescuentoSeleccionado());
    }

    @Test
    public void testGetListaTiposMarcaInactivosYSeCachea() {
        TipoDescuento t1 = tipo("Por fecha", true, 50);
        TipoDescuento t2 = tipo("Viejo", false, 10);
        when(tipoDescuentoRepository.findAll()).thenReturn(new ArrayList<>(List.of(t1, t2)));

        List<SelectItem> primera = modelo.getListaTipos();
        List<SelectItem> segunda = modelo.getListaTipos();

        assertEquals(2, primera.size());
        assertEquals("Por fecha", primera.get(0).getLabel());
        assertFalse(primera.get(0).isDisabled());
        assertEquals(t1.getIdTipoDescuento(), primera.get(0).getValue());
        assertEquals("Viejo", segunda.get(1).getLabel());
        assertTrue(segunda.get(1).isDisabled());
        assertEquals(t2.getIdTipoDescuento(), segunda.get(1).getValue());
        verify(tipoDescuentoRepository, times(1)).findAll();
    }

    @Test
    public void testGetListaProductosMarcaInactivosYSeCachea() {
        UUID idActivo = UUID.randomUUID();
        UUID idInactivo = UUID.randomUUID();
        Producto activo = new Producto(idActivo);
        activo.setNombre("Café");
        activo.setActivo(true);
        Producto inactivo = new Producto(idInactivo);
        inactivo.setNombre("Té");
        inactivo.setActivo(false);
        when(productoRepository.findAll()).thenReturn(new ArrayList<>(List.of(activo, inactivo)));

        List<SelectItem> primera = modelo.getListaProductos();
        List<SelectItem> segunda = modelo.getListaProductos();

        assertEquals(2, primera.size());
        assertEquals("Café", primera.get(0).getLabel());
        assertFalse(primera.get(0).isDisabled());
        assertEquals(idActivo, primera.get(0).getValue());
        assertEquals("Té", segunda.get(1).getLabel());
        assertTrue(segunda.get(1).isDisabled());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    public void testGetDescuentoElegidoMuestraNombreONinguno() {
        modelo.limpiar();
        assertEquals("(ninguno)", modelo.getDescuentoElegido());

        UUID idDescuento = UUID.randomUUID();
        modelo.setDescuentoSeleccionado(idDescuento);
        Descuento d = new Descuento(idDescuento);
        d.setNombre("20% en bebidas");
        when(descuentoRepository.findById(idDescuento)).thenReturn(d);
        assertEquals("20% en bebidas", modelo.getDescuentoElegido());

        modelo.setDescuentoSeleccionado(UUID.randomUUID());
        assertEquals("(ninguno)", modelo.getDescuentoElegido());
    }

    @Test
    public void testEditarPrecaguaLosTresCombos() {
        UUID idTipo = UUID.randomUUID();
        UUID idDescuento = UUID.randomUUID();
        UUID idProducto = UUID.randomUUID();
        TipoDescuento t = new TipoDescuento(idTipo);
        t.setNombre("Por fecha");
        t.setActivo(true);
        Descuento d = new Descuento(idDescuento);
        d.setNombre("Platinum");
        d.setIdTipoDescuento(t);
        Producto p = new Producto(idProducto);
        DescuentoProducto dp = new DescuentoProducto(UUID.randomUUID());
        dp.setIdDescuento(d);
        dp.setIdProducto(p);
        modelo.setSeleccion(dp);

        modelo.btnEditarHandler();

        assertEquals(idTipo, modelo.getTipoSeleccionado());
        assertEquals(idDescuento, modelo.getDescuentoSeleccionado());
        assertEquals(idProducto, modelo.getProductoSeleccionado());
        assertTrue(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.MODIFICAR, modelo.getEstado());
        assertSame(dp, modelo.getRegistro());
    }

    @Test
    public void testOnTabChangeVaciaLasListasParaQueSeRecarguen() {
        when(tipoDescuentoRepository.findAll()).thenReturn(new ArrayList<>(
                List.of(tipo("Por fecha", true, null))));
        Producto producto = new Producto(UUID.randomUUID());
        producto.setNombre("Café");
        when(productoRepository.findAll()).thenReturn(new ArrayList<>(List.of(producto)));
        modelo.getListaTipos();
        modelo.getListaProductos();

        modelo.onTabChange(mock(TabChangeEvent.class));
        modelo.getListaTipos();
        modelo.getListaProductos();

        verify(tipoDescuentoRepository, times(2)).findAll();
        verify(productoRepository, times(2)).findAll();
    }

    @Test
    public void testEliminarConErrorLimpiaYAvisa() {
        DescuentoProducto dp = new DescuentoProducto(UUID.randomUUID());
        modelo.setRegistro(dp);
        modelo.setFormVisible(true);
        doThrow(new RuntimeException("boom")).when(descuentoProductoRepository).eliminar(dp);

        modelo.btnEliminarHandler();

        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertNotSame(dp, modelo.getRegistro());
        assertEquals("No se pudo eliminar el descuento del producto",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }
}
