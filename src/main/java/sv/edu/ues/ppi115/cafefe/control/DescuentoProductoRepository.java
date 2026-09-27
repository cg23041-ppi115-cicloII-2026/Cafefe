package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.DescuentoProducto;

@Stateless
public class DescuentoProductoRepository extends DefaultDAO<DescuentoProducto, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public DescuentoProductoRepository() {
        super(DescuentoProducto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
