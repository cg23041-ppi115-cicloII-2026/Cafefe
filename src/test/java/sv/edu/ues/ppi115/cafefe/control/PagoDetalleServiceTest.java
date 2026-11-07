package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.ppi115.cafefe.entity.PagoDetalle;
import static org.junit.jupiter.api.Assertions.*;

public class PagoDetalleServiceTest {
    @Test
    public void constructorTest() {
        assertNotNull(new PagoDetalleService());
    }

    @Test
    public void getEntityManagerTest() {
        PagoDetalleService service = new PagoDetalleService();
        assertNull(service.getEntityManager());
        EntityManager em = Mockito.mock(EntityManager.class);
        service.em = em;
        assertEquals(em, service.getEntityManager());
    }

    @Test
    public void findByPagoTest() {
        UUID idPago = UUID.randomUUID();
        PagoDetalleService service = new PagoDetalleService();
        assertThrows(IllegalArgumentException.class, () -> service.findByPago(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> service.findByPago(idPago, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> service.findByPago(idPago, 0, 0));
        assertThrows(IllegalStateException.class, () -> service.findByPago(idPago, 0, 10));

        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<PagoDetalle> query = Mockito.mock(TypedQuery.class);
        Mockito.when(em.createNamedQuery("PagoDetalle.findByPago", PagoDetalle.class))
                .thenReturn(query);
        Mockito.when(query.setParameter("idPago", idPago)).thenReturn(query);
        Mockito.when(query.setFirstResult(0)).thenReturn(query);
        Mockito.when(query.setMaxResults(10)).thenReturn(query);
        Mockito.when(query.getResultList())
                .thenReturn(Arrays.asList(new PagoDetalle(UUID.randomUUID())));
        service.em = em;

        assertNotNull(service.findByPago(idPago, 0, 10));
    }

    @Test
    public void contarPorPagoTest() {
        UUID idPago = UUID.randomUUID();
        PagoDetalleService service = new PagoDetalleService();
        assertThrows(IllegalArgumentException.class, () -> service.contarPorPago(null));
        assertThrows(IllegalStateException.class, () -> service.contarPorPago(idPago));

        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<Long> query = Mockito.mock(TypedQuery.class);
        Mockito.when(em.createNamedQuery("PagoDetalle.contarPorPago", Long.class))
                .thenReturn(query);
        Mockito.when(query.setParameter("idPago", idPago)).thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(2L);
        service.em = em;

        assertEquals(2L, service.contarPorPago(idPago));
    }
}
