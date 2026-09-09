package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.entity.TipoProducto;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoProductoDAOTest {

    @Mock
    private EntityManager entityManagerMock;

    private DefaultDAO<TipoProducto, UUID> defaultDAO;
    private TipoProducto tipoProducto;
    private UUID idPrueba;

    @BeforeEach
    public void setUp() {
        idPrueba = UUID.randomUUID();
        tipoProducto = new TipoProducto();
        tipoProducto.setIdTipoProducto(idPrueba);
        tipoProducto.setNombre("Café Molido");

        defaultDAO = new DefaultDAO<TipoProducto, UUID>(TipoProducto.class) {
            @Override
            protected EntityManager getEntityManager() {
                return entityManagerMock;
            }
        };
    }

    @Test
    public void testCrear() {
        defaultDAO.crear(tipoProducto);
        verify(entityManagerMock, times(1)).persist(tipoProducto);
    }

    @Test
    public void testFindById() {
        when(entityManagerMock.find(TipoProducto.class, idPrueba)).thenReturn(tipoProducto);

        TipoProducto resultado = defaultDAO.findById(idPrueba);

        assertNotNull(resultado);
        assertEquals(idPrueba, resultado.getIdTipoProducto());
        verify(entityManagerMock, times(1)).find(TipoProducto.class, idPrueba);
    }
}