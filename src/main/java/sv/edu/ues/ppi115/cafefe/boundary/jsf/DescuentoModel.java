package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.DescuentoRepository;
import sv.edu.ues.ppi115.cafefe.control.TipoDescuentoRepository;
import sv.edu.ues.ppi115.cafefe.entity.Descuento;
import sv.edu.ues.ppi115.cafefe.entity.TipoDescuento;

/**
 * Descuento (tabla descuento): reutiliza AbstractModel (CRUD generico con
 * ESTADO_CRUD) y trae el desplegable de tipos de descuento.
 *
 * @author 659684
 */
@Named
@ViewScoped
public class DescuentoModel extends AbstractModel<Descuento, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private DescuentoRepository descuentoRepository;

    @Inject
    private TipoDescuentoRepository tipoDescuentoRepository;

    // ---- Desplegable de tipo de descuento (mismo patron que ProductoModel) ----
    private UUID tipoSeleccionado;
    private List<SelectItem> listaTipos;

    @Override
    public DefaultDAO<Descuento, UUID> getDao() {
        return descuentoRepository;
    }

    @Override
    public Descuento instanciarRegistro() {
        // Siempre con id nuevo: el PK es NOT NULL en la base de datos
        Descuento r = new Descuento(UUID.randomUUID());
        r.setObservaciones("");
        // fechas (desde/hasta) y tipo se asignan desde la vista
        return r;
    }

    @Override
    public Descuento getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(r -> r.getIdDescuento().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(Descuento dato) {
        return dato != null ? dato.getIdDescuento() : null;
    }

    @Override
    public void limpiar() {
        super.limpiar();
        this.tipoSeleccionado = null; // un descuento nuevo empieza sin tipo elegido
    }

    @Override
    public void btnGuardarHandler() {
        // Validaciones minimas antes de tocar la base de datos
        if (this.registro != null) {
            if (this.registro.getNombre() == null || this.registro.getNombre().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN, "El descuento necesita nombre", null));
                return;
            }
            if (this.tipoSeleccionado == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN, "Seleccione un tipo de descuento", null));
                return;
            }
            if (this.registro.getFechaDesde() != null && this.registro.getFechaHasta() != null
                    && this.registro.getFechaDesde().after(this.registro.getFechaHasta())) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "La fecha desde no puede ser mayor que la fecha hasta", null));
                return;
            }
            // FK: aplica el tipo elegido en el combo ANTES de persistir
            this.registro.setIdTipoDescuento(tipoDescuentoRepository.findById(this.tipoSeleccionado));
        }
        try {
            super.btnGuardarHandler(); // guarda, recarga la tabla y limpia
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "No se pudo guardar: verifique los datos e intente de nuevo", null));
        }
    }

    @Override
    public void btnEditarHandler() {
        super.btnEditarHandler();
        // Muestra en el combo el tipo ya asignado (si tiene)
        this.tipoSeleccionado = null;
        if (this.registro != null && this.registro.getIdTipoDescuento() != null) {
            this.tipoSeleccionado = this.registro.getIdTipoDescuento().getIdTipoDescuento();
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

    /**
     * Lista desplegable de tipos de descuento (carga perezosa: una vez por vista).
     */
    public List<SelectItem> getListaTipos() {
        if (this.listaTipos == null) {
            this.listaTipos = new ArrayList<>();
            for (TipoDescuento t : tipoDescuentoRepository.findAll()) {
                this.listaTipos.add(new SelectItem(t.getIdTipoDescuento(), t.getNombre()));
            }
        }
        return this.listaTipos;
    }

    public UUID getTipoSeleccionado() {
        return tipoSeleccionado;
    }

    public void setTipoSeleccionado(UUID tipoSeleccionado) {
        this.tipoSeleccionado = tipoSeleccionado;
    }
}
