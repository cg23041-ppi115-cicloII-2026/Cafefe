package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.ppi115.cafefe.entity.Pago;
import java.util.UUID;

@Stateless
public class PagoRepository extends DefaultDAO<Pago, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public PagoRepository() {
        super(Pago.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
