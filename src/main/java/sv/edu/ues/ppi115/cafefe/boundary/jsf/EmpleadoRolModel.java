package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRolRepository;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;

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
}
