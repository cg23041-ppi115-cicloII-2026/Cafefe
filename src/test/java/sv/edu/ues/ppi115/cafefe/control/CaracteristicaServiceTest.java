package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import sv.edu.ues.ppi115.cafefe.entity.Caracteristica;

public class CaracteristicaServiceTest {

    @Test
    public void constructorTest() {
        System.out.println("CaracteristicaServiceTest.constructorTest");
        CaracteristicaService cut = new CaracteristicaService();
        assertNotNull(cut);
        System.out.println("CaracteristicaServiceTest.constructorTest - finalizado");
    }

    @Test
    public void getEntityManagerTest() {
        System.out.println("CaracteristicaServiceTest.getEntityManagerTest");
        CaracteristicaService cut = new CaracteristicaService();
        assertNull(cut.getEntityManager());
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        cut.em = mockEM;
        assertEquals(mockEM, cut.getEntityManager());
        System.out.println("CaracteristicaServiceTest.getEntityManagerTest - finalizado");
    }

    @Test
    public void findByTipoCaracteristicaTest() {
        System.out.println("CaracteristicaServiceTest.findByTipoCaracteristicaTest");
        UUID idTipoCaracteristica = UUID.randomUUID();
        CaracteristicaService cut = new CaracteristicaService();
        assertThrows(IllegalArgumentException.class, () -> cut.findByTipoCaracteristica(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByTipoCaracteristica(idTipoCaracteristica, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByTipoCaracteristica(idTipoCaracteristica, 0, 0));
        assertThrows(IllegalStateException.class, () -> cut.findByTipoCaracteristica(idTipoCaracteristica, 0, 10));

        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<Caracteristica> mockQuery = Mockito.mock(TypedQuery.class);
        List<Caracteristica> expected = Arrays.asList(new Caracteristica(UUID.randomUUID()));
        Mockito.when(mockEM.createNamedQuery("Caracteristica.findByTipoCaracteristica", Caracteristica.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idTipoCaracteristica", idTipoCaracteristica)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setFirstResult(0)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setMaxResults(10)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getResultList()).thenReturn(expected);
        cut.em = mockEM;
        assertEquals(expected, cut.findByTipoCaracteristica(idTipoCaracteristica, 0, 10));
        System.out.println("CaracteristicaServiceTest.findByTipoCaracteristicaTest - finalizado");
    }

    @Test
    public void contarPorTipoCaracteristicaTest() {
        System.out.println("CaracteristicaServiceTest.contarPorTipoCaracteristicaTest");
        UUID idTipoCaracteristica = UUID.randomUUID();
        CaracteristicaService cut = new CaracteristicaService();
        assertThrows(IllegalArgumentException.class, () -> cut.contarPorTipoCaracteristica(null));
        assertThrows(IllegalStateException.class, () -> cut.contarPorTipoCaracteristica(idTipoCaracteristica));

        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<Long> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("Caracteristica.contarPorTipoCaracteristica", Long.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idTipoCaracteristica", idTipoCaracteristica)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getSingleResult()).thenReturn(3L);
        cut.em = mockEM;
        assertEquals(3L, cut.contarPorTipoCaracteristica(idTipoCaracteristica));
        System.out.println("CaracteristicaServiceTest.contarPorTipoCaracteristicaTest - finalizado");
    }
}
