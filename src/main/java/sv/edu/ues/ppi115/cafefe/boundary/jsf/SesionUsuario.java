package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Bean de sesión encargado de la localización (idioma) de la aplicación.
 *
 * Mantiene la localidad escogida por el usuario durante toda la sesión y la
 * aplica de forma global: al ViewRoot actual (la respuesta sale en el nuevo
 * idioma) y al locale por defecto de la Application (las vistas nuevas también).
 *
 * @ManagedBean es obsoleto desde JSF 2.3, por eso se usa @Named (CDI),
 * que es el equivalente moderno.
 *
 * Uso en la vista XHTML:
 * <pre>
 *   &lt;h:selectOneMenu value="#{sesionUsuario.localidad}" onchange="this.form.submit()"&gt;
 *       &lt;f:selectItems value="#{sesionUsuario.listaidiomas}" /&gt;
 *       &lt;f:valueChangeListener type="..."/&gt;  (o p:ajax)
 *   &lt;/h:selectOneMenu&gt;
 * </pre>
 */
@Named
@SessionScoped
public class SesionUsuario implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Idioma por defecto de la aplicación. */
    private static final String IDIOMA_DEFECTO = "es";

    /**
     * Catálogo estático de idiomas: clave = texto visible en el combo,
     * valor = objeto Locale correspondiente. Instanciado como LinkedHashMap
     * para conservar el orden de inserción.
     */
    private static Map<String, Object> listaidiomas = new LinkedHashMap<>();
    static {
        listaidiomas.put("Español", Locale.forLanguageTag("es"));
        listaidiomas.put("English", Locale.ENGLISH);
        listaidiomas.put("Português", Locale.forLanguageTag("pt"));
        listaidiomas.put("Français", Locale.FRENCH);
    }

    /** Localidad actual escogida por el usuario (ej: "es", "en", "pt", "fr"). */
    private String localidad = IDIOMA_DEFECTO;

    // ---------------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------------
    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = validarLocalidad(localidad);
        aplicarLocale(getLocale());
    }

    public Map<String, Object> getListaidiomas() {
        return listaidiomas;
    }

    public void setListaidiomas(Map<String, Object> listaidiomas) {
        if (listaidiomas != null) {
            SesionUsuario.listaidiomas = listaidiomas;
        }
    }

    /**
     * Devuelve el Locale actual según la localidad de la sesión.
     */
    public Locale getLocale() {
        Locale locale = Locale.forLanguageTag(this.localidad);
        // Si no es un idioma soportado, regresa al defecto
        return locale.getLanguage().isEmpty() ? Locale.forLanguageTag(IDIOMA_DEFECTO) : locale;
    }

    // ---------------------------------------------------------------
    // Método del ValueChangeListener (estilo video): cambia el idioma
    // de la vista actual cuando el usuario selecciona en el combo
    // ---------------------------------------------------------------
    public void localidadChanged(ValueChangeEvent e) {
        if (e == null || e.getNewValue() == null) {
            return;
        }
        String nuevoValor = e.getNewValue().toString();
        for (Map.Entry<String, Object> entry : listaidiomas.entrySet()) {
            if (entry.getValue().toString().equals(nuevoValor)) {
                this.localidad = validarLocalidad(nuevoValor);
                aplicarLocale((Locale) entry.getValue());
                break;
            }
        }
    }

    /**
     * Acción alternativa para botones/enlaces de cambio de idioma.
     */
    public String cambiarIdioma(String nuevo) {
        this.localidad = validarLocalidad(nuevo);
        aplicarLocale(getLocale());
        return null; // re-renderiza la vista actual
    }

    // ---------------------------------------------------------------
    // Lógica privada
    // ---------------------------------------------------------------
    /**
     * Aplica el idioma de forma global:
     * 1) ViewRoot actual -> ESTA respuesta sale en el nuevo idioma.
     * 2) Application     -> las vistas nuevas también.
     */
    private void aplicarLocale(Locale locale) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            return; // fuera de un ciclo JSF
        }
        if (fc.getViewRoot() != null) {
            fc.getViewRoot().setLocale(locale);
        }
        fc.getApplication().setDefaultLocale(locale);
    }

    /**
     * Solo acepta localidades presentes en listaidiomas;
     * cualquier otro valor regresa al defecto.
     */
    private String validarLocalidad(String valor) {
        if (valor != null) {
            for (Map.Entry<String, Object> entry : listaidiomas.entrySet()) {
                if (entry.getValue().toString().equals(valor) || entry.getKey().equals(valor)) {
                    return entry.getValue().toString();
                }
            }
        }
        return IDIOMA_DEFECTO;
    }
}
