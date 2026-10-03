package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRolRepository;
import sv.edu.ues.ppi115.cafefe.control.OrdenRepository;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;
import sv.edu.ues.ppi115.cafefe.entity.Orden;

/**
 * Orden.xhtml, las dos pestanas:
 *
 *  - Pestana 1 "Tomar Orden": solo la CABECERA de la orden, lo que guarda
 *    la tabla 'orden' (id_orden, id_empleado_rol, fecha_creacion). El ID y
 *    la fecha se muestran bloqueados; la fecha se vuelve a escribir con la
 *    hora exacta del momento en que se guarda. Valida en el handler (sin
 *    required) y deja UN solo mensaje arriba. Una orden guardada no se
 *    edita ni se anula.
 *
 *  - Pestana 2 "Detalle de Orden": sus lineas (tabla orden_producto) las
 *    administra OrdenProductoModel; este modelo no interviene ahi.
 */
@Named
@ViewScoped
public class OrdenModel extends AbstractModel<Orden, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private OrdenRepository ordenRepository;

    @Inject
    private EmpleadoRolRepository empleadoRolRepository;

    // ---------------------------------------------------------------
    // Pestana 1 "Tomar Orden": cabecera de la orden
    // ---------------------------------------------------------------
    private UUID cobradorSeleccionado;
    private List<SelectItem> listaCobradores;
    /**
     * true = la orden actual ya se guardo: el formulario queda bloqueado
     * mostrandola (no se edita ni se anula); "Nuevo Ordenes" inicia la
     * siguiente.
     */
    private boolean ordenGuardada = false;

    /**
     * Nueva cabecera de orden: uuid nuevo y fecha de AHORA. El ID y la
     * fecha se muestran bloqueados; la fecha se vuelve a escribir en el
     * guardar con la hora exacta del momento en que se graba.
     */
    @Override
    public Orden instanciarRegistro() {
        Orden orden = new Orden(UUID.randomUUID());
        orden.setFechaCreacion(new Date());
        return orden;
    }

    /**
     * Asignaciones que pueden TOMAR orden: empleados con rol cajero,
     * gerente, administrador o mesero (lista fija en findCobradores, sin
     * campo en la BD). Devuelve tambien las inactivas: el combo las
     * muestra en gris y no deja elegirlas.
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

    /**
     * Guardar de la pestana 1: crea la cabecera de la orden en la tabla
     * 'orden' (id_orden, id_empleado_rol, fecha_creacion).
     *
     * Validacion manual (sin required en la vista, un solo mensaje arriba)
     * y SIEMPRE creacion: una orden guardada no se edita ni se anula.
     * Al terminar deja lista la orden siguiente, conservando al empleado
     * para poder tomar varias seguidas.
     */
    @Override
    public void btnGuardarHandler() {
        if (this.registro == null || getDao() == null) {
            return;
        }
        if (this.cobradorSeleccionado == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN, "Elija quien realiza la orden", null));
            return;
        }
        EmpleadoRol atiende = empleadoRolRepository.findById(this.cobradorSeleccionado);
        if (atiende == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "No se encontro la asignacion empleado-rol", null));
            return;
        }
        this.registro.setIdEmpleadoRol(atiende);
        this.registro.setFechaCreacion(new Date());
        getDao().crear(this.registro);

        // La orden guardada queda visible y el formulario se bloquea;
        // la siguiente se arranca con el boton "Nuevo Ordenes".
        this.ordenGuardada = true;

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO, "Guardado correctamente", null));
    }

    /**
     * "Nuevo Ordenes" (boton de pagina): arranca la siguiente orden.
     * Limpia TODO el formulario de Tomar Orden, incluido el empleado
     * elegido (nada se conserva de la orden anterior).
     */
    public void btnNuevaOrdenHandler() {
        this.limpiar();
    }

    /**
     * "Cancelar Ordenes" (boton de pagina): limpia SOLO el formulario de
     * Tomar Orden. No escribe nada en la base de datos: una orden ya
     * guardada sigue existiendo tal cual (las ordenes guardadas no se
     * cancelan) y la pestana Detalle de Orden no se toca.
     */
    public void btnCancelarOrdenHandler() {
        this.limpiar();
    }

    /**
     * Estado inicial del formulario: cabecera nueva (uuid y fecha de
     * ahora), sin empleado elegido, desbloqueada.
     */
    @Override
    public void limpiar() {
        super.limpiar();
        this.cobradorSeleccionado = null;
        this.ordenGuardada = false;
    }

    @Override
    public DefaultDAO<Orden, UUID> getDao() {
        return ordenRepository;
    }

    @Override
    public Orden getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(o -> o.getIdOrden().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(Orden dato) {
        return dato != null ? dato.getIdOrden() : null;
    }

    public UUID getCobradorSeleccionado() {
        return cobradorSeleccionado;
    }

    public void setCobradorSeleccionado(UUID cobradorSeleccionado) {
        this.cobradorSeleccionado = cobradorSeleccionado;
    }

    /** La vista lo usa para bloquear el combo y el boton Guardar. */
    public boolean isOrdenGuardada() {
        return ordenGuardada;
    }
}
