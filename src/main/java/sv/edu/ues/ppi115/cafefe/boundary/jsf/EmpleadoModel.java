package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
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
     * Guardar valida nombre y apellido AQUI en el modelo (la vista no usa
     * required, igual que productoCaracteristica): asi el boton Cancelar
     * funciona aunque el formulario este vacio, y el aviso sale solo en la
     * parte superior sin duplicarse junto al campo.
     */
    @Override
    public void btnGuardarHandler() {
        if (this.registro != null) {
            if (this.registro.getNombre() == null || this.registro.getNombre().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN, "Debe ingresar el nombre", null));
                return; // no se guarda sin nombre y el formulario queda abierto
            }
            if (this.registro.getApellido() == null || this.registro.getApellido().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN, "Debe ingresar el apellido", null));
                return; // no se guarda sin apellido
            }
        }
        try {
            super.btnGuardarHandler(); // guarda, recarga la tabla y limpia
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "No se pudo guardar: verifique los datos e intente de nuevo", null));
        }
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
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "No se puede eliminar: el empleado tiene asignaciones en otras tablas", null));
        }
    }
}
