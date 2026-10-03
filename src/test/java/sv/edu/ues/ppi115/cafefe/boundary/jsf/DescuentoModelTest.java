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
import sv.edu.ues.ppi115.cafefe.control.DescuentoRepository;
import sv.edu.ues.ppi115.cafefe.control.TipoDescuentoRepository;
import sv.edu.ues.ppi115.cafefe.entity.Descuento;
import sv.edu.ues.ppi115.cafefe.entity.TipoDescuento;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DescuentoModelTest {

    @Mock
    private DescuentoRepository descuentoRepository;

    @Mock
    private TipoDescuentoRepository tipoDescuentoRepository;

    @InjectMocks
    private DescuentoModel modelo;

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

    private TipoDescuento tipo(UUID id, String nombre, boolean activo) {
        TipoDescuento t = new TipoDescuento(id);
        t.setNombre(nombre);
        t.setActivo(activo);
        return t;
    }

    @Test
    public void testInstanciarRegistroConIdNuevoyObservacionesVacias() {
        Descuento r = modelo.instanciarRegistro();

        assertNotNull(r.getIdDescuento());
        assertEquals("", r.getObservaciones());
    }

    @Test
    public void testGuardarSinNombreAvisaYNoGuarda() {
        modelo.limpiar();

        modelo.btnGuardarHandler();

        assertEquals("El descuento necesita nombre", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(descuentoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarSinTipoAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("Happy Hour");

        modelo.btnGuardarHandler();

        assertEquals("Seleccione un tipo de descuento", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(descuentoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarConFechasInvertidasAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("Happy Hour");
        modelo.setTipoSeleccionado(UUID.randomUUID());
        modelo.getRegistro().setFechaDesde(new Date(3000000000000L));
        modelo.getRegistro().setFechaHasta(new Date(1000000000000L));

        modelo.btnGuardarHandler();

        assertEquals("La fecha desde no puede ser mayor que la fecha hasta",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(descuentoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarValidoAplicaElTipoYCreaElRegistro() {
        modelo.limpiar();
        Descuento r = modelo.getRegistro();
        r.setNombre("Happy Hour");
        UUID idTipo = UUID.randomUUID();
        modelo.setTipoSeleccionado(idTipo);
        TipoDescuento tipo = tipo(idTipo, "Por fecha", true);
        when(tipoDescuentoRepository.findById(idTipo)).thenReturn(tipo);

        modelo.btnGuardarHandler();

        verify(descuentoRepository, times(1)).crear(r);
        assertSame(tipo, r.getIdTipoDescuento());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
        assertFalse(modelo.isFormVisible());
    }

    @Test
    public void testGuardarCuandoElDaoFallaAvisaAlUsuario() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("Happy Hour");
        UUID idTipo = UUID.randomUUID();
        modelo.setTipoSeleccionado(idTipo);
        when(tipoDescuentoRepository.findById(idTipo)).thenReturn(tipo(idTipo, "Por fecha", true));
        doThrow(new RuntimeException("boom")).when(descuentoRepository).crear(any());

        modelo.btnGuardarHandler();

        assertEquals("No se pudo guardar: verifique los datos e intente de nuevo",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testLimpiarReiniciaElTipoSeleccionado() {
        modelo.setTipoSeleccionado(UUID.randomUUID());

        modelo.limpiar();

        assertNull(modelo.getTipoSeleccionado());
    }

    @Test
    public void testEditarPrecargaElTipoEnElCombo() {
        UUID idTipo = UUID.randomUUID();
        UUID idDescuento = UUID.randomUUID();
        Descuento d = new Descuento(idDescuento);
        d.setNombre("Veraniego");
        d.setIdTipoDescuento(tipo(idTipo, "Por fecha", true));
        modelo.setSeleccion(d);

        modelo.btnEditarHandler();

        assertEquals(idTipo, modelo.getTipoSeleccionado());
        assertTrue(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.MODIFICAR, modelo.getEstado());
        assertSame(d, modelo.getRegistro());
    }

    @Test
    public void testListaTiposMarcaInactivosYSeCachea() {
        UUID idActivo = UUID.randomUUID();
        UUID idInactivo = UUID.randomUUID();
        when(tipoDescuentoRepository.findAll()).thenReturn(new ArrayList<>(
                List.of(tipo(idActivo, "Global", true), tipo(idInactivo, "Viejo", false))));

        List<SelectItem> primera = modelo.getListaTipos();
        List<SelectItem> segunda = modelo.getListaTipos();

        assertEquals(2, primera.size());
        assertEquals("Global", primera.get(0).getLabel());
        assertFalse(primera.get(0).isDisabled());
        assertEquals(idActivo, primera.get(0).getValue());
        assertEquals("Viejo", segunda.get(1).getLabel());
        assertTrue(segunda.get(1).isDisabled());
        verify(tipoDescuentoRepository, times(1)).findAll();
    }

    @Test
    public void testEliminarConRestriccionLimpiaYAvisa() {
        Descuento d = new Descuento(UUID.randomUUID());
        modelo.setRegistro(d);
        modelo.setFormVisible(true);
        doThrow(new RuntimeException("RESTRICT")).when(descuentoRepository).eliminar(d);

        modelo.btnEliminarHandler();

        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertNotSame(d, modelo.getRegistro());
        assertEquals("No se puede eliminar: existen descuentos de este tipo",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testGetRegistroByIdYGetIdByRegistro() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Descuento d1 = new Descuento(id1);
        Descuento d2 = new Descuento(id2);
        modelo.setRegistros(new ArrayList<>(List.of(d1, d2)));

        assertSame(d1, modelo.getRegistroById(id1));
        assertSame(d2, modelo.getRegistroById(id2));
        assertNull(modelo.getRegistroById(UUID.randomUUID()));
        assertNull(modelo.getRegistroById(null));
        modelo.setRegistros(null);
        assertNull(modelo.getRegistroById(id1));

        assertEquals(id1, modelo.getIdByRegistro(d1));
        assertNull(modelo.getIdByRegistro(null));
    }
}
