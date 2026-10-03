package sv.edu.ues.ppi115.cafefe;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BundleTest {

    private static final String[] ARCHIVOS = {
        "CRUD.properties",
        "CRUD_es.properties",
        "CRUD_en.properties",
        "CRUD_fr.properties",
        "CRUD_pt.properties"
    };

    private Properties cargar(String archivo) throws IOException {
        Properties props = new Properties();
        try (InputStream entrada = BundleTest.class.getClassLoader()
                .getResourceAsStream("traducciones/" + archivo)) {
            assertNotNull(entrada, "No se encontro traducciones/" + archivo);
            props.load(entrada);
        }
        return props;
    }

    @Test
    public void testLosCincoArchivosTienenLasMismasClaves() throws IOException {
        Set<String> base = cargar("CRUD.properties").stringPropertyNames();

        assertFalse(base.isEmpty());
        for (String archivo : ARCHIVOS) {
            Set<String> otro = cargar(archivo).stringPropertyNames();
            assertEquals(base, otro, archivo + " difiere de las claves de CRUD.properties");
        }
    }

    @Test
    public void testNingunaTraduccionQuedaVacia() throws IOException {
        for (String archivo : ARCHIVOS) {
            Properties props = cargar(archivo);
            for (String clave : props.stringPropertyNames()) {
                assertFalse(props.getProperty(clave).isBlank(),
                        "Valor vacio para " + clave + " en " + archivo);
            }
        }
    }

    @Test
    public void testEstanLasClavesDeLosBotonesDeOrdenYDelDialogo() throws IOException {
        String[] claves = {
            "crud.seleccionar",
            "crud.seleccionarFila",
            "crud.seleccionarDescuento",
            "crud.seleccionarCaracteristica",
            "pagina.orden.nuevaOrden",
            "pagina.orden.cancelarOrden",
            "pagina.orden.aplicarDescuento",
            "pagina.orden.seleccionarProducto",
            "pagina.orden.seleccionar",
            "pagina.orden.aplicar"
        };
        for (String archivo : ARCHIVOS) {
            Properties props = cargar(archivo);
            for (String clave : claves) {
                assertTrue(props.containsKey(clave),
                        "Falta " + clave + " en " + archivo);
            }
        }
    }
}
