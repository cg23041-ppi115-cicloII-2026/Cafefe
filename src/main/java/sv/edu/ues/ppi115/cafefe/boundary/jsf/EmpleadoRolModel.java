package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.primefaces.model.DualListModel;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRepository;
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRolRepository;
import sv.edu.ues.ppi115.cafefe.control.RolRepository;
import sv.edu.ues.ppi115.cafefe.entity.Empleado;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;
import sv.edu.ues.ppi115.cafefe.entity.Rol;

/**
 * Asignacion Empleado &lt;-&gt; Rol (tabla empleado_rol, relacion N:N):
 * reutiliza AbstractModel y usa un p:pickList con DualListModel para que un
 * empleado pueda llevar VARIOS roles a la vez (sugerencia del docente).
 * Al guardar hace el "diff": inserta los roles nuevos y borra los quitados.
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

    // ---- Formulario: empleado + pickList de roles ----
    private UUID empleadoSeleccionado;
    private List<SelectItem> listaEmpleados;
    private DualListModel<Rol> roles;

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

    /**
     * El combo de empleado cambio (lo llama el p:ajax): descarta el pickList
     * para que se reconstruya con los roles que YA tiene ese empleado.
     */
    public void cambioEmpleado() {
        this.roles = null;
    }

    /**
     * PickList de roles: source = los que todavia NO tiene,
     * target = los que YA tiene asignados.
     */
    public DualListModel<Rol> getRoles() {
        if (this.roles == null) {
            List<Rol> todos = rolRepository.findAll();
            Set<UUID> idsAsignados = new HashSet<>();
            if (this.empleadoSeleccionado != null) {
                for (EmpleadoRol er : empleadoRolRepository.findAll()) {
                    if (er.getIdEmpleado() != null
                            && this.empleadoSeleccionado.equals(er.getIdEmpleado().getIdEmpleado())
                            && er.getIdRol() != null) {
                        idsAsignados.add(er.getIdRol().getIdRol());
                    }
                }
            }
            List<Rol> disponibles = new ArrayList<>();
            List<Rol> asignados = new ArrayList<>();
            for (Rol r : todos) {
                if (idsAsignados.contains(r.getIdRol())) {
                    // los ya asignados se muestran aunque el rol este inactivo
                    asignados.add(r);
                } else if (!Boolean.FALSE.equals(r.getActivo())) {
                    disponibles.add(r);
                }
            }
            this.roles = new DualListModel<>(disponibles, asignados);
        }
        return this.roles;
    }

    public void setRoles(DualListModel<Rol> roles) {
        this.roles = roles;
    }

    @Override
    public void limpiar() {
        super.limpiar();
        // una asignacion nueva empieza sin empleado ni roles elegidos
        this.empleadoSeleccionado = null;
        this.roles = null;
    }

    /**
     * Guardar = sincronizar empleado_rol con el pickList:
     * inserta los roles nuevos del target y borra los que se quitaron.
     * activo/observaciones del formulario se aplican a los roles NUEVOS y,
     * al Editar, ademas SOLO a la fila que se esta editando (las demas
     * filas del target no se tocan).
     */
    @Override
    public void btnGuardarHandler() {
        if (this.empleadoSeleccionado == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN, "Seleccione un empleado", null));
            return;
        }
        try {
            List<Rol> elegidos = getRoles().getTarget();

            // activo/observaciones del formulario: en Nuevo vienen de la
            // instancia nueva de registro, en Editar de la fila seleccionada
            final Boolean activoForm = this.registro != null && this.registro.getActivo() != null
                    ? this.registro.getActivo() : Boolean.TRUE;
            final String obsForm = this.registro != null
                    && this.registro.getObservaciones() != null
                    ? this.registro.getObservaciones() : "";
            // ¿Hay una fila existente seleccionada para editar?
            final boolean editandoFila = this.estado == ESTADO_CRUD.MODIFICAR
                    && this.registro != null && this.registro.getIdEmpleadoRol() != null;

            // Filas actuales de ese empleado en empleado_rol
            List<EmpleadoRol> existentes = new ArrayList<>();
            for (EmpleadoRol er : empleadoRolRepository.findAll()) {
                if (er.getIdEmpleado() != null
                        && this.empleadoSeleccionado.equals(er.getIdEmpleado().getIdEmpleado())) {
                    existentes.add(er);
                }
            }

            // 1) Quita los roles que ya NO estan en el target del pickList;
            //    si la fila editada sigue vigente, le aplica lo del formulario
            for (EmpleadoRol er : existentes) {
                final UUID idRolFila = er.getIdRol() != null ? er.getIdRol().getIdRol() : null;
                boolean sigue = idRolFila != null && elegidos.stream()
                        .anyMatch(r -> r.getIdRol().equals(idRolFila));
                if (!sigue) {
                    empleadoRolRepository.eliminar(er);
                } else if (editandoFila
                        && this.registro.getIdEmpleadoRol().equals(er.getIdEmpleadoRol())) {
                    er.setActivo(activoForm);
                    er.setObservaciones(obsForm);
                    empleadoRolRepository.modificar(er);
                }
            }

            // 2) Inserta los roles que son nuevos en el target, con los
            //    valores de activo/observaciones del formulario
            for (Rol r : elegidos) {
                final UUID idRolElegido = r.getIdRol();
                boolean existia = existentes.stream().anyMatch(er ->
                        er.getIdRol() != null && idRolElegido.equals(er.getIdRol().getIdRol()));
                if (!existia) {
                    EmpleadoRol fila = new EmpleadoRol(UUID.randomUUID());
                    fila.setIdEmpleado(empleadoRepository.findById(this.empleadoSeleccionado));
                    fila.setIdRol(rolRepository.findById(idRolElegido));
                    fila.setActivo(activoForm);
                    fila.setObservaciones(obsForm);
                    empleadoRolRepository.crear(fila);
                }
            }

            this.cargarRegistros();
            this.limpiar();
            this.formVisible = false;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "No se pudo guardar la asignacion de roles: intente de nuevo", null));
        }
    }

    /**
     * Editar una fila de la tabla: carga en el form el empleado de esa fila y
     * el pickList con TODOS sus roles (target) para agregar o quitar.
     */
    @Override
    public void btnEditarHandler() {
        super.btnEditarHandler();
        this.empleadoSeleccionado = null;
        this.roles = null;
        if (this.registro != null && this.registro.getIdEmpleado() != null) {
            this.empleadoSeleccionado = this.registro.getIdEmpleado().getIdEmpleado();
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
     * Lista desplegable de empleados (carga perezosa: una vez por vista).
     */
    public List<SelectItem> getListaEmpleados() {
        if (this.listaEmpleados == null) {
            this.listaEmpleados = new ArrayList<>();
            for (Empleado e : empleadoRepository.findAll()) {
                if (Boolean.FALSE.equals(e.getActivo())) {
                    continue; // los inactivos no se pueden asignar
                }
                this.listaEmpleados.add(new SelectItem(
                        e.getIdEmpleado(),
                        (e.getNombre() != null ? e.getNombre() : "") + " "
                        + (e.getApellido() != null ? e.getApellido() : "")));
            }
        }
        return incluirActual(this.listaEmpleados, this.empleadoSeleccionado, () -> {
            Empleado e = empleadoRepository.findById(this.empleadoSeleccionado);
            return e == null ? null : new SelectItem(
                    e.getIdEmpleado(),
                    (e.getNombre() != null ? e.getNombre() : "") + " "
                    + (e.getApellido() != null ? e.getApellido() : ""));
        });
    }

    public UUID getEmpleadoSeleccionado() {
        return empleadoSeleccionado;
    }

    public void setEmpleadoSeleccionado(UUID empleadoSeleccionado) {
        this.empleadoSeleccionado = empleadoSeleccionado;
    }
}
