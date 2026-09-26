package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;

@Stateless
public class EmpleadoRolRepository extends DefaultDAO<EmpleadoRol, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public EmpleadoRolRepository() {
        super(EmpleadoRol.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
