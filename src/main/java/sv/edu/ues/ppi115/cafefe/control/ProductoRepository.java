package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.Producto;

@Stateless
@LocalBean

public class ProductoRepository extends DefaultDAO<Producto, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public ProductoRepository() {
        super(Producto.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<Producto> findActivos() {
        return getEntityManager()
                .createNamedQuery("Producto.findActivos", Producto.class)
                .getResultList();
    }

    public List<Producto> findByNombreLike(String nombre) {
        return getEntityManager()
                .createNamedQuery("Producto.findByNombreLike", Producto.class)
                .setParameter("nombre", "%" + nombre + "%")
                .getResultList();
    }

    public List<Producto> findByPrecioRange(BigDecimal min, BigDecimal max) {
        return getEntityManager()
                .createNamedQuery("Producto.findByPrecioRange", Producto.class)
                .setParameter("min", min)
                .setParameter("max", max)
                .getResultList();
    }

    /**
     * Eliminar un producto: primero quita las relaciones que crea la propia
     * vista (producto_tipo_producto del desplegable de tipo y
     * producto_caracteristica de la pestana de asignaciones) y luego el
     * producto. Todo ocurre en UNA sola transaccion: si el producto tuviera
     * ordenes o descuentos (claves foraneas que si deben bloquear el
     * borrado), la transaccion completa se revierte y no se pierde ninguna
     * relacion.
     */
    @Override
    public void eliminar(Producto entity) {
        if (entity == null || entity.getIdProducto() == null) {
            return;
        }
        UUID id = entity.getIdProducto();
        getEntityManager().createQuery(
                "DELETE FROM ProductoTipoProducto p WHERE p.idProducto.idProducto = :id")
                .setParameter("id", id)
                .executeUpdate();
        getEntityManager().createQuery(
                "DELETE FROM ProductoCaracteristica p WHERE p.idProducto.idProducto = :id")
                .setParameter("id", id)
                .executeUpdate();
        super.eliminar(entity);
    }
}


