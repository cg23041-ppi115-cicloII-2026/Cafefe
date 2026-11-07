package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.ProductoTipoProducto;

@Stateless
@LocalBean
public class ProductoTipoProductoService extends AbstractDataAccess<ProductoTipoProducto> {

    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProductoTipoProductoService() {
        super(ProductoTipoProducto.class);
    }

    /**
     * Busca tipos de producto asociados a un producto en un rango paginado.
     * @param idProducto identificador del producto
     * @param first índice inicial del rango
     * @param max cantidad máxima de registros
     * @return lista de tipos de producto encontrados
     * @throws IllegalArgumentException si algún parámetro es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<ProductoTipoProducto> findByProducto(UUID idProducto, int first, int max) {
        if (idProducto == null || first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em == null) {
                throw new NullPointerException("El repositorio es nulo");
            }
            return em.createNamedQuery("ProductoTipoProducto.findByProducto", ProductoTipoProducto.class)
                    .setParameter("idProducto", idProducto)
                    .setFirstResult(first)
                    .setMaxResults(max)
                    .getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Cuenta tipos de producto asociados a un producto.
     * @param idProducto identificador del producto
     * @return total de tipos de producto encontrados
     * @throws IllegalArgumentException si el identificador es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public long contarPorProducto(UUID idProducto) {
        if (idProducto == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em == null) {
                throw new NullPointerException("El repositorio es nulo");
            }
            return em.createNamedQuery("ProductoTipoProducto.contarPorProducto", Long.class)
                    .setParameter("idProducto", idProducto)
                    .getSingleResult();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public ProductoTipoProducto findByIdYProducto(UUID id, UUID idProducto) {
        if (id == null || idProducto == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em == null) {
                throw new NullPointerException("El repositorio es nulo");
            }
            return em.createNamedQuery("ProductoTipoProducto.findByIdYProducto",
                            ProductoTipoProducto.class)
                    .setParameter("id", id)
                    .setParameter("idProducto", idProducto)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
