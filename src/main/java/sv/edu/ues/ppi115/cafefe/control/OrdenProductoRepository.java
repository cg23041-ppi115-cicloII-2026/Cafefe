package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;

@Stateless
@LocalBean
public class OrdenProductoRepository extends DefaultDAO<OrdenProducto, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public OrdenProductoRepository() {
        super(OrdenProducto.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<OrdenProducto> findByPrecio(BigDecimal precio) {
        return getEntityManager()
                .createNamedQuery("OrdenProducto.findByPrecio", OrdenProducto.class)
                .setParameter("precio", precio)
                .getResultList();
    }

    public List<OrdenProducto> findByObservaciones(String observaciones) {
        return getEntityManager()
                .createNamedQuery("OrdenProducto.findByObservaciones", OrdenProducto.class)
                .setParameter("observaciones", observaciones)
                .getResultList();
    }

    /**
     * Productos que componen una orden especifica.
     */
    public List<OrdenProducto> findByOrden(UUID idOrden) {
        return getEntityManager()
                .createNamedQuery("OrdenProducto.findByOrden", OrdenProducto.class)
                .setParameter("idOrden", idOrden)
                .getResultList();
    }

    /**
     * Aparecimientos de un producto en distintas ordenes.
     */
    public List<OrdenProducto> findByProducto(UUID idProducto) {
        return getEntityManager()
                .createNamedQuery("OrdenProducto.findByProducto", OrdenProducto.class)
                .setParameter("idProducto", idProducto)
                .getResultList();
    }
}
