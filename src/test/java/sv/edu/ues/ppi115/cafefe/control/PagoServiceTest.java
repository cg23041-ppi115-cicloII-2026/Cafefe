package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import sv.edu.ues.ppi115.cafefe.entity.Pago;

public class PagoServiceTest {

    @Test
    public void constructorTest() {
        System.out.println("PagoServiceTest.constructorTest");
        PagoService cut = new PagoService();
        assertNotNull(cut);
        System.out.println("PagoServiceTest.constructorTest - finalizado");
    }

    @Test
    public void getEntityManagerTest() {
        System.out.println("PagoServiceTest.getEntityManagerTest");
        PagoService cut = new PagoService();
        assertNull(cut.getEntityManager());
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        cut.em = mockEM;
        assertEquals(mockEM, cut.getEntityManager());
        System.out.println("PagoServiceTest.getEntityManagerTest - finalizado");
    }

    @Test
    public void findByFacturaTest() {
        UUID idFactura = UUID.randomUUID();
        PagoService cut = new PagoService();
        assertThrows(IllegalArgumentException.class, () -> cut.findByFactura(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByFactura(idFactura, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByFactura(idFactura, 0, 0));
        assertThrows(IllegalStateException.class, () -> cut.findByFactura(idFactura, 0, 10));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<Pago> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("Pago.findByFactura", Pago.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idFactura", idFactura)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setFirstResult(0)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setMaxResults(10)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(new Pago(UUID.randomUUID())));
        cut.em = mockEM;
        assertNotNull(cut.findByFactura(idFactura, 0, 10));
    }

    @Test
    public void contarPorFacturaTest() {
        UUID idFactura = UUID.randomUUID();
        PagoService cut = new PagoService();
        assertThrows(IllegalArgumentException.class, () -> cut.contarPorFactura(null));
        assertThrows(IllegalStateException.class, () -> cut.contarPorFactura(idFactura));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<Long> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("Pago.contarPorFactura", Long.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idFactura", idFactura)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getSingleResult()).thenReturn(1L);
        cut.em = mockEM;
        assertEquals(1L, cut.contarPorFactura(idFactura));
    }
}
