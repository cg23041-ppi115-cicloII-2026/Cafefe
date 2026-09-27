package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.ProductoCaracteristica;

/**
 * Repositorio del CRUD ProductoCaracteristica (tabla intermedia
 * producto <-> caracteristica).
 *
 * Hereda de DefaultDAO crear/modificar/eliminar/findById/findAll.
 */
@Stateless
@LocalBean
public class ProductoCaracteristicaRepository extends DefaultDAO<ProductoCaracteristica, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public ProductoCaracteristicaRepository() {
        super(ProductoCaracteristica.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<ProductoCaracteristica> findByValor(String valor) {
        return getEntityManager()
                .createNamedQuery("ProductoCaracteristica.findByValor", ProductoCaracteristica.class)
                .setParameter("valor", valor)
                .getResultList();
    }

    /**
     * Filas de caracteristicas asignadas a un producto.
     */
    public List<ProductoCaracteristica> findByProducto(UUID idProducto) {
        return getEntityManager()
                .createNamedQuery("ProductoCaracteristica.findByProducto", ProductoCaracteristica.class)
                .setParameter("idProducto", idProducto)
                .getResultList();
    }

    /**
     * Filas donde una caracteristica esta asignada a cualquier producto.
     */
    public List<ProductoCaracteristica> findByCaracteristica(UUID idCaracteristica) {
        return getEntityManager()
                .createNamedQuery("ProductoCaracteristica.findByCaracteristica", ProductoCaracteristica.class)
                .setParameter("idCaracteristica", idCaracteristica)
                .getResultList();
    }
}

