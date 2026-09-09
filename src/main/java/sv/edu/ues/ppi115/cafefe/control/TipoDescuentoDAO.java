package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.ppi115.cafefe.entity.TipoDescuento;

@Stateless
public class TipoDescuentoDAO extends DefaultDAO<TipoDescuento, Integer> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public TipoDescuentoDAO() {
        super(TipoDescuento.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}