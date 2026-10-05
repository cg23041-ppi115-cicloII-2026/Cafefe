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
import org.primefaces.event.TabChangeEvent;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.DescuentoRepository;
import sv.edu.ues.ppi115.cafefe.control.DescuentoProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.ProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.TipoDescuentoRepository;
import sv.edu.ues.ppi115.cafefe.entity.Descuento;
import sv.edu.ues.ppi115.cafefe.entity.DescuentoProducto;
import sv.edu.ues.ppi115.cafefe.entity.Producto;
import sv.edu.ues.ppi115.cafefe.entity.TipoDescuento;

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

    @Inject
    private TipoDescuentoRepository tipoDescuentoRepository;

    // ---- Desplegables del formulario (mismo patron que ProductoModel) ----
    // tipoSeleccionado = primer nivel de la cascada de la ventana emergente
    private UUID tipoSeleccionado;
    private UUID descuentoSeleccionado;
    private UUID productoSeleccionado;
    private List<SelectItem> listaTipos;
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
        // una asignacion nueva empieza sin tipo, descuento ni producto elegidos
        this.tipoSeleccionado = null;
        this.descuentoSeleccionado = null;
        this.productoSeleccionado = null;
        this.listaDescuentos = null;
    }

    @Override
    public void btnGuardarHandler() {
        // Validaciones antes de tocar la base de datos
        if (this.registro != null) {
            if (this.descuentoSeleccionado == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Seleccione un descuento (tipo de descuento y descuento)", null));
                return;
            }
            if (this.productoSeleccionado == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN, "Seleccione un producto", null));
                return;
            }
            // Datos del descuento elegido en el cascada
            Descuento descuento = descuentoRepository.findById(this.descuentoSeleccionado);
            TipoDescuento tipo = (descuento != null) ? descuento.getIdTipoDescuento() : null;
            String nombreTipo = (tipo != null && tipo.getNombre() != null)
                    ? tipo.getNombre() : "(sin tipo)";

            // El tipo inactivo no se puede usar (los descuentos no tienen estado)
            if (tipo != null && Boolean.FALSE.equals(tipo.getActivo())) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "El tipo de descuento '" + nombreTipo + "' está inactivo", null));
                return;
            }

            // Valor: obligatorio, mayor a 0 y sin sobrepasar el maximo del tipo
            if (this.registro.getValor() == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "El valor no es válido. El valor no puede ser nulo", null));
                return;
            }
            if (this.registro.getValor() <= 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "El valor no es válido. El valor debe ser mayor a 0", null));
                return;
            }
            Integer maximo = (tipo != null) ? tipo.getDescuentoMaximo() : null;
            if (maximo != null && this.registro.getValor() > maximo) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "El valor no es válido. El valor no cumple la validación de "
                                + nombreTipo + ": debe ser menor o igual a " + maximo, null));
                return;
            }

            // Fechas: "desde" es obligatoria; "hasta" puede quedar vacia (descuento abierto)
            if (this.registro.getFechaDesde() == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "La fecha inicial no es válida. La fecha no puede quedar vacía", null));
                return;
            }
            if (this.registro.getFechaHasta() != null
                    && this.registro.getFechaDesde().after(this.registro.getFechaHasta())) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "La fecha final no es válida. La fecha de finalización no puede ser "
                                + "anterior a la fecha de inicio", null));
                return;
            }
            // Fechas: dentro del rango del descuento elegido
            if (descuento != null && descuento.getFechaDesde() != null
                    && this.registro.getFechaDesde().before(descuento.getFechaDesde())) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "La fecha inicial no es válida. La fecha de inicio debe ser después "
                                + "de la fecha inicial aplicable del descuento", null));
                return;
            }
            if (descuento != null && descuento.getFechaHasta() != null
                    && this.registro.getFechaHasta() != null
                    && this.registro.getFechaHasta().after(descuento.getFechaHasta())) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "La fecha final no es válida. La fecha de finalización no puede ser "
                                + "posterior a la de fin del descuento", null));
                return;
            }
            // FKs: aplica descuento y producto elegidos en los combos ANTES de persistir
            this.registro.setIdDescuento(descuento);
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
                // el tipo primero: su setter limpia la seleccion de descuento
                if (this.registro.getIdDescuento().getIdTipoDescuento() != null) {
                    setTipoSeleccionado(this.registro.getIdDescuento()
                            .getIdTipoDescuento().getIdTipoDescuento());
                }
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
     * Al cambiar de pestaña en Producto.xhtml se vacían los desplegables
     * de la ventana emergente (tipo y descuento) y el de productos, para
     * que muestren lo recién creado (carga perezosa: se recargan en el
     * siguiente render).
     */
    public void onTabChange(TabChangeEvent evento) {
        this.listaTipos = null;
        this.listaDescuentos = null;
        this.listaProductos = null;
    }

    /**
     * Tipos de descuento del primer select de la ventana emergente
     * (carga perezosa: una vez por vista). Los inactivos aparecen en
     * gris y no se pueden elegir.
     */
    public List<SelectItem> getListaTipos() {
        if (this.listaTipos == null) {
            this.listaTipos = new ArrayList<>();
            for (TipoDescuento t : tipoDescuentoRepository.findAll()) {
                boolean inactivo = Boolean.FALSE.equals(t.getActivo());
                this.listaTipos.add(new SelectItem(t.getIdTipoDescuento(),
                        t.getNombre(), null, inactivo));
            }
        }
        return this.listaTipos;
    }

    /**
     * Descuentos del tipo elegido (cascada: vacía hasta elegir el tipo).
     * Los descuentos no tienen campo activo.
     */
    public List<SelectItem> getListaDescuentos() {
        if (this.listaDescuentos == null) {
            this.listaDescuentos = new ArrayList<>();
            if (this.tipoSeleccionado != null) {
                TipoDescuento tipo = tipoDescuentoRepository.findById(this.tipoSeleccionado);
                UUID idTipo = (tipo != null) ? tipo.getIdTipoDescuento() : null;
                for (Descuento d : descuentoRepository.findAll()) {
                    if (idTipo != null && d.getIdTipoDescuento() != null
                            && idTipo.equals(d.getIdTipoDescuento().getIdTipoDescuento())) {
                        this.listaDescuentos.add(new SelectItem(
                                d.getIdDescuento(), d.getNombre()));
                    }
                }
            }
        }
        return this.listaDescuentos;
    }

    public UUID getTipoSeleccionado() {
        return tipoSeleccionado;
    }

    /**
     * Cambiar el tipo en la ventana emergente reinicia la selección de
     * descuento y recarga su lista (cascada Tipo > Descuento).
     */
    public void setTipoSeleccionado(UUID tipoSeleccionado) {
        this.tipoSeleccionado = tipoSeleccionado;
        this.descuentoSeleccionado = null;
        this.listaDescuentos = null;
    }

    /**
     * Nombre del descuento elegido que muestra el input del formulario
     * junto al botón de la ventana emergente.
     */
    public String getDescuentoElegido() {
        if (this.descuentoSeleccionado == null) {
            return "(ninguno)";
        }
        Descuento d = descuentoRepository.findById(this.descuentoSeleccionado);
        if (d == null) {
            return "(ninguno)";
        }
        return d.getNombre();
    }

    /**
     * Lista desplegable de productos (carga perezosa: una vez por vista).
     */
    public List<SelectItem> getListaProductos() {
        if (this.listaProductos == null) {
            this.listaProductos = new ArrayList<>();
            for (Producto p : productoRepository.findAll()) {
                // inactivo: se muestra en gris pero no se puede elegir
                boolean inactivo = Boolean.FALSE.equals(p.getActivo());
                this.listaProductos.add(new SelectItem(p.getIdProducto(), p.getNombre(),
                        null, inactivo));
            }
        }
        return this.listaProductos;
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
