package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.entity.TipoDescuento;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DefaultDAOTest {

    @Mock
    private EntityManager entityManagerMock;

    @Mock
    private CriteriaBuilder criteriaBuilderMock;

    @Mock
    private CriteriaQuery<TipoDescuento> criteriaQueryMock;

    @Mock
    private Root<TipoDescuento> rootMock;

    @Mock
    private TypedQuery<TipoDescuento> typedQueryMock;

    private DefaultDAO<TipoDescuento, UUID> defaultDAO;
    private TipoDescuento tipoDescuento;
    private UUID idPrueba;

    @BeforeEach
    public void setUp() {
        idPrueba = UUID.randomUUID();
        tipoDescuento = new TipoDescuento();
        tipoDescuento.setIdTipoDescuento(idPrueba);
        tipoDescuento.setNombre("Descuento Estudiantil");

        // Instanciamos la clase abstracta pasándole el tipo de ID UUID
        defaultDAO = new DefaultDAO<TipoDescuento, UUID>(TipoDescuento.class) {
            @Override
            protected EntityManager getEntityManager() {
                return entityManagerMock;
            }
        };
    }

    @Test
    public void testCrear() {
        defaultDAO.crear(tipoDescuento);
        verify(entityManagerMock, times(1)).persist(tipoDescuento);
    }

    @Test
    public void testModificar() {
        when(entityManagerMock.merge(tipoDescuento)).thenReturn(tipoDescuento);

        TipoDescuento resultado = defaultDAO.modificar(tipoDescuento);

        assertNotNull(resultado);
        assertEquals("Descuento Estudiantil", resultado.getNombre());
        verify(entityManagerMock, times(1)).merge(tipoDescuento);
    }

    @Test
    public void testEliminar() {
        when(entityManagerMock.merge(tipoDescuento)).thenReturn(tipoDescuento);

        defaultDAO.eliminar(tipoDescuento);

        verify(entityManagerMock, times(1)).merge(tipoDescuento);
        verify(entityManagerMock, times(1)).remove(tipoDescuento);
    }

    @Test
    public void testFindById() {
        when(entityManagerMock.find(TipoDescuento.class, idPrueba)).thenReturn(tipoDescuento);

        TipoDescuento resultado = defaultDAO.findById(idPrueba);

        assertNotNull(resultado);
        assertEquals(idPrueba, resultado.getIdTipoDescuento());
        verify(entityManagerMock, times(1)).find(TipoDescuento.class, idPrueba);
    }

    @Test
    public void testFindAll() {
        when(entityManagerMock.getCriteriaBuilder()).thenReturn(criteriaBuilderMock);
        when(criteriaBuilderMock.createQuery(TipoDescuento.class)).thenReturn(criteriaQueryMock);
        when(criteriaQueryMock.from(TipoDescuento.class)).thenReturn(rootMock);
        when(entityManagerMock.createQuery(criteriaQueryMock)).thenReturn(typedQueryMock);
        when(typedQueryMock.getResultList()).thenReturn(List.of(tipoDescuento));

        List<TipoDescuento> resultado = defaultDAO.findAll();

        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        verify(entityManagerMock, times(1)).createQuery(criteriaQueryMock);
    }
}