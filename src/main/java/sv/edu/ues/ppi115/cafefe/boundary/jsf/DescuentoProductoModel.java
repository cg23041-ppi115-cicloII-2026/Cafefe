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
import sv.edu.ues.ppi115.cafefe.control.DescuentoProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.ProductoRepository;
import sv.edu.ues.ppi115.cafefe.entity.Descuento;
import sv.edu.ues.ppi115.cafefe.entity.DescuentoProducto;
import sv.edu.ues.ppi115.cafefe.entity.Producto;

/**
 * Descuento aplicado a un producto (tabla descuento_producto): reutiliza
 * AbstractModel (CRUD generico con ESTADO_CRUD) y trae los desplegables de
 * descuento y producto.
 *
 * @author 659684
 */
@Named
@ViewScoped
public class DescuentoProductoModel extends AbstractModel<DescuentoProducto, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private DescuentoProductoRepository descuentoProductoRepository;

    @Inject
    private DescuentoRepository descuentoRepository;

    @Inject
    private ProductoRepository productoRepository;

    // ---- Desplegables del formulario (mismo patron que ProductoModel) ----
    private UUID descuentoSeleccionado;
    private UUID productoSeleccionado;
    private List<SelectItem> listaDescuentos;
    private List<SelectItem> listaProductos;

    @Override
    public DefaultDAO<DescuentoProducto, UUID> getDao() {
        return descuentoProductoRepository;
    }

    @Override
    public DescuentoProducto instanciarRegistro() {
        // Siempre con id nuevo: el PK es NOT NULL en la base de datos
        DescuentoProducto r = new DescuentoProducto(UUID.randomUUID());
        r.setValor(0);
        r.setObservaciones("");
        // descuento, producto y fechas se asignan desde la vista
        return r;
    }

    @Override
    public DescuentoProducto getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(r -> r.getIdDescuentoProducto().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(DescuentoProducto dato) {
        return dato != null ? dato.getIdDescuentoProducto() : null;
    }

    @Override
    public void limpiar() {
        super.limpiar();
        // una asignacion nueva empieza sin descuento ni producto elegidos
        this.descuentoSeleccionado = null;
        this.productoSeleccionado = null;
    }

    @Override
    public void btnGuardarHandler() {
        // Validaciones minimas antes de tocar la base de datos
        if (this.registro != null) {
            if (this.descuentoSeleccionado == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN, "Seleccione un descuento", null));
                return;
            }
            if (this.productoSeleccionado == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN, "Seleccione un producto", null));
                return;
            }
            if (this.registro.getValor() == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN, "Indique el valor del descuento", null));
                return;
            }
            if (this.registro.getFechaDesde() != null && this.registro.getFechaHasta() != null
                    && this.registro.getFechaDesde().after(this.registro.getFechaHasta())) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "La fecha desde no puede ser mayor que la fecha hasta", null));
                return;
            }
            // FKs: aplica descuento y producto elegidos en los combos ANTES de persistir
            this.registro.setIdDescuento(descuentoRepository.findById(this.descuentoSeleccionado));
            this.registro.setIdProducto(productoRepository.findById(this.productoSeleccionado));
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
        // Muestra en los combos lo ya asignado (si tiene)
        this.descuentoSeleccionado = null;
        this.productoSeleccionado = null;
        if (this.registro != null) {
            if (this.registro.getIdDescuento() != null) {
                this.descuentoSeleccionado = this.registro.getIdDescuento().getIdDescuento();
            }
            if (this.registro.getIdProducto() != null) {
                this.productoSeleccionado = this.registro.getIdProducto().getIdProducto();
            }
        }
    }

    /**
     * Eliminar con manejo de error: capturamos la excepcion para mostrar un
     * mensaje amigable en vez de fallar en silencio en el AJAX.
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
                    "No se pudo eliminar el descuento del producto", null));
        }
    }

    /**
     * Lista desplegable de descuentos (carga perezosa: una vez por vista).
     */
    public List<SelectItem> getListaDescuentos() {
        if (this.listaDescuentos == null) {
            this.listaDescuentos = new ArrayList<>();
            for (Descuento d : descuentoRepository.findAll()) {
                this.listaDescuentos.add(new SelectItem(d.getIdDescuento(), d.getNombre()));
            }
        }
        return this.listaDescuentos;
    }

    /**
     * Lista desplegable de productos (carga perezosa: una vez por vista).
     */
    public List<SelectItem> getListaProductos() {
        if (this.listaProductos == null) {
            this.listaProductos = new ArrayList<>();
            for (Producto p : productoRepository.findAll()) {
                if (Boolean.FALSE.equals(p.getActivo())) {
                    continue; // los inactivos no se pueden asignar
                }
                this.listaProductos.add(new SelectItem(p.getIdProducto(), p.getNombre()));
            }
        }
        return incluirActual(this.listaProductos, this.productoSeleccionado, () -> {
            Producto p = productoRepository.findById(this.productoSeleccionado);
            return p == null ? null : new SelectItem(p.getIdProducto(), p.getNombre());
        });
    }

    public UUID getDescuentoSeleccionado() {
        return descuentoSeleccionado;
    }

    public void setDescuentoSeleccionado(UUID descuentoSeleccionado) {
        this.descuentoSeleccionado = descuentoSeleccionado;
    }

    public UUID getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public void setProductoSeleccionado(UUID productoSeleccionado) {
        this.productoSeleccionado = productoSeleccionado;
    }
}
