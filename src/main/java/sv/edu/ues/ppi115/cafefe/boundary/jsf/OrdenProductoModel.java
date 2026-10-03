package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.primefaces.PrimeFaces;
import sv.edu.ues.ppi115.cafefe.control.DescuentoProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.OrdenProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.OrdenRepository;
import sv.edu.ues.ppi115.cafefe.control.ProductoRepository;
import sv.edu.ues.ppi115.cafefe.entity.Descuento;
import sv.edu.ues.ppi115.cafefe.entity.DescuentoProducto;
import sv.edu.ues.ppi115.cafefe.entity.Orden;
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;
import sv.edu.ues.ppi115.cafefe.entity.Producto;
import sv.edu.ues.ppi115.cafefe.entity.TipoDescuento;

/**
 * Linea de la pestana "Detalle de Orden" (tabla orden_producto).
 *
 * La tabla de la pestana usa el paginador lazy de AbstractModel y muestra
 * TODAS las lineas de todos los ordenes. El formulario de la linea solo
 * crea registros (las lineas de una orden no se editan ni se borran):
 *
 *  - "Nuevo" muestra el formulario con un id_orden_producto nuevo (uuid).
 *  - El producto se elige en la ventana emergente y al elegirlo el campo
 *    Precio se llena con el precio sugerido del producto (editable).
 *  - "Guardar" valida en el handler (sin required JSF, un solo mensaje
 *    arriba) y la linea se guarda apuntando a la orden de la pestana
 *    "Tomar Orden"; si esa orden aun no se ha guardado, se avisa.
 */
