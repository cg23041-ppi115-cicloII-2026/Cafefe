package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.entity.TipoProducto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LazyModelTest {

    @Mock
    private DefaultDAO<TipoProducto, UUID> dao;

    private LazyModel<TipoProducto> modelo() {
        return new LazyModel<>(dao, TipoProducto::getIdTipoProducto);
    }

    @Test
    public void testCountConsultaElTotalAlDao() {
        when(dao.count()).thenReturn(7L);

        int total = modelo().count(null);

        assertEquals(7, total);
        verify(dao, times(1)).count();
    }

    @Test
    public void testCountConDaoNuloRegresaCero() {
        LazyModel<TipoProducto> sinDao = new LazyModel<>(null, TipoProducto::getIdTipoProducto);

        assertEquals(0, sinDao.count(null));
    }

    @Test
    public void testCountConTotalNuloRegresaCero() {
        when(dao.count()).thenReturn(null);

        assertEquals(0, modelo().count(null));
    }

    @Test
    public void testGetRowCountUsaElMismoCount() {
        when(dao.count()).thenReturn(3L);

        assertEquals(3, modelo().getRowCount());
        verify(dao, times(1)).count();
    }

    @Test
    public void testLoadDevuelveLaPaginaPedida() {
        List<TipoProducto> pagina = new ArrayList<>(List.of(
                new TipoProducto(UUID.randomUUID()),
                new TipoProducto(UUID.randomUUID())));
        when(dao.findRange(10, 5)).thenReturn(pagina);

        List<TipoProducto> resultado = modelo().load(10, 5, null, null);

        assertSame(pagina, resultado);
        assertEquals(2, resultado.size());
        verify(dao, times(1)).findRange(10, 5);
    }

    @Test
    public void testLoadConTamanioInvalidoDevuelveVacio() {
        List<TipoProducto> resultado = modelo().load(0, 0, null, null);

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(dao);
    }

    @Test
    public void testLoadConDaoNuloDevuelveVacio() {
        LazyModel<TipoProducto> sinDao = new LazyModel<>(null, TipoProducto::getIdTipoProducto);

        assertTrue(sinDao.load(0, 10, null, null).isEmpty());
    }

    @Test
    public void testGetRowKeyConvierteElIdATexto() {
        LazyModel<TipoProducto> modelo = modelo();
        TipoProducto conId = new TipoProducto(UUID.randomUUID());
        TipoProducto sinId = new TipoProducto();

        assertEquals(conId.getIdTipoProducto().toString(), modelo.getRowKey(conId));
        assertNull(modelo.getRowKey(sinId));
        assertNull(modelo.getRowKey(null));
    }

    @Test
    public void testGetRowDataBuscaEnLaPaginaCargada() {
        LazyModel<TipoProducto> modelo = modelo();
        TipoProducto primero = new TipoProducto(UUID.randomUUID());
        TipoProducto segundo = new TipoProducto(UUID.randomUUID());
        modelo.setWrappedData(new ArrayList<>(List.of(primero, segundo)));

        assertSame(primero, modelo.getRowData(primero.getIdTipoProducto().toString()));
        assertSame(segundo, modelo.getRowData(segundo.getIdTipoProducto().toString()));
        assertNull(modelo.getRowData("no-existe"));
        assertNull(modelo.getRowData(null));
    }

    @Test
    public void testGetRowDataSinPaginaCargadaDevuelveNull() {
        LazyModel<TipoProducto> modelo = modelo();

        assertNull(modelo.getRowData(UUID.randomUUID().toString()));
    }
}
