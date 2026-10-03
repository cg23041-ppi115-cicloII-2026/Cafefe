package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdenProductoRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<OrdenProducto> query;

    private OrdenProductoRepository repositorio;

    @BeforeEach
    public void setUp() {
        repositorio = new OrdenProductoRepository() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
        lenient().when(query.setParameter(anyString(), any())).thenReturn(query);
    }

    @Test
    public void testFindByPrecioDevuelveLaListaDelQuery() {
        BigDecimal precio = new BigDecimal("2.50");
        OrdenProducto linea = new OrdenProducto();
        when(em.createNamedQuery("OrdenProducto.findByPrecio", OrdenProducto.class))
                .thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(linea));

        List<OrdenProducto> resultado = repositorio.findByPrecio(precio);

        assertEquals(1, resultado.size());
        assertSame(linea, resultado.get(0));
        verify(query, times(1)).setParameter("precio", precio);
    }

    @Test
    public void testFindByObservacionesEnviaElTexto() {
        when(em.createNamedQuery("OrdenProducto.findByObservaciones", OrdenProducto.class))
                .thenReturn(query);

        repositorio.findByObservaciones("para llevar");

        verify(query, times(1)).setParameter("observaciones", "para llevar");
    }

    @Test
    public void testFindByOrdenEnviaElIdDeLaOrden() {
        UUID idOrden = UUID.randomUUID();
        when(em.createNamedQuery("OrdenProducto.findByOrden", OrdenProducto.class))
                .thenReturn(query);

        repositorio.findByOrden(idOrden);

        verify(query, times(1)).setParameter("idOrden", idOrden);
    }
}
