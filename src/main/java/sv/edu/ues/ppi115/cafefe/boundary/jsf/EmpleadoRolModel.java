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
 * Asignacion Empleado &lt;-&gt; Rol (tabla empleado_rol, relacion N:N):
 * el formulario usa combo "Empleado" y combo "Rol" (UN rol por fila,
 * tal como el ejemplo de la rubrica); si un empleado necesita varios
 * roles se crean varias filas en la tabla.
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

    // ---- Formulario: combo empleado + combo rol ----
    private UUID empleadoSeleccionado;
    private List<SelectItem> listaEmpleados;
    private UUID rolSeleccionado;
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

    /**
     * Guardar = una fila de empleado_rol (un rol por asignacion).
     * Los combos estan habilitados tambien en edicion: si cambian, se
     * actualizan las FKs de la fila. Valida que no exista ya esa
     * combinacion empleado + rol (sin contar la propia fila al editar).
     */
    @Override
    public void btnGuardarHandler() {
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
        try {
            // Evita duplicados: una asignacion por (empleado, rol)
            for (EmpleadoRol er : empleadoRolRepository.findAll()) {
                boolean propiaFila = this.registro.getIdEmpleadoRol() != null
                        && this.registro.getIdEmpleadoRol().equals(er.getIdEmpleadoRol());
                if (!propiaFila
                        && er.getIdEmpleado() != null
                        && this.empleadoSeleccionado.equals(er.getIdEmpleado().getIdEmpleado())
                        && er.getIdRol() != null
                        && this.rolSeleccionado.equals(er.getIdRol().getIdRol())) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Ese empleado ya tiene ese rol asignado", null));
                    return;
                }
            }
            this.registro.setIdEmpleado(empleadoRepository.findById(this.empleadoSeleccionado));
            this.registro.setIdRol(rolRepository.findById(this.rolSeleccionado));
            if (this.estado == ESTADO_CRUD.MODIFICAR) {
                empleadoRolRepository.modificar(this.registro);
            } else {
                empleadoRolRepository.crear(this.registro);
            }

            // Confirmacion de exito: este guardar es personalizado (no pasa
            // por super), asi que avisa aqui (un solo mensaje limpio)
            boolean eraModificacion = (this.estado == ESTADO_CRUD.MODIFICAR);
            this.cargarRegistros();
            this.limpiar();
            this.formVisible = false;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    eraModificacion ? "Cambios guardados correctamente" : "Guardado correctamente",
                    null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "No se pudo guardar la asignacion: intente de nuevo", null));
        }
    }

    /**
     * Editar una fila de la tabla: carga en el form el empleado y el rol
     * de esa fila para que los combos los muestren seleccionados.
     */
    @Override
    public void btnEditarHandler() {
        super.btnEditarHandler();
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
                    "No se pudo eliminar la asignacion", null));
        }
    }

    /**
     * Lista desplegable de empleados (carga perezosa: una vez por vista):
     * los inactivos salen en gris, visibles pero no elegibles.
     */
    public List<SelectItem> getListaEmpleados() {
        if (this.listaEmpleados == null) {
            this.listaEmpleados = new ArrayList<>();
            for (Empleado e : empleadoRepository.findAll()) {
                boolean inactivo = Boolean.FALSE.equals(e.getActivo());
                this.listaEmpleados.add(new SelectItem(
                        e.getIdEmpleado(),
                        (e.getNombre() != null ? e.getNombre() : "") + " "
                        + (e.getApellido() != null ? e.getApellido() : ""),
                        null, inactivo));
            }
        }
        return this.listaEmpleados;
    }

    /**
     * Lista desplegable de roles (carga perezosa: una vez por vista):
     * los inactivos salen en gris, visibles pero no elegibles.
     */
    public List<SelectItem> getListaRoles() {
        if (this.listaRoles == null) {
            this.listaRoles = new ArrayList<>();
            for (Rol r : rolRepository.findAll()) {
                boolean inactivo = Boolean.FALSE.equals(r.getActivo());
                this.listaRoles.add(new SelectItem(r.getIdRol(), r.getNombre(),
                        null, inactivo));
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
