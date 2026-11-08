package sv.edu.ues.ppi115.cafefe.control;
 
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;
 
@Stateless
@LocalBean
public class OrdenProductoService extends AbstractDataAccess<OrdenProducto> {
 
    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;
 
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
 
    public OrdenProductoService() {
        super(OrdenProducto.class);
    }
    /**
     * Busca productos por orden en un rango paginado.
     * @param idOrden identificador de la orden
     * @param first índice inicial del rango
     * @param max cantidad máxima de registros
     * @return lista de productos encontrados
     * @throws IllegalArgumentException si algún parámetro es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<OrdenProducto> findByOrden(UUID idOrden, int first, int max) {
        if (idOrden != null && first >= 0 && max > 0) {
            try {
                if (em != null) {
                    return em.createNamedQuery("OrdenProducto.findByOrden", OrdenProducto.class)
                            .setParameter("idOrden", idOrden)
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
     * Busca TODAS las lineas de una orden, sin paginar: el detalle de una
     * orden es corto y se pide completo (la variante paginada es para el
     * recurso REST).
     * @param idOrden identificador de la orden
     * @return lista de lineas de la orden
     * @throws IllegalArgumentException si el identificador es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<OrdenProducto> findByOrden(UUID idOrden) {
        if (idOrden != null) {
            try {
                if (em != null) {
                    return em.createNamedQuery("OrdenProducto.findByOrden", OrdenProducto.class)
                            .setParameter("idOrden", idOrden)
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
     * Cuenta productos por orden.
     * @param idOrden identificador de la orden
     * @return total de productos encontrados
     * @throws IllegalArgumentException si el identificador es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public long contarPorOrden(UUID idOrden) {
        if (idOrden == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em != null) {
                return em.createNamedQuery("OrdenProducto.contarPorOrden", Long.class)
                        .setParameter("idOrden", idOrden)
                        .getSingleResult();
            }
            throw new NullPointerException("El repositorio es nulo");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
    /**
     * Busca productos por producto en un rango paginado.
     * @param idProducto identificador del producto
     * @param first índice inicial del rango
     * @param max cantidad máxima de registros
     * @return lista de productos encontrados
     * @throws IllegalArgumentException si algún parámetro es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<OrdenProducto> findByProducto(UUID idProducto, int first, int max) {
        if (idProducto != null && first >= 0 && max > 0) {
            try {
                if (em != null) {
                    return em.createNamedQuery("OrdenProducto.findByProducto", OrdenProducto.class)
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
     * Cuenta productos por producto.
     * @param idProducto identificador del producto
     * @return total de productos encontrados
     * @throws IllegalArgumentException si el identificador es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public long contarPorProducto(UUID idProducto) {
        if (idProducto == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em != null) {
                return em.createNamedQuery("OrdenProducto.contarPorProducto", Long.class)
                        .setParameter("idProducto", idProducto)
                        .getSingleResult();
            }
            throw new NullPointerException("El repositorio es nulo");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public OrdenProducto findByIdYOrden(UUID id, UUID idOrden) {
        if (id == null || idOrden == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em == null) {
                throw new NullPointerException("El repositorio es nulo");
            }
            return em.createNamedQuery("OrdenProducto.findByIdYOrden", OrdenProducto.class)
                    .setParameter("id", id)
                    .setParameter("idOrden", idOrden)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
 
