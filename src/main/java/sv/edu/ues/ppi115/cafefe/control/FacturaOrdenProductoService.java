package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.FacturaOrdenProducto;

@Stateless
@LocalBean
public class FacturaOrdenProductoService extends AbstractDataAccess<FacturaOrdenProducto> {

    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public FacturaOrdenProductoService() {
        super(FacturaOrdenProducto.class);
    }

    /**
     * Busca productos de una factura en un rango paginado.
     * @param idFactura identificador de la factura
     * @param first índice inicial del rango
     * @param max cantidad máxima de registros
     * @return lista de productos encontrados
     * @throws IllegalArgumentException si algún parámetro es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<FacturaOrdenProducto> findByFactura(UUID idFactura, int first, int max) {
        if (idFactura == null || first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em == null) {
                throw new NullPointerException("El repositorio es nulo");
            }
            return em.createNamedQuery("FacturaOrdenProducto.findByFactura", FacturaOrdenProducto.class)
                    .setParameter("idFactura", idFactura)
                    .setFirstResult(first)
                    .setMaxResults(max)
                    .getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Cuenta productos asociados a una factura.
     * @param idFactura identificador de la factura
     * @return total de productos encontrados
     * @throws IllegalArgumentException si el identificador es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public long contarPorFactura(UUID idFactura) {
        if (idFactura == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em == null) {
                throw new NullPointerException("El repositorio es nulo");
            }
            return em.createNamedQuery("FacturaOrdenProducto.contarPorFactura", Long.class)
                    .setParameter("idFactura", idFactura)
                    .getSingleResult();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public FacturaOrdenProducto findByIdYFactura(UUID id, UUID idFactura) {
        if (id == null || idFactura == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em == null) {
                throw new NullPointerException("El repositorio es nulo");
            }
            return em.createNamedQuery("FacturaOrdenProducto.findByIdYFactura",
                            FacturaOrdenProducto.class)
                    .setParameter("id", id)
                    .setParameter("idFactura", idFactura)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