@Named
@ViewScoped
public class OrdenProductoModel extends AbstractModel<OrdenProducto, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private OrdenProductoRepository ordenProductoRepository;

    @Inject
    private OrdenRepository ordenRepository;

    @Inject
    private ProductoRepository productoRepository;

    @Inject
    private DescuentoProductoRepository descuentoProductoRepository;

    /** Texto escrito en el autoComplete de la ventana "Seleccionar Producto". */
    private String productoBusqueda;

    // ---- Ventana "Descuento aplicable" ----
    /** Orden cuyos productos filtran los descuentos de la ventana. */
    private UUID idOrdenDescuentos;
    /** Descuentos aplicables a los productos de esa orden (se recalcula al abrir). */
    private List<SelectItem> listaDescuentosAplicables;
    /** Descuento elegido en el combo de la ventana. */
    private UUID descuentoAplicableSeleccionado;
    /** true = todo validado en el servidor, la ventana se puede abrir. */
    private boolean descuentosListos;

    public OrdenProductoModel() {
    }

    @Override
    public OrdenProductoRepository getDao() {
        return ordenProductoRepository;
    }

    @Override
    public OrdenProducto instanciarRegistro() {
        // Siempre con id nuevo: el PK es NOT NULL en la base de datos
        OrdenProducto r = new OrdenProducto(UUID.randomUUID());
        r.setObservaciones("");
        // producto, orden y precio se asignan al elegir producto / guardar
        return r;
    }

    @Override
    public OrdenProducto getRegistroById(Object id) {
        return ordenProductoRepository.findById((UUID) id);
    }

    @Override
    public Object getIdByRegistro(OrdenProducto dato) {
        return dato != null ? dato.getIdOrdenProducto() : null;
    }

    @Override
    public void limpiar() {
        super.limpiar();
        // una linea nueva empieza sin producto buscado en la ventana
        this.productoBusqueda = null;
    }

    // ---------------------------------------------------------------
    // Seleccion de producto (ventana emergente)
    // ---------------------------------------------------------------
    /**
     * Sugerencias del autocomplete: nombres de los productos ACTIVOS
     * (los inactivos no se listan, asi no se pueden elegir). Con
     * minQueryLength="0" una consulta vacia devuelve todos.
     */
    public List<String> completarProductos(String texto) {
        List<String> nombres = new ArrayList<>();
        for (Producto p : productoRepository.findActivos()) {
            if (texto == null || texto.trim().isEmpty()
                    || (p.getNombre() != null
                    && p.getNombre().toLowerCase().contains(texto.trim().toLowerCase()))) {
                nombres.add(p.getNombre());
            }
        }
        return nombres;
    }

    /**
     * "Seleccionar" de la ventana: confirma el producto escrito en el
     * autocomplete, lo asigna a la linea y pega en el campo Precio el
     * precio sugerido del producto (la usuaria puede ajustarlo).
     */
    public void btnSeleccionarProductoHandler() {
        if (this.productoBusqueda == null || this.productoBusqueda.trim().isEmpty()) {
            mensaje(FacesMessage.SEVERITY_WARN, "Escriba el nombre del producto");
            return;
        }
        Producto encontrado = null;
        for (Producto p : productoRepository.findActivos()) {
            if (p.getNombre() != null
                    && p.getNombre().equalsIgnoreCase(this.productoBusqueda.trim())) {
                encontrado = p;
                break;
            }
        }
        if (encontrado == null) {
            mensaje(FacesMessage.SEVERITY_WARN,
                    "No se encontró el producto indicado (o está inactivo)");
            return;
        }
        this.registro.setIdProducto(encontrado);
        this.registro.setPrecio(encontrado.getPrecioSugerido());
    }

    /** Nombre del producto elegido para el campo deshabilitado del formulario. */
    public String getProductoElegido() {
        if (this.registro == null || this.registro.getIdProducto() == null) {
            return "(ninguno)";
        }
        return this.registro.getIdProducto().getNombre();
    }

    // ---------------------------------------------------------------
    // Guardar la linea
    // ---------------------------------------------------------------
    /**
     * Guardar de la pestana "Detalle de Orden": la linea se guarda en la
     * tabla 'orden_producto' apuntando a la orden de la pestana "Tomar
     * Orden" (recibida desde la vista). Validacion manual en el handler
     * (sin required) con un solo mensaje arriba.
     */
    public void btnGuardarLinea(UUID idOrden) {
        if (this.registro == null || getDao() == null) {
            return;
        }
        Orden orden = (idOrden != null) ? ordenRepository.findById(idOrden) : null;
        if (orden == null) {
            mensaje(FacesMessage.SEVERITY_WARN,
                    "Primero guarde la orden en la pestaña Tomar Orden");
            return;
        }
        if (this.registro.getIdProducto() == null) {
            mensaje(FacesMessage.SEVERITY_WARN, "Seleccione un producto");
            return;
        }
        if (this.registro.getPrecio() == null || this.registro.getPrecio().signum() <= 0) {
            mensaje(FacesMessage.SEVERITY_WARN, "El precio debe ser mayor a 0");
            return;
        }
        this.registro.setIdOrden(orden);
        try {
            super.btnGuardarHandler(); // crea, recarga la tabla, limpia y avisa
        } catch (Exception e) {
            mensaje(FacesMessage.SEVERITY_WARN,
                    "No se pudo guardar: verifique los datos e intente de nuevo");
        }
    }

    /** Un solo mensaje limpio arriba (regla del proyecto). */
    private void mensaje(FacesMessage.Severity severidad, String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severidad, texto, null));
    }

    // ---------------------------------------------------------------
    // Ventana "Descuento aplicable"
    // ---------------------------------------------------------------
    /**
     * "Aplicar Descuento": valida que la orden de la pestana "Tomar
     * Orden" ya este guardada y que exista al menos un descuento
     * aplicable; solo si todo esta bien se abre la ventana (la revisa el
     * oncomplete del boton, bandera descuentosListos).
     */
    public void btnAbrirDescuentos(UUID idOrden) {
        this.descuentosListos = false;
        PrimeFaces.current().ajax().addCallbackParam("listos", false);
        
        Orden orden = (idOrden != null) ? ordenRepository.findById(idOrden) : null;
        if (orden == null) {
            mensaje(FacesMessage.SEVERITY_WARN,
                    "Primero guarde la orden en la pestaña Tomar Orden");
            return;
        }
        this.idOrdenDescuentos = idOrden;
        this.listaDescuentosAplicables = null; // se recalcula con los productos actuales
        this.descuentoAplicableSeleccionado = null;
        if (getListaDescuentosAplicables().isEmpty()) {
            mensaje(FacesMessage.SEVERITY_WARN,
                    "No hay descuentos aplicables a los productos de la orden");
            return;
        }
        this.descuentosListos = true;
        PrimeFaces.current().ajax().addCallbackParam("listos", true);
    }

    /**
     * Descuentos que aplican a los productos de la orden: solo los que
     * estan asignados a un producto de la orden (descuento_producto) y
     * cuyo rango de fechas cubre la FECHA EN QUE SE TOMO la orden.
     * Fecha vacia = sin ese limite: un descuento sin fecha final no
     * vence y siempre puede aplicarse. El tipo de descuento debe estar
     * activo. Etiqueta de la maqueta: "Cliente Platinum (20)%".
     */
    public List<SelectItem> getListaDescuentosAplicables() {
        if (this.listaDescuentosAplicables == null) {
            this.listaDescuentosAplicables = new ArrayList<>();
            Orden orden = (this.idOrdenDescuentos != null)
                    ? ordenRepository.findById(this.idOrdenDescuentos) : null;
            if (orden != null) {
                // Productos que componen la orden (sus lineas guardadas)
                Set<UUID> productosOrden = new HashSet<>();
                for (OrdenProducto linea : ordenProductoRepository.findByOrden(orden.getIdOrden())) {
                    if (linea.getIdProducto() != null) {
                        productosOrden.add(linea.getIdProducto().getIdProducto());
                    }
                }
                for (DescuentoProducto dp : descuentoProductoRepository.findAll()) {
                    if (dp.getIdProducto() == null || dp.getValor() == null
                            || !productosOrden.contains(dp.getIdProducto().getIdProducto())) {
                        continue;
                    }
                    if (!aplicaEnFecha(dp, orden.getFechaCreacion())) {
                        continue;
                    }
                    Descuento descuento = dp.getIdDescuento();
                    if (descuento == null) {
                        continue;
                    }
                    TipoDescuento tipo = descuento.getIdTipoDescuento();
                    if (tipo == null || Boolean.FALSE.equals(tipo.getActivo())) {
                        continue;
                    }
                    this.listaDescuentosAplicables.add(new SelectItem(
                            dp.getIdDescuentoProducto(), etiqueta(dp)));
                }
            }
        }
        return this.listaDescuentosAplicables;
    }

    /**
     * "Aplicar": descuenta el valor del descuento del precio de las
     * lineas de ESTA orden cuyo producto tenga ese descuento asignado y
     * anota el descuento en observaciones (la BD no tiene columna de
     * descuento en orden_producto). Se puede aplicar varias veces y
     * varias veces a la misma linea (acumulativo).
     */
    public void btnAplicarDescuentoHandler() {
        if (this.descuentoAplicableSeleccionado == null) {
            mensaje(FacesMessage.SEVERITY_WARN, "Seleccione un descuento");
            return;
        }
        Orden orden = (this.idOrdenDescuentos != null)
                ? ordenRepository.findById(this.idOrdenDescuentos) : null;
        DescuentoProducto dp = descuentoProductoRepository
                .findById(this.descuentoAplicableSeleccionado);
        if (orden == null || dp == null) {
            mensaje(FacesMessage.SEVERITY_WARN,
                    "El descuento seleccionado ya no está disponible");
            return;
        }
        // Revalidar lo mismo que al abrir: vigencia con la fecha de la orden
        if (!aplicaEnFecha(dp, orden.getFechaCreacion())) {
            mensaje(FacesMessage.SEVERITY_WARN,
                    "El descuento ya no aplica a la fecha de la orden");
            return;
        }
        Descuento descuento = dp.getIdDescuento();
        TipoDescuento tipo = (descuento != null) ? descuento.getIdTipoDescuento() : null;
        if (tipo == null || Boolean.FALSE.equals(tipo.getActivo())) {
            mensaje(FacesMessage.SEVERITY_WARN,
                    "El tipo de descuento del elegido está inactivo");
            return;
        }
        Integer valor = dp.getValor();
        if (valor == null || valor <= 0 || valor > 100) {
            mensaje(FacesMessage.SEVERITY_WARN, "El descuento no tiene un valor válido");
            return;
        }
        // Lineas de esta orden cuyo producto tenga el descuento asignado
        List<OrdenProducto> lineas = new ArrayList<>();
        for (OrdenProducto linea : ordenProductoRepository.findByOrden(orden.getIdOrden())) {
            if (linea.getIdProducto() != null && dp.getIdProducto() != null
                    && linea.getIdProducto().getIdProducto()
                            .equals(dp.getIdProducto().getIdProducto())) {
                lineas.add(linea);
            }
        }
        if (lineas.isEmpty()) {
            mensaje(FacesMessage.SEVERITY_WARN,
                    "La orden ya no tiene el producto de ese descuento");
            return;
        }
        String texto = etiqueta(dp);
        try {
            for (OrdenProducto linea : lineas) {
                if (linea.getPrecio() == null) {
                    continue;
                }
                linea.setPrecio(aplicar(linea.getPrecio(), valor));
                String obs = linea.getObservaciones();
                linea.setObservaciones(obs == null || obs.trim().isEmpty()
                        ? texto : obs + "; " + texto);
                ordenProductoRepository.modificar(linea);
            }
        } catch (Exception e) {
            mensaje(FacesMessage.SEVERITY_WARN, "No se pudo aplicar el descuento");
            return;
        }
        mensaje(FacesMessage.SEVERITY_INFO, "Registro actualizado con éxito");
    }

    /** Vigencia del descuento para la fecha en que se tomo la orden. */
    private boolean aplicaEnFecha(DescuentoProducto dp, Date fechaOrden) {
        if (fechaOrden == null) {
            return false;
        }
        LocalDate dia = aFechaLocal(fechaOrden);
        Descuento descuento = dp.getIdDescuento();
        return enRango(dia, dp.getFechaDesde(), dp.getFechaHasta())
                && descuento != null
                && enRango(dia, descuento.getFechaDesde(), descuento.getFechaHasta());
    }

    /** desde <= dia <= hasta; fecha vacia = sin ese limite (no vence). */
    private boolean enRango(LocalDate dia, Date desde, Date hasta) {
        if (desde != null && dia.isBefore(aFechaLocal(desde))) {
            return false;
        }
        if (hasta != null && dia.isAfter(aFechaLocal(hasta))) {
            return false;
        }
        return true;
    }

    private LocalDate aFechaLocal(Date fecha) {
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** Texto del descuento, igual en el combo y en observaciones: "Nombre (20)%". */
    private String etiqueta(DescuentoProducto dp) {
        String nombre = (dp.getIdDescuento() != null)
                ? dp.getIdDescuento().getNombre() : "Descuento";
        return nombre + " (" + dp.getValor() + ")%";
    }

    /** precio - valor% redondeado a 2 decimales (numeric(8,2)). */
    private BigDecimal aplicar(BigDecimal precio, Integer valor) {
        return precio.multiply(BigDecimal.valueOf(100 - valor))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    // ---------------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------------
    public String getProductoBusqueda() {
        return productoBusqueda;
    }

    public void setProductoBusqueda(String productoBusqueda) {
        this.productoBusqueda = productoBusqueda;
    }

    public UUID getDescuentoAplicableSeleccionado() {
        return descuentoAplicableSeleccionado;
    }

    public void setDescuentoAplicableSeleccionado(UUID descuentoAplicableSeleccionado) {
        this.descuentoAplicableSeleccionado = descuentoAplicableSeleccionado;
    }

    /** La ventana solo se abre si el servidor valido todo (oncomplete). */
    public boolean isDescuentosListos() {
        return descuentosListos;
    }
}
