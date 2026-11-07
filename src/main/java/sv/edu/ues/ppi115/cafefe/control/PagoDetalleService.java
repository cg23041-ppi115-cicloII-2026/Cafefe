package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.PagoDetalle;

@Stateless
@LocalBean
public class PagoDetalleService extends AbstractDataAccess<PagoDetalle> {

    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public PagoDetalleService() {
        super(PagoDetalle.class);
    }

    /**
     * Busca detalles asociados a un pago en un rango paginado.
     * @param idPago identificador del pago
     * @param first índice inicial del rango
     * @param max cantidad máxima de registros
     * @return lista de detalles encontrados
     * @throws IllegalArgumentException si algún parámetro es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<PagoDetalle> findByPago(UUID idPago, int first, int max) {
        if (idPago == null || first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em == null) {
                throw new NullPointerException("El repositorio es nulo");
            }
            return em.createNamedQuery("PagoDetalle.findByPago", PagoDetalle.class)
                    .setParameter("idPago", idPago)
                    .setFirstResult(first)
                    .setMaxResults(max)
                    .getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Cuenta detalles asociados a un pago.
     * @param idPago identificador del pago
     * @return total de detalles encontrados
     * @throws IllegalArgumentException si el identificador es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public long contarPorPago(UUID idPago) {
        if (idPago == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em == null) {
                throw new NullPointerException("El repositorio es nulo");
            }
            return em.createNamedQuery("PagoDetalle.contarPorPago", Long.class)
                    .setParameter("idPago", idPago)
                    .getSingleResult();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public PagoDetalle findByIdYPago(UUID id, UUID idPago) {
        if (id == null || idPago == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em == null) {
                throw new NullPointerException("El repositorio es nulo");
            }
            return em.createNamedQuery("PagoDetalle.findByIdYPago", PagoDetalle.class)
                    .setParameter("id", id)
                    .setParameter("idPago", idPago)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}

