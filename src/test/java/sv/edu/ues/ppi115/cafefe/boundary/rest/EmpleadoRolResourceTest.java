package sv.edu.ues.ppi115.cafefe.boundary.rest;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRolService;
import sv.edu.ues.ppi115.cafefe.control.EmpleadoService;
import sv.edu.ues.ppi115.cafefe.control.RolService;
import sv.edu.ues.ppi115.cafefe.entity.Empleado;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;
import sv.edu.ues.ppi115.cafefe.entity.Rol;

public class EmpleadoRolResourceTest {

    private UriInfo mockUriInfo;
    private EmpleadoRolService mockCS;
    private EmpleadoService mockES;
    private RolService mockRS;
    private EmpleadoRolResource cut;

    @BeforeEach
    public void setup() {
        mockUriInfo = Mockito.mock(UriInfo.class);
        UriBuilder mockUriBuilder = Mockito.mock(UriBuilder.class);
        mockCS = Mockito.mock(EmpleadoRolService.class);
        mockES = Mockito.mock(EmpleadoService.class);
        mockRS = Mockito.mock(RolService.class);

        Mockito.when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.path(Mockito.anyString())).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.build())
                .thenReturn(URI.create("http://localhost:8080/v1/empleado/1/rol/1"));

        cut = new EmpleadoRolResource();
        cut.empleadoRolService = mockCS;
        cut.empleadoService = mockES;
        cut.rolService = mockRS;
    }

    @Test
    public void crearExitosoTest() {
        UUID empleadoId = UUID.randomUUID();
        Empleado empleado = new Empleado(empleadoId);
        EmpleadoRol nuevo = new EmpleadoRol();
        UUID rolId = UUID.randomUUID();
        nuevo.setIdRol(new Rol(rolId));
        Mockito.when(mockES.findById(empleadoId)).thenReturn(empleado);
        Mockito.when(mockRS.findById(rolId)).thenReturn(new Rol(rolId));
        Mockito.doNothing().when(mockCS).create(nuevo);

        Response resultado = cut.crear(empleadoId, nuevo, mockUriInfo);

        assertEquals(201, resultado.getStatus());
        assertNotNull(nuevo.getIdEmpleadoRol());
        assertEquals(empleado, nuevo.getIdEmpleado());
        Mockito.verify(mockES).findById(empleadoId);
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearConIdTest() {
        UUID empleadoId = UUID.randomUUID();
        UUID empleadoRolId = UUID.randomUUID();
        EmpleadoRol nuevo = new EmpleadoRol(empleadoRolId);
        UUID rolId = UUID.randomUUID();
        nuevo.setIdRol(new Rol(rolId));
        Mockito.when(mockES.findById(empleadoId)).thenReturn(new Empleado(empleadoId));
        Mockito.when(mockRS.findById(rolId)).thenReturn(new Rol(rolId));

        Response resultado = cut.crear(empleadoId, nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS, mockRS);
    }

    @Test
    public void crearNullTest() {
        assertEquals(400, cut.crear(UUID.randomUUID(), null, mockUriInfo).getStatus());
        assertEquals(400, cut.crear(null, new EmpleadoRol(), mockUriInfo).getStatus());
        Mockito.verifyNoInteractions(mockCS, mockES);
    }

    @Test
    public void crearEmpleadoNoEncontradoTest() {
        UUID empleadoId = UUID.randomUUID();
        EmpleadoRol nuevo = new EmpleadoRol();
        UUID rolId = UUID.randomUUID();
        nuevo.setIdRol(new Rol(rolId));
        Mockito.when(mockES.findById(empleadoId)).thenReturn(null);

        Response resultado = cut.crear(empleadoId, nuevo, mockUriInfo);

        assertEquals(404, resultado.getStatus());
        Mockito.verify(mockES).findById(empleadoId);
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearConExcepcionTest() {
        UUID empleadoId = UUID.randomUUID();
        EmpleadoRol nuevo = new EmpleadoRol();
        UUID rolId = UUID.randomUUID();
        nuevo.setIdRol(new Rol(rolId));
        Mockito.when(mockES.findById(empleadoId))
                .thenThrow(new RuntimeException("Error en base de datos"));

        Response resultado = cut.crear(empleadoId, nuevo, mockUriInfo);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockES).findById(empleadoId);
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoExitosoTest() {
        UUID empleadoId = UUID.randomUUID();
        Mockito.when(mockCS.findByEmpleado(empleadoId, 0, 50))
                .thenReturn(List.of(new EmpleadoRol()));
        Mockito.when(mockCS.contarPorEmpleado(empleadoId)).thenReturn(1L);

        Response resultado = cut.buscarPorRango(empleadoId, 0, 50);

        assertEquals(200, resultado.getStatus());
        assertNotNull(resultado.getEntity());
        assertEquals("1", resultado.getHeaderString(ResponseHeaders.TOTAL_REGISTROS.toString()));
        Mockito.verify(mockCS).findByEmpleado(empleadoId, 0, 50);
        Mockito.verify(mockCS).contarPorEmpleado(empleadoId);
    }

    @Test
    public void buscarPorRangoParametrosInvalidosTest() {
        assertEquals(400, cut.buscarPorRango(null, 0, 50).getStatus());
        assertEquals(400, cut.buscarPorRango(UUID.randomUUID(), -1, 50).getStatus());
        assertEquals(400, cut.buscarPorRango(UUID.randomUUID(), 0, 0).getStatus());
        assertEquals(400, cut.buscarPorRango(UUID.randomUUID(), 0, 51).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoConExcepcionTest() {
        UUID empleadoId = UUID.randomUUID();
        Mockito.when(mockCS.findByEmpleado(empleadoId, 0, 50))
                .thenThrow(new RuntimeException("Error en base de datos"));

        Response resultado = cut.buscarPorRango(empleadoId, 0, 50);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findByEmpleado(empleadoId, 0, 50);
    }

    @Test
    public void buscarPorIdExitosoTest() {
        UUID empleadoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        EmpleadoRol encontrado = new EmpleadoRol(id);
        encontrado.setIdEmpleado(new Empleado(empleadoId));
        Mockito.when(mockCS.findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(encontrado);

        Response resultado = cut.buscarPorId(empleadoId, id);

        assertEquals(200, resultado.getStatus());
        assertNotNull(resultado.getEntity());
        Mockito.verify(mockCS).findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class));
    }

    @Test
    public void buscarPorIdNoEncontradoTest() {
        UUID empleadoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.buscarPorId(empleadoId, id).getStatus());

        EmpleadoRol deOtroEmpleado = new EmpleadoRol(id);
        deOtroEmpleado.setIdEmpleado(new Empleado(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(deOtroEmpleado);
        assertEquals(404, cut.buscarPorId(empleadoId, id).getStatus());
    }

    @Test
    public void buscarPorIdSinEmpleadoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new EmpleadoRol(id));

        assertEquals(404, cut.buscarPorId(UUID.randomUUID(), id).getStatus());
    }

    @Test
    public void buscarPorIdNullTest() {
        assertEquals(400, cut.buscarPorId(null, UUID.randomUUID()).getStatus());
        assertEquals(400, cut.buscarPorId(UUID.randomUUID(), null).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorIdConExcepcionTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class)))
                .thenThrow(new RuntimeException("Error en base de datos"));

        assertEquals(500, cut.buscarPorId(UUID.randomUUID(), id).getStatus());
        Mockito.verify(mockCS).findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class));
    }

    @Test
    public void actualizarExitosoTest() {
        UUID empleadoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Empleado empleado = new Empleado(empleadoId);
        EmpleadoRol existente = new EmpleadoRol(id);
        existente.setIdEmpleado(empleado);
        EmpleadoRol nuevo = new EmpleadoRol();
        UUID rolId = UUID.randomUUID();
        nuevo.setIdRol(new Rol(rolId));
        Mockito.when(mockRS.findById(rolId)).thenReturn(new Rol(rolId));
        Mockito.when(mockCS.findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        Mockito.when(mockCS.update(nuevo)).thenReturn(nuevo);

        Response resultado = cut.actualizar(empleadoId, id, nuevo);

        assertEquals(200, resultado.getStatus());
        assertEquals(id, nuevo.getIdEmpleadoRol());
        assertEquals(empleado, nuevo.getIdEmpleado());
        assertEquals(nuevo.getIdRol().getIdRol(), rolId);
        Mockito.verify(mockCS).update(nuevo);
    }

    @Test
    public void actualizarNoEncontradoTest() {
        UUID empleadoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);

        assertEquals(404, cut.actualizar(empleadoId, id, new EmpleadoRol()).getStatus());
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarEmpleadoDiferenteTest() {
        UUID empleadoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        EmpleadoRol existente = new EmpleadoRol(id);
        existente.setIdEmpleado(new Empleado(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);

        assertEquals(404, cut.actualizar(empleadoId, id, new EmpleadoRol()).getStatus());
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarSinEmpleadoExistenteTest() {
        UUID empleadoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new EmpleadoRol(id));

        assertEquals(404, cut.actualizar(empleadoId, id, new EmpleadoRol()).getStatus());
        Mockito.verify(mockCS).findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class));
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarNullTest() {
        assertEquals(400, cut.actualizar(null, UUID.randomUUID(), new EmpleadoRol()).getStatus());
        assertEquals(400, cut.actualizar(UUID.randomUUID(), null, new EmpleadoRol()).getStatus());
        assertEquals(400, cut.actualizar(UUID.randomUUID(), UUID.randomUUID(), null).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void actualizarConExcepcionTest() {
        UUID empleadoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        EmpleadoRol existente = new EmpleadoRol(id);
        existente.setIdEmpleado(new Empleado(empleadoId));
        EmpleadoRol nuevo = new EmpleadoRol();
        UUID rolId = UUID.randomUUID();
        nuevo.setIdRol(new Rol(rolId));
        Mockito.when(mockCS.findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        Mockito.when(mockRS.findById(rolId)).thenReturn(new Rol(rolId));
        Mockito.when(mockCS.update(nuevo))
                .thenThrow(new RuntimeException("Error en base de datos"));

        assertEquals(500, cut.actualizar(empleadoId, id, nuevo).getStatus());
        Mockito.verify(mockCS).update(nuevo);
    }

    @Test
    public void eliminarExitosoTest() {
        UUID empleadoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        EmpleadoRol existente = new EmpleadoRol(id);
        existente.setIdEmpleado(new Empleado(empleadoId));
        Mockito.when(mockCS.findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);

        assertEquals(204, cut.eliminar(empleadoId, id).getStatus());
        Mockito.verify(mockCS).delete(existente);
    }

    @Test
    public void eliminarNoEncontradoTest() {
        UUID empleadoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(null);
        assertEquals(404, cut.eliminar(empleadoId, id).getStatus());

        EmpleadoRol deOtroEmpleado = new EmpleadoRol(id);
        deOtroEmpleado.setIdEmpleado(new Empleado(UUID.randomUUID()));
        Mockito.when(mockCS.findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(deOtroEmpleado);
        assertEquals(404, cut.eliminar(empleadoId, id).getStatus());
        Mockito.verify(mockCS, Mockito.never()).delete(Mockito.any());
    }

    @Test
    public void eliminarSinEmpleadoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(new EmpleadoRol(id));

        assertEquals(404, cut.eliminar(UUID.randomUUID(), id).getStatus());
    }

    @Test
    public void eliminarNullTest() {
        assertEquals(400, cut.eliminar(null, UUID.randomUUID()).getStatus());
        assertEquals(400, cut.eliminar(UUID.randomUUID(), null).getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void eliminarConExcepcionTest() {
        UUID empleadoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        EmpleadoRol existente = new EmpleadoRol(id);
        existente.setIdEmpleado(new Empleado(empleadoId));
        Mockito.when(mockCS.findByIdYEmpleado(Mockito.eq(id), Mockito.any(UUID.class))).thenReturn(existente);
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).delete(existente);

        assertEquals(500, cut.eliminar(empleadoId, id).getStatus());
        Mockito.verify(mockCS).delete(existente);
    }
}
