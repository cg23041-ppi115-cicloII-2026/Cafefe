package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.ProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.ProductoTipoProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.TipoProductoRepository;
import sv.edu.ues.ppi115.cafefe.entity.Producto;
import sv.edu.ues.ppi115.cafefe.entity.ProductoTipoProducto;
import sv.edu.ues.ppi115.cafefe.entity.TipoProducto;

/**
 * Modelo de vista para el CRUD de ProductoTipoProducto.
 *
 * Ademas del CRUD generico de AbstractModel, maneja:
 *  - dos listas desplegables (productos y tipos) para elegir las
 *    relaciones de la tabla intermedia;
 *  - filtros por producto, tipo y observaciones.
 */
@Named
@ViewScoped
public class ProductoTipoProductoModel extends AbstractModel<ProductoTipoProducto, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ProductoTipoProductoRepository productoTipoProductoRepository;

    // Repositorios extra: solo para cargar las listas desplegables
    @Inject
    private ProductoRepository productoRepository;

    @Inject
    private TipoProductoRepository tipoProductoRepository;

    // Selecciones de los desplegables (se guardan como UUID y se
    // convierten a las entidades Producto/TipoProducto al guardar)
    private UUID productoSeleccionado;
    private UUID tipoSeleccionado;

    // Listas de los desplegables (se cargan una sola vez por vista)
    private List<SelectItem> listaProductos;
    private List<SelectItem> listaTipos;

    // Filtros: los UUID llegan como texto desde el formulario
    private String filtroProducto;
    private String filtroTipoProducto;
    private String filtroObservaciones;

    public ProductoTipoProductoModel() {
    }

    @Override
    public ProductoTipoProducto instanciarRegistro() {
        ProductoTipoProducto r = new ProductoTipoProducto(UUID.randomUUID());
        r.setFechaCreacion(new Date()); // se crea sola al dar de alta
        return r;
    }

    @Override
    public ProductoTipoProducto getRegistroById(Object id) {
        return productoTipoProductoRepository.findById((UUID) id);
    }

    @Override
    public Object getIdByRegistro(ProductoTipoProducto dato) {
        return dato != null ? dato.getIdProductoTipoProducto() : null;
    }

    @Override
    public ProductoTipoProductoRepository getDao() {
        return productoTipoProductoRepository;
    }

    // ---------------------------------------------------------------
    // Ciclo del CRUD adaptado a las relaciones
    // ---------------------------------------------------------------
    @Override
    public void limpiar() {
        super.limpiar();
        // un registro nuevo no tiene relaciones elegidas todavia
        this.productoSeleccionado = null;
        this.tipoSeleccionado = null;
    }

    @Override
    public void btnGuardarHandler() {
        if (this.registro != null) {
            // Convierte los UUID elegidos en las entidades reales
            this.registro.setIdProducto(productoSeleccionado != null
                    ? productoRepository.findById(productoSeleccionado) : null);
            this.registro.setIdTipoProducto(tipoSeleccionado != null
                    ? tipoProductoRepository.findById(tipoSeleccionado) : null);
        }
        super.btnGuardarHandler();
    }

    @Override
    public void btnEditarHandler() {
        super.btnEditarHandler();
        // Al editar, los desplegables deben marcar lo que ya tiene el registro
        if (this.registro != null) {
            this.productoSeleccionado = registro.getIdProducto() != null
                    ? registro.getIdProducto().getIdProducto() : null;
            this.tipoSeleccionado = registro.getIdTipoProducto() != null
                    ? registro.getIdTipoProducto().getIdTipoProducto() : null;
        }
    }

    // ---------------------------------------------------------------
    // Listas de los desplegables (carga perezosa: una vez por vista)
    // ---------------------------------------------------------------
    public List<SelectItem> getListaProductos() {
        if (this.listaProductos == null) {
            this.listaProductos = new ArrayList<>();
            for (Producto p : productoRepository.findAll()) {
                this.listaProductos.add(new SelectItem(p.getIdProducto(), p.getNombre()));
            }
        }
        return this.listaProductos;
    }

    public List<SelectItem> getListaTipos() {
        if (this.listaTipos == null) {
            this.listaTipos = new ArrayList<>();
            for (TipoProducto t : tipoProductoRepository.findAll()) {
                this.listaTipos.add(new SelectItem(t.getIdTipoProducto(), t.getNombre()));
            }
        }
        return this.listaTipos;
    }

    // ---------------------------------------------------------------
    // Filtros
    // ---------------------------------------------------------------
    public void buscarPorProducto() {
        UUID id = convertirUUID(filtroProducto);
        if (id != null) {
            this.registros = productoTipoProductoRepository.findByProducto(id);
        } else {
            this.cargarRegistros(); // vacio o texto invalido: se muestra todo
        }
    }

    public void buscarPorTipoProducto() {
        UUID id = convertirUUID(filtroTipoProducto);
        if (id != null) {
            this.registros = productoTipoProductoRepository.findByTipoProducto(id);
        } else {
            this.cargarRegistros();
        }
    }

    public void buscarPorObservaciones() {
        if (this.filtroObservaciones != null && !this.filtroObservaciones.trim().isEmpty()) {
            this.registros = productoTipoProductoRepository.findByObservaciones(filtroObservaciones.trim());
        } else {
            this.cargarRegistros();
        }
    }

    /**
     * Convierte el texto del formulario a UUID de forma segura;
     * devuelve null si el texto esta vacio o no es un UUID valido
     * (UUID.fromString lanza IllegalArgumentException si no lo es).
     */
    private UUID convertirUUID(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(texto.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ---------------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------------
    public UUID getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public void setProductoSeleccionado(UUID productoSeleccionado) {
        this.productoSeleccionado = productoSeleccionado;
    }

    public UUID getTipoSeleccionado() {
        return tipoSeleccionado;
    }

    public void setTipoSeleccionado(UUID tipoSeleccionado) {
        this.tipoSeleccionado = tipoSeleccionado;
    }

    public String getFiltroProducto() {
        return filtroProducto;
    }

    public void setFiltroProducto(String filtroProducto) {
        this.filtroProducto = filtroProducto;
    }

    public String getFiltroTipoProducto() {
        return filtroTipoProducto;
    }

    public void setFiltroTipoProducto(String filtroTipoProducto) {
        this.filtroTipoProducto = filtroTipoProducto;
    }

    public String getFiltroObservaciones() {
        return filtroObservaciones;
    }

    public void setFiltroObservaciones(String filtroObservaciones) {
        this.filtroObservaciones = filtroObservaciones;
    }
}
