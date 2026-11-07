package sv.edu.ues.ppi115.cafefe.control;

import java.util.List;
import java.util.UUID;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.ppi115.cafefe.entity.DescuentoProducto;

@Stateless
@LocalBean
public class DescuentoProductoService extends AbstractDataAccess<DescuentoProducto> {

    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DescuentoProductoService() {
        super(DescuentoProducto.class);
    }

    /**
     * Busca productos asociados a un descuento en un rango paginado.
     * @param idDescuento identificador del descuento
     * @param first índice inicial del rango
     * @param max cantidad máxima de registros
     * @return lista de productos asociados encontradas
     * @throws IllegalArgumentException si algún parámetro es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<DescuentoProducto> findByDescuento(UUID idDescuento, int first, int max) {
        if (idDescuento == null || first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em == null) {
                throw new NullPointerException("El repositorio es nulo");
            }
            return em.createNamedQuery("DescuentoProducto.findByDescuento", DescuentoProducto.class)
                    .setParameter("idDescuento", idDescuento)
                    .setFirstResult(first)
                    .setMaxResults(max)
                    .getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Cuenta productos asociados a un descuento.
     * @param idDescuento identificador del descuento
     * @return total de productos asociados encontrados
     * @throws IllegalArgumentException si el identificador es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public long contarPorDescuento(UUID idDescuento) {
        if (idDescuento == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em == null) {
                throw new NullPointerException("El repositorio es nulo");
            }
            return em.createNamedQuery("DescuentoProducto.contarPorDescuento", Long.class)
                    .setParameter("idDescuento", idDescuento)
                    .getSingleResult();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public DescuentoProducto findByIdYDescuento(UUID id, UUID idDescuento) {
            if (id == null || idDescuento == null) {
                throw new IllegalArgumentException("Parámetros inválidos");
            }
            try {
                if (em == null) {
                    throw new NullPointerException("El repositorio es nulo");
                }
                return em.createNamedQuery("DescuentoProducto.findByIdYDescuento", DescuentoProducto.class)
                        .setParameter("id", id)
                        .setParameter("idDescuento", idDescuento)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

