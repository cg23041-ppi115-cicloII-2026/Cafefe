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
import sv.edu.ues.ppi115.cafefe.control.CaracteristicaRepository;
import sv.edu.ues.ppi115.cafefe.control.ProductoCaracteristicaRepository;
import sv.edu.ues.ppi115.cafefe.control.ProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.TipoCaracteristicaRepository;
import sv.edu.ues.ppi115.cafefe.entity.Caracteristica;
import sv.edu.ues.ppi115.cafefe.entity.Producto;
import sv.edu.ues.ppi115.cafefe.entity.ProductoCaracteristica;
import sv.edu.ues.ppi115.cafefe.entity.TipoCaracteristica;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoCaracteristicaModelTest {

    @Mock
    private ProductoCaracteristicaRepository productoCaracteristicaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CaracteristicaRepository caracteristicaRepository;

    @Mock
    private TipoCaracteristicaRepository tipoCaracteristicaRepository;

    @InjectMocks
    private ProductoCaracteristicaModel modelo;

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

    private void elegirProductoYCaracteristica() {
        modelo.setProductoSeleccionado(UUID.randomUUID());
        modelo.setCaracteristicaSeleccionada(UUID.randomUUID());
    }

    private void prepararTipoConRegex(String expresion) {
        TipoCaracteristica tipo = new TipoCaracteristica();
        tipo.setNombre("Tamaño");
        tipo.setExpresionRegular(expresion);
        Caracteristica car = new Caracteristica();
        car.setIdTipoCaracteristica(tipo);
        when(caracteristicaRepository.findById(modelo.getCaracteristicaSeleccionada())).thenReturn(car);
    }

    private FacesMessage unicoMensaje() {
        assertEquals(1, contexto.mensajes.size());
        return contexto.mensajes.get(0);
    }

    // ---------------------------------------------------------------
    // Guards de btnGuardarHandler (validaciones propias del modelo)
    // ---------------------------------------------------------------

    @Test
    public void testGuardarSinProductoNiCaracteristicaAvisaYNoGuarda() {
        modelo.limpiar();

        modelo.btnGuardarHandler();

        assertEquals("Debe elegir el producto y la caracteristica", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(productoCaracteristicaRepository, never()).crear(any());
        verify(productoCaracteristicaRepository, never()).modificar(any());
    }

    @Test
    public void testGuardarSinRegexDefinidaDelTipoAvisaYNoGuarda() {
        modelo.limpiar();
        elegirProductoYCaracteristica();
        modelo.getRegistro().setValor("grande");
        prepararTipoConRegex(null);

        modelo.btnGuardarHandler();

        assertEquals("El tipo de característica 'Tamaño' no tiene expresión regular definida",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(productoCaracteristicaRepository, never()).crear(any());
    }

    @Test
    public void testGuardarValorNoCumpleRegexAvisaYNoGuarda() {
        modelo.limpiar();
        elegirProductoYCaracteristica();
        modelo.getRegistro().setValor("onza");
        prepararTipoConRegex("\\d+");

        modelo.btnGuardarHandler();

        assertEquals("El valor no cumple la validación de Tamaño: \\d+",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(productoCaracteristicaRepository, never()).crear(any());
    }

    @Test
    public void testGuardarRegexDelTipoInvalidaAvisaYNoGuarda() {
        modelo.limpiar();
        elegirProductoYCaracteristica();
        modelo.getRegistro().setValor("8");
        prepararTipoConRegex("[");

        modelo.btnGuardarHandler();

        assertEquals("La expresión regular del tipo 'Tamaño' no es válida: [",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(productoCaracteristicaRepository, never()).crear(any());
    }

    @Test
    public void testGuardarValorCumpleRegexCreaElRegistro() {
        modelo.limpiar();
        elegirProductoYCaracteristica();
        ProductoCaracteristica t = modelo.getRegistro();
        t.setValor("8");
        prepararTipoConRegex("\\d+");
        when(productoRepository.findById(modelo.getProductoSeleccionado())).thenReturn(new Producto());

        modelo.btnGuardarHandler();

        verify(productoCaracteristicaRepository, times(1)).crear(t);
        verify(productoCaracteristicaRepository, never()).modificar(any());
        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testGuardarValorVacioOmiteValidacionRegexYCrea() {
        modelo.limpiar();
        elegirProductoYCaracteristica();
        ProductoCaracteristica t = modelo.getRegistro();

        modelo.btnGuardarHandler();

        verify(productoCaracteristicaRepository, times(1)).crear(t);
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
    }

    @Test
    public void testGuardarRegistroExistenteEnEstadoCrearUsaModificar() {
        modelo.limpiar();
        ProductoCaracteristica existente = new ProductoCaracteristica(UUID.randomUUID());
        modelo.setRegistro(existente);
        elegirProductoYCaracteristica();
        when(productoCaracteristicaRepository.findById(existente.getIdProductoCaracteristica()))
                .thenReturn(existente);

        modelo.btnGuardarHandler();

        verify(productoCaracteristicaRepository, never()).crear(any());
        verify(productoCaracteristicaRepository, times(1)).modificar(existente);
        assertEquals("Registro actualizado con éxito", unicoMensaje().getSummary());
    }

    @Test
    public void testGuardarCuandoElDaoFallaAvisaSinRomper() {
        modelo.limpiar();
        elegirProductoYCaracteristica();
        doThrow(new RuntimeException("boom")).when(productoCaracteristicaRepository).crear(any());

        modelo.btnGuardarHandler();

        assertEquals("No se pudo guardar: verifique los datos e intente de nuevo",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    // ---------------------------------------------------------------
    // Editar y Eliminar
    // ---------------------------------------------------------------

    @Test
    public void testEditarPrecargaLosDesplegablesDelRegistro() {
        UUID idProducto = UUID.randomUUID();
        UUID idCaracteristica = UUID.randomUUID();
        ProductoCaracteristica pc = new ProductoCaracteristica(UUID.randomUUID());
        pc.setIdProducto(new Producto(idProducto));
        pc.setIdCaracteristica(new Caracteristica(idCaracteristica));
        modelo.setSeleccion(pc);

        modelo.btnEditarHandler();

        assertEquals(idProducto, modelo.getProductoSeleccionado());
        assertEquals(idCaracteristica, modelo.getCaracteristicaSeleccionada());
        assertTrue(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.MODIFICAR, modelo.getEstado());
    }

    @Test
    public void testEliminarConRestricionLimpiaYAvisa() {
        ProductoCaracteristica pc = new ProductoCaracteristica(UUID.randomUUID());
        modelo.setRegistro(pc);
        modelo.setFormVisible(true);
        doThrow(new RuntimeException("RESTRICT")).when(productoCaracteristicaRepository).eliminar(pc);

        modelo.btnEliminarHandler();

        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertNotSame(pc, modelo.getRegistro());
        assertEquals("No se pudo eliminar la característica del producto",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    // ---------------------------------------------------------------
    // Cascada de la ventana emergente Tipo > Caracteristica
    // ---------------------------------------------------------------

    @Test
    public void testSetTipoSeleccionadoReiniciaLaCaracteristica() {
        modelo.setCaracteristicaSeleccionada(UUID.randomUUID());

        modelo.setTipoSeleccionado(UUID.randomUUID());

        assertNull(modelo.getCaracteristicaSeleccionada());
    }

    @Test
    public void testListaCaracteristicasSoloMuestraLasDelTipoElegido() {
        UUID idTipo = UUID.randomUUID();
        TipoCaracteristica tipo = new TipoCaracteristica(idTipo);
        Caracteristica activa = new Caracteristica();
        activa.setNombre("Onzas");
        activa.setIdTipoCaracteristica(tipo);
        activa.setActivo(true);
        Caracteristica inactiva = new Caracteristica();
        inactiva.setNombre("Litros");
        inactiva.setIdTipoCaracteristica(tipo);
        inactiva.setActivo(false);
        Caracteristica deOtroTipo = new Caracteristica();
        deOtroTipo.setNombre("Color");
        deOtroTipo.setIdTipoCaracteristica(new TipoCaracteristica(UUID.randomUUID()));
        when(tipoCaracteristicaRepository.findById(idTipo)).thenReturn(tipo);
        when(caracteristicaRepository.findAll())
                .thenReturn(new ArrayList<>(List.of(activa, inactiva, deOtroTipo)));

        modelo.setTipoSeleccionado(idTipo);
        List<SelectItem> lista = modelo.getListaCaracteristicas();

        assertEquals(2, lista.size());
        assertFalse(lista.get(0).isDisabled());
        assertTrue(lista.get(1).isDisabled());
    }

    @Test
    public void testListasMarcanInactivosEnGris() {
        Producto activo = new Producto();
        activo.setNombre("Café");
        activo.setActivo(true);
        Producto inactivo = new Producto();
        inactivo.setNombre("Té");
        inactivo.setActivo(false);
        TipoCaracteristica tipoActivo = new TipoCaracteristica();
        tipoActivo.setNombre("Tamaño");
        tipoActivo.setActivo(true);
        TipoCaracteristica tipoInactivo = new TipoCaracteristica();
        tipoInactivo.setNombre("Volumen");
        tipoInactivo.setActivo(false);
        when(productoRepository.findAll()).thenReturn(new ArrayList<>(List.of(activo, inactivo)));
        when(tipoCaracteristicaRepository.findAll())
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

    // ---------------------------------------------------------------
    // Utilidades de vista
    // ---------------------------------------------------------------

    @Test
    public void testGetResumenConcatenaTipoValorYNombre() {
        ProductoCaracteristica pc = new ProductoCaracteristica();
        TipoCaracteristica tipo = new TipoCaracteristica();
        tipo.setNombre("Volumen");
        Caracteristica car = new Caracteristica();
        car.setNombre("Onzas");
        car.setIdTipoCaracteristica(tipo);
        pc.setIdCaracteristica(car);
        pc.setValor("8.00");

        assertEquals("Volumen: 8.00 Onzas", modelo.getResumen(pc));
        assertEquals("", modelo.getResumen(null));
    }

    @Test
    public void testOnTabChangeVaciaLasListas() {
        when(productoRepository.findAll()).thenReturn(new ArrayList<>());

        modelo.getListaProductos();
        modelo.onTabChange(null);
        modelo.getListaProductos();

        verify(productoRepository, times(2)).findAll();
    }

    // ---------------------------------------------------------------
    // Filtros de la tabla
    // ---------------------------------------------------------------

    @Test
    public void testBuscarPorProductoIdInvalidoRecargaTodo() {
        modelo.setFiltroProducto("no-es-uuid");

        modelo.buscarPorProducto();

        verify(productoCaracteristicaRepository, never()).findByProducto(any());
        verify(productoCaracteristicaRepository, times(1)).findAll();
    }

    @Test
    public void testBuscarPorProductoIdValidoUsaElRepositorio() {
        UUID id = UUID.randomUUID();
        modelo.setFiltroProducto(id.toString());

        modelo.buscarPorProducto();

        verify(productoCaracteristicaRepository, times(1)).findByProducto(id);
    }
}
