package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.TipoDescuentoRepository;
import sv.edu.ues.ppi115.cafefe.entity.TipoDescuento;

/**
 * reutiliza AbstractModel (CRUD generico con ESTADO_CRUD) y TipoDescuentoRepository.
 */
@Named
@ViewScoped
public class TipoDescuentoModel extends AbstractModel<TipoDescuento, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private TipoDescuentoRepository tipoDescuentoRepository;

    @Override
    public DefaultDAO<TipoDescuento, UUID> getDao() {
        return tipoDescuentoRepository;
    }

    @Override
    public TipoDescuento instanciarRegistro() {
        // Siempre con id nuevo: el PK es NOT NULL en la base de datos
        TipoDescuento r = new TipoDescuento(UUID.randomUUID());
        r.setActivo(Boolean.TRUE);
        r.setObservaciones("");
        r.setDescuentoMaximo(0);
        return r;
    }

    @Override
    public TipoDescuento getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(r -> r.getIdTipoDescuento().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(TipoDescuento dato) {
        return dato != null ? dato.getIdTipoDescuento() : null;
    }

    /**
     * Guardar valida nombre y descuento maximo AQUI en el modelo (la vista no
     * usa required, igual que productoCaracteristica): asi el boton Cancelar
     * funciona aunque el formulario este vacio, y el aviso sale solo en la
     * parte superior sin duplicarse junto al campo.
     */
    @Override
    public void btnGuardarHandler() {
        if (this.registro != null) {
            if (this.registro.getNombre() == null || this.registro.getNombre().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN, "Debe ingresar el nombre", null));
                return; // no se guarda sin nombre y el formulario queda abierto
            }
            if (this.registro.getDescuentoMaximo() == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN, "Indique el descuento maximo", null));
                return; // no se guarda sin descuento maximo (el valor por defecto es 0)
            }
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
     * ON DELETE RESTRICT desde descuento, por lo que capturamos la excepcion
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
                    "No se puede eliminar: existen descuentos de este tipo", null));
        }
    }
}
