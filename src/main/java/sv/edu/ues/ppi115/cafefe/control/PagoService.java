package sv.edu.ues.ppi115.cafefe.control;
 
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.Pago;
 
@Stateless
@LocalBean
public class PagoService extends AbstractDataAccess<Pago> {
 
    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;
 
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
 
    public PagoService() {
        super(Pago.class);
    }
 
    /**
     * Busca pagos por factura en un rango paginado.
     * @param idFactura identificador de la factura
     * @param first índice inicial del rango
     * @param max cantidad máxima de registros
     * @return lista de pagos encontrados
     * @throws IllegalArgumentException si algún parámetro es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<Pago> findByFactura(UUID idFactura, int first, int max) {
        if (idFactura != null && first >= 0 && max > 0) {
            try {
                if (em != null) {
                    return em.createNamedQuery("Pago.findByFactura", Pago.class)
                            .setParameter("idFactura", idFactura)
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
     * Cuenta pagos por factura.
     * @param idFactura identificador de la factura
     * @return total de pagos encontrados
     * @throws IllegalArgumentException si el identificador es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public long contarPorFactura(UUID idFactura) {
        if (idFactura == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em != null) {
                return em.createNamedQuery("Pago.contarPorFactura", Long.class)
                        .setParameter("idFactura", idFactura)
                        .getSingleResult();
            }
            throw new NullPointerException("El repositorio es nulo");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
