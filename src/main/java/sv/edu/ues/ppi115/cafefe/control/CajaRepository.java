package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.Caja;

@Stateless
@LocalBean
public class CajaRepository extends DefaultDAO<Caja, UUID> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public CajaRepository() {
        super(Caja.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}


