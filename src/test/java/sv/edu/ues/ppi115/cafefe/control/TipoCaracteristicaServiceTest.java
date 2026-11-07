package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TipoCaracteristicaServiceTest {

    @Test
    public void constructorTest() {
        System.out.println("TipoCaracteristicaServiceTest.constructorTest");
        TipoCaracteristicaService cut = new TipoCaracteristicaService();
        assertNotNull(cut);
        System.out.println("TipoCaracteristicaServiceTest.constructorTest - finalizado");
    }

    @Test
    public void getEntityManagerTest() {
        System.out.println("TipoCaracteristicaServiceTest.getEntityManagerTest");
        TipoCaracteristicaService cut = new TipoCaracteristicaService();
        assertNull(cut.getEntityManager());
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        cut.em = mockEM;
        assertEquals(mockEM, cut.getEntityManager());
        System.out.println("TipoCaracteristicaServiceTest.getEntityManagerTest - finalizado");
    }
}
