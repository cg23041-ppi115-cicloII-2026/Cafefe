package sv.edu.ues.ppi115.cafefe.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.persistence.EntityManager;

public class DescuentoServiceTest {

    @Test
    public void constructorTest() {
        assertNotNull(new DescuentoService());
    }

    @Test
    public void getEntityManagerTest() {
        DescuentoService service = new DescuentoService();
        assertNull(service.getEntityManager());
        EntityManager em = Mockito.mock(EntityManager.class);
        service.em = em;
        assertEquals(em, service.getEntityManager());
    }
}
