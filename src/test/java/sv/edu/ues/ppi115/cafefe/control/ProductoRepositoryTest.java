package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.entity.Producto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Producto> query;

    private ProductoRepository repositorio;

    @BeforeEach
    public void setUp() {
        repositorio = new ProductoRepository() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
        lenient().when(query.setParameter(anyString(), any())).thenReturn(query);
    }

    @Test
    public void testFindActivosDevuelveLaListaDelQuery() {
        Producto producto = new Producto();
        when(em.createNamedQuery("Producto.findActivos", Producto.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(producto));

        List<Producto> resultado = repositorio.findActivos();

        assertEquals(1, resultado.size());
        assertSame(producto, resultado.get(0));
        verify(em, times(1)).createNamedQuery("Producto.findActivos", Producto.class);
    }

    @Test
    public void testFindByNombreLikeEnviaElComodinEnAmbosLados() {
        when(em.createNamedQuery("Producto.findByNombreLike", Producto.class)).thenReturn(query);

        repositorio.findByNombreLike("Caf");

        verify(query, times(1)).setParameter("nombre", "%Caf%");
    }

    @Test
    public void testFindByPrecioRangeEnviaElMinimoYElMaximo() {
        BigDecimal min = new BigDecimal("1.00");
        BigDecimal max = new BigDecimal("9.99");
        when(em.createNamedQuery("Producto.findByPrecioRange", Producto.class)).thenReturn(query);

        repositorio.findByPrecioRange(min, max);

        verify(query, times(1)).setParameter("min", min);
        verify(query, times(1)).setParameter("max", max);
    }

    @Test
    public void testEliminarSinIdONuloNoTocaLaBase() {
        repositorio.eliminar(null);
        repositorio.eliminar(new Producto());

        verifyNoInteractions(em);
    }

    @Test
    public void testEliminarConIdHaceMergeYRemove() {
        Producto producto = new Producto(UUID.randomUUID());
        when(em.merge(producto)).thenReturn(producto);

        repositorio.eliminar(producto);

        verify(em, times(1)).merge(producto);
        verify(em, times(1)).remove(producto);
    }
}
