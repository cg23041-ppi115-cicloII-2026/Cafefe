package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.model.SelectItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.ppi115.cafefe.control.CaracteristicaRepository;
import sv.edu.ues.ppi115.cafefe.control.TipoCaracteristicaRepository;
import sv.edu.ues.ppi115.cafefe.entity.Caracteristica;
import sv.edu.ues.ppi115.cafefe.entity.TipoCaracteristica;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CaracteristicaModelTest {

    @Mock
    private CaracteristicaRepository caracteristicaRepository;

    @Mock
    private TipoCaracteristicaRepository tipoCaracteristicaRepository;

    @InjectMocks
    private CaracteristicaModel modelo;

    private ContextoPrueba contexto;

    @BeforeEach
    public void setUp() {
        contexto = new ContextoPrueba();
        ContextoPrueba.colocarComoActual(contexto);
    }

    @AfterEach
    public void quitarContexto() {
        ContextoPrueba.colocarComoActual(null);
    }

    private FacesMessage unicoMensaje() {
        assertEquals(1, contexto.mensajes.size());
        return contexto.mensajes.get(0);
    }

    private TipoCaracteristica prepararTipo() {
        TipoCaracteristica tipo = new TipoCaracteristica(UUID.randomUUID());
        tipo.setNombre("Tamaño");
        when(tipoCaracteristicaRepository.findById(modelo.getTipoSeleccionado())).thenReturn(tipo);
        return tipo;
    }

    @Test
    public void testInstanciarRegistroCreaActivoYObservacionesVacias() {
        Caracteristica c = modelo.instanciarRegistro();

        assertNotNull(c.getIdCaracteristica());
        assertEquals(Boolean.TRUE, c.getActivo());
        assertEquals("", c.getObservaciones());
    }

    @Test
    public void testLimpiarReiniciaElTipoSeleccionado() {
        modelo.setTipoSeleccionado(UUID.randomUUID());

        modelo.limpiar();

        assertNull(modelo.getTipoSeleccionado());
    }

    @Test
    public void testGuardarSinNombreAvisaYNoGuarda() {
        modelo.limpiar();

        modelo.btnGuardarHandler();

        assertEquals("Debe ingresar el nombre", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(caracteristicaRepository, never()).crear(any());
        verify(caracteristicaRepository, never()).modificar(any());
    }

    @Test
    public void testGuardarNombreEnBlancoAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("   ");

        modelo.btnGuardarHandler();

        assertEquals("Debe ingresar el nombre", unicoMensaje().getSummary());
        verify(caracteristicaRepository, never()).crear(any());
    }

    @Test
    public void testGuardarSinTipoAvisaYNoGuarda() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("Dulce");

        modelo.btnGuardarHandler();

        assertEquals("Debe elegir el tipo de caracteristica", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
        verify(caracteristicaRepository, never()).crear(any());
    }

    @Test
    public void testGuardarConNombreYTipoCreaElRegistro() {
        modelo.limpiar();
        Caracteristica c = modelo.getRegistro();
        c.setNombre("Dulce");
        modelo.setTipoSeleccionado(UUID.randomUUID());
        TipoCaracteristica tipo = prepararTipo();

        modelo.btnGuardarHandler();

        verify(caracteristicaRepository, times(1)).crear(c);
        verify(caracteristicaRepository, never()).modificar(any());
        assertSame(tipo, c.getIdTipoCaracteristica());
        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertEquals("Guardado correctamente", unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_INFO, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testGuardarRegistroExistenteEnEstadoCrearUsaModificar() {
        modelo.limpiar();
        Caracteristica existente = new Caracteristica(UUID.randomUUID());
        existente.setNombre("Dulce");
        modelo.setRegistro(existente);
        modelo.setTipoSeleccionado(UUID.randomUUID());
        TipoCaracteristica tipo = prepararTipo();
        when(caracteristicaRepository.findById(existente.getIdCaracteristica())).thenReturn(existente);

        modelo.btnGuardarHandler();

        verify(caracteristicaRepository, never()).crear(any());
        verify(caracteristicaRepository, times(1)).modificar(existente);
        assertSame(tipo, existente.getIdTipoCaracteristica());
        assertEquals("Registro actualizado con éxito", unicoMensaje().getSummary());
    }

    @Test
    public void testGuardarCuandoElDaoFallaAvisaSinRomper() {
        modelo.limpiar();
        modelo.getRegistro().setNombre("Dulce");
        modelo.setTipoSeleccionado(UUID.randomUUID());
        prepararTipo();
        doThrow(new RuntimeException("boom")).when(caracteristicaRepository).crear(any());

        modelo.btnGuardarHandler();

        assertEquals("No se pudo guardar: verifique los datos e intente de nuevo",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testEditarPrecargaElTipoDelRegistro() {
        UUID idTipo = UUID.randomUUID();
        Caracteristica c = new Caracteristica(UUID.randomUUID());
        c.setIdTipoCaracteristica(new TipoCaracteristica(idTipo));
        modelo.setSeleccion(c);

        modelo.btnEditarHandler();

        assertEquals(idTipo, modelo.getTipoSeleccionado());
        assertTrue(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.MODIFICAR, modelo.getEstado());
    }

    @Test
    public void testEditarSinTipoDejaElComboVacio() {
        Caracteristica c = new Caracteristica(UUID.randomUUID());
        modelo.setTipoSeleccionado(UUID.randomUUID());
        modelo.setSeleccion(c);

        modelo.btnEditarHandler();

        assertNull(modelo.getTipoSeleccionado());
    }

    @Test
    public void testEliminarConRestricionLimpiaYAvisa() {
        Caracteristica c = new Caracteristica(UUID.randomUUID());
        modelo.setRegistro(c);
        modelo.setFormVisible(true);
        doThrow(new RuntimeException("RESTRICT")).when(caracteristicaRepository).eliminar(c);

        modelo.btnEliminarHandler();

        assertFalse(modelo.isFormVisible());
        assertEquals(ESTADO_CRUD.CREAR, modelo.getEstado());
        assertNotSame(c, modelo.getRegistro());
        assertEquals("No se puede eliminar: existen productos con esta caracteristica",
                unicoMensaje().getSummary());
        assertEquals(FacesMessage.SEVERITY_WARN, contexto.mensajes.get(0).getSeverity());
    }

    @Test
    public void testOnTabChangeVaciaLaListaDeTipos() {
        when(tipoCaracteristicaRepository.findAll()).thenReturn(new ArrayList<>());

        modelo.getListaTipos();
        modelo.onTabChange(null);
        modelo.getListaTipos();

        verify(tipoCaracteristicaRepository, times(2)).findAll();
    }

    @Test
    public void testListaTiposMarcaInactivosEnGris() {
        TipoCaracteristica activo = new TipoCaracteristica();
        activo.setNombre("Tamaño");
        activo.setActivo(true);
        TipoCaracteristica inactivo = new TipoCaracteristica();
        inactivo.setNombre("Volumen");
        inactivo.setActivo(false);
        when(tipoCaracteristicaRepository.findAll())
                .thenReturn(new ArrayList<>(List.of(activo, inactivo)));

        List<SelectItem> lista = modelo.getListaTipos();

        assertEquals(2, lista.size());
        assertFalse(lista.get(0).isDisabled());
        assertTrue(lista.get(1).isDisabled());
    }
}
