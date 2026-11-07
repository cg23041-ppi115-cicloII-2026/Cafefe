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
import sv.edu.ues.ppi115.cafefe.control.FacturaService;
import sv.edu.ues.ppi115.cafefe.control.CajaService;
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRolService;
import sv.edu.ues.ppi115.cafefe.entity.Caja;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;
import sv.edu.ues.ppi115.cafefe.entity.Factura;

public class FacturaResourceTest {

    private UriInfo mockUriInfo;
    private FacturaService mockCS;
    private CajaService mockCajaService;
    private EmpleadoRolService mockEmpleadoRolService;
    private FacturaResource cut;

    @BeforeEach
    public void setup() {
        mockUriInfo = Mockito.mock(UriInfo.class);
        UriBuilder mockUriBuilder = Mockito.mock(UriBuilder.class);
        mockCS = Mockito.mock(FacturaService.class);
        mockCajaService = Mockito.mock(CajaService.class);
        mockEmpleadoRolService = Mockito.mock(EmpleadoRolService.class);

        Mockito.when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.path(Mockito.anyString())).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.build())
                .thenReturn(URI.create("http://localhost:8080/v1/factura/1"));

        cut = new FacturaResource();
        cut.facturaService = mockCS;
        cut.cajaService = mockCajaService;
        cut.empleadoRolService = mockEmpleadoRolService;
    }

    @Test
    public void crearExitosoTest() {
        Factura nuevo = new Factura();
        UUID cajaId = UUID.randomUUID();
        UUID empleadoRolId = UUID.randomUUID();
        nuevo.setIdCaja(new Caja(cajaId));
        nuevo.setIdEmpleadoRol(new EmpleadoRol(empleadoRolId));
        Mockito.when(mockCajaService.findById(cajaId)).thenReturn(new Caja(cajaId));
        Mockito.when(mockEmpleadoRolService.findById(empleadoRolId)).thenReturn(new EmpleadoRol(empleadoRolId));
        Mockito.doNothing().when(mockCS).create(nuevo);

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(201, resultado.getStatus());
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearConExcepcionTest() {
        Factura nuevo = new Factura();
        UUID cajaId = UUID.randomUUID();
        UUID empleadoRolId = UUID.randomUUID();
        nuevo.setIdCaja(new Caja(cajaId));
        nuevo.setIdEmpleadoRol(new EmpleadoRol(empleadoRolId));
        Mockito.when(mockCajaService.findById(cajaId)).thenReturn(new Caja(cajaId));
        Mockito.when(mockEmpleadoRolService.findById(empleadoRolId)).thenReturn(new EmpleadoRol(empleadoRolId));
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).create(nuevo);

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearNullTest() {
        Response resultado = cut.create(null, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearConIdTest() {
        Factura nuevo = new Factura(UUID.randomUUID());

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearSinCajaTest() {
        Response resultado = cut.create(new Factura(), mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
        Mockito.verifyNoInteractions(mockCajaService);
    }

    @Test
    public void crearCajaSinIdTest() {
        Factura nuevo = new Factura();
        nuevo.setIdCaja(new Caja());

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
        Mockito.verifyNoInteractions(mockCajaService);
    }

    @Test
    public void crearCajaNoEncontradaTest() {
        Factura nuevo = new Factura();
        UUID cajaId = UUID.randomUUID();
        nuevo.setIdCaja(new Caja(cajaId));
        Mockito.when(mockCajaService.findById(cajaId)).thenReturn(null);

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verify(mockCajaService).findById(cajaId);
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearSinEmpleadoRolTest() {
        Factura nuevo = new Factura();
        UUID cajaId = UUID.randomUUID();
        nuevo.setIdCaja(new Caja(cajaId));
        Mockito.when(mockCajaService.findById(cajaId)).thenReturn(new Caja(cajaId));

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
        Mockito.verifyNoInteractions(mockEmpleadoRolService);
    }

    @Test
    public void crearEmpleadoRolSinIdTest() {
        Factura nuevo = new Factura();
        UUID cajaId = UUID.randomUUID();
        nuevo.setIdCaja(new Caja(cajaId));
        nuevo.setIdEmpleadoRol(new EmpleadoRol());
        Mockito.when(mockCajaService.findById(cajaId)).thenReturn(new Caja(cajaId));

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
        Mockito.verifyNoInteractions(mockEmpleadoRolService);
    }

    @Test
    public void crearEmpleadoRolNoEncontradoTest() {
        Factura nuevo = new Factura();
        UUID cajaId = UUID.randomUUID();
        UUID empleadoRolId = UUID.randomUUID();
        nuevo.setIdCaja(new Caja(cajaId));
        nuevo.setIdEmpleadoRol(new EmpleadoRol(empleadoRolId));
        Mockito.when(mockCajaService.findById(cajaId)).thenReturn(new Caja(cajaId));
        Mockito.when(mockEmpleadoRolService.findById(empleadoRolId)).thenReturn(null);

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verify(mockEmpleadoRolService).findById(empleadoRolId);
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void eliminarExitosoTest() {
        UUID id = UUID.randomUUID();
        Factura encontrado = new Factura(id);
        Mockito.when(mockCS.findById(id)).thenReturn(encontrado);
        Mockito.doNothing().when(mockCS).delete(encontrado);

        Response resultado = cut.delete(id);

        assertEquals(204, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS).delete(encontrado);
    }

    @Test
    public void eliminarNoEncontradoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findById(id)).thenReturn(null);

        Response resultado = cut.delete(id);

        assertEquals(404, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS, Mockito.never()).delete(Mockito.any());
    }

    @Test
    public void eliminarConExcepcionTest() {
        UUID id = UUID.randomUUID();
        Factura encontrado = new Factura(id);
        Mockito.when(mockCS.findById(id)).thenReturn(encontrado);
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).delete(encontrado);

        Response resultado = cut.delete(id);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS).delete(encontrado);
    }

    @Test
    public void eliminarIdNullTest() {
        Response resultado = cut.delete(null);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoExitosoTest() {
        Mockito.when(mockCS.findRange(0, 50))
                .thenReturn(List.of(new Factura(UUID.randomUUID()), new Factura(UUID.randomUUID())));
        Mockito.when(mockCS.contar()).thenReturn(2L);

        Response resultado = cut.findRange(0, 50);

        assertEquals(200, resultado.getStatus());
        assertNotNull(resultado.getEntity());
        Mockito.verify(mockCS).findRange(0, 50);
        Mockito.verify(mockCS).contar();
    }

    @Test
    public void buscarPorRangoParametrosInvalidosTest() {
        Response resultado = cut.findRange(-1, 0);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoMaxCeroTest() {
        Response resultado = cut.findRange(0, 0);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoMaxMayorACincuentaTest() {
        Response resultado = cut.findRange(0, 51);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoConExcepcionTest() {
        Mockito.when(mockCS.findRange(0, 50))
                .thenThrow(new RuntimeException("Error en base de datos"));

        Response resultado = cut.findRange(0, 50);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findRange(0, 50);
    }

    @Test
    public void buscarPorIdExitosoTest() {
        UUID id = UUID.randomUUID();
        Factura encontrado = new Factura(id);
        Mockito.when(mockCS.findById(id)).thenReturn(encontrado);

        Response resultado = cut.findById(id);

        assertNotNull(resultado.getEntity());
        assertEquals(200, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
    }

    @Test
    public void buscarPorIdNoEncontradoTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findById(id)).thenReturn(null);

        Response resultado = cut.findById(id);

        assertEquals(404, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
    }

    @Test
    public void buscarPorIdConExcepcionTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findById(id))
                .thenThrow(new RuntimeException("Error en base de datos"));

        Response resultado = cut.findById(id);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
    }

    @Test
    public void buscarPorIdNullTest() {
        Response resultado = cut.findById(null);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void actualizarExitosoTest() {
        UUID id = UUID.randomUUID();
        Factura factura = new Factura(id);
        UUID cajaId = UUID.randomUUID();
        UUID empleadoRolId = UUID.randomUUID();
        factura.setIdCaja(new Caja(cajaId));
        factura.setIdEmpleadoRol(new EmpleadoRol(empleadoRolId));
        Mockito.when(mockCS.findById(id)).thenReturn(factura);
        Mockito.when(mockCajaService.findById(cajaId)).thenReturn(new Caja(cajaId));
        Mockito.when(mockEmpleadoRolService.findById(empleadoRolId)).thenReturn(new EmpleadoRol(empleadoRolId));
        Mockito.when(mockCS.update(factura)).thenReturn(factura);

        Response resultado = cut.update(id, factura);

        assertEquals(200, resultado.getStatus());
        assertNotNull(resultado.getEntity());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS).update(factura);
    }

    @Test
    public void actualizarNoEncontradoTest() {
        UUID id = UUID.randomUUID();
        Factura factura = new Factura(id);
        Mockito.when(mockCS.findById(id)).thenReturn(null);

        Response resultado = cut.update(id, factura);

        assertEquals(404, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarSinCajaTest() {
        UUID id = UUID.randomUUID();
        Factura factura = new Factura();
        Mockito.when(mockCS.findById(id)).thenReturn(new Factura(id));

        Response resultado = cut.update(id, factura);

        assertEquals(400, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verifyNoInteractions(mockCajaService);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarCajaSinIdTest() {
        UUID id = UUID.randomUUID();
        Factura factura = new Factura();
        factura.setIdCaja(new Caja());
        Mockito.when(mockCS.findById(id)).thenReturn(new Factura(id));

        Response resultado = cut.update(id, factura);

        assertEquals(400, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verifyNoInteractions(mockCajaService);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarCajaNoEncontradaTest() {
        UUID id = UUID.randomUUID();
        UUID cajaId = UUID.randomUUID();
        Factura factura = new Factura();
        factura.setIdCaja(new Caja(cajaId));
        Mockito.when(mockCS.findById(id)).thenReturn(new Factura(id));
        Mockito.when(mockCajaService.findById(cajaId)).thenReturn(null);

        Response resultado = cut.update(id, factura);

        assertEquals(400, resultado.getStatus());
        Mockito.verify(mockCajaService).findById(cajaId);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarSinEmpleadoRolTest() {
        UUID id = UUID.randomUUID();
        UUID cajaId = UUID.randomUUID();
        Factura factura = new Factura();
        factura.setIdCaja(new Caja(cajaId));
        Mockito.when(mockCS.findById(id)).thenReturn(new Factura(id));
        Mockito.when(mockCajaService.findById(cajaId)).thenReturn(new Caja(cajaId));

        Response resultado = cut.update(id, factura);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockEmpleadoRolService);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarEmpleadoRolSinIdTest() {
        UUID id = UUID.randomUUID();
        UUID cajaId = UUID.randomUUID();
        Factura factura = new Factura();
        factura.setIdCaja(new Caja(cajaId));
        factura.setIdEmpleadoRol(new EmpleadoRol());
        Mockito.when(mockCS.findById(id)).thenReturn(new Factura(id));
        Mockito.when(mockCajaService.findById(cajaId)).thenReturn(new Caja(cajaId));

        Response resultado = cut.update(id, factura);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockEmpleadoRolService);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarEmpleadoRolNoEncontradoTest() {
        UUID id = UUID.randomUUID();
        UUID cajaId = UUID.randomUUID();
        UUID empleadoRolId = UUID.randomUUID();
        Factura factura = new Factura();
        factura.setIdCaja(new Caja(cajaId));
        factura.setIdEmpleadoRol(new EmpleadoRol(empleadoRolId));
        Mockito.when(mockCS.findById(id)).thenReturn(new Factura(id));
        Mockito.when(mockCajaService.findById(cajaId)).thenReturn(new Caja(cajaId));
        Mockito.when(mockEmpleadoRolService.findById(empleadoRolId)).thenReturn(null);

        Response resultado = cut.update(id, factura);

        assertEquals(400, resultado.getStatus());
        Mockito.verify(mockEmpleadoRolService).findById(empleadoRolId);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarConExcepcionTest() {
        UUID id = UUID.randomUUID();
        Factura factura = new Factura(id);
        UUID cajaId = UUID.randomUUID();
        UUID empleadoRolId = UUID.randomUUID();
        factura.setIdCaja(new Caja(cajaId));
        factura.setIdEmpleadoRol(new EmpleadoRol(empleadoRolId));
        Mockito.when(mockCS.findById(id)).thenReturn(factura);
        Mockito.when(mockCajaService.findById(cajaId)).thenReturn(new Caja(cajaId));
        Mockito.when(mockEmpleadoRolService.findById(empleadoRolId)).thenReturn(new EmpleadoRol(empleadoRolId));
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).update(factura);

        Response resultado = cut.update(id, factura);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS).update(factura);
    }

    @Test
    public void actualizarNullTest() {
        Response resultado = cut.update(null, null);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void actualizarSinIdTest() {
        Factura factura = new Factura();

        Response resultado = cut.update(factura.getIdFactura(), factura);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }
}
