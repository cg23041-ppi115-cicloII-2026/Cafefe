package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.ppi115.cafefe.entity.DescuentoProducto;
import static org.junit.jupiter.api.Assertions.*;

public class DescuentoProductoServiceTest {
    @Test
    public void constructorTest() {
        assertNotNull(new DescuentoProductoService());
    }

    @Test
    public void getEntityManagerTest() {
        DescuentoProductoService service = new DescuentoProductoService();
        assertNull(service.getEntityManager());
        EntityManager em = Mockito.mock(EntityManager.class);
        service.em = em;
        assertEquals(em, service.getEntityManager());
    }

    @Test
    public void findByDescuentoTest() {
        UUID idDescuento = UUID.randomUUID();
        DescuentoProductoService service = new DescuentoProductoService();
        assertThrows(IllegalArgumentException.class, () -> service.findByDescuento(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> service.findByDescuento(idDescuento, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> service.findByDescuento(idDescuento, 0, 0));
        assertThrows(IllegalStateException.class, () -> service.findByDescuento(idDescuento, 0, 10));

        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<DescuentoProducto> query = Mockito.mock(TypedQuery.class);
        Mockito.when(em.createNamedQuery("DescuentoProducto.findByDescuento", DescuentoProducto.class))
                .thenReturn(query);
        Mockito.when(query.setParameter("idDescuento", idDescuento)).thenReturn(query);
        Mockito.when(query.setFirstResult(0)).thenReturn(query);
        Mockito.when(query.setMaxResults(10)).thenReturn(query);
        Mockito.when(query.getResultList())
                .thenReturn(Arrays.asList(new DescuentoProducto(UUID.randomUUID())));
        service.em = em;

        assertNotNull(service.findByDescuento(idDescuento, 0, 10));
    }

    @Test
    public void contarPorDescuentoTest() {
        UUID idDescuento = UUID.randomUUID();
        DescuentoProductoService service = new DescuentoProductoService();
        assertThrows(IllegalArgumentException.class, () -> service.contarPorDescuento(null));
        assertThrows(IllegalStateException.class, () -> service.contarPorDescuento(idDescuento));

        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<Long> query = Mockito.mock(TypedQuery.class);
        Mockito.when(em.createNamedQuery("DescuentoProducto.contarPorDescuento", Long.class))
                .thenReturn(query);
        Mockito.when(query.setParameter("idDescuento", idDescuento)).thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(2L);
        service.em = em;

        assertEquals(2L, service.contarPorDescuento(idDescuento));
    }
}
