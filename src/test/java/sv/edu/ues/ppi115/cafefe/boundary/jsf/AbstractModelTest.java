package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.entity.TipoProducto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AbstractModelTest {

    @Mock
    private DefaultDAO<TipoProducto, UUID> daoMock;

    private ContextoPrueba contexto;

    private AbstractModel<TipoProducto, UUID> modelo;

    @BeforeEach
    public void setUp() {
        contexto = new ContextoPrueba();
        ContextoPrueba.colocarComoActual(contexto);

        modelo = new AbstractModel<TipoProducto, UUID>() {
            @Override
            public TipoProducto instanciarRegistro() {
                return new TipoProducto();
            }

            @Override
            public TipoProducto getRegistroById(Object id) {
                return daoMock.findById((UUID) id);
            }

            @Override
            public Object getIdByRegistro(TipoProducto dato) {
                return dato != null ? dato.getIdTipoProducto() : null;
            }

            @Override
            public DefaultDAO<TipoProducto, UUID> getDao() {
                return daoMock;
            }
        };
    }

    @AfterEach
    public void quitarContexto() {
        ContextoPrueba.colocarComoActual(null);
    }

    @Test
    public void testInitCargaRegistrosYLimpia() {
        TipoProducto t1 = new TipoProducto();
        when(daoMock.findAll()).thenReturn(new ArrayList<>(List.of(t1)));

        modelo.init();

        assertNotNull(modelo.getRegistros());
        assertEquals(1, modelo.getRegistros().size());
        assertNotNull(modelo.getRegistro());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertNull(modelo.getSeleccion());
        assertFalse(modelo.isFormVisible());
    }

    @Test
    public void testLimpiarCreaRegistroNuevoEnEstadoCrear() {
        TipoProducto t = new TipoProducto();
        modelo.seleccionarRegistro(t);
        modelo.setSeleccion(t);

        modelo.limpiar();

        assertNotNull(modelo.getRegistro());
        assertNotSame(t, modelo.getRegistro());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertNull(modelo.getSeleccion());
    }

    @Test
    public void testSeleccionarRegistroPoneEstadoModificar() {
        TipoProducto t = new TipoProducto();

        modelo.seleccionarRegistro(t);

        assertEquals(ESTADO_CRUD.MODIFICAR, modelo.getEstado());
        assertSame(t, modelo.getRegistro());
        assertTrue(modelo.isEditando());
    }

    @Test
    public void testBtnNuevoLimpiaYMuestraFormulario() {
        modelo.setFormVisible(false);

        modelo.btnNuevoHandler();

        assertTrue(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertNotNull(modelo.getRegistro());
    }

    @Test
    public void testBtnEditarConSeleccionAbreFormularioEnModoModificar() {
        TipoProducto t = new TipoProducto();
        modelo.setSeleccion(t);

        modelo.btnEditarHandler();

        assertTrue(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.MODIFICAR, modelo.getEstado());
        assertSame(t, modelo.getRegistro());
    }

    @Test
    public void testBtnEditarSinSeleccionNoHaceNada() {
        modelo.setSeleccion(null);

        modelo.btnEditarHandler();

        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.NINGUNO, modelo.getEstado());
    }

    @Test
    public void testBtnGuardarEnEstadoCrearLlamaACrearYConfirma() {
        modelo.limpiar();
        TipoProducto t = modelo.getRegistro();

        modelo.btnGuardarHandler();

        verify(daoMock, times(1)).crear(t);
        verify(daoMock, never()).modificar(any());
        verify(daoMock, times(1)).findAll();

        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());

        assertEquals(1, contexto.mensajes.size());
        assertEquals("Guardado correctamente", contexto.mensajes.get(0).getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testBtnGuardarEnEstadoModificarLlamaAModificarYConfirma() {
        TipoProducto t = new TipoProducto();
        modelo.seleccionarRegistro(t);

        modelo.btnGuardarHandler();

        verify(daoMock, times(1)).modificar(t);
        verify(daoMock, never()).crear(any());
        assertFalse(modelo.isFormVisible());

        assertEquals(1, contexto.mensajes.size());
        assertEquals("Registro actualizado con éxito", contexto.mensajes.get(0).getSummary());
    }

    @Test
    public void testBtnModificarHandlerActualizaYCierra() {
        TipoProducto t = new TipoProducto();
        modelo.setRegistro(t);
        modelo.setFormVisible(true);

        modelo.btnModificarHandler();

        verify(daoMock, times(1)).modificar(t);
        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
    }

    @Test
    public void testBtnEliminarInvocaAlDaoCierraYConfirma() {
        TipoProducto t = new TipoProducto();
        modelo.setRegistro(t);
        modelo.setFormVisible(true);

        modelo.btnEliminarHandler();

        verify(daoMock, times(1)).eliminar(t);
        assertFalse(modelo.isFormVisible());

        assertEquals(1, contexto.mensajes.size());
        assertEquals("Registro eliminado correctamente", contexto.mensajes.get(0).getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testBtnEliminarSinRegistroNoInvocaAlDao() {
        modelo.setRegistro(null);

        modelo.btnEliminarHandler();

        verify(daoMock, never()).eliminar(any());
        assertTrue(contexto.mensajes.isEmpty());
    }

    @Test
    public void testBtnCancelarLimpiaYCierraFormulario() {
        modelo.btnNuevoHandler();
        assertTrue(modelo.isFormVisible());

        modelo.btnCancelarHandler();

        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertNotNull(modelo.getRegistro());
        assertNull(modelo.getSeleccion());
    }

    @Test
    public void testIsEditandoReflejaElEstadoCrud() {
        modelo.limpiar();
        assertFalse(modelo.isEditando());

        modelo.seleccionarRegistro(new TipoProducto());
        assertTrue(modelo.isEditando());
    }
}
