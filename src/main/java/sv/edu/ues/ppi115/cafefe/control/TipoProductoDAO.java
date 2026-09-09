package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.ppi115.cafefe.entity.TipoProducto;
import java.util.UUID;

@Stateless
public class TipoProductoDAO extends DefaultDAO<TipoProducto, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public TipoProductoDAO() {
        super(TipoProducto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}