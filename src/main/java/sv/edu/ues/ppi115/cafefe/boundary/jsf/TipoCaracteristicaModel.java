package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.TipoCaracteristicaRepository;
import sv.edu.ues.ppi115.cafefe.entity.TipoCaracteristica;

/**
 *
 * @author 659684
 */
@Named
@ViewScoped
public class TipoCaracteristicaModel extends AbstractModel<TipoCaracteristica, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private TipoCaracteristicaRepository tipoCaracteristicaRepository;

    @Override
    public DefaultDAO<TipoCaracteristica, UUID> getDao() {
        return tipoCaracteristicaRepository;
    }

    @Override
    public TipoCaracteristica instanciarRegistro() {
        TipoCaracteristica r = new TipoCaracteristica(UUID.randomUUID());
        r.setActivo(Boolean.TRUE);
        r.setObservaciones("");
        return r;
    }

    @Override
    public TipoCaracteristica getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(r -> r.getIdTipoCaracteristica().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(TipoCaracteristica dato) {
        return dato != null ? dato.getIdTipoCaracteristica() : null;
    }

    /**
     * Validacion de nombre obligatorio con un SOLO mensaje limpio en la
     * parte superior: el required del JSF duplicaba el mensaje (arriba y
     * junto al campo) y salia con el clientId de por medio.
     */
    @Override
    public void btnGuardarHandler() {
        if (this.registro != null
                && (this.registro.getNombre() == null || this.registro.getNombre().trim().isEmpty())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "Debe ingresar el nombre", null));
            return; // no se guarda sin nombre y el formulario queda abierto
        }
        try {
            super.btnGuardarHandler(); // guarda, recarga la tabla y limpia
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "No se pudo guardar: verifique los datos e intente de nuevo", null));
        }
    }

    /**
     * Eliminar con manejo de error: la base de datos tiene claves foraneas
     * ON DELETE RESTRICT desde caracteristica, por lo que capturamos la excepcion
     * para mostrar un mensaje amigable en vez de fallar en silencio en el AJAX.
     */
    @Override
    public void btnEliminarHandler() {
        try {
            super.btnEliminarHandler();
        } catch (Exception e) {
            // Estado limpio: no dejar el registro apuntando a la fila que no
            // se pudo borrar (evita un INSERT duplicado en el siguiente Guardar)
            this.limpiar();
            this.formVisible = false;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "No se puede eliminar: existen características de este tipo", null));
        }
    }

}
