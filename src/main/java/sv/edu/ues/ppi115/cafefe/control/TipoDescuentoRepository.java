package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.TipoDescuento;

@Stateless
public class TipoDescuentoRepository extends DefaultDAO<TipoDescuento, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public TipoDescuentoRepository() {
        super(TipoDescuento.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}