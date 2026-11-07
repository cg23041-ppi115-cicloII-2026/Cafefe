package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EmpleadoServiceTest {

    @Test
    public void constructorTest() {
        System.out.println("EmpleadoServiceTest.constructorTest");
        EmpleadoService cut = new EmpleadoService();
        assertNotNull(cut);
        System.out.println("EmpleadoServiceTest.constructorTest - finalizado");
    }

    @Test
    public void getEntityManagerTest() {
        System.out.println("EmpleadoServiceTest.getEntityManagerTest");
        EmpleadoService cut = new EmpleadoService();
        assertNull(cut.getEntityManager());
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        cut.em = mockEM;
        assertEquals(mockEM, cut.getEntityManager());
        System.out.println("EmpleadoServiceTest.getEntityManagerTest - finalizado");
    }
}
