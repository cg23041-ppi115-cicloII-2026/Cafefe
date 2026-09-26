package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.ProductoTipoProducto;

/**
 * Repositorio del CRUD ProductoTipoProducto (tabla intermedia
 * producto <-> tipo_producto).
 *
 * Hereda de DefaultDAO crear/modificar/eliminar/findById/findAll.
 */
@Stateless
@LocalBean
public class ProductoTipoProductoRepository extends DefaultDAO<ProductoTipoProducto, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public ProductoTipoProductoRepository() {
        super(ProductoTipoProducto.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<ProductoTipoProducto> findByFechaCreacion(Date fechaCreacion) {
        return getEntityManager()
                .createNamedQuery("ProductoTipoProducto.findByFechaCreacion", ProductoTipoProducto.class)
                .setParameter("fechaCreacion", fechaCreacion)
                .getResultList();
    }

    public List<ProductoTipoProducto> findByObservaciones(String observaciones) {
        return getEntityManager()
                .createNamedQuery("ProductoTipoProducto.findByObservaciones", ProductoTipoProducto.class)
                .setParameter("observaciones", observaciones)
                .getResultList();
    }

    /**
     * Filas que relacionan un producto con cualquier tipo.
     */
    public List<ProductoTipoProducto> findByProducto(UUID idProducto) {
        return getEntityManager()
                .createNamedQuery("ProductoTipoProducto.findByProducto", ProductoTipoProducto.class)
                .setParameter("idProducto", idProducto)
                .getResultList();
    }

    /**
     * Filas que relacionan un tipo de producto con cualquier producto.
     */
    public List<ProductoTipoProducto> findByTipoProducto(UUID idTipoProducto) {
        return getEntityManager()
                .createNamedQuery("ProductoTipoProducto.findByTipoProducto", ProductoTipoProducto.class)
                .setParameter("idTipoProducto", idTipoProducto)
                .getResultList();
    }
}
