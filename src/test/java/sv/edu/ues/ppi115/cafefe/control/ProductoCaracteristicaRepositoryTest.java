package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.entity.ProductoCaracteristica;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoCaracteristicaRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<ProductoCaracteristica> query;

    private ProductoCaracteristicaRepository repositorio;

    @BeforeEach
    public void setUp() {
        repositorio = new ProductoCaracteristicaRepository() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
        lenient().when(query.setParameter(anyString(), any())).thenReturn(query);
    }

    @Test
    public void testFindByValorDevuelveLaListaDelQuery() {
        ProductoCaracteristica fila = new ProductoCaracteristica();
        when(em.createNamedQuery("ProductoCaracteristica.findByValor",
                ProductoCaracteristica.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(fila));

        List<ProductoCaracteristica> resultado = repositorio.findByValor("Grande");

        assertEquals(1, resultado.size());
        assertSame(fila, resultado.get(0));
        verify(query, times(1)).setParameter("valor", "Grande");
    }

    @Test
    public void testFindByProductoEnviaElIdDelProducto() {
        UUID idProducto = UUID.randomUUID();
        when(em.createNamedQuery("ProductoCaracteristica.findByProducto",
                ProductoCaracteristica.class)).thenReturn(query);

        repositorio.findByProducto(idProducto);

        verify(query, times(1)).setParameter("idProducto", idProducto);
    }

    @Test
    public void testFindByCaracteristicaEnviaElIdDeLaCaracteristica() {
        UUID idCaracteristica = UUID.randomUUID();
        when(em.createNamedQuery("ProductoCaracteristica.findByCaracteristica",
                ProductoCaracteristica.class)).thenReturn(query);

        repositorio.findByCaracteristica(idCaracteristica);

        verify(query, times(1)).setParameter("idCaracteristica", idCaracteristica);
    }
}
