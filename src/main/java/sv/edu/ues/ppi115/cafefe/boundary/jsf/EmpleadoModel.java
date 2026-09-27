package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.entity.Empleado;
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRepository;

@Named
@ViewScoped
public class EmpleadoModel extends AbstractModel<Empleado, UUID> implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Inject
    private EmpleadoRepository empleadoRepository;

    @Override
    public Empleado instanciarRegistro() {
        Empleado r = new Empleado(UUID.randomUUID());
        r.setActivo(Boolean.TRUE);
        r.setComentarios("");
        return r;
    }

    @Override
    public Empleado getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(r -> r.getIdEmpleado().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(Empleado dato) {
        return dato != null ? dato.getIdEmpleado() : null;
    }

    @Override
    public DefaultDAO<Empleado, UUID> getDao() {
        return empleadoRepository;
    }

    /**
     * Eliminar con manejo de error: la base de datos tiene claves foraneas
     * ON DELETE RESTRICT (empleado_rol puede referenciar al empleado), por lo
     * que capturamos la excepcion para mostrar un mensaje amigable.
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
            jakarta.faces.context.FacesContext.getCurrentInstance().addMessage(null,
                    new jakarta.faces.application.FacesMessage(
                            jakarta.faces.application.FacesMessage.SEVERITY_WARN,
                            "No se puede eliminar: el empleado tiene asignaciones en otras tablas", null));
        }
    }
}


