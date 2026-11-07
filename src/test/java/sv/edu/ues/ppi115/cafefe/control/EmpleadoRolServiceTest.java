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
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;

public class EmpleadoRolServiceTest {

    @Test
    public void constructorTest() {
        System.out.println("EmpleadoRolServiceTest.constructorTest");
        EmpleadoRolService cut = new EmpleadoRolService();
        assertNotNull(cut);
        System.out.println("EmpleadoRolServiceTest.constructorTest - finalizado");
    }

    @Test
    public void getEntityManagerTest() {
        System.out.println("EmpleadoRolServiceTest.getEntityManagerTest");
        EmpleadoRolService cut = new EmpleadoRolService();
        assertNull(cut.getEntityManager());
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        cut.em = mockEM;
        assertEquals(mockEM, cut.getEntityManager());
        System.out.println("EmpleadoRolServiceTest.getEntityManagerTest - finalizado");
    }

    @Test
    public void findByEmpleadoTest() {
        UUID idEmpleado = UUID.randomUUID();
        EmpleadoRolService cut = new EmpleadoRolService();
        assertThrows(IllegalArgumentException.class, () -> cut.findByEmpleado(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByEmpleado(idEmpleado, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByEmpleado(idEmpleado, 0, 0));
        assertThrows(IllegalStateException.class, () -> cut.findByEmpleado(idEmpleado, 0, 10));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<EmpleadoRol> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("EmpleadoRol.findByEmpleado", EmpleadoRol.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idEmpleado", idEmpleado)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setFirstResult(0)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setMaxResults(10)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(new EmpleadoRol(UUID.randomUUID())));
        cut.em = mockEM;
        assertNotNull(cut.findByEmpleado(idEmpleado, 0, 10));
    }

    @Test
    public void contarPorEmpleadoTest() {
        UUID idEmpleado = UUID.randomUUID();
        EmpleadoRolService cut = new EmpleadoRolService();
        assertThrows(IllegalArgumentException.class, () -> cut.contarPorEmpleado(null));
        assertThrows(IllegalStateException.class, () -> cut.contarPorEmpleado(idEmpleado));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<Long> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("EmpleadoRol.contarPorEmpleado", Long.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idEmpleado", idEmpleado)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getSingleResult()).thenReturn(2L);
        cut.em = mockEM;
        assertEquals(2L, cut.contarPorEmpleado(idEmpleado));
    }

    @Test
    public void findByRolTest() {
        UUID idRol = UUID.randomUUID();
        EmpleadoRolService cut = new EmpleadoRolService();
        assertThrows(IllegalArgumentException.class, () -> cut.findByRol(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByRol(idRol, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByRol(idRol, 0, 0));
        assertThrows(IllegalStateException.class, () -> cut.findByRol(idRol, 0, 10));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<EmpleadoRol> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("EmpleadoRol.findByRol", EmpleadoRol.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idRol", idRol)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setFirstResult(0)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setMaxResults(10)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(new EmpleadoRol(UUID.randomUUID())));
        cut.em = mockEM;
        assertNotNull(cut.findByRol(idRol, 0, 10));
    }

    @Test
    public void contarPorRolTest() {
        UUID idRol = UUID.randomUUID();
        EmpleadoRolService cut = new EmpleadoRolService();
        assertThrows(IllegalArgumentException.class, () -> cut.contarPorRol(null));
        assertThrows(IllegalStateException.class, () -> cut.contarPorRol(idRol));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<Long> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("EmpleadoRol.contarPorRol", Long.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idRol", idRol)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getSingleResult()).thenReturn(3L);
        cut.em = mockEM;
        assertEquals(3L, cut.contarPorRol(idRol));
    }
}
