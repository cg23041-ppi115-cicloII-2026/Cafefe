package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
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

    /**
     * Descuentos asignados a un producto (pestana "Tomar Orden": se
     * muestran debajo de observaciones y los vigentes se pueden aplicar).
     */
    public List<DescuentoProducto> findByProducto(UUID idProducto) {
        return getEntityManager()
                .createNamedQuery("DescuentoProducto.findByProducto", DescuentoProducto.class)
                .setParameter("idProducto", idProducto)
                .getResultList();
    }
}
