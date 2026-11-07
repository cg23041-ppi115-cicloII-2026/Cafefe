package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ProductoServiceTest {

    @Test
    public void constructorTest() {
        System.out.println("ProductoServiceTest.constructorTest");
        ProductoService cut = new ProductoService();
        assertNotNull(cut);
        System.out.println("ProductoServiceTest.constructorTest - finalizado");
    }

    @Test
    public void getEntityManagerTest() {
        System.out.println("ProductoServiceTest.getEntityManagerTest");
        ProductoService cut = new ProductoService();
        assertNull(cut.getEntityManager());
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        cut.em = mockEM;
        assertEquals(mockEM, cut.getEntityManager());
        System.out.println("ProductoServiceTest.getEntityManagerTest - finalizado");
    }
}
