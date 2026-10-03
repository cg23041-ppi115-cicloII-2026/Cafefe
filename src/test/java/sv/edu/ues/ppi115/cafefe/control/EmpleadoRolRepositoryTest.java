package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmpleadoRolRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<EmpleadoRol> query;

    private EmpleadoRolRepository repositorio;

    @BeforeEach
    public void setUp() {
        repositorio = new EmpleadoRolRepository() {
            @Override
            protected EntityManager getEntityManager() {
                return em;
            }
        };
    }

    @Test
    public void testFindCobradoresFiltraLosCuatroRolesPermitidos() {
        EmpleadoRol asignacion = new EmpleadoRol();
        when(em.createQuery(contains("cajero"), eq(EmpleadoRol.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(asignacion));

        List<EmpleadoRol> resultado = repositorio.findCobradores();

        assertEquals(1, resultado.size());
        assertSame(asignacion, resultado.get(0));
        verify(em, times(1)).createQuery(contains("mesero"), eq(EmpleadoRol.class));
    }
}
