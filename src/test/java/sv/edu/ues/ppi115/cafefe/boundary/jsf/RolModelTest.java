package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.control.RolRepository;
import sv.edu.ues.ppi115.cafefe.entity.Rol;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RolModelTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolModel modelo;

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
        Rol r = modelo.instanciarRegistro();

        assertNotNull(r.getIdRol());
        assertEquals(Boolean.TRUE, r.getActivo());
        assertEquals("", r.getObservaciones());
    }

    @Test
    public void testGetRegistroByIdBuscaEnLaListaCargada() {
        UUID id1 = UUID.randomUUID();
        Rol r1 = new Rol(id1);
        Rol r2 = new Rol(UUID.randomUUID());
        modelo.setRegistros(new ArrayList<>(List.of(r1, r2)));

        assertSame(r1, modelo.getRegistroById(id1));
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
        verify(rolRepository, never()).crear(any());
        verify(rolRepository, never()).modificar(any());
    }

    @Test
    public void testGuardarNombreEnBlancoAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("  ");

        modelo.btnGuardarHandler();

        assertEquals("Debe ingresar el nombre", unicoMensaje().getSummary());
        verify(rolRepository, never()).crear(any());
    }

    @Test
    public void testGuardarConNombreCreaElRegistro() {
        modelo.limpiar();
        Rol r = modelo.getRegistro();
        r.setNombre("Encargado");

        modelo.btnGuardarHandler();

        verify(rolRepository, times(1)).crear(r);
        verify(rolRepository, never()).modificar(any());
        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testGuardarCuandoElDaoFallaAvisaSinRomper() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("Encargado");
        doThrow(new RuntimeException("boom")).when(rolRepository).crear(any());

        modelo.btnGuardarHandler();

        assertEquals("No se pudo guardar: verifique los datos e intente de nuevo",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testBuscarPorNombreUsaElRepositorio() {
        Rol r = new Rol(UUID.randomUUID());
        when(rolRepository.findByNombre("Enc")).thenReturn(new ArrayList<>(List.of(r)));

        modelo.buscarPorNombre("Enc");

        assertEquals(1, modelo.getRegistros().size());
        verify(rolRepository, times(1)).findByNombre("Enc");
    }

    @Test
    public void testEliminarConRestricionLimpiaYAvisa() {
        Rol r = new Rol(UUID.randomUUID());
        modelo.setRegistro(r);
        modelo.setFormVisible(true);
        doThrow(new RuntimeException("RESTRICT")).when(rolRepository).eliminar(r);

        modelo.btnEliminarHandler();

        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertNotSame(r, modelo.getRegistro());
        assertEquals("No se pudo eliminar el rol (puede estar asignado a empleados)",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }
}
