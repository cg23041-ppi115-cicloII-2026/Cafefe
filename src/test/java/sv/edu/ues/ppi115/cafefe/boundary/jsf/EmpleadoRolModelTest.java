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
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRepository;
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRolRepository;
import sv.edu.ues.ppi115.cafefe.control.RolRepository;
import sv.edu.ues.ppi115.cafefe.entity.Empleado;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;
import sv.edu.ues.ppi115.cafefe.entity.Rol;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmpleadoRolModelTest {

    @Mock
    private EmpleadoRolRepository empleadoRolRepository;

    @Mock
    private EmpleadoRepository empleadoRepository;

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private EmpleadoRolModel modelo;

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
        EmpleadoRol r = modelo.instanciarRegistro();

        assertNotNull(r.getIdEmpleadoRol());
        assertEquals(Boolean.TRUE, r.getActivo());
        assertEquals("", r.getObservaciones());
    }

    @Test
    public void testLimpiarReiniciaAmbosCombos() {
        modelo.setEmpleadoSeleccionado(UUID.randomUUID());
        modelo.setRolSeleccionado(UUID.randomUUID());

        modelo.limpiar();

        assertNull(modelo.getEmpleadoSeleccionado());
        assertNull(modelo.getRolSeleccionado());
    }

    @Test
    public void testGetRegistroByIdBuscaEnLaListaCargada() {
        UUID id1 = UUID.randomUUID();
        EmpleadoRol r1 = new EmpleadoRol(id1);
        EmpleadoRol r2 = new EmpleadoRol(UUID.randomUUID());
        modelo.setRegistros(new ArrayList<>(List.of(r1, r2)));

        assertSame(r1, modelo.getRegistroById(id1));
        assertNull(modelo.getRegistroById(UUID.randomUUID()));
        assertNull(modelo.getRegistroById(null));

        modelo.setRegistros(null);
        assertNull(modelo.getRegistroById(id1));
    }

    @Test
    public void testGuardarSinEmpleadoAvisaYNoGuarda() {
        modelo.limpiar();

        modelo.btnGuardarHandler();

        assertEquals("Seleccione un empleado", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(empleadoRolRepository, never()).crear(any());
        verify(empleadoRolRepository, never()).modificar(any());
    }

    @Test
    public void testGuardarSinRolAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.setEmpleadoSeleccionado(UUID.randomUUID());

        modelo.btnGuardarHandler();

        assertEquals("Seleccione un rol", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(empleadoRolRepository, never()).crear(any());
    }

    @Test
    public void testGuardarCreaLaAsignacion() {
        modelo.limpiar();
        EmpleadoRol r = modelo.getRegistro();
        UUID idEmpleado = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();
        modelo.setEmpleadoSeleccionado(idEmpleado);
        modelo.setRolSeleccionado(idRol);
        Empleado empleado = new Empleado(idEmpleado);
        Rol rol = new Rol(idRol);
        when(empleadoRolRepository.findAll()).thenReturn(new ArrayList<>());
        when(empleadoRepository.findById(idEmpleado)).thenReturn(empleado);
        when(rolRepository.findById(idRol)).thenReturn(rol);

        modelo.btnGuardarHandler();

        verify(empleadoRolRepository, times(1)).crear(r);
        verify(empleadoRolRepository, never()).modificar(any());
        assertSame(empleado, r.getIdEmpleado());
        assertSame(rol, r.getIdRol());
        assertNull(modelo.getEmpleadoSeleccionado());
        assertNull(modelo.getRolSeleccionado());
        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testGuardarDetectaDuplicadoYNoCrea() {
        modelo.limpiar();
        UUID idEmpleado = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();
        modelo.setEmpleadoSeleccionado(idEmpleado);
        modelo.setRolSeleccionado(idRol);
        EmpleadoRol existente = new EmpleadoRol(UUID.randomUUID());
        existente.setIdEmpleado(new Empleado(idEmpleado));
        existente.setIdRol(new Rol(idRol));
        when(empleadoRolRepository.findAll()).thenReturn(new ArrayList<>(List.of(existente)));

        modelo.btnGuardarHandler();

        assertEquals("Ese empleado ya tiene ese rol asignado", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(empleadoRolRepository, never()).crear(any());
        verify(empleadoRolRepository, never()).modificar(any());
        assertEquals(idEmpleado, modelo.getEmpleadoSeleccionado());
    }

    @Test
    public void testGuardarEditandoNoDetectaSuPropiaFilaComoDuplicado() {
        UUID idEmpleado = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();
        EmpleadoRol propia = new EmpleadoRol(UUID.randomUUID());
        propia.setIdEmpleado(new Empleado(idEmpleado));
        propia.setIdRol(new Rol(idRol));
        modelo.seleccionarRegistro(propia);
        modelo.setEmpleadoSeleccionado(idEmpleado);
        modelo.setRolSeleccionado(idRol);
        Empleado empleado = new Empleado(idEmpleado);
        Rol rol = new Rol(idRol);
        when(empleadoRolRepository.findAll()).thenReturn(new ArrayList<>(List.of(propia)));
        when(empleadoRepository.findById(idEmpleado)).thenReturn(empleado);
        when(rolRepository.findById(idRol)).thenReturn(rol);

        modelo.btnGuardarHandler();

        verify(empleadoRolRepository, never()).crear(any());
        verify(empleadoRolRepository, times(1)).modificar(propia);
        assertSame(empleado, propia.getIdEmpleado());
        assertSame(rol, propia.getIdRol());
        assertEquals("Registro actualizado con éxito", unicoMensaje().getSummary());
    }

    @Test
    public void testGuardarCuandoElDaoFallaAvisaSinRomper() {
        modelo.limpiar();
        modelo.setEmpleadoSeleccionado(UUID.randomUUID());
        modelo.setRolSeleccionado(UUID.randomUUID());
        when(empleadoRolRepository.findAll()).thenReturn(new ArrayList<>());
        when(empleadoRepository.findById(any())).thenReturn(new Empleado());
        when(rolRepository.findById(any())).thenReturn(new Rol());
        doThrow(new RuntimeException("boom")).when(empleadoRolRepository).crear(any());

        modelo.btnGuardarHandler();

        assertEquals("No se pudo guardar la asignacion: intente de nuevo",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testEditarPrecargaLosCombos() {
        UUID idEmpleado = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();
        EmpleadoRol r = new EmpleadoRol(UUID.randomUUID());
        r.setIdEmpleado(new Empleado(idEmpleado));
        r.setIdRol(new Rol(idRol));
        modelo.setSeleccion(r);

        modelo.btnEditarHandler();

        assertEquals(idEmpleado, modelo.getEmpleadoSeleccionado());
        assertEquals(idRol, modelo.getRolSeleccionado());
        assertTrue(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.MODIFICAR, modelo.getEstado());
    }

    @Test
    public void testEditarFilaSinDatosDejaCombosVacios() {
        EmpleadoRol r = new EmpleadoRol(UUID.randomUUID());
        modelo.setEmpleadoSeleccionado(UUID.randomUUID());
        modelo.setRolSeleccionado(UUID.randomUUID());
        modelo.setSeleccion(r);

        modelo.btnEditarHandler();

        assertNull(modelo.getEmpleadoSeleccionado());
        assertNull(modelo.getRolSeleccionado());
    }

    @Test
    public void testEliminarConErrorLimpiaYAvisa() {
        EmpleadoRol r = new EmpleadoRol(UUID.randomUUID());
        modelo.setRegistro(r);
        modelo.setFormVisible(true);
        doThrow(new RuntimeException("RESTRICT")).when(empleadoRolRepository).eliminar(r);

        modelo.btnEliminarHandler();

        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertNotSame(r, modelo.getRegistro());
        assertEquals("No se pudo eliminar la asignacion", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testListaEmpleadosConNombreCompletoYGris() {
        Empleado activo = new Empleado();
        activo.setNombre("Juan");
        activo.setApellido("Pérez");
        activo.setActivo(true);
        Empleado inactivo = new Empleado();
        inactivo.setNombre("Ana");
        inactivo.setApellido("López");
        inactivo.setActivo(false);
        when(empleadoRepository.findAll()).thenReturn(new ArrayList<>(List.of(activo, inactivo)));

        List<SelectItem> lista = modelo.getListaEmpleados();

        assertEquals(2, lista.size());
        assertEquals("Juan Pérez", lista.get(0).getLabel());
        assertFalse(lista.get(0).isDisabled());
        assertEquals("Ana López", lista.get(1).getLabel());
        assertTrue(lista.get(1).isDisabled());
    }

    @Test
    public void testListaRolesMarcaInactivosEnGris() {
        Rol activo = new Rol();
        activo.setNombre("Encargado");
        activo.setActivo(true);
        Rol inactivo = new Rol();
        inactivo.setNombre("Cajero");
        inactivo.setActivo(false);
        when(rolRepository.findAll()).thenReturn(new ArrayList<>(List.of(activo, inactivo)));

        List<SelectItem> lista = modelo.getListaRoles();

        assertEquals(2, lista.size());
        assertFalse(lista.get(0).isDisabled());
        assertTrue(lista.get(1).isDisabled());
    }
}
