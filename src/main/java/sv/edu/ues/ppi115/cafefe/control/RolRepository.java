package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.ppi115.cafefe.entity.Rol;

@Stateless
@LocalBean
public class RolRepository extends DefaultDAO<Rol, java.util.UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public RolRepository() {
        super(Rol.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<Rol> findByNombre(String nombre) {
        return getEntityManager()
                .createNamedQuery("Rol.findByNombre", Rol.class)
                .setParameter("nombre", nombre)
                .getResultList();
    }
}

