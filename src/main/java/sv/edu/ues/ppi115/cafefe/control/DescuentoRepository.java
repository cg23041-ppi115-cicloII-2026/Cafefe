package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.Descuento;

@Stateless
public class DescuentoRepository extends DefaultDAO<Descuento, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public DescuentoRepository() {
        super(Descuento.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
