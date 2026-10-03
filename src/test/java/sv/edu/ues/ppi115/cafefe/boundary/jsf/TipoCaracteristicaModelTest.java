package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.control.TipoCaracteristicaRepository;
import sv.edu.ues.ppi115.cafefe.entity.TipoCaracteristica;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoCaracteristicaModelTest {

    @Mock
    private TipoCaracteristicaRepository tipoCaracteristicaRepository;

    @InjectMocks
    private TipoCaracteristicaModel modelo;

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
    public void testInstanciarRegistroActivoYObservaciones() {
        TipoCaracteristica r = modelo.instanciarRegistro();

        assertNotNull(r.getIdTipoCaracteristica());
        assertEquals(Boolean.TRUE, r.getActivo());
        assertEquals("", r.getObservaciones());
    }

    @Test
    public void testGetRegistroByIdBuscaEnLaListaCargada() {
        UUID id1 = UUID.randomUUID();
        TipoCaracteristica t1 = new TipoCaracteristica(id1);
        TipoCaracteristica t2 = new TipoCaracteristica(UUID.randomUUID());
        modelo.setRegistros(new ArrayList<>(List.of(t1, t2)));

        assertSame(t1, modelo.getRegistroById(id1));
        assertNull(modelo.getRegistroById(UUID.randomUUID()));
        assertNull(modelo.getRegistroById(null));

        modelo.setRegistros(null);
        assertNull(modelo.getRegistroById(id1));
    }

    @Test
    public void testGuardarSinNombreAvisaYNoGuarda() {
        modelo.limpiar();

        modelo.btnGuardarHandler();

        assertEquals("Debe ingresar el nombre", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(tipoCaracteristicaRepository, never()).crear(any());
        verify(tipoCaracteristicaRepository, never()).modificar(any());
    }

    @Test
    public void testGuardarNombreEnBlancoAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("  ");

        modelo.btnGuardarHandler();

        assertEquals("Debe ingresar el nombre", unicoMensaje().getSummary());
        verify(tipoCaracteristicaRepository, never()).crear(any());
    }

    @Test
    public void testGuardarConNombreCreaElRegistro() {
        modelo.limpiar();
        TipoCaracteristica t = modelo.getRegistro();
        t.setNombre("Tamaño");

        modelo.btnGuardarHandler();

        verify(tipoCaracteristicaRepository, times(1)).crear(t);
        verify(tipoCaracteristicaRepository, never()).modificar(any());
        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testGuardarCuandoElDaoFallaPropagaLaExcepcion() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("Tamaño");
        doThrow(new RuntimeException("boom")).when(tipoCaracteristicaRepository).crear(any());

        assertThrows(RuntimeException.class, () -> modelo.btnGuardarHandler());

        assertTrue(contexto.mensajes.isEmpty());
    }

    @Test
    public void testEliminarConRestricionLimpiaYAvisa() {
        TipoCaracteristica t = new TipoCaracteristica(UUID.randomUUID());
        modelo.setRegistro(t);
        modelo.setFormVisible(true);
        doThrow(new RuntimeException("RESTRICT")).when(tipoCaracteristicaRepository).eliminar(t);

        modelo.btnEliminarHandler();

        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertNotSame(t, modelo.getRegistro());
        assertEquals("No se puede eliminar: existen características de este tipo",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }
}
