package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.ppi115.cafefe.entity.PagoDetalle;
import java.util.UUID;

@Stateless
public class PagoDetalleRepository extends DefaultDAO<PagoDetalle, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public PagoDetalleRepository() {
        super(PagoDetalle.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
