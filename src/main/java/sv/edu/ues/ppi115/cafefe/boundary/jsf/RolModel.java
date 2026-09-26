package sv.edu.ues.ppi115.cafefe.boundary.jsf;

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
}
