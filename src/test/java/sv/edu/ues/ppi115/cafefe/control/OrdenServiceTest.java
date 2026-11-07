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
import sv.edu.ues.ppi115.cafefe.entity.Orden;

public class OrdenServiceTest {

    @Test
    public void constructorTest() {
        System.out.println("OrdenServiceTest.constructorTest");
        OrdenService cut = new OrdenService();
        assertNotNull(cut);
        System.out.println("OrdenServiceTest.constructorTest - finalizado");
    }

    @Test
    public void getEntityManagerTest() {
        System.out.println("OrdenServiceTest.getEntityManagerTest");
        OrdenService cut = new OrdenService();
        assertNull(cut.getEntityManager());
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        cut.em = mockEM;
        assertEquals(mockEM, cut.getEntityManager());
        System.out.println("OrdenServiceTest.getEntityManagerTest - finalizado");
    }

    @Test
    public void findByEmpleadoRolTest() {
        UUID idEmpleadoRol = UUID.randomUUID();
        OrdenService cut = new OrdenService();
        assertThrows(IllegalArgumentException.class, () -> cut.findByEmpleadoRol(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByEmpleadoRol(idEmpleadoRol, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByEmpleadoRol(idEmpleadoRol, 0, 0));
        assertThrows(IllegalStateException.class, () -> cut.findByEmpleadoRol(idEmpleadoRol, 0, 10));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<Orden> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("Orden.findByEmpleadoRol", Orden.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idEmpleadoRol", idEmpleadoRol)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setFirstResult(0)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setMaxResults(10)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(new Orden(UUID.randomUUID())));
        cut.em = mockEM;
        assertNotNull(cut.findByEmpleadoRol(idEmpleadoRol, 0, 10));
    }

    @Test
    public void contarPorEmpleadoRolTest() {
        UUID idEmpleadoRol = UUID.randomUUID();
        OrdenService cut = new OrdenService();
        assertThrows(IllegalArgumentException.class, () -> cut.contarPorEmpleadoRol(null));
        assertThrows(IllegalStateException.class, () -> cut.contarPorEmpleadoRol(idEmpleadoRol));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<Long> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("Orden.contarPorEmpleadoRol", Long.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idEmpleadoRol", idEmpleadoRol)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getSingleResult()).thenReturn(2L);
        cut.em = mockEM;
        assertEquals(2L, cut.contarPorEmpleadoRol(idEmpleadoRol));
    }
}
