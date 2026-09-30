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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRolRepository;
import sv.edu.ues.ppi115.cafefe.control.OrdenRepository;
import sv.edu.ues.ppi115.cafefe.control.ProductoCaracteristicaRepository;
import sv.edu.ues.ppi115.cafefe.control.ProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.DescuentoProductoRepository;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;
import sv.edu.ues.ppi115.cafefe.entity.Orden;
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;
import sv.edu.ues.ppi115.cafefe.entity.Producto;
import sv.edu.ues.ppi115.cafefe.entity.ProductoCaracteristica;
import sv.edu.ues.ppi115.cafefe.entity.DescuentoProducto;

/**
 * Tomar Orden: pestana 1 de Orden.xhtml (el flujo de caja).
 *
 * No extiende AbstractModel porque no es el CRUD de un registro: es un
 * "carrito". La orden vive editable EN MEMORIA mientras se toma (agregar,
 * quitar, cambiar precio); al pulsar "Guardar orden" se cierra para siempre
 * (1 INSERT de orden + N de lineas en UNA transaccion) y ya no se edita.
 *
 * Cada linea representa UNA unidad del producto: las ordenes no manejan
 * cantidad (si el cliente lleva dos, son dos lineas).
 *
 * Estados:
 *  - combo "Atendido por": solo roles cajero/gerente/administrador (findCobradores)
 *  - tabla lazy de productos: SOLO activos, con busqueda por nombre
 *  - al elegir producto se prellenan precio y caracteristicas, y se
 *    muestran sus descuentos (debajo de observaciones); si hay uno
 *    vigente aparece la casilla "Aplicar descuento"
 *  - carrito: List<OrdenProducto> en memoria (todavia sin orden)
 */
