package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.entity.Rol;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RolRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Rol> query;

    private RolRepository repositorio;

    @BeforeEach
    public void setUp() {
        repositorio = new RolRepository() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
        lenient().when(query.setParameter(anyString(), any())).thenReturn(query);
    }

    @Test
    public void testFindByNombreDevuelveLaListaDelQuery() {
        Rol rol = new Rol(UUID.randomUUID());
        when(em.createNamedQuery("Rol.findByNombre", Rol.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(rol));

        List<Rol> resultado = repositorio.findByNombre("cajero");

        assertEquals(1, resultado.size());
        assertSame(rol, resultado.get(0));
        verify(query, times(1)).setParameter("nombre", "cajero");
    }
}
