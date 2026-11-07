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
import sv.edu.ues.ppi115.cafefe.entity.ProductoCaracteristica;

public class ProductoCaracteristicaServiceTest {

    @Test
    public void constructorTest() {
        System.out.println("ProductoCaracteristicaServiceTest.constructorTest");
        ProductoCaracteristicaService cut = new ProductoCaracteristicaService();
        assertNotNull(cut);
        System.out.println("ProductoCaracteristicaServiceTest.constructorTest - finalizado");
    }

    @Test
    public void getEntityManagerTest() {
        System.out.println("ProductoCaracteristicaServiceTest.getEntityManagerTest");
        ProductoCaracteristicaService cut = new ProductoCaracteristicaService();
        assertNull(cut.getEntityManager());
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        cut.em = mockEM;
        assertEquals(mockEM, cut.getEntityManager());
        System.out.println("ProductoCaracteristicaServiceTest.getEntityManagerTest - finalizado");
    }

    @Test
    public void findByProductoTest() {
        UUID idProducto = UUID.randomUUID();
        ProductoCaracteristicaService cut = new ProductoCaracteristicaService();
        assertThrows(IllegalArgumentException.class, () -> cut.findByProducto(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByProducto(idProducto, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> cut.findByProducto(idProducto, 0, 0));
        assertThrows(IllegalStateException.class, () -> cut.findByProducto(idProducto, 0, 10));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<ProductoCaracteristica> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("ProductoCaracteristica.findByProducto", ProductoCaracteristica.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idProducto", idProducto)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setFirstResult(0)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setMaxResults(10)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getResultList()).thenReturn(Arrays.asList(new ProductoCaracteristica(UUID.randomUUID())));
        cut.em = mockEM;
        assertNotNull(cut.findByProducto(idProducto, 0, 10));
    }

    @Test
    public void contarPorProductoTest() {
        UUID idProducto = UUID.randomUUID();
        ProductoCaracteristicaService cut = new ProductoCaracteristicaService();
        assertThrows(IllegalArgumentException.class, () -> cut.contarPorProducto(null));
        assertThrows(IllegalStateException.class, () -> cut.contarPorProducto(idProducto));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery<Long> mockQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(mockEM.createNamedQuery("ProductoCaracteristica.contarPorProducto", Long.class)).thenReturn(mockQuery);
        Mockito.when(mockQuery.setParameter("idProducto", idProducto)).thenReturn(mockQuery);
        Mockito.when(mockQuery.getSingleResult()).thenReturn(5L);
        cut.em = mockEM;
        assertEquals(5L, cut.contarPorProducto(idProducto));
    }
}
