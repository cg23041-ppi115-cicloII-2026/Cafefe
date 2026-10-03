package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRepository;
import sv.edu.ues.ppi115.cafefe.entity.Empleado;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmpleadoModelTest {

    @Mock
    private EmpleadoRepository empleadoRepository;

    @InjectMocks
    private EmpleadoModel modelo;

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
    public void testInstanciarRegistroActivoYComentarios() {
        Empleado e = modelo.instanciarRegistro();

        assertNotNull(e.getIdEmpleado());
        assertEquals(Boolean.TRUE, e.getActivo());
        assertEquals("", e.getComentarios());
    }

    @Test
    public void testGetRegistroByIdBuscaEnLaListaCargada() {
        UUID id1 = UUID.randomUUID();
        Empleado e1 = new Empleado(id1);
        Empleado e2 = new Empleado(UUID.randomUUID());
        modelo.setRegistros(new ArrayList<>(List.of(e1, e2)));

        assertSame(e1, modelo.getRegistroById(id1));
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
        verify(empleadoRepository, never()).crear(any());
        verify(empleadoRepository, never()).modificar(any());
    }

    @Test
    public void testGuardarNombreEnBlancoAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("   ");
        modelo.getRegistro().setApellido("Pérez");

        modelo.btnGuardarHandler();

        assertEquals("Debe ingresar el nombre", unicoMensaje().getSummary());
        verify(empleadoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarSinApellidoAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("Juan");

        modelo.btnGuardarHandler();

        assertEquals("Debe ingresar el apellido", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(empleadoRepository, never()).crear(any());
    }

    @Test
    public void testGuardarConNombreYApellidoCreaElRegistro() {
        modelo.limpiar();
        Empleado e = modelo.getRegistro();
        e.setNombre("Juan");
        e.setApellido("Pérez");

        modelo.btnGuardarHandler();

        verify(empleadoRepository, times(1)).crear(e);
        verify(empleadoRepository, never()).modificar(any());
        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testGuardarCuandoElDaoFallaAvisaSinRomper() {
        modelo.limpiar();
        Empleado e = modelo.getRegistro();
        e.setNombre("Juan");
        e.setApellido("Pérez");
        doThrow(new RuntimeException("boom")).when(empleadoRepository).crear(any());

        modelo.btnGuardarHandler();

        assertEquals("No se pudo guardar: verifique los datos e intente de nuevo",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testEliminarConRestricionLimpiaYAvisa() {
        Empleado e = new Empleado(UUID.randomUUID());
        modelo.setRegistro(e);
        modelo.setFormVisible(true);
        doThrow(new RuntimeException("RESTRICT")).when(empleadoRepository).eliminar(e);

        modelo.btnEliminarHandler();

        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertNotSame(e, modelo.getRegistro());
        assertEquals("No se puede eliminar: el empleado tiene asignaciones en otras tablas",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }
}
