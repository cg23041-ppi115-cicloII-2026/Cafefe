package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.Application;
import jakarta.faces.event.ValueChangeEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SesionUsuarioTest {

    private SesionUsuario sesion;

    @BeforeEach
    public void setUp() {
        sesion = new SesionUsuario();
        ContextoPrueba.colocarComoActual(new ContextoConAplicacion());
    }

    @AfterEach
    public void quitarContexto() {
        ContextoPrueba.colocarComoActual(null);
    }

    @Test
    public void testLocalidadInicialEsEspanol() {
        assertEquals("es", sesion.getLocalidad());
        assertEquals(Locale.forLanguageTag("es"), sesion.getLocale());
    }

    @Test
    public void testSetLocalidadValidaCambiaElIdioma() {
        sesion.setLocalidad("en");

        assertEquals("en", sesion.getLocalidad());
        assertEquals(Locale.ENGLISH, sesion.getLocale());
    }

    @Test
    public void testSetLocalidadInvalidaRegresaAlDefecto() {
        sesion.setLocalidad("xx");

        assertEquals("es", sesion.getLocalidad());
    }

    @Test
    public void testSetLocalidadNulaRegresaAlDefecto() {
        sesion.setLocalidad(null);

        assertEquals("es", sesion.getLocalidad());
    }

    @Test
    public void testSetLocalidadConElNombreVisibleTambienSirve() {
        sesion.setLocalidad("Português");

        assertEquals("pt", sesion.getLocalidad());
        assertEquals(Locale.forLanguageTag("pt"), sesion.getLocale());
    }

    @Test
    public void testListaIdiomasTieneLosCuatroIdiomas() {
        Map<String, Object> idiomas = sesion.getListaIdiomas();

        assertEquals(4, idiomas.size());
        assertTrue(idiomas.containsKey("Español"));
        assertTrue(idiomas.containsKey("English"));
        assertTrue(idiomas.containsKey("Português"));
        assertTrue(idiomas.containsKey("Français"));
        assertEquals(Locale.ENGLISH, idiomas.get("English"));
    }

    @Test
    public void testCambiarIdiomaCambiaYRegresaNull() {
        String resultado = sesion.cambiarIdioma("fr");

        assertNull(resultado);
        assertEquals("fr", sesion.getLocalidad());
        assertEquals(Locale.FRENCH, sesion.getLocale());
    }

    @Test
    public void testCambiarIdiomaInvalidoRegresaAlDefecto() {
        String resultado = sesion.cambiarIdioma("xx");

        assertNull(resultado);
        assertEquals("es", sesion.getLocalidad());
    }

    @Test
    public void testLocalidadChangedSinEventoNoCambiaNada() {
        sesion.localidadChanged(null);

        assertEquals("es", sesion.getLocalidad());
    }

    @Test
    public void testLocalidadChangedAplicaElIdiomaElegido() {
        ValueChangeEvent evento = mock(ValueChangeEvent.class);
        when(evento.getNewValue()).thenReturn("fr");

        sesion.localidadChanged(evento);

        assertEquals("fr", sesion.getLocalidad());
    }

    @Test
    public void testLocalidadChangedConValorDesconocidoNoCambiaNada() {
        ValueChangeEvent evento = mock(ValueChangeEvent.class);
        when(evento.getNewValue()).thenReturn("zz");

        sesion.localidadChanged(evento);

        assertEquals("es", sesion.getLocalidad());
    }

    @Test
    public void testSetListaIdiomasNulaNoLaCambia() {
        sesion.setListaIdiomas(null);

        assertEquals(4, sesion.getListaIdiomas().size());
    }

    private static class ContextoConAplicacion extends ContextoPrueba {

        private final Application aplicacion = mock(Application.class);

        @Override
        public Application getApplication() {
            return aplicacion;
        }
    }
}
