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
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;

public class OrdenProductoServiceTest {

    @Test
    public void constructorTest() {
        System.out.println("OrdenProductoServiceTest.constructorTest");
        OrdenProductoService cut = new OrdenProductoService();
        assertNotNull(cut);
        System.out.println("OrdenProductoServiceTest.constructorTest - finalizado");
    }

    @Test
    public void getEntityManagerTest() {
        System.out.println("OrdenProductoServiceTest.getEntityManagerTest");
        OrdenProductoService cut = new OrdenProductoService();
        assertNull(cut.getEntityManager());
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        cut.em = mockEM;
        assertEquals(mockEM, cut.getEntityManager());
        System.out.println("OrdenProductoServiceTest.getEntityManagerTest - finalizado");
    }

    @Test
    public void findByOrdenTest() {
        UUID idOrden = UUID.randomUUID();
        OrdenProductoService cut = new OrdenProductoService();
        assertThrows(IllegalArgumentException.class, () -> cut.findByOrden(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByOrden(idOrden, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByOrden(idOrden, 0, 0));
        assertThrows(IllegalStateException.class, () -> cut.findByOrden(idOrden, 0, 10));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<OrdenProducto> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("OrdenProducto.findByOrden", OrdenProducto.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idOrden", idOrden)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setFirstResult(0)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setMaxResults(10)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(new OrdenProducto(UUID.randomUUID())));
        cut.em = mockEM;
        assertNotNull(cut.findByOrden(idOrden, 0, 10));
    }

    @Test
    public void contarPorOrdenTest() {
        UUID idOrden = UUID.randomUUID();
        OrdenProductoService cut = new OrdenProductoService();
        assertThrows(IllegalArgumentException.class, () -> cut.contarPorOrden(null));
        assertThrows(IllegalStateException.class, () -> cut.contarPorOrden(idOrden));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<Long> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("OrdenProducto.contarPorOrden", Long.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idOrden", idOrden)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getSingleResult()).thenReturn(2L);
        cut.em = mockEM;
        assertEquals(2L, cut.contarPorOrden(idOrden));
    }

    @Test
    public void findByProductoTest() {
        UUID idProducto = UUID.randomUUID();
        OrdenProductoService cut = new OrdenProductoService();
        assertThrows(IllegalArgumentException.class, () -> cut.findByProducto(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByProducto(idProducto, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByProducto(idProducto, 0, 0));
        assertThrows(IllegalStateException.class, () -> cut.findByProducto(idProducto, 0, 10));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<OrdenProducto> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("OrdenProducto.findByProducto", OrdenProducto.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idProducto", idProducto)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setFirstResult(0)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setMaxResults(10)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(new OrdenProducto(UUID.randomUUID())));
        cut.em = mockEM;
        assertNotNull(cut.findByProducto(idProducto, 0, 10));
    }

    @Test
    public void contarPorProductoTest() {
        UUID idProducto = UUID.randomUUID();
        OrdenProductoService cut = new OrdenProductoService();
        assertThrows(IllegalArgumentException.class, () -> cut.contarPorProducto(null));
        assertThrows(IllegalStateException.class, () -> cut.contarPorProducto(idProducto));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<Long> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("OrdenProducto.contarPorProducto", Long.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idProducto", idProducto)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getSingleResult()).thenReturn(4L);
        cut.em = mockEM;
        assertEquals(4L, cut.contarPorProducto(idProducto));
    }
}
