package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.ppi115.cafefe.entity.FacturaOrdenProducto;
import static org.junit.jupiter.api.Assertions.*;

public class FacturaOrdenProductoServiceTest {
    @Test
    public void constructorTest() {
        assertNotNull(new FacturaOrdenProductoService());
    }

    @Test
    public void getEntityManagerTest() {
        FacturaOrdenProductoService service = new FacturaOrdenProductoService();
        assertNull(service.getEntityManager());
        EntityManager em = Mockito.mock(EntityManager.class);
        service.em = em;
        assertEquals(em, service.getEntityManager());
    }

    @Test
    public void findByFacturaTest() {
        UUID idFactura = UUID.randomUUID();
        FacturaOrdenProductoService service = new FacturaOrdenProductoService();
        assertThrows(IllegalArgumentException.class, () -> service.findByFactura(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> service.findByFactura(idFactura, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> service.findByFactura(idFactura, 0, 0));
        assertThrows(IllegalStateException.class, () -> service.findByFactura(idFactura, 0, 10));

        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<FacturaOrdenProducto> query = Mockito.mock(TypedQuery.class);
        Mockito.when(em.createNamedQuery("FacturaOrdenProducto.findByFactura", FacturaOrdenProducto.class))
                .thenReturn(query);
        Mockito.when(query.setParameter("idFactura", idFactura)).thenReturn(query);
        Mockito.when(query.setFirstResult(0)).thenReturn(query);
        Mockito.when(query.setMaxResults(10)).thenReturn(query);
        Mockito.when(query.getResultList())
                .thenReturn(Arrays.asList(new FacturaOrdenProducto(UUID.randomUUID())));
        service.em = em;

        assertNotNull(service.findByFactura(idFactura, 0, 10));
    }

    @Test
    public void contarPorFacturaTest() {
        UUID idFactura = UUID.randomUUID();
        FacturaOrdenProductoService service = new FacturaOrdenProductoService();
        assertThrows(IllegalArgumentException.class, () -> service.contarPorFactura(null));
        assertThrows(IllegalStateException.class, () -> service.contarPorFactura(idFactura));

        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<Long> query = Mockito.mock(TypedQuery.class);
        Mockito.when(em.createNamedQuery("FacturaOrdenProducto.contarPorFactura", Long.class))
                .thenReturn(query);
        Mockito.when(query.setParameter("idFactura", idFactura)).thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(2L);
        service.em = em;

        assertEquals(2L, service.contarPorFactura(idFactura));
    }
}
