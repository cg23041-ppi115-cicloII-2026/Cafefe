package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.control.TipoDescuentoRepository;
import sv.edu.ues.ppi115.cafefe.entity.TipoDescuento;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoDescuentoModelTest {

    @Mock
    private TipoDescuentoRepository tipoDescuentoRepository;

    @InjectMocks
    private TipoDescuentoModel modelo;

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
    public void testInstanciarRegistroConIdNuevoyValoresPorDefecto() {
        TipoDescuento r = modelo.instanciarRegistro();

        assertNotNull(r.getIdTipoDescuento());
        assertEquals(Boolean.TRUE, r.getActivo());
        assertEquals("", r.getObservaciones());
        assertEquals(0, r.getDescuentoMaximo());
    }

    @Test
    public void testGuardarSinNombreAvisaYNoGuarda() {
        modelo.limpiar();

        modelo.btnGuardarHandler();

        assertEquals("Debe ingresar el nombre", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(tipoDescuentoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarSinMaximoAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("Vip");
        modelo.getRegistro().setDescuentoMaximo(null);

        modelo.btnGuardarHandler();

        assertEquals("Indique el descuento maximo", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(tipoDescuentoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarConMaximoMayorACienAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("Vip");
        modelo.getRegistro().setDescuentoMaximo(150);

        modelo.btnGuardarHandler();

        assertEquals("El descuento máximo debe estar entre 0 y 100", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(tipoDescuentoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarConMaximoNegativoAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("Vip");
        modelo.getRegistro().setDescuentoMaximo(-10);

        modelo.btnGuardarHandler();

        assertEquals("El descuento máximo debe estar entre 0 y 100", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(tipoDescuentoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarValidoCreaElRegistro() {
        modelo.limpiar();
        TipoDescuento r = modelo.getRegistro();
        r.setNombre("Vip");
        r.setDescuentoMaximo(25);

        modelo.btnGuardarHandler();

        verify(tipoDescuentoRepository, times(1)).crear(r);
        verify(tipoDescuentoRepository, never()).modificar(any());
        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testGuardarCuandoElDaoFallaAvisaAlUsuario() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("Vip");
        doThrow(new RuntimeException("boom")).when(tipoDescuentoRepository).crear(any());

        modelo.btnGuardarHandler();

        assertEquals("No se pudo guardar: verifique los datos e intente de nuevo",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(tipoDescuentoRepository, never()).modificar(any());
    }

    @Test
    public void testEliminarExitosoAvisa() {
        TipoDescuento t = new TipoDescuento(UUID.randomUUID());
        modelo.setRegistro(t);

        modelo.btnEliminarHandler();

        verify(tipoDescuentoRepository, times(1)).eliminar(t);
        assertEquals("Registro eliminado correctamente", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testEliminarConRestriccionLimpiaYAvisa() {
        TipoDescuento t = new TipoDescuento(UUID.randomUUID());
        modelo.setRegistro(t);
        modelo.setFormVisible(true);
        doThrow(new RuntimeException("RESTRICT")).when(tipoDescuentoRepository).eliminar(t);

        modelo.btnEliminarHandler();

        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertNotSame(t, modelo.getRegistro());
        assertEquals("No se puede eliminar: existen descuentos de este tipo",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testGetRegistroByIdBuscaPorIdYDevuelveNullSiNoEsta() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        TipoDescuento t1 = new TipoDescuento(id1);
        TipoDescuento t2 = new TipoDescuento(id2);
        modelo.setRegistros(new ArrayList<>(List.of(t1, t2)));

        assertSame(t1, modelo.getRegistroById(id1));
        assertSame(t2, modelo.getRegistroById(id2));
        assertNull(modelo.getRegistroById(UUID.randomUUID()));
        assertNull(modelo.getRegistroById(null));
        modelo.setRegistros(null);
        assertNull(modelo.getRegistroById(id1));
    }

    @Test
    public void testGetIdByRegistroDevuelveElIdONulo() {
        UUID id = UUID.randomUUID();

        assertEquals(id, modelo.getIdByRegistro(new TipoDescuento(id)));
        assertNull(modelo.getIdByRegistro(null));
    }
}
