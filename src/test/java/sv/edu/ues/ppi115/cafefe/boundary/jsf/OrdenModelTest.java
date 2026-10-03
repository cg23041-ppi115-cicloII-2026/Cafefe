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
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRolRepository;
import sv.edu.ues.ppi115.cafefe.control.OrdenRepository;
import sv.edu.ues.ppi115.cafefe.entity.Empleado;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;
import sv.edu.ues.ppi115.cafefe.entity.Orden;
import sv.edu.ues.ppi115.cafefe.entity.Rol;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdenModelTest {

    @Mock
    private OrdenRepository ordenRepository;

    @Mock
    private EmpleadoRolRepository empleadoRolRepository;

    @InjectMocks
    private OrdenModel modelo;

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

    private void guardarOrdenDePrueba() {
        modelo.limpiar();
        UUID id = UUID.randomUUID();
        modelo.setCobradorSeleccionado(id);
        when(empleadoRolRepository.findById(id)).thenReturn(new EmpleadoRol(id));
        modelo.btnGuardarHandler();
        contexto.mensajes.clear();
    }

    private Empleado empleado(String nombre, String apellido, boolean activo) {
        Empleado e = new Empleado();
        e.setNombre(nombre);
        e.setApellido(apellido);
        e.setActivo(activo);
        return e;
    }

    private Rol rol(String nombre, boolean activo) {
        Rol r = new Rol();
        r.setNombre(nombre);
        r.setActivo(activo);
        return r;
    }

    @Test
    public void testInstanciarRegistroCreaOrdenConIdYFecha() {
        Orden orden = modelo.instanciarRegistro();

        assertNotNull(orden.getIdOrden());
        assertNotNull(orden.getFechaCreacion());
    }

    @Test
    public void testGuardarSinCobradorAvisaYNoCrea() {
        modelo.limpiar();

        modelo.btnGuardarHandler();

        assertEquals("Elija quien realiza la orden", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(ordenRepository, never()).crear(any());
        assertFalse(modelo.isOrdenGuardada());
    }

    @Test
    public void testGuardarConCobradorInexistenteAvisaYNoCrea() {
        modelo.limpiar();
        UUID id = UUID.randomUUID();
        modelo.setCobradorSeleccionado(id);
        when(empleadoRolRepository.findById(id)).thenReturn(null);

        modelo.btnGuardarHandler();

        assertEquals("No se encontro la asignacion empleado-rol", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(ordenRepository, never()).crear(any());
        assertFalse(modelo.isOrdenGuardada());
    }

    @Test
    public void testGuardarCreaLaCabeceraAsignaCobradorYEscribeLaFecha() {
        modelo.limpiar();
        Orden orden = modelo.getRegistro();
        orden.setFechaCreacion(new Date(0));
        UUID id = UUID.randomUUID();
        modelo.setCobradorSeleccionado(id);
        EmpleadoRol atiende = new EmpleadoRol(id);
        when(empleadoRolRepository.findById(id)).thenReturn(atiende);

        modelo.btnGuardarHandler();

        verify(ordenRepository, times(1)).crear(orden);
        assertSame(atiende, orden.getIdEmpleadoRol());
        assertNotEquals(new Date(0), orden.getFechaCreacion());
        assertTrue(modelo.isOrdenGuardada());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testNuevaOrdenLimpiaElFormulario() {
        guardarOrdenDePrueba();
        UUID idGuardada = modelo.getRegistro().getIdOrden();

        modelo.btnNuevaOrdenHandler();

        assertNull(modelo.getCobradorSeleccionado());
        assertFalse(modelo.isOrdenGuardada());
        assertNotEquals(idGuardada, modelo.getRegistro().getIdOrden());
    }

    @Test
    public void testCancelarOrdenLimpiaSinTocarLaBase() {
        guardarOrdenDePrueba();

        modelo.btnCancelarOrdenHandler();

        assertNull(modelo.getCobradorSeleccionado());
        assertFalse(modelo.isOrdenGuardada());
        verify(ordenRepository, times(1)).crear(any());
        verify(ordenRepository, never()).modificar(any());
        verify(ordenRepository, never()).eliminar(any());
    }

    @Test
    public void testLimpiarReiniciaCobradorBloqueoYRegistro() {
        guardarOrdenDePrueba();
        UUID idGuardada = modelo.getRegistro().getIdOrden();

        modelo.limpiar();

        assertNull(modelo.getCobradorSeleccionado());
        assertFalse(modelo.isOrdenGuardada());
        assertNotEquals(idGuardada, modelo.getRegistro().getIdOrden());
    }

    @Test
    public void testGetListaCobradoresCreaEtiquetasYMarcaInactivos() {
        EmpleadoRol activa = new EmpleadoRol(UUID.randomUUID());
        activa.setActivo(true);
        activa.setIdEmpleado(empleado("Ana", "López", true));
        activa.setIdRol(rol("cajero", true));

        EmpleadoRol sinEmpleado = new EmpleadoRol(UUID.randomUUID());
        sinEmpleado.setActivo(true);
        sinEmpleado.setIdEmpleado(empleado("Luis", "Pérez", false));
        sinEmpleado.setIdRol(rol("mesero", true));

        EmpleadoRol sinRol = new EmpleadoRol(UUID.randomUUID());
        sinRol.setActivo(true);
        sinRol.setIdEmpleado(empleado("Rosa", "Díaz", true));
        sinRol.setIdRol(rol("gerente", false));

        EmpleadoRol inactiva = new EmpleadoRol(UUID.randomUUID());
        inactiva.setActivo(false);
        inactiva.setIdEmpleado(empleado("Juan", "Sánchez", true));
        inactiva.setIdRol(rol("administrador", true));

        when(empleadoRolRepository.findCobradores())
                .thenReturn(new ArrayList<>(List.of(activa, sinEmpleado, sinRol, inactiva)));

        List<SelectItem> items = modelo.getListaCobradores();

        assertEquals(4, items.size());
        assertEquals("Ana López (cajero)", items.get(0).getLabel());
        assertFalse(items.get(0).isDisabled());
        assertEquals(activa.getIdEmpleadoRol(), items.get(0).getValue());
        assertEquals("Luis Pérez (mesero)", items.get(1).getLabel());
        assertTrue(items.get(1).isDisabled());
        assertEquals("Rosa Díaz (gerente)", items.get(2).getLabel());
        assertTrue(items.get(2).isDisabled());
        assertEquals("Juan Sánchez (administrador)", items.get(3).getLabel());
        assertTrue(items.get(3).isDisabled());
    }

    @Test
    public void testGetListaCobradoresSeCachea() {
        when(empleadoRolRepository.findCobradores()).thenReturn(new ArrayList<>());

        modelo.getListaCobradores();
        List<SelectItem> segunda = modelo.getListaCobradores();

        assertTrue(segunda.isEmpty());
        verify(empleadoRolRepository, times(1)).findCobradores();
    }

    @Test
    public void testGetRegistroByIdBuscaPorIdYDevuelveNullSiNoEsta() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Orden o1 = new Orden(id1);
        Orden o2 = new Orden(id2);
        modelo.setRegistros(new ArrayList<>(List.of(o1, o2)));

        assertSame(o1, modelo.getRegistroById(id1));
        assertSame(o2, modelo.getRegistroById(id2));
        assertNull(modelo.getRegistroById(UUID.randomUUID()));
        assertNull(modelo.getRegistroById(null));
        modelo.setRegistros(new ArrayList<>());
        assertNull(modelo.getRegistroById(id1));
        modelo.setRegistros(null);
        assertNull(modelo.getRegistroById(id1));
    }

    @Test
    public void testGetIdByRegistroDevuelveElIdONulo() {
        UUID id = UUID.randomUUID();

        assertEquals(id, modelo.getIdByRegistro(new Orden(id)));
        assertNull(modelo.getIdByRegistro(null));
    }
}
