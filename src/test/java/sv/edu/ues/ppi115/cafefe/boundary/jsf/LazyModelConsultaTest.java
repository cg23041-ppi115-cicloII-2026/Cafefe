package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import org.junit.jupiter.api.Test;
import sv.edu.ues.ppi115.cafefe.entity.TipoProducto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

public class LazyModelConsultaTest {

    private List<TipoProducto> paginaDeEjemplo() {
        return new ArrayList<>(List.of(
                new TipoProducto(UUID.randomUUID()),
                new TipoProducto(UUID.randomUUID())));
    }

    @Test
    public void testCountUsaElSupplierDeTotal() {
        LazyModelConsulta<TipoProducto> modelo =
                new LazyModelConsulta<>(null, () -> 7L, TipoProducto::getIdTipoProducto);

        assertEquals(7, modelo.count(null));
    }

    @Test
    public void testCountSinFuncionDeTotalRegresaCero() {
        LazyModelConsulta<TipoProducto> modelo =
                new LazyModelConsulta<>(null, null, TipoProducto::getIdTipoProducto);

        assertEquals(0, modelo.count(null));
    }

    @Test
    public void testCountConTotalNuloRegresaCero() {
        LazyModelConsulta<TipoProducto> modelo =
                new LazyModelConsulta<>(null, () -> null, TipoProducto::getIdTipoProducto);

        assertEquals(0, modelo.count(null));
    }

    @Test
    public void testGetRowCountUsaElMismoTotal() {
        LazyModelConsulta<TipoProducto> modelo =
                new LazyModelConsulta<>(null, () -> 3L, TipoProducto::getIdTipoProducto);

        assertEquals(3, modelo.getRowCount());
    }

    @Test
    public void testLoadDevuelveLaPaginaQuePideElModelo() {
        List<TipoProducto> pagina = paginaDeEjemplo();
        int[] llamada = new int[2];
        BiFunction<Integer, Integer, List<TipoProducto>> paginas = (primero, tamano) -> {
            llamada[0] = primero;
            llamada[1] = tamano;
            return pagina;
        };
        LazyModelConsulta<TipoProducto> modelo =
                new LazyModelConsulta<>(paginas, () -> 2L, TipoProducto::getIdTipoProducto);

        List<TipoProducto> resultado = modelo.load(10, 5, null, null);

        assertSame(pagina, resultado);
        assertEquals(10, llamada[0]);
        assertEquals(5, llamada[1]);
    }

    @Test
    public void testLoadSinFuncionDePaginasDevuelveVacio() {
        LazyModelConsulta<TipoProducto> modelo =
                new LazyModelConsulta<>(null, () -> 2L, TipoProducto::getIdTipoProducto);

        assertTrue(modelo.load(0, 10, null, null).isEmpty());
    }

    @Test
    public void testLoadConTamanoInvalidoDevuelveVacioSinConsultar() {
        int[] aplicaciones = new int[1];
        BiFunction<Integer, Integer, List<TipoProducto>> paginas = (primero, tamano) -> {
            aplicaciones[0]++;
            return paginaDeEjemplo();
        };
        LazyModelConsulta<TipoProducto> modelo =
                new LazyModelConsulta<>(paginas, () -> 2L, TipoProducto::getIdTipoProducto);

        assertTrue(modelo.load(0, 0, null, null).isEmpty());
        assertEquals(0, aplicaciones[0]);
    }

    @Test
    public void testGetRowKeyConvierteElIdATexto() {
        LazyModelConsulta<TipoProducto> modelo =
                new LazyModelConsulta<>(null, null, TipoProducto::getIdTipoProducto);
        TipoProducto conId = new TipoProducto(UUID.randomUUID());
        TipoProducto sinId = new TipoProducto();

        assertEquals(conId.getIdTipoProducto().toString(), modelo.getRowKey(conId));
        assertNull(modelo.getRowKey(sinId));
        assertNull(modelo.getRowKey(null));
    }

    @Test
    public void testGetRowKeySinFuncionDeIdRegresaNull() {
        LazyModelConsulta<TipoProducto> modelo = new LazyModelConsulta<>(null, null, null);

        assertNull(modelo.getRowKey(new TipoProducto(UUID.randomUUID())));
    }

    @Test
    public void testGetRowDataBuscaEnLaPaginaCargada() {
        LazyModelConsulta<TipoProducto> modelo =
                new LazyModelConsulta<>(null, null, TipoProducto::getIdTipoProducto);
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
        LazyModelConsulta<TipoProducto> modelo =
                new LazyModelConsulta<>(null, null, TipoProducto::getIdTipoProducto);

        assertNull(modelo.getRowData(UUID.randomUUID().toString()));
    }
}