@Named
@ViewScoped
public class TomarOrdenModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ProductoRepository productoRepository;
    @Inject
    private EmpleadoRolRepository empleadoRolRepository;
    @Inject
    private OrdenRepository ordenRepository;
    @Inject
    private ProductoCaracteristicaRepository productoCaracteristicaRepository;
    @Inject
    private DescuentoProductoRepository descuentoProductoRepository;

    // ---------------------------------------------------------------
    // Cabecera de la orden
    // ---------------------------------------------------------------
    private UUID cobradorSeleccionado;
    private List<SelectItem> listaCobradores;

    // ---------------------------------------------------------------
    // Selector de productos (tabla lazy de activos)
    // ---------------------------------------------------------------
    private String busqueda;
    private Producto productoSeleccionado; // fila marcada en la tabla
    private List<ProductoCaracteristica> caracteristicas = new ArrayList<>();
    private LazyModelConsulta<Producto> productosLazy;

    // ---------------------------------------------------------------
    // Linea en edicion (se agrega al carrito con "Agregar a la orden")
    // ---------------------------------------------------------------
    private BigDecimal precioLinea;
    private String observacionesLinea = "";

    // ---------------------------------------------------------------
    // Descuento del producto elegido (mostrado bajo Observaciones)
    // ---------------------------------------------------------------
    private List<DescuentoProducto> descuentosProducto = new ArrayList<>();
    private Boolean aplicarDescuento = Boolean.FALSE;

    // ---------------------------------------------------------------
    // Carrito: lineas en memoria, todavia sin orden
    // ---------------------------------------------------------------
    private List<OrdenProducto> carrito = new ArrayList<>();

    // ---------------------------------------------------------------
    // Combo: solo asignaciones activas de cajero, gerente o administrador
    // ---------------------------------------------------------------
    public List<SelectItem> getListaCobradores() {
        if (this.listaCobradores == null) {
            this.listaCobradores = new ArrayList<>();
            for (EmpleadoRol er : empleadoRolRepository.findCobradores()) {
                String etiqueta = er.getIdEmpleado().getNombre() + " "
                        + er.getIdEmpleado().getApellido()
                        + " (" + er.getIdRol().getNombre() + ")";
                this.listaCobradores.add(new SelectItem(er.getIdEmpleadoRol(), etiqueta));
            }
        }
        return this.listaCobradores;
    }

    // ---------------------------------------------------------------
    // Tabla lazy de productos: solo activos + busqueda opcional por nombre
    // ---------------------------------------------------------------
    public LazyModelConsulta<Producto> getProductosLazy() {
        if (this.productosLazy == null) {
            this.productosLazy = new LazyModelConsulta<>(
                    (primero, tamano)
                    -> productoRepository.findActivosRange(primero, tamano, normalizar(this.busqueda)),
                    () -> productoRepository.countActivos(normalizar(this.busqueda)),
                    p -> p.getIdProducto());
        }
        return this.productosLazy;
    }

    /** Texto vacio o en blanco cuenta como "sin filtro". */
    private String normalizar(String texto) {
        if (texto == null) {
            return null;
        }
        String t = texto.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Al marcar una fila de productos: la linea se prellena con el
     * precio_sugerido, se cargan sus caracteristicas y sus descuentos
     * (y se desmarca "Aplicar descuento" para que nunca se arrastre
     * de otro producto).
     */
    public void onProductoSeleccionado() {
        if (this.productoSeleccionado != null) {
            this.precioLinea = this.productoSeleccionado.getPrecioSugerido();
            this.observacionesLinea = "";
            this.caracteristicas = productoCaracteristicaRepository
                    .findByProducto(this.productoSeleccionado.getIdProducto());
            this.descuentosProducto = descuentoProductoRepository
                    .findByProducto(this.productoSeleccionado.getIdProducto());
        } else {
            this.caracteristicas = new ArrayList<>();
            this.descuentosProducto = new ArrayList<>();
        }
        this.aplicarDescuento = Boolean.FALSE;
    }

    // ---------------------------------------------------------------
    // Descuentos del producto elegido
    // ---------------------------------------------------------------
    /** Descuentos del producto que estan vigentes HOY (casilla aplicable). */
    public List<DescuentoProducto> getDescuentosVigentes() {
        List<DescuentoProducto> vigentes = new ArrayList<>();
        for (DescuentoProducto d : this.descuentosProducto) {
            if (esVigente(d)) {
                vigentes.add(d);
            }
        }
        return vigentes;
    }

    /**
     * El descuento que se aplicaria al marcar la casilla: el de mayor
     * valor entre los vigentes (si hay varios).
     */
    public DescuentoProducto getDescuentoAplicable() {
        DescuentoProducto mejor = null;
        for (DescuentoProducto d : getDescuentosVigentes()) {
            if (mejor == null || valorDe(d) > valorDe(mejor)) {
                mejor = d;
            }
        }
        return mejor;
    }

    /**
     * Vigente = hoy dentro de las fechas del descuento_producto Y dentro
     * de las fechas del descuento general (null = sin ese limite).
     */
    private boolean esVigente(DescuentoProducto d) {
        LocalDate hoy = LocalDate.now();
        if (!enRango(hoy, d.getFechaDesde(), d.getFechaHasta())) {
            return false;
        }
        return d.getIdDescuento() != null
                && enRango(hoy, d.getIdDescuento().getFechaDesde(), d.getIdDescuento().getFechaHasta());
    }

    private boolean enRango(LocalDate hoy, Date desde, Date hasta) {
        if (desde != null && hoy.isBefore(aFechaLocal(desde))) {
            return false;
        }
        if (hasta != null && hoy.isAfter(aFechaLocal(hasta))) {
            return false;
        }
        return true;
    }

    private LocalDate aFechaLocal(Date fecha) {
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private int valorDe(DescuentoProducto d) {
        return d.getValor() == null ? 0 : d.getValor();
    }

    /** Texto de una linea de descuento, ej: "10% con Carné Estudiantil (estudiantil) — 10% — vigente del 27/09/2026 al 26/12/2026". */
    public String getDescripcionDescuento(DescuentoProducto d) {
        if (d == null) {
            return "";
        }
        String nombre = d.getIdDescuento() != null ? d.getIdDescuento().getNombre() : "Descuento";
        String tipo = (d.getIdDescuento() != null && d.getIdDescuento().getIdTipoDescuento() != null)
                ? d.getIdDescuento().getIdTipoDescuento().getNombre() : "";
        String valor = d.getValor() != null ? d.getValor() + "%" : "";
        String rango = formatoRango(d.getFechaDesde(), d.getFechaHasta());
        StringBuilder sb = new StringBuilder(nombre);
        if (!tipo.isEmpty()) {
            sb.append(" (").append(tipo).append(")");
        }
        if (!valor.isEmpty()) {
            sb.append(" — ").append(valor);
        }
        sb.append(esVigente(d) ? " — vigente " : " — NO vigente ").append(rango);
        return sb.toString();
    }

    private String formatoRango(Date desde, Date hasta) {
        if (desde == null && hasta == null) {
            return "";
        }
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
        if (desde == null) {
            return "hasta " + f.format(hasta);
        }
        if (hasta == null) {
            return "desde " + f.format(desde);
        }
        return "del " + f.format(desde) + " al " + f.format(hasta);
    }

    /**
     * Precio de la linea con el descuento aplicado (vista previa en la
     * interfaz y precio final al agregar la linea).
     */
    public BigDecimal getPrecioConDescuento() {
        DescuentoProducto aplicable = getDescuentoAplicable();
        if (aplicable == null || this.precioLinea == null) {
            return this.precioLinea;
        }
        return aplicar(this.precioLinea, aplicable.getValor());
    }

    /** precio - valor% redondeado a 2 decimales (numeric(8,2)). */
    private BigDecimal aplicar(BigDecimal precio, Integer valor) {
        if (valor == null || valor <= 0 || valor > 100) {
            return precio;
        }
        return precio.multiply(BigDecimal.valueOf(100 - valor))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    // ---------------------------------------------------------------
    // Carrito: agregar / quitar lineas
    // ---------------------------------------------------------------
    /**
     * Agrega la linea en edicion al carrito. Validaciones manuales (mismo
     * criterio que los guards del resto del proyecto, sin required JSF).
     * Si "Aplicar descuento" esta marcado, la linea se guarda con el
     * precio ya descontado y se anota el descuento en observaciones
     * (la BD no tiene columna de descuento en orden_producto).
     */
    public void agregarLinea() {
        if (this.productoSeleccionado == null) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Elija un producto de la tabla");
            return;
        }
        if (this.precioLinea == null || this.precioLinea.signum() <= 0) {
            mensaje(FacesMessage.SEVERITY_ERROR, "El precio debe ser mayor a 0");
            return;
        }

        // precio de venta al momento de tomar la orden (snapshot)
        BigDecimal precioFinal = this.precioLinea.setScale(2, RoundingMode.HALF_UP);
        String obsFinal = this.observacionesLinea == null ? "" : this.observacionesLinea;

        DescuentoProducto aplicable = Boolean.TRUE.equals(this.aplicarDescuento)
                ? getDescuentoAplicable() : null;
        if (aplicable != null) {
            precioFinal = aplicar(precioFinal, aplicable.getValor());
            String nota = "Descuento aplicado: "
                    + (aplicable.getIdDescuento() != null
                            ? aplicable.getIdDescuento().getNombre() : "")
                    + " (-" + aplicable.getValor() + "%)";
            obsFinal = obsFinal.isEmpty() ? nota : obsFinal + " [" + nota + "]";
        }

        OrdenProducto linea = new OrdenProducto(UUID.randomUUID());
        linea.setIdProducto(this.productoSeleccionado);
        linea.setPrecio(precioFinal);
        linea.setObservaciones(obsFinal);
        this.carrito.add(linea);

        // listo para la siguiente linea
        this.productoSeleccionado = null;
        this.precioLinea = null;
        this.observacionesLinea = "";
        this.caracteristicas = new ArrayList<>();
        this.descuentosProducto = new ArrayList<>();
        this.aplicarDescuento = Boolean.FALSE;

        mensaje(FacesMessage.SEVERITY_INFO,
                "Linea agregada - total de la orden: " + getTotal());
    }

    public void quitarLinea(OrdenProducto linea) {
        if (linea != null) {
            this.carrito.remove(linea);
            mensaje(FacesMessage.SEVERITY_INFO,
                    "Linea quitada - total de la orden: " + getTotal());
        }
    }

    // ---------------------------------------------------------------
    // Guardar (cierra la orden) y cancelar (descarta todo)
    // ---------------------------------------------------------------
    /**
     * Cierra la orden: INSERT de la orden + INSERT de todas sus lineas en
     * UNA sola transaccion (OrdenRepository.crearConLineas). Despues de
     * esto la orden ya no se puede editar; conserva el cobrador para poder
     * tomar la siguiente orden seguida.
     */
    public void guardarOrden() {
        if (this.cobradorSeleccionado == null) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Elija quien atiende la orden");
            return;
        }
        if (this.carrito.isEmpty()) {
            mensaje(FacesMessage.SEVERITY_ERROR, "La orden no tiene lineas");
            return;
        }
        try {
            EmpleadoRol atiende = empleadoRolRepository.findById(this.cobradorSeleccionado);
            if (atiende == null) {
                mensaje(FacesMessage.SEVERITY_ERROR, "No se encontro la asignacion empleado-rol");
                return;
            }
            Orden orden = new Orden(UUID.randomUUID());
            orden.setFechaCreacion(new Date());
            orden.setIdEmpleadoRol(atiende);
            ordenRepository.crearConLineas(orden, this.carrito);

            int cantidadLineas = this.carrito.size();
            BigDecimal total = getTotal();

            // orden cerrada: se limpia todo menos el cobrador
            this.carrito = new ArrayList<>();
            this.productoSeleccionado = null;
            this.precioLinea = null;
            this.observacionesLinea = "";
            this.caracteristicas = new ArrayList<>();
            this.descuentosProducto = new ArrayList<>();
            this.aplicarDescuento = Boolean.FALSE;
            this.busqueda = null;

            mensaje(FacesMessage.SEVERITY_INFO,
                    "Orden guardada: " + cantidadLineas + " linea(s) - total " + total);
        } catch (Exception e) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No se pudo guardar la orden");
        }
    }

    /** Descarta la orden en curso sin escribir nada en la BD. */
    public void cancelar() {
        this.cobradorSeleccionado = null;
        this.busqueda = null;
        this.productoSeleccionado = null;
        this.precioLinea = null;
        this.observacionesLinea = "";
        this.caracteristicas = new ArrayList<>();
        this.descuentosProducto = new ArrayList<>();
        this.aplicarDescuento = Boolean.FALSE;
        this.carrito = new ArrayList<>();
        mensaje(FacesMessage.SEVERITY_INFO, "Orden descartada");
    }

    // ---------------------------------------------------------------
    // Totales
    // ---------------------------------------------------------------
    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrdenProducto linea : this.carrito) {
            total = total.add(getSubtotal(linea));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /** Subtotal de la linea: cada linea es una unidad, asi que es el precio. */
    public BigDecimal getSubtotal(OrdenProducto linea) {
        if (linea == null || linea.getPrecio() == null) {
            return BigDecimal.ZERO;
        }
        return linea.getPrecio();
    }

    private void mensaje(FacesMessage.Severity severidad, String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severidad, texto, null));
    }

    // ---------------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------------
    public UUID getCobradorSeleccionado() {
        return cobradorSeleccionado;
    }

    public void setCobradorSeleccionado(UUID cobradorSeleccionado) {
        this.cobradorSeleccionado = cobradorSeleccionado;
    }

    public String getBusqueda() {
        return busqueda;
    }

    public void setBusqueda(String busqueda) {
        this.busqueda = busqueda;
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public void setProductoSeleccionado(Producto productoSeleccionado) {
        this.productoSeleccionado = productoSeleccionado;
    }

    public List<ProductoCaracteristica> getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(List<ProductoCaracteristica> caracteristicas) {
        this.caracteristicas = caracteristicas;
    }

    public BigDecimal getPrecioLinea() {
        return precioLinea;
    }

    public void setPrecioLinea(BigDecimal precioLinea) {
        this.precioLinea = precioLinea;
    }

    public String getObservacionesLinea() {
        return observacionesLinea;
    }

    public void setObservacionesLinea(String observacionesLinea) {
        this.observacionesLinea = observacionesLinea;
    }

    public List<DescuentoProducto> getDescuentosProducto() {
        return descuentosProducto;
    }

    public void setDescuentosProducto(List<DescuentoProducto> descuentosProducto) {
        this.descuentosProducto = descuentosProducto;
    }

    public Boolean getAplicarDescuento() {
        return aplicarDescuento;
    }

    public void setAplicarDescuento(Boolean aplicarDescuento) {
        this.aplicarDescuento = aplicarDescuento;
    }

    public List<OrdenProducto> getCarrito() {
        return carrito;
    }

    public void setCarrito(List<OrdenProducto> carrito) {
        this.carrito = carrito;
    }
}
