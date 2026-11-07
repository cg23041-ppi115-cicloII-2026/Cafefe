package sv.edu.ues.ppi115.cafefe.control;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import sv.edu.ues.ppi115.cafefe.entity.Caja;

public class CajaServiceTest {

    protected List<Caja> findResult ;

        public CajaServiceTest() {
        findResult = Arrays.asList(new Caja[]{
                new Caja(UUID.randomUUID()), new Caja(UUID.randomUUID()), new Caja(UUID.randomUUID())
        });
    }

    @Test
    public void crearTest() {
        System.out.println("CajaServiceTest.crearTest");
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        Caja caja = new Caja();
        CajaService cut = new CajaService();
        assertThrows(IllegalArgumentException.class, ()->cut.create(null));
        assertThrows(IllegalStateException.class, ()->cut.create(caja));
        cut.em = mockEM;
        cut.create(caja);
        System.out.println("CajaServiceTest.crearTest");
    }

    @Test
    public void eliminarTest() {
        System.out.println("CajaServiceTest.eliminarTest");
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        Caja eliminado = new Caja(UUID.randomUUID());
        CajaService cut = new CajaService();
        assertThrows(IllegalArgumentException.class, ()->cut.delete(null));
        assertThrows(IllegalStateException.class, ()->cut.delete(eliminado));
        Mockito.when(mockEM.contains(eliminado)).thenReturn(true);
        cut.em = mockEM;
        cut.delete(eliminado);
        Mockito.verify(mockEM, Mockito.times(1)).remove(eliminado);
        Mockito.when(mockEM.contains(eliminado)).thenReturn(false);
        Mockito.when(mockEM.merge(eliminado)).thenReturn(eliminado);
        cut.em = mockEM;
        cut.delete(eliminado);
        System.out.println("CajaServiceTest.eliminarTest");
    }

    @Test
    public void buscarPorRango(){
        System.out.println("CajaServiceTest.buscarPorRango");
        int first = 0;
        int max = 1000;
        CajaService cut = new CajaService();
        assertThrows(IllegalArgumentException.class, ()->cut.findRange(-1,10));
        assertThrows(IllegalArgumentException.class, ()->cut.findRange(10,-1));
        assertThrows(IllegalStateException.class, ()->cut.findRange(first,max));
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TypedQuery tqQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(tqQuery.getResultList()).thenReturn(findResult);
        Mockito.when(tqQuery.setFirstResult(first)).thenReturn(tqQuery);
        Mockito.when(tqQuery.setMaxResults(max)).thenReturn(tqQuery);
        Mockito.when(mockEM.createNamedQuery("Caja.findAll", Caja.class)).thenReturn(tqQuery);
        cut.em = mockEM;
        List<Caja> encontrados = cut.findRange(first,max);
        assertNotNull(encontrados);
        assertEquals(findResult.size(), encontrados.size());
    }

    @Test
    void contarTest() {
        System.out.println("CajaServiceTest.count");
        CajaService cut = new CajaService();
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            cut.contar();
        });
        EntityManager emMock = Mockito.mock(EntityManager.class);
        TypedQuery<Long> tqMock = Mockito.mock(TypedQuery.class);
        Mockito.when(tqMock.getSingleResult()).thenReturn(2L);
        Mockito.when(emMock.createNamedQuery("Caja.countAll", Long.class)).thenReturn(tqMock);
        cut.em = emMock;
        cut.contar();
    }

    @Test
    void update() {
        System.out.println("CajaServiceTest.update");
        CajaService cut = new CajaService();
        Caja modificado = new Caja(UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> {
            cut.update(null);
        });
        assertThrows(IllegalStateException.class, () -> {
            cut.update(modificado);
        });
        EntityManager emMock = Mockito.mock(EntityManager.class);
        Mockito.when(emMock.merge(modificado)).thenReturn(modificado);
        cut.em = emMock;
        Caja resultado = cut.update(modificado);
        assertNotNull(resultado);
        assertEquals(modificado, resultado);
    }

    @Test
    public void testFindById(){
        System.out.println("findById");
        UUID id=UUID.randomUUID();
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        CajaService cut = new CajaService();
        assertThrows(IllegalStateException.class,()->cut.findById(id));

        cut.em=mockEM;
        Caja resultado = cut.findById(id);
        assertNull(resultado);

        assertThrows(IllegalArgumentException.class,()->cut.findById(null));
    }
}