package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.RolRepository;
import sv.edu.ues.ppi115.cafefe.entity.Rol;

/**
 * Rol: reutiliza AbstractModel (CRUD generico con ESTADO_CRUD).
 */
@Named
@ViewScoped
public class RolModel extends AbstractModel<Rol, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private RolRepository rolRepository;

    @Override
    public DefaultDAO<Rol, UUID> getDao() {
        return rolRepository;
    }

    @Override
    public Rol instanciarRegistro() {
        // Siempre con id nuevo: el PK es NOT NULL en la base de datos
        Rol r = new Rol(UUID.randomUUID());
        r.setActivo(Boolean.TRUE);
        r.setObservaciones("");
        return r;
    }

    @Override
    public Rol getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(r -> r.getIdRol().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(Rol dato) {
        return dato != null ? dato.getIdRol() : null;
    }

    public void buscarPorNombre(String nombre) {
        this.registros = rolRepository.findByNombre(nombre);
    }

    /**
     * Eliminar con manejo de error: capturamos la excepcion para mostrar un
     * mensaje amigable en vez de fallar en silencio en el AJAX. La BD tiene
     * ON DELETE RESTRICT, asi que un rol asignado a empleados no se puede borrar.
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
                    "No se pudo eliminar el rol (puede estar asignado a empleados)", null));
        }
    }
}
