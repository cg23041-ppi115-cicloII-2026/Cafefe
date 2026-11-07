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
import sv.edu.ues.ppi115.cafefe.entity.Factura;

public class FacturaServiceTest {

    @Test
    public void constructorTest() {
        System.out.println("FacturaServiceTest.constructorTest");
        FacturaService cut = new FacturaService();
        assertNotNull(cut);
        System.out.println("FacturaServiceTest.constructorTest - finalizado");
    }

    @Test
    public void getEntityManagerTest() {
        System.out.println("FacturaServiceTest.getEntityManagerTest");
        FacturaService cut = new FacturaService();
        assertNull(cut.getEntityManager());
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        cut.em = mockEM;
        assertEquals(mockEM, cut.getEntityManager());
        System.out.println("FacturaServiceTest.getEntityManagerTest - finalizado");
    }

    @Test
    public void findByCajaTest() {
        UUID idCaja = UUID.randomUUID();
        FacturaService cut = new FacturaService();
        assertThrows(IllegalArgumentException.class, () -> cut.findByCaja(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByCaja(idCaja, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByCaja(idCaja, 0, 0));
        assertThrows(IllegalStateException.class, () -> cut.findByCaja(idCaja, 0, 10));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<Factura> mockQuery = Mockito.mock(TypedQuery.class);
        List<Factura> expected = Arrays.asList(new Factura(UUID.randomUUID()));
        Mockito.when(mockEM.createNamedQuery("Factura.findByCaja", Factura.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idCaja", idCaja)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setFirstResult(0)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setMaxResults(10)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getResultList()).thenReturn(expected);
        cut.em = mockEM;
        assertEquals(expected, cut.findByCaja(idCaja, 0, 10));
    }

    @Test
    public void contarPorCajaTest() {
        UUID idCaja = UUID.randomUUID();
        FacturaService cut = new FacturaService();
        assertThrows(IllegalArgumentException.class, () -> cut.contarPorCaja(null));
        assertThrows(IllegalStateException.class, () -> cut.contarPorCaja(idCaja));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<Long> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("Factura.contarPorCaja", Long.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idCaja", idCaja)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getSingleResult()).thenReturn(2L);
        cut.em = mockEM;
        assertEquals(2L, cut.contarPorCaja(idCaja));
    }

    @Test
    public void findByEmpleadoRolTest() {
        UUID idEmpleadoRol = UUID.randomUUID();
        FacturaService cut = new FacturaService();
        assertThrows(IllegalArgumentException.class, () -> cut.findByEmpleadoRol(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByEmpleadoRol(idEmpleadoRol, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByEmpleadoRol(idEmpleadoRol, 0, 0));
        assertThrows(IllegalStateException.class, () -> cut.findByEmpleadoRol(idEmpleadoRol, 0, 10));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<Factura> mockQuery = Mockito.mock(TypedQuery.class);
        List<Factura> expected = Arrays.asList(new Factura(UUID.randomUUID()));
        Mockito.when(mockEM.createNamedQuery("Factura.findByEmpleadoRol", Factura.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idEmpleadoRol", idEmpleadoRol)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setFirstResult(0)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setMaxResults(10)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getResultList()).thenReturn(expected);
        cut.em = mockEM;
        assertEquals(expected, cut.findByEmpleadoRol(idEmpleadoRol, 0, 10));
    }

    @Test
    public void contarPorEmpleadoRolTest() {
        UUID idEmpleadoRol = UUID.randomUUID();
        FacturaService cut = new FacturaService();
        assertThrows(IllegalArgumentException.class, () -> cut.contarPorEmpleadoRol(null));
        assertThrows(IllegalStateException.class, () -> cut.contarPorEmpleadoRol(idEmpleadoRol));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<Long> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("Factura.contarPorEmpleadoRol", Long.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idEmpleadoRol", idEmpleadoRol)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getSingleResult()).thenReturn(4L);
        cut.em = mockEM;
        assertEquals(4L, cut.contarPorEmpleadoRol(idEmpleadoRol));
    }
}
