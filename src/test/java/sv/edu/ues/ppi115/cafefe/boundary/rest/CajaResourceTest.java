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
import sv.edu.ues.ppi115.cafefe.control.CajaService;
import sv.edu.ues.ppi115.cafefe.entity.Caja;

public class CajaResourceTest {

    private UriInfo mockUriInfo;
    private CajaService mockCS;
    private CajaResource cut;

    @BeforeEach
    public void setup() {
        mockUriInfo = Mockito.mock(UriInfo.class);
        UriBuilder mockUriBuilder = Mockito.mock(UriBuilder.class);
        mockCS = Mockito.mock(CajaService.class);

        Mockito.when(mockUriInfo.getAbsolutePathBuilder())
                .thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.path(Mockito.anyString()))
                .thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.build())
                .thenReturn(URI.create("http://localhost:8080/v1/caja/1"));

        cut = new CajaResource();
        cut.cajaService = mockCS;
    }

    @Test
    public void crearExitosoTest() {
        System.out.println("Ejecutando test: crearExitosoTest en CajaResource");
        Caja nuevo = new Caja();
        Mockito.doNothing().when(mockCS).create(nuevo);
        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(201, resultado.getStatus());
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearConExcepcionTest() {
        System.out.println("Ejecutando test: crearConExcepcionTest en CajaResource");
        Caja nuevo = new Caja();
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).create(nuevo);

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearNullTest() {
        System.out.println("Ejecutando test: crearNullTest en CajaResource");
        Response resultado = cut.create(null, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearConIdTest() {
        System.out.println("Ejecutando test: crearConIdTest en CajaResource");
        Caja nuevo = new Caja(UUID.randomUUID());
        Response resultado = cut.create(nuevo, mockUriInfo);
        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void eliminarExitosoTest() {
        System.out.println("Ejecutando test: eliminarExitosoTest en CajaResource");
        UUID id = UUID.randomUUID();
        Caja cajaExistente = new Caja(id);

        Mockito.when(mockCS.findById(id)).thenReturn(cajaExistente);
        Mockito.doNothing().when(mockCS).delete(cajaExistente);

        Response resultado = cut.delete(id);

        assertEquals(204, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS).delete(cajaExistente);
    }

    @Test
    public void eliminarNoEncontradoTest() {
        System.out.println("Ejecutando test: eliminarNoEncontradoTest en CajaResource");
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findById(id)).thenReturn(null);
        Response resultado = cut.delete(id);

        assertEquals(404, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS, Mockito.never()).delete(Mockito.any());
    }

    @Test
    public void eliminarConExcepcionTest() {
        System.out.println("Ejecutando test: eliminarConExcepcionTest en CajaResource");
        UUID id = UUID.randomUUID();
        Caja cajaExistente = new Caja(id);
        Mockito.when(mockCS.findById(id)).thenReturn(cajaExistente);
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).delete(cajaExistente);

        Response resultado = cut.delete(id);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS).delete(cajaExistente);
    }

    @Test
    public void eliminarIdNullTest() {
        System.out.println("Ejecutando test: eliminarIdNullTest en CajaResource");
        Response resultado = cut.delete(null);
        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoExitosoTest() {
        System.out.println("Ejecutando test: buscarPorRangoExitosoTest en CajaResource");
        Mockito.when(mockCS.findRange(0, 50)).thenReturn(List.of(new Caja(UUID.randomUUID()), new Caja(UUID.randomUUID())));
        Mockito.when(mockCS.contar()).thenReturn(2L);
        Response resultado = cut.findRange(0, 50);
        assertEquals(200, resultado.getStatus());
        assertNotNull(resultado.getEntity());
        Mockito.verify(mockCS).findRange(0, 50);
        Mockito.verify(mockCS).contar();
    }

    @Test
    public void buscarPorRangoParametrosInvalidosTest() {
        System.out.println("Ejecutando test: buscarPorRangoParametrosInvalidosTest en CajaResource");
        Response resultado = cut.findRange(-1, 0);
        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoMaxCeroTest() {
        System.out.println("Ejecutando test: buscarPorRangoMaxCeroTest en CajaResource");
        Response resultado = cut.findRange(0, 0);
        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoMaxMayorACincuentaTest() {
        System.out.println("Ejecutando test: buscarPorRangoMaxMayorACincuentaTest en CajaResource");
        Response resultado = cut.findRange(0, 51);
        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void buscarPorRangoConExcepcionTest() {
        System.out.println("Ejecutando test: buscarPorRangoConExcepcionTest en CajaResource");
        Mockito.when(mockCS.findRange(0, 50)).thenThrow(new RuntimeException("Error en base de datos"));
        Response resultado = cut.findRange(0, 50);
        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findRange(0, 50);
    }

    @Test
    public void buscarPorIdExitosoTest() {
        System.out.println("Ejecutando test: buscarPorIdExitosoTest en CajaResource");
        Caja encontrado = new Caja(UUID.randomUUID());
        Mockito.when(mockCS.findById(Mockito.any())).thenReturn(encontrado);
        Response resultado = cut.findById(UUID.randomUUID());
        assertNotNull(resultado.getEntity());
        assertEquals(200, resultado.getStatus());
        Mockito.verify(mockCS).findById(Mockito.any());
    }

    @Test
    public void buscarPorIdNoEncontradoTest() {
        System.out.println("Ejecutando test: buscarPorIdNoEncontradoTest en CajaResource");
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findById(id)).thenReturn(null);
        Response resultado = cut.findById(id);
        assertEquals(404, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
    }

    @Test
    public void buscarPorIdConExcepcionTest() {
        System.out.println("Ejecutando test: buscarPorIdConExcepcionTest en CajaResource");
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findById(id)).thenThrow(new RuntimeException("Error en base de datos"));
        Response resultado = cut.findById(id);
        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
    }

    @Test
    public void buscarPorIdNullTest() {
        System.out.println("Ejecutando test: buscarPorIdNullTest en CajaResource");
        Response resultado = cut.findById(null);
        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void actualizarExitosoTest() {
        System.out.println("Ejecutando test: actualizarExitosoTest en CajaResource");
        Caja caja = new Caja(UUID.randomUUID());
        Mockito.when(mockCS.findById(caja.getIdCaja())).thenReturn(caja);
        Mockito.when(mockCS.update(caja)).thenReturn(caja);

        Response resultado = cut.update(caja.getIdCaja(), caja);

        assertEquals(200, resultado.getStatus());
        assertNotNull(resultado.getEntity());
        Mockito.verify(mockCS).findById(caja.getIdCaja());
        Mockito.verify(mockCS).update(caja);
    }

    @Test
    public void actualizarNoEncontradoTest() {
        System.out.println("Ejecutando test: actualizarNoEncontradoTest en CajaResource");
        Caja caja = new Caja(UUID.randomUUID());
        Mockito.when(mockCS.findById(caja.getIdCaja())).thenReturn(null);

        Response resultado = cut.update(caja.getIdCaja(), caja);

        assertEquals(404, resultado.getStatus());
        Mockito.verify(mockCS).findById(caja.getIdCaja());
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarConExcepcionTest() {
        System.out.println("Ejecutando test: actualizarConExcepcionTest en CajaResource");
        Caja caja = new Caja(UUID.randomUUID());
        Mockito.when(mockCS.findById(caja.getIdCaja())).thenReturn(caja);
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).update(caja);

        Response resultado = cut.update(caja.getIdCaja(), caja);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findById(caja.getIdCaja());
        Mockito.verify(mockCS).update(caja);
    }

    @Test
    public void actualizarNullTest() {
        System.out.println("Ejecutando test: actualizarNullTest en CajaResource");
        Response resultado = cut.update(null, null);
        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void actualizarSinIdTest() {
        System.out.println("Ejecutando test: actualizarSinIdTest en CajaResource");
        Caja caja = new Caja();
        Response resultado = cut.update(caja.getIdCaja(), caja);
        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }
}