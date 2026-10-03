package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.entity.Orden;
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdenRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private Query consulta;

    private OrdenRepository repositorio;

    @BeforeEach
    public void setUp() {
        repositorio = new OrdenRepository() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    @Test
    public void testCrearConLineasSinIdOsinOrdenNoTocaLaBase() {
        repositorio.crearConLineas(null, List.of(new OrdenProducto()));
        repositorio.crearConLineas(new Orden(), List.of(new OrdenProducto()));

        verifyNoInteractions(em);
    }

    @Test
    public void testCrearConLineasPersisteTodoYAtaCadaLineaALaOrden() {
        Orden orden = new Orden(UUID.randomUUID());
        OrdenProducto linea1 = new OrdenProducto(UUID.randomUUID());
        OrdenProducto linea2 = new OrdenProducto(UUID.randomUUID());

        repositorio.crearConLineas(orden, List.of(linea1, linea2));

        verify(em, times(1)).persist(orden);
        verify(em, times(1)).persist(linea1);
        verify(em, times(1)).persist(linea2);
        assertSame(orden, linea1.getIdOrden());
        assertSame(orden, linea2.getIdOrden());
    }

    @Test
    public void testCrearConLineasNulasSoloPersisteLaOrden() {
        Orden orden = new Orden(UUID.randomUUID());

        repositorio.crearConLineas(orden, null);

        verify(em, times(1)).persist(orden);
        verify(em, times(1)).persist(any());
    }

    @Test
    public void testEliminarSinIdONuloNoTocaLaBase() {
        repositorio.eliminar(null);
        repositorio.eliminar(new Orden());

        verifyNoInteractions(em);
    }

    @Test
    public void testEliminarBorraLasLineasPrimeroYDespuesLaOrden() {
        Orden orden = new Orden(UUID.randomUUID());
        when(em.createQuery(anyString())).thenReturn(consulta);
        when(consulta.setParameter("id", orden.getIdOrden())).thenReturn(consulta);
        when(em.contains(orden)).thenReturn(false);
        when(em.find(Orden.class, orden.getIdOrden())).thenReturn(orden);

        repositorio.eliminar(orden);

        verify(consulta, times(1)).executeUpdate();
        verify(em, times(1)).remove(orden);
    }

    @Test
    public void testEliminarConOrdenManejadaLaRemueveSinBuscarla() {
        Orden orden = new Orden(UUID.randomUUID());
        when(em.createQuery(anyString())).thenReturn(consulta);
        when(consulta.setParameter("id", orden.getIdOrden())).thenReturn(consulta);
        when(em.contains(orden)).thenReturn(true);

        repositorio.eliminar(orden);

        verify(em, never()).find(any(), any());
        verify(em, times(1)).remove(orden);
    }
}
