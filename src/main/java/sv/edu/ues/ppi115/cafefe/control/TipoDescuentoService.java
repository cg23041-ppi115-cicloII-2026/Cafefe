package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.ppi115.cafefe.entity.TipoDescuento;

@Stateless
@LocalBean
public class TipoDescuentoService extends AbstractDataAccess<TipoDescuento> {

    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TipoDescuentoService() {
        super(TipoDescuento.class);
    }
}