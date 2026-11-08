package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.ppi115.cafefe.entity.Rol;

@Stateless
@LocalBean
public class RolService extends AbstractDataAccess<Rol> {

    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RolService() {
        super(Rol.class);
    }
    public List<Rol> findByNombre(String nombre) {
    if (nombre == null || nombre.trim().isEmpty()) {
        throw new IllegalArgumentException("Parámetros inválidos");
    }
    try {
        if (em == null) {
            throw new NullPointerException("El repositorio es nulo");
        }
        return em.createNamedQuery("Rol.findByNombre", Rol.class)
                .setParameter("nombre", nombre.trim())
                .getResultList();
    } catch (Exception ex) {
        throw new IllegalStateException(ex);
    }
}
}
