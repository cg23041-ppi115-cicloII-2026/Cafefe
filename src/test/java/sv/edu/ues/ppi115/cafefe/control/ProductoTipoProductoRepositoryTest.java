package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.entity.ProductoTipoProducto;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoTipoProductoRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<ProductoTipoProducto> query;

    private ProductoTipoProductoRepository repositorio;

    @BeforeEach
    public void setUp() {
        repositorio = new ProductoTipoProductoRepository() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
        lenient().when(query.setParameter(anyString(), any())).thenReturn(query);
        lenient().when(query.setParameter(anyString(), any(Date.class))).thenReturn(query);
    }

    @Test
    public void testFindByFechaCreacionDevuelveLaListaDelQuery() {
        Date fecha = new Date();
        ProductoTipoProducto fila = new ProductoTipoProducto();
        when(em.createNamedQuery("ProductoTipoProducto.findByFechaCreacion",
                ProductoTipoProducto.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(fila));

        List<ProductoTipoProducto> resultado = repositorio.findByFechaCreacion(fecha);

        assertEquals(1, resultado.size());
        assertSame(fila, resultado.get(0));
        verify(query, times(1)).setParameter("fechaCreacion", fecha);
    }

    @Test
    public void testFindByObservacionesEnviaElTexto() {
        when(em.createNamedQuery("ProductoTipoProducto.findByObservaciones",
                ProductoTipoProducto.class)).thenReturn(query);

        repositorio.findByObservaciones("temporada");

        verify(query, times(1)).setParameter("observaciones", "temporada");
    }

    @Test
    public void testFindByProductoEnviaElIdDelProducto() {
        UUID idProducto = UUID.randomUUID();
        when(em.createNamedQuery("ProductoTipoProducto.findByProducto",
                ProductoTipoProducto.class)).thenReturn(query);

        repositorio.findByProducto(idProducto);

        verify(query, times(1)).setParameter("idProducto", idProducto);
    }

    @Test
    public void testFindByTipoProductoEnviaElIdDelTipo() {
        UUID idTipo = UUID.randomUUID();
        when(em.createNamedQuery("ProductoTipoProducto.findByTipoProducto",
                ProductoTipoProducto.class)).thenReturn(query);

        repositorio.findByTipoProducto(idTipo);

        verify(query, times(1)).setParameter("idTipoProducto", idTipo);
    }
}
