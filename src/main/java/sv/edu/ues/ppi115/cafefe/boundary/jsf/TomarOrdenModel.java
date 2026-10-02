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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DescuentoProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.DescuentoRepository;
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRolRepository;
import sv.edu.ues.ppi115.cafefe.control.OrdenProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.OrdenRepository;
import sv.edu.ues.ppi115.cafefe.control.ProductoRepository;
import sv.edu.ues.ppi115.cafefe.entity.Descuento;
import sv.edu.ues.ppi115.cafefe.entity.DescuentoProducto;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;
import sv.edu.ues.ppi115.cafefe.entity.Orden;
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;
import sv.edu.ues.ppi115.cafefe.entity.Producto;

/**
 * Orden.xhtml completo: pestana "Tomar Orden" (cabecera) y pestana
 * "Detalle de Orden" (lineas del producto).
 *
 * Logica incremental:
 *  - "Guardar Ordenes" INSERTA la orden (persona que la toma + fecha);
 *    una orden guardada NO se edita ni se cancela: sus campos quedan
 *    bloqueados en pantalla.
 *  - cada producto se INSERTA individualmente con "Guardar" del
 *    formulario de linea; las lineas no se editan ni se eliminan.
 *  - "Cancelar Ordenes" solo limpia el formulario (no toca la BD).
 *  - "Aplicar Descuento" recalcula el Precio de las lineas cuyo producto
 *    tenga el descuento vigente HOY y lo anota en Observaciones; el
 *    dialogo "Descuentos que aplican" solo lista descuentos aplicables
 *    (vigentes y con productos asignados).
 *
 * Roles: la combo de persona usa EmpleadoRolRepository.findCobradores
 * (cajero, gerente, administrador o mesero) y los inactivos salen en
 * gris. Las lineas se eligen con el dialogo de productos (solo activos).
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
    private OrdenProductoRepository ordenProductoRepository;
    @Inject
    private DescuentoProductoRepository descuentoProductoRepository;
    @Inject
    private DescuentoRepository descuentoRepository;

    // ---------------------------------------------------------------
    // Cabecera de la orden (pestana Tomar Orden)
    // ---------------------------------------------------------------
    /** Orden en curso: se crea en memoria y se INSERTA al guardar. */
    private Orden orden;
    /** true cuando ya hizo INSERT (despues no se edita ni se cancela). */
    private boolean ordenGuardada;
    /** Persona que toma la orden (PK de empleado_rol, combo). */
    private UUID empleadoRolSeleccionado;
    private List<SelectItem> listaCobradores;

    // ---------------------------------------------------------------
    // Lineas (pestana Detalle de Orden)
    // ---------------------------------------------------------------
    private List<OrdenProducto> lineas = new ArrayList<>();

    private boolean formLineaVisible;
    private UUID lineaId;
    private String productoTexto;              // valor del autocomplete
    private Producto productoSeleccionado;
    private BigDecimal precioLinea;
    private String observacionesLinea = "";

    // ---------------------------------------------------------------
    // Dialogo "Descuentos que aplican"
    // ---------------------------------------------------------------
    private UUID descuentoSeleccionado;
    private List<SelectItem> listaDescuentosAplicables;

    // ---------------------------------------------------------------
    // Cabecera: combo de persona (roles que toman orden) y orden
    // ---------------------------------------------------------------
    /**
     * Asignaciones de cajero, gerente, administrador o mesero; las
     * inactivas salen en gris y no se pueden elegir.
     */
    public List<SelectItem> getListaCobradores() {
        if (this.listaCobradores == null) {
            this.listaCobradores = new ArrayList<>();
            for (EmpleadoRol er : empleadoRolRepository.findCobradores()) {
                String etiqueta = er.getIdEmpleado().getNombre() + " "
                        + er.getIdEmpleado().getApellido()
                        + " (" + er.getIdRol().getNombre() + ")";
                boolean inactivo = Boolean.FALSE.equals(er.getActivo())
                        || Boolean.FALSE.equals(er.getIdEmpleado().getActivo())
                        || Boolean.FALSE.equals(er.getIdRol().getActivo());
                this.listaCobradores.add(new SelectItem(er.getIdEmpleadoRol(), etiqueta,
                        null, inactivo));
            }
        }
        return this.listaCobradores;
    }

    /** Orden en curso; si aun no existe se crea con id y fecha nuevos. */
    public Orden getOrden() {
        if (this.orden == null) {
            Orden nueva = new Orden(UUID.randomUUID());
            nueva.setFechaCreacion(new Date());
            this.orden = nueva;
        }
        return this.orden;
    }

    /**
     * Guardar Ordenes: INSERTA la orden. Una orden guardada no se edita
     * (el formulario queda bloqueado en la vista).
     */
    public void btnGuardarOrdenHandler() {
        if (this.ordenGuardada) {
            return;
        }
        if (this.empleadoRolSeleccionado == null) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Elija la persona que toma la orden");
            return;
        }
        Orden o = getOrden();
        if (o.getFechaCreacion() == null) {
            mensaje(FacesMessage.SEVERITY_ERROR, "La fecha de orden no puede quedar vacía");
            return;
        }
        EmpleadoRol persona = empleadoRolRepository.findById(this.empleadoRolSeleccionado);
        if (persona == null) {
            mensaje(FacesMessage.SEVERITY_ERROR, "La persona que toma la orden no es válida");
            return;
        }
        o.setIdEmpleadoRol(persona);
        try {
            ordenRepository.crear(o);
        } catch (Exception e) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No se pudo guardar la orden");
            return;
        }
        this.ordenGuardada = true;
        this.lineas = new ArrayList<>();
        mensaje(FacesMessage.SEVERITY_INFO, "Guardado correctamente");
    }

    /**
     * Nuevo Ordenes: estado limpio para capturar OTRA orden (la anterior
     * queda guardada en la BD; una orden guardada no se cancela).
     */
    public void btnNuevaOrdenHandler() {
        this.orden = null;         // se recrea con id y fecha nuevos
        this.ordenGuardada = false;
        this.empleadoRolSeleccionado = null;
        this.lineas = new ArrayList<>();
        limpiarFormularioLinea();
        this.descuentoSeleccionado = null;
    }

    /**
     * Cancelar Ordenes: solo limpia el formulario; no borra ni modifica
     * nada en la BD (una orden guardada no se cancela).
     */
    public void btnCancelarOrdenHandler() {
        if (!this.ordenGuardada) {
            this.orden = null;     // id y fecha nuevos
            this.empleadoRolSeleccionado = null;
        }
        limpiarFormularioLinea();
        this.descuentoSeleccionado = null;
    }

    // ---------------------------------------------------------------
    // Lineas: abrir/cerrar formulario, guardar y dialogo de producto
    // ---------------------------------------------------------------
    /** Nuevo: abre el formulario de linea con id nuevo. */
    public void btnNuevoLineaHandler() {
        if (!this.ordenGuardada) {
            mensaje(FacesMessage.SEVERITY_ERROR,
                    "Primero guarde la orden en la pestaña Tomar Orden");
            return;
        }
        this.lineaId = UUID.randomUUID();
        this.productoTexto = null;
        this.productoSeleccionado = null;
        this.precioLinea = null;
        this.observacionesLinea = "";
        this.formLineaVisible = true;
    }

    /** Cancelar: cierra el formulario de linea sin tocar la BD. */
    public void btnCancelarLineaHandler() {
        limpiarFormularioLinea();
    }

    private void limpiarFormularioLinea() {
        this.formLineaVisible = false;
        this.lineaId = null;
        this.productoTexto = null;
        this.productoSeleccionado = null;
        this.precioLinea = null;
        this.observacionesLinea = "";
    }

    /**
     * Sugerencias del autocomplete del dialogo de producto: SOLO
     * productos activos, por nombre (los inactivos no aparecen).
     */
    public List<String> buscarProductos(String consulta) {
        String q = (consulta == null) ? "" : consulta.trim().toLowerCase();
        List<String> nombres = new ArrayList<>();
        for (Producto p : productoRepository.findActivos()) {
            String nombre = p.getNombre();
            if (nombre == null || nombre.trim().isEmpty()) {
                continue;
            }
            if (q.isEmpty() || nombre.toLowerCase().contains(q)) {
                nombres.add(nombre);
            }
        }
        Collections.sort(nombres);
        return nombres;
    }

    /**
     * Seleccionar (dialogo de producto): toma el producto activo cuyo
     * nombre coincide y prellena el precio con el precio sugerido.
     */
    public void btnSeleccionarProductoHandler() {
        String texto = (this.productoTexto == null) ? "" : this.productoTexto.trim();
        if (texto.isEmpty()) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Elija un producto de la lista");
            return;
        }
        Producto encontrado = null;
        for (Producto p : productoRepository.findActivos()) {
            if (texto.equalsIgnoreCase(p.getNombre())) {
                encontrado = p;
                break;
            }
        }
        if (encontrado == null) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Elija un producto de la lista");
            return;
        }
        this.productoSeleccionado = encontrado;
        this.precioLinea = encontrado.getPrecioSugerido();
    }

    /**
     * Guardar linea: INSERT en orden_producto apuntando a la orden ya
     * guardada. Las lineas no se editan ni se eliminan.
     */
    public void btnGuardarLineaHandler() {
        if (!this.ordenGuardada) {
            mensaje(FacesMessage.SEVERITY_ERROR,
                    "Primero guarde la orden en la pestaña Tomar Orden");
            return;
        }
        if (this.productoSeleccionado == null) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Elija un producto");
            return;
        }
        if (this.precioLinea == null || this.precioLinea.signum() <= 0) {
            mensaje(FacesMessage.SEVERITY_ERROR, "El precio debe ser mayor a 0");
            return;
        }
        Orden gestionada = ordenRepository.findById(this.orden.getIdOrden());
        if (gestionada == null) {
            mensaje(FacesMessage.SEVERITY_ERROR, "La orden ya no existe");
            return;
        }
        Producto prod = productoRepository.findById(this.productoSeleccionado.getIdProducto());
        if (prod == null) {
            mensaje(FacesMessage.SEVERITY_ERROR, "El producto ya no existe");
            return;
        }
        OrdenProducto linea = new OrdenProducto(
                this.lineaId != null ? this.lineaId : UUID.randomUUID());
        linea.setIdOrden(gestionada);
        linea.setIdProducto(prod);
        linea.setPrecio(this.precioLinea.setScale(2, RoundingMode.HALF_UP));
        linea.setObservaciones(this.observacionesLinea == null
                ? "" : this.observacionesLinea.trim());
        try {
            ordenProductoRepository.crear(linea);
        } catch (Exception e) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No se pudo guardar la línea");
            return;
        }
        this.lineas = ordenProductoRepository.findByOrden(gestionada.getIdOrden());
        limpiarFormularioLinea();
        mensaje(FacesMessage.SEVERITY_INFO, "Guardado correctamente");
    }

    // ---------------------------------------------------------------
    // Dialogo "Descuentos que aplican"
    // ---------------------------------------------------------------
    /**
     * Descuentos aplicables: vigentes HOY (rango del descuento y de la
     * asignacion a producto) con valor y al menos un producto; los de
     * tipo inactivo no aparecen. Etiqueta: "Nombre (20)%".
     */
    public List<SelectItem> getListaDescuentosAplicables() {
        if (this.listaDescuentosAplicables == null) {
            Map<UUID, String> nombres = new LinkedHashMap<>();
            Map<UUID, Integer> valores = new LinkedHashMap<>();
            for (DescuentoProducto dp : descuentoProductoRepository.findAll()) {
                Descuento d = dp.getIdDescuento();
                if (d == null || d.getIdDescuento() == null) {
                    continue;
                }
                if (dp.getValor() == null || dp.getValor() <= 0) {
                    continue;
                }
                if (!esVigente(dp)) {
                    continue;
                }
                if (d.getIdTipoDescuento() != null
                        && Boolean.FALSE.equals(d.getIdTipoDescuento().getActivo())) {
                    continue;
                }
                UUID id = d.getIdDescuento();
                if (!nombres.containsKey(id)) {
                    nombres.put(id, d.getNombre());
                    valores.put(id, dp.getValor());
                } else if (dp.getValor() > valores.get(id)) {
                    valores.put(id, dp.getValor());
                }
            }
            this.listaDescuentosAplicables = new ArrayList<>();
            for (Map.Entry<UUID, String> e : nombres.entrySet()) {
                this.listaDescuentosAplicables.add(new SelectItem(
                        e.getKey(), e.getValue() + " (" + valores.get(e.getKey()) + ")%"));
            }
        }
        return this.listaDescuentosAplicables;
    }

    /**
     * Aplicar descuento: recalcula el Precio de cada linea cuyo producto
     * tenga el descuento elegido vigente hoy y anota el descuento en
     * Observaciones. Una linea ya anotada con ese descuento se salta
     * (no se aplica dos veces).
     */
    public void btnAplicarDescuentoHandler() {
        if (!this.ordenGuardada) {
            mensaje(FacesMessage.SEVERITY_ERROR,
                    "Primero guarde la orden en la pestaña Tomar Orden");
            return;
        }
        if (this.descuentoSeleccionado == null) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Seleccione un descuento");
            return;
        }
        Descuento desc = descuentoRepository.findById(this.descuentoSeleccionado);
        if (desc == null) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Seleccione un descuento");
            return;
        }
        List<OrdenProducto> actuales = ordenProductoRepository.findByOrden(this.orden.getIdOrden());
        int aplicadas = 0;
        for (OrdenProducto linea : actuales) {
            if (linea.getIdProducto() == null || linea.getPrecio() == null) {
                continue;
            }
            DescuentoProducto mejor = null;
            for (DescuentoProducto dp
                    : descuentoProductoRepository.findByProducto(linea.getIdProducto().getIdProducto())) {
                if (dp.getIdDescuento() == null || dp.getIdDescuento().getIdDescuento() == null
                        || desc.getIdDescuento() == null) {
                    continue;
                }
                if (!desc.getIdDescuento().equals(dp.getIdDescuento().getIdDescuento())) {
                    continue;
                }
                if (dp.getValor() == null || dp.getValor() <= 0 || !esVigente(dp)) {
                    continue;
                }
                if (mejor == null || dp.getValor() > mejor.getValor()) {
                    mejor = dp;
                }
            }
            if (mejor == null) {
                continue;
            }
            String obs = (linea.getObservaciones() == null) ? "" : linea.getObservaciones();
            String marca = "Descuento aplicado: "
                    + (desc.getNombre() == null ? "" : desc.getNombre());
            if (obs.contains(marca)) {
                continue;
            }
            linea.setPrecio(aplicar(linea.getPrecio(), mejor.getValor()));
            String nota = "[" + marca + " (-" + mejor.getValor() + "%)]";
            linea.setObservaciones(obs.trim().isEmpty() ? nota : obs + " " + nota);
            ordenProductoRepository.modificar(linea);
            aplicadas++;
        }
        this.lineas = ordenProductoRepository.findByOrden(this.orden.getIdOrden());
        if (aplicadas == 0) {
            mensaje(FacesMessage.SEVERITY_WARN,
                    "La orden no tiene productos con este descuento aplicable");
        } else {
            this.descuentoSeleccionado = null;
            mensaje(FacesMessage.SEVERITY_INFO,
                    "Descuento aplicado a " + aplicadas + " producto(s)");
        }
    }

    // ---------------------------------------------------------------
    // Vigencia de los descuentos y calculo del precio
    // ---------------------------------------------------------------
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

    /** precio - valor% redondeado a 2 decimales (numeric(8,2)). */
    private BigDecimal aplicar(BigDecimal precio, Integer valor) {
        if (valor == null || valor <= 0 || valor > 100) {
            return precio;
        }
        return precio.multiply(BigDecimal.valueOf(100 - valor))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private void mensaje(FacesMessage.Severity severidad, String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severidad, texto, null));
    }

    // ---------------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------------
    public boolean isOrdenGuardada() {
        return ordenGuardada;
    }

    public UUID getEmpleadoRolSeleccionado() {
        return empleadoRolSeleccionado;
    }

    public void setEmpleadoRolSeleccionado(UUID empleadoRolSeleccionado) {
        this.empleadoRolSeleccionado = empleadoRolSeleccionado;
    }

    public List<OrdenProducto> getLineas() {
        return lineas;
    }

    public boolean isFormLineaVisible() {
        return formLineaVisible;
    }

    public UUID getLineaId() {
        return lineaId;
    }

    public void setLineaId(UUID lineaId) {
        this.lineaId = lineaId;
    }

    public String getProductoTexto() {
        return productoTexto;
    }

    public void setProductoTexto(String productoTexto) {
        this.productoTexto = productoTexto;
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public void setProductoSeleccionado(Producto productoSeleccionado) {
        this.productoSeleccionado = productoSeleccionado;
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

    public UUID getDescuentoSeleccionado() {
        return descuentoSeleccionado;
    }

    public void setDescuentoSeleccionado(UUID descuentoSeleccionado) {
        this.descuentoSeleccionado = descuentoSeleccionado;
    }
}
