package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.ProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.ProductoTipoProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.TipoProductoRepository;
import sv.edu.ues.ppi115.cafefe.entity.Producto;
import sv.edu.ues.ppi115.cafefe.entity.ProductoTipoProducto;
import sv.edu.ues.ppi115.cafefe.entity.TipoProducto;

/**
 * Producto: reutiliza AbstractModel (CRUD generico con ESTADO_CRUD) y
 * agrega filtros opcionales (nombre, activos, rango de precios) y el
 * desplegable de tipos de producto (al guardar deja la relacion en
 * producto_tipo_producto).
 */
@Named
@ViewScoped
public class ProductoModel extends AbstractModel<Producto, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Tipado concreto de la tabla: AbstractModel declara getRegistros()
     * como List<T> y el analizador EL de NetBeans no sigue el tipo T,
     * por eso marcaba "propiedad desconocida" en cada #{item...} de la
     * vista. Con el tipo explicito aqui, el IDE infiere item como Producto.
     * No cambia nada en tiempo de ejecucion.
     */
    @Override
    public List<Producto> getRegistros() {
        return super.getRegistros();
    }

    @Override
    public Producto getSeleccion() {
        return super.getSeleccion();
    }

    @Inject
    private ProductoRepository productoRepository;

    // Para el desplegable de tipos y para registrar la relacion al guardar
    @Inject
    private TipoProductoRepository tipoProductoRepository;

    @Inject
    private ProductoTipoProductoRepository productoTipoProductoRepository;

    // Tipo elegido en el desplegable (UUID; se convierte a entidad al guardar)
    private UUID tipoSeleccionado;

    // Lista del desplegable (se carga una sola vez por vista)
    private List<SelectItem> listaTipos;

    // Filtros opcionales para la vista
    private String filtroNombre;
    private BigDecimal filtroPrecioMin;
    private BigDecimal filtroPrecioMax;

    @Override
    public DefaultDAO<Producto, UUID> getDao() {
        return productoRepository;
    }

    @Override
    public Producto instanciarRegistro() {
        // Siempre con id nuevo: el PK es NOT NULL en la base de datos
        Producto r = new Producto(UUID.randomUUID());
        r.setActivo(Boolean.TRUE);
        r.setComentarios("");
        r.setPrecioSugerido(BigDecimal.ZERO); // NOT NULL en BD
        return r;
    }

    @Override
    public Producto getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(r -> r.getIdProducto().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(Producto dato) {
        return dato != null ? dato.getIdProducto() : null;
    }

    // ---------------------------------------------------------------
    // Desplegable de tipos + relacion con producto_tipo_producto
    // ---------------------------------------------------------------
    @Override
    public void limpiar() {
        super.limpiar();
        this.tipoSeleccionado = null; // un producto nuevo empieza sin tipo elegido
    }

    @Override
    public void btnGuardarHandler() {
        // Validacion minima: nombre y precio son obligatorios
        if (this.registro != null) {
            if (this.registro.getNombre() == null || this.registro.getNombre().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN, "El producto necesita nombre", null));
                return;
            }
            if (this.registro.getPrecioSugerido() == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN, "El producto necesita precio sugerido", null));
                return;
            }
        }
        // Estado coherente: si el registro apunta a un producto que YA existe
        // pero el estado quedo en CREAR (p. ej. tras un eliminar fallido),
        // guardarlo como MODIFICAR para no intentar un INSERT con ID duplicado.
        if (this.estado != ESTADO_CRUD.MODIFICAR
                && this.registro != null
                && this.registro.getIdProducto() != null
                && productoRepository.findById(this.registro.getIdProducto()) != null) {
            this.estado = ESTADO_CRUD.MODIFICAR;
        }
        // Recuerda los datos ANTES de guardar: super() limpia el registro
        UUID idProducto = (this.registro != null) ? this.registro.getIdProducto() : null;
        UUID idTipo = this.tipoSeleccionado;

        try {
            super.btnGuardarHandler(); // guarda el producto, recarga la tabla y limpia

            // Si se elegio un tipo, deja registrada la relacion en
            // producto_tipo_producto (solo si aun no existe para no duplicar)
            if (idProducto != null && idTipo != null) {
                boolean existe = productoTipoProductoRepository.findByProducto(idProducto).stream()
                        .anyMatch(f -> f.getIdTipoProducto() != null
                                && idTipo.equals(f.getIdTipoProducto().getIdTipoProducto()));
                if (!existe) {
                    ProductoTipoProducto fila = new ProductoTipoProducto(UUID.randomUUID());
                    fila.setFechaCreacion(new Date());
                    fila.setObservaciones("");
                    fila.setIdProducto(productoRepository.findById(idProducto));
                    fila.setIdTipoProducto(tipoProductoRepository.findById(idTipo));
                    productoTipoProductoRepository.crear(fila);
                }
            }
        } catch (Exception e) {
            // Sin esto la excepcion de EJB la traga MyFaces y NO se pinta nada
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "No se pudo guardar: verifique los datos e intente de nuevo", null));
        }
    }

    @Override
    public void btnEditarHandler() {
        super.btnEditarHandler();
        // Muestra en el combo el tipo que ya tiene asignado (si tiene)
        this.tipoSeleccionado = null;
        if (this.registro != null) {
            List<ProductoTipoProducto> filas
                    = productoTipoProductoRepository.findByProducto(this.registro.getIdProducto());
            if (!filas.isEmpty() && filas.get(0).getIdTipoProducto() != null) {
                this.tipoSeleccionado = filas.get(0).getIdTipoProducto().getIdTipoProducto();
            }
        }
    }

    /**
     * Eliminar con manejo de error: la base de datos tiene claves foraneas
     * ON DELETE RESTRICT, por lo que no deja borrar registros que tengan
     * datos relacionados. Se captura la excepcion para mostrar un mensaje
     * amigable en vez de fallar en silencio en el AJAX.
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
                    "No se puede eliminar: existen datos relacionados con este producto",
                    null));
        }
    }

    /**
     * Lista desplegable de tipos de producto (carga perezosa: una vez por vista).
     */
    public List<SelectItem> getListaTipos() {
        if (this.listaTipos == null) {
            this.listaTipos = new ArrayList<>();
            for (TipoProducto t : tipoProductoRepository.findAll()) {
                if (Boolean.FALSE.equals(t.getActivo())) {
                    continue; // los inactivos no se pueden asignar
                }
                this.listaTipos.add(new SelectItem(t.getIdTipoProducto(), t.getNombre()));
            }
        }
        return incluirActual(this.listaTipos, this.tipoSeleccionado, () -> {
            TipoProducto t = tipoProductoRepository.findById(this.tipoSeleccionado);
            return t == null ? null : new SelectItem(t.getIdTipoProducto(), t.getNombre());
        });
    }

    public UUID getTipoSeleccionado() {
        return tipoSeleccionado;
    }

    public void setTipoSeleccionado(UUID tipoSeleccionado) {
        this.tipoSeleccionado = tipoSeleccionado;
    }

    // ---- Filtros opcionales ----
    public void buscarPorNombre() {
        if (filtroNombre != null && !filtroNombre.isEmpty()) {
            this.registros = productoRepository.findByNombreLike(filtroNombre);
        } else {
            this.cargarRegistros();
        }
    }

    public void buscarActivos() {
        this.registros = productoRepository.findActivos();
    }

    public void buscarPorPrecio() {
        if (filtroPrecioMin != null && filtroPrecioMax != null) {
            this.registros = productoRepository.findByPrecioRange(filtroPrecioMin, filtroPrecioMax);
        } else {
            this.cargarRegistros();
        }
    }

    public String getFiltroNombre() {
        return filtroNombre;
    }

    public void setFiltroNombre(String filtroNombre) {
        this.filtroNombre = filtroNombre;
    }

    public BigDecimal getFiltroPrecioMin() {
        return filtroPrecioMin;
    }

    public void setFiltroPrecioMin(BigDecimal filtroPrecioMin) {
        this.filtroPrecioMin = filtroPrecioMin;
    }

    public BigDecimal getFiltroPrecioMax() {
        return filtroPrecioMax;
    }

    public void setFiltroPrecioMax(BigDecimal filtroPrecioMax) {
        this.filtroPrecioMax = filtroPrecioMax;
    }
}
