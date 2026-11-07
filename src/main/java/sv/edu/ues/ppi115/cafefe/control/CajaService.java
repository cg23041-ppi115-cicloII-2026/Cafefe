package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.ppi115.cafefe.entity.Caja;

@Stateless
@LocalBean
public class CajaService extends AbstractDataAccess<Caja> {

    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;

    public CajaService() {
        super(Caja.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
