package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.entity.Caracteristica;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CaracteristicaRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Caracteristica> query;

    private CaracteristicaRepository repositorio;

    @BeforeEach
    public void setUp() {
        repositorio = new CaracteristicaRepository() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
        lenient().when(query.setParameter(anyString(), any())).thenReturn(query);
    }

    @Test
    public void testFindByNombreDevuelveLaListaDelQuery() {
        Caracteristica caracteristica = new Caracteristica();
        when(em.createNamedQuery("Caracteristica.findByNombre", Caracteristica.class))
                .thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(caracteristica));

        List<Caracteristica> resultado = repositorio.findByNombre("Tamaño");

        assertEquals(1, resultado.size());
        assertSame(caracteristica, resultado.get(0));
        verify(query, times(1)).setParameter("nombre", "Tamaño");
    }

    @Test
    public void testFindByActivoEnviaElEstado() {
        when(em.createNamedQuery("Caracteristica.findByActivo", Caracteristica.class))
                .thenReturn(query);

        repositorio.findByActivo(true);

        verify(query, times(1)).setParameter("activo", true);
    }

    @Test
    public void testFindByObservacionesEnviaElTexto() {
        when(em.createNamedQuery("Caracteristica.findByObservaciones", Caracteristica.class))
                .thenReturn(query);

        repositorio.findByObservaciones("sin azúcar");

        verify(query, times(1)).setParameter("observaciones", "sin azúcar");
    }

    @Test
    public void testFindByTipoCaracteristicaEnviaElId() {
        UUID idTipo = UUID.randomUUID();
        when(em.createNamedQuery("Caracteristica.findByTipoCaracteristica", Caracteristica.class))
                .thenReturn(query);

        repositorio.findByTipoCaracteristica(idTipo);

        verify(query, times(1)).setParameter("idTipoCaracteristica", idTipo);
    }
}
