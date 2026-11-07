package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.ppi115.cafefe.entity.Empleado;

@Stateless
@LocalBean
public class EmpleadoService extends AbstractDataAccess<Empleado> {

    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public EmpleadoService() {
        super(Empleado.class);
    }
}
