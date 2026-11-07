package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.ppi115.cafefe.entity.ProductoTipoProducto;
import static org.junit.jupiter.api.Assertions.*;

public class ProductoTipoProductoServiceTest {
    @Test
    public void constructorTest() {
        assertNotNull(new ProductoTipoProductoService());
    }

    @Test
    public void getEntityManagerTest() {
        ProductoTipoProductoService service = new ProductoTipoProductoService();
        assertNull(service.getEntityManager());
        EntityManager em = Mockito.mock(EntityManager.class);
        service.em = em;
        assertEquals(em, service.getEntityManager());
    }

    @Test
    public void findByProductoTest() {
        UUID idProducto = UUID.randomUUID();
        ProductoTipoProductoService service = new ProductoTipoProductoService();
        assertThrows(IllegalArgumentException.class, () -> service.findByProducto(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> service.findByProducto(idProducto, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> service.findByProducto(idProducto, 0, 0));
        assertThrows(IllegalStateException.class, () -> service.findByProducto(idProducto, 0, 10));

        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<ProductoTipoProducto> query = Mockito.mock(TypedQuery.class);
        Mockito.when(em.createNamedQuery("ProductoTipoProducto.findByProducto", ProductoTipoProducto.class))
                .thenReturn(query);
        Mockito.when(query.setParameter("idProducto", idProducto)).thenReturn(query);
        Mockito.when(query.setFirstResult(0)).thenReturn(query);
        Mockito.when(query.setMaxResults(10)).thenReturn(query);
        Mockito.when(query.getResultList())
                .thenReturn(Arrays.asList(new ProductoTipoProducto(UUID.randomUUID())));
        service.em = em;

        assertNotNull(service.findByProducto(idProducto, 0, 10));
    }

    @Test
    public void contarPorProductoTest() {
        UUID idProducto = UUID.randomUUID();
        ProductoTipoProductoService service = new ProductoTipoProductoService();
        assertThrows(IllegalArgumentException.class, () -> service.contarPorProducto(null));
        assertThrows(IllegalStateException.class, () -> service.contarPorProducto(idProducto));

        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<Long> query = Mockito.mock(TypedQuery.class);
        Mockito.when(em.createNamedQuery("ProductoTipoProducto.contarPorProducto", Long.class))
                .thenReturn(query);
        Mockito.when(query.setParameter("idProducto", idProducto)).thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(2L);
        service.em = em;

        assertEquals(2L, service.contarPorProducto(idProducto));
    }
}
