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
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRepository;
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRolRepository;
import sv.edu.ues.ppi115.cafefe.control.RolRepository;
import sv.edu.ues.ppi115.cafefe.entity.Empleado;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;
import sv.edu.ues.ppi115.cafefe.entity.Rol;

/**
 * Asignacion Empleado &lt;-&gt; Rol (tabla empleado_rol): reutiliza
 * AbstractModel (CRUD generico con ESTADO_CRUD).
 *
 * @author 659684
 */
@Named
@ViewScoped
public class EmpleadoRolModel extends AbstractModel<EmpleadoRol, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EmpleadoRolRepository empleadoRolRepository;

    @Inject
    private EmpleadoRepository empleadoRepository;

    @Inject
    private RolRepository rolRepository;

    // ---- Desplegables del formulario (como en ProductoModel) ----
    private UUID empleadoSeleccionado;
    private UUID rolSeleccionado;
    private List<SelectItem> listaEmpleados;
    private List<SelectItem> listaRoles;

    @Override
    public DefaultDAO<EmpleadoRol, UUID> getDao() {
        return empleadoRolRepository;
    }

    @Override
    public EmpleadoRol instanciarRegistro() {
        // Siempre con id nuevo: el PK es NOT NULL en la base de datos
        EmpleadoRol r = new EmpleadoRol(UUID.randomUUID());
        r.setActivo(Boolean.TRUE);
        r.setObservaciones("");
        // idEmpleado y idRol se asignan desde la vista (combos)
        return r;
    }

    @Override
    public EmpleadoRol getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(r -> r.getIdEmpleadoRol().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(EmpleadoRol dato) {
        return dato != null ? dato.getIdEmpleadoRol() : null;
    }

    @Override
    public void limpiar() {
        super.limpiar();
        // una asignacion nueva empieza sin empleado ni rol elegidos
        this.empleadoSeleccionado = null;
        this.rolSeleccionado = null;
    }

    @Override
    public void btnGuardarHandler() {
        // Validacion minima: la asignacion necesita empleado y rol
        if (this.registro != null) {
            if (this.empleadoSeleccionado == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN, "Seleccione un empleado", null));
                return;
            }
            if (this.rolSeleccionado == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN, "Seleccione un rol", null));
                return;
            }
            // Aplica las FKs elegidas en los combos ANTES de persistir
            this.registro.setIdEmpleado(empleadoRepository.findById(this.empleadoSeleccionado));
            this.registro.setIdRol(rolRepository.findById(this.rolSeleccionado));
        }
        try {
            super.btnGuardarHandler(); // guarda, recarga la tabla y limpia
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
        // Muestra en los combos el empleado y el rol ya asignados (si tiene)
        this.empleadoSeleccionado = null;
        this.rolSeleccionado = null;
        if (this.registro != null) {
            if (this.registro.getIdEmpleado() != null) {
                this.empleadoSeleccionado = this.registro.getIdEmpleado().getIdEmpleado();
            }
            if (this.registro.getIdRol() != null) {
                this.rolSeleccionado = this.registro.getIdRol().getIdRol();
            }
        }
    }

    /**
     * Eliminar con manejo de error: la base de datos tiene claves foraneas
     * ON DELETE RESTRICT en empleado_rol, por lo que capturamos la excepcion
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
                    "No se pudo eliminar la asignacion", null));
        }
    }

    /**
     * Lista desplegable de empleados (carga perezosa: una vez por vista).
     */
    public List<SelectItem> getListaEmpleados() {
        if (this.listaEmpleados == null) {
            this.listaEmpleados = new ArrayList<>();
            for (Empleado e : empleadoRepository.findAll()) {
                this.listaEmpleados.add(new SelectItem(
                        e.getIdEmpleado(),
                        (e.getNombre() != null ? e.getNombre() : "") + " "
                        + (e.getApellido() != null ? e.getApellido() : "")));
            }
        }
        return this.listaEmpleados;
    }

    /**
     * Lista desplegable de roles (carga perezosa: una vez por vista).
     */
    public List<SelectItem> getListaRoles() {
        if (this.listaRoles == null) {
            this.listaRoles = new ArrayList<>();
            for (Rol r : rolRepository.findAll()) {
                this.listaRoles.add(new SelectItem(r.getIdRol(), r.getNombre()));
            }
        }
        return this.listaRoles;
    }

    public UUID getEmpleadoSeleccionado() {
        return empleadoSeleccionado;
    }

    public void setEmpleadoSeleccionado(UUID empleadoSeleccionado) {
        this.empleadoSeleccionado = empleadoSeleccionado;
    }

    public UUID getRolSeleccionado() {
        return rolSeleccionado;
    }

    public void setRolSeleccionado(UUID rolSeleccionado) {
        this.rolSeleccionado = rolSeleccionado;
    }
}
