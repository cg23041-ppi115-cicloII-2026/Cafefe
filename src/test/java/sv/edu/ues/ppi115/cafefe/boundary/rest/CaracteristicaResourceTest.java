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
import sv.edu.ues.ppi115.cafefe.control.CaracteristicaService;
import sv.edu.ues.ppi115.cafefe.control.TipoCaracteristicaService;
import sv.edu.ues.ppi115.cafefe.entity.Caracteristica;
import sv.edu.ues.ppi115.cafefe.entity.TipoCaracteristica;

public class CaracteristicaResourceTest {

    private UriInfo mockUriInfo;
    private CaracteristicaService mockCS;
    private TipoCaracteristicaService mockTCS;
    private CaracteristicaResource cut;

    @BeforeEach
    public void setup() {
        mockUriInfo = Mockito.mock(UriInfo.class);
        UriBuilder mockUriBuilder = Mockito.mock(UriBuilder.class);
        mockCS = Mockito.mock(CaracteristicaService.class);
        mockTCS = Mockito.mock(TipoCaracteristicaService.class);

        Mockito.when(mockUriInfo.getAbsolutePathBuilder()).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.path(Mockito.anyString())).thenReturn(mockUriBuilder);
        Mockito.when(mockUriBuilder.build())
                .thenReturn(URI.create("http://localhost:8080/v1/caracteristica/1"));

