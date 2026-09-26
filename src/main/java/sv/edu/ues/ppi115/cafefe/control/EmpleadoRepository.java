package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.ppi115.cafefe.entity.Empleado;
import java.util.UUID;

@Stateless
@LocalBean
public class EmpleadoRepository extends DefaultDAO<Empleado, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;
    
    
    public EmpleadoRepository() {
        super(Empleado.class);
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}


