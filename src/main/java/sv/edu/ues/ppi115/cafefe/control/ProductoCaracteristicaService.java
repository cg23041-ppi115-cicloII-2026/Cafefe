package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.ProductoCaracteristica;

@Stateless
@LocalBean
public class ProductoCaracteristicaService extends AbstractDataAccess<ProductoCaracteristica> {

    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProductoCaracteristicaService() {
        super(ProductoCaracteristica.class);
    }

    /**
     * Busca características de producto en un rango paginado.
     * @param idProducto identificador del producto
     * @param first índice inicial del rango
     * @param max cantidad máxima de registros
     * @return lista de características encontradas
     * @throws IllegalArgumentException si algún parámetro es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<ProductoCaracteristica> findByProducto(UUID idProducto, int first, int max) {
        if (idProducto != null && first >= 0 && max > 0) {
            try {
                if (em != null) {
                    return em.createNamedQuery("ProductoCaracteristica.findByProducto", ProductoCaracteristica.class)
                            .setParameter("idProducto", idProducto)
                            .setFirstResult(first)
                            .setMaxResults(max)
                            .getResultList();
                }
                throw new NullPointerException("El repositorio es nulo");
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        throw new IllegalArgumentException("Parámetros inválidos");
    }

    /**
     * Cuenta características por producto.
     * @param idProducto identificador del producto
     * @return total de características encontradas
     * @throws IllegalArgumentException si el identificador es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public long contarPorProducto(UUID idProducto) {
        if (idProducto == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em != null) {
                return em.createNamedQuery("ProductoCaracteristica.contarPorProducto", Long.class)
                        .setParameter("idProducto", idProducto)
                        .getSingleResult();
            }
            throw new NullPointerException("El repositorio es nulo");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public ProductoCaracteristica findByIdYProducto(UUID id, UUID idProducto) {
        if (id == null || idProducto == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em == null) {
                throw new NullPointerException("El repositorio es nulo");
            }
            return em.createNamedQuery("ProductoCaracteristica.findByIdYProducto",
                            ProductoCaracteristica.class)
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