        cut = new CaracteristicaResource();
        cut.caracteristicaService = mockCS;
        cut.tipoCaracteristicaService = mockTCS;
    }

    @Test
    public void crearExitosoTest() {
        Caracteristica nuevo = new Caracteristica();
        UUID tipoId = UUID.randomUUID();
        TipoCaracteristica tipo = new TipoCaracteristica(tipoId);
        nuevo.setIdTipoCaracteristica(new TipoCaracteristica(tipoId));
        Mockito.when(mockTCS.findById(tipoId)).thenReturn(tipo);
        Mockito.doNothing().when(mockCS).create(nuevo);

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(201, resultado.getStatus());
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearConExcepcionTest() {
        Caracteristica nuevo = new Caracteristica();
        UUID tipoId = UUID.randomUUID();
        TipoCaracteristica tipo = new TipoCaracteristica(tipoId);
        nuevo.setIdTipoCaracteristica(new TipoCaracteristica(tipoId));
        Mockito.when(mockTCS.findById(tipoId)).thenReturn(tipo);
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).create(nuevo);

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).create(nuevo);
    }

    @Test
    public void crearTipoConExcepcionTest() {
        Caracteristica nuevo = new Caracteristica();
        UUID tipoId = UUID.randomUUID();
        nuevo.setIdTipoCaracteristica(new TipoCaracteristica(tipoId));
        Mockito.when(mockTCS.findById(tipoId))
                .thenThrow(new RuntimeException("Error en base de datos"));

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockTCS).findById(tipoId);
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearNullTest() {
        Response resultado = cut.create(null, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearSinTipoTest() {
        Response resultado = cut.create(new Caracteristica(), mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearConIdTest() {
        Caracteristica nuevo = new Caracteristica(UUID.randomUUID());

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void crearTipoSinIdTest() {
        Caracteristica nuevo = new Caracteristica();
        nuevo.setIdTipoCaracteristica(new TipoCaracteristica());

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
        Mockito.verifyNoInteractions(mockTCS);
    }

    @Test
    public void crearTipoNoEncontradoTest() {
        Caracteristica nuevo = new Caracteristica();
        UUID tipoId = UUID.randomUUID();
        nuevo.setIdTipoCaracteristica(new TipoCaracteristica(tipoId));
        Mockito.when(mockTCS.findById(tipoId)).thenReturn(null);

        Response resultado = cut.create(nuevo, mockUriInfo);

        assertEquals(400, resultado.getStatus());
        Mockito.verify(mockTCS).findById(tipoId);
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void eliminarExitosoTest() {
        UUID id = UUID.randomUUID();
        Caracteristica encontrado = new Caracteristica(id);
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
        Caracteristica encontrado = new Caracteristica(id);
        Mockito.when(mockCS.findById(id)).thenReturn(encontrado);
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).delete(encontrado);

        Response resultado = cut.delete(id);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS).delete(encontrado);
    }

    @Test
    public void eliminarBusquedaConExcepcionTest() {
        UUID id = UUID.randomUUID();
        Mockito.when(mockCS.findById(id))
                .thenThrow(new RuntimeException("Error en base de datos"));

        Response resultado = cut.delete(id);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS, Mockito.never()).delete(Mockito.any());
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
                .thenReturn(List.of(new Caracteristica(UUID.randomUUID()), new Caracteristica(UUID.randomUUID())));
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
    public void buscarPorRangoConteoConExcepcionTest() {
        Mockito.when(mockCS.findRange(0, 50))
                .thenReturn(List.of(new Caracteristica()));
        Mockito.when(mockCS.contar())
                .thenThrow(new RuntimeException("Error en base de datos"));

        Response resultado = cut.findRange(0, 50);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findRange(0, 50);
        Mockito.verify(mockCS).contar();
    }

    @Test
    public void buscarPorIdExitosoTest() {
        UUID id = UUID.randomUUID();
        Caracteristica encontrado = new Caracteristica(id);
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
        Caracteristica caracteristica = new Caracteristica(id);
        UUID tipoId = UUID.randomUUID();
        TipoCaracteristica tipo = new TipoCaracteristica(tipoId);
        caracteristica.setIdTipoCaracteristica(new TipoCaracteristica(tipoId));
        Mockito.when(mockCS.findById(id)).thenReturn(caracteristica);
        Mockito.when(mockTCS.findById(tipoId)).thenReturn(tipo);
        Mockito.when(mockCS.update(caracteristica)).thenReturn(caracteristica);

        Response resultado = cut.update(id, caracteristica);

        assertEquals(200, resultado.getStatus());
        assertNotNull(resultado.getEntity());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS).update(caracteristica);
    }

    @Test
    public void actualizarNoEncontradoTest() {
        UUID id = UUID.randomUUID();
        Caracteristica caracteristica = new Caracteristica(id);
        Mockito.when(mockCS.findById(id)).thenReturn(null);

        Response resultado = cut.update(id, caracteristica);

        assertEquals(404, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarTipoSinIdTest() {
        UUID id = UUID.randomUUID();
        Caracteristica caracteristica = new Caracteristica();
        caracteristica.setIdTipoCaracteristica(new TipoCaracteristica());
        Mockito.when(mockCS.findById(id)).thenReturn(new Caracteristica(id));

        Response resultado = cut.update(id, caracteristica);

        assertEquals(400, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verifyNoInteractions(mockTCS);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarTipoNoEncontradoTest() {
        UUID id = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();
        Caracteristica caracteristica = new Caracteristica();
        caracteristica.setIdTipoCaracteristica(new TipoCaracteristica(tipoId));
        Mockito.when(mockCS.findById(id)).thenReturn(new Caracteristica(id));
        Mockito.when(mockTCS.findById(tipoId)).thenReturn(null);

        Response resultado = cut.update(id, caracteristica);

        assertEquals(400, resultado.getStatus());
        Mockito.verify(mockTCS).findById(tipoId);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarConExcepcionTest() {
        UUID id = UUID.randomUUID();
        Caracteristica caracteristica = new Caracteristica(id);
        UUID tipoId = UUID.randomUUID();
        caracteristica.setIdTipoCaracteristica(new TipoCaracteristica(tipoId));
        Mockito.when(mockCS.findById(id)).thenReturn(caracteristica);
        Mockito.when(mockTCS.findById(tipoId)).thenReturn(new TipoCaracteristica(tipoId));
        Mockito.doThrow(new RuntimeException("Error en base de datos"))
                .when(mockCS).update(caracteristica);

        Response resultado = cut.update(id, caracteristica);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockCS).update(caracteristica);
    }

    @Test
    public void actualizarBusquedaConExcepcionTest() {
        UUID id = UUID.randomUUID();
        Caracteristica caracteristica = new Caracteristica();
        Mockito.when(mockCS.findById(id))
                .thenThrow(new RuntimeException("Error en base de datos"));

        Response resultado = cut.update(id, caracteristica);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verifyNoInteractions(mockTCS);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarTipoConExcepcionTest() {
        UUID id = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();
        Caracteristica caracteristica = new Caracteristica();
        caracteristica.setIdTipoCaracteristica(new TipoCaracteristica(tipoId));
        Mockito.when(mockCS.findById(id)).thenReturn(new Caracteristica(id));
        Mockito.when(mockTCS.findById(tipoId))
                .thenThrow(new RuntimeException("Error en base de datos"));

        Response resultado = cut.update(id, caracteristica);

        assertEquals(500, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
        Mockito.verify(mockTCS).findById(tipoId);
        Mockito.verify(mockCS, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void actualizarNullTest() {
        Response resultado = cut.update(null, null);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }

    @Test
    public void actualizarSinTipoTest() {
        UUID id = UUID.randomUUID();
        Caracteristica caracteristica = new Caracteristica(id);
        Mockito.when(mockCS.findById(id)).thenReturn(caracteristica);

        Response resultado = cut.update(id, caracteristica);

        assertEquals(400, resultado.getStatus());
        Mockito.verify(mockCS).findById(id);
    }

    @Test
    public void actualizarSinIdTest() {
        Caracteristica caracteristica = new Caracteristica();

        Response resultado = cut.update(caracteristica.getIdCaracteristica(), caracteristica);

        assertEquals(400, resultado.getStatus());
        Mockito.verifyNoInteractions(mockCS);
    }
}
