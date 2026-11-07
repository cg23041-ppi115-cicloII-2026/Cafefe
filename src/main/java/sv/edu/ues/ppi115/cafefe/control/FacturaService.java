package sv.edu.ues.ppi115.cafefe.control;
 
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.Factura;
 
@Stateless
@LocalBean
public class FacturaService extends AbstractDataAccess<Factura> {
 
    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;
 
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
 
    public FacturaService() {
        super(Factura.class);
    }
 
    /**
     * Busca facturas por caja en un rango paginado.
     * @param idCaja identificador de la caja
     * @param first índice inicial del rango
     * @param max cantidad máxima de registros
     * @return lista de facturas encontradas
     * @throws IllegalArgumentException si algún parámetro es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<Factura> findByCaja(UUID idCaja, int first, int max) {
        if (idCaja != null && first >= 0 && max > 0) {
            try {
                if (em != null) {
                    return em.createNamedQuery("Factura.findByCaja", Factura.class)
                            .setParameter("idCaja", idCaja)
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
     * Cuenta facturas por caja.
     * @param idCaja identificador de la caja
     * @return total de facturas encontradas
     * @throws IllegalArgumentException si el identificador es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public long contarPorCaja(UUID idCaja) {
        if (idCaja == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em != null) {
                return em.createNamedQuery("Factura.contarPorCaja", Long.class)
                        .setParameter("idCaja", idCaja)
                        .getSingleResult();
            }
            throw new NullPointerException("El repositorio es nulo");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Busca facturas por empleado y rol en un rango paginado.
     * @param idEmpleadoRol identificador de la relación empleado-rol
     * @param first índice inicial del rango
     * @param max cantidad máxima de registros
     * @return lista de facturas encontradas
     * @throws IllegalArgumentException si algún parámetro es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<Factura> findByEmpleadoRol(UUID idEmpleadoRol, int first, int max) {
        if (idEmpleadoRol != null && first >= 0 && max > 0) {
            try {
                if (em != null) {
                    return em.createNamedQuery("Factura.findByEmpleadoRol", Factura.class)
                            .setParameter("idEmpleadoRol", idEmpleadoRol)
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
     * Cuenta facturas por empleado y rol.
     * @param idEmpleadoRol identificador de la relación empleado-rol
     * @return total de facturas encontradas
     * @throws IllegalArgumentException si el identificador es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public long contarPorEmpleadoRol(UUID idEmpleadoRol) {
        if (idEmpleadoRol == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em != null) {
                return em.createNamedQuery("Factura.contarPorEmpleadoRol", Long.class)
                        .setParameter("idEmpleadoRol", idEmpleadoRol)
                        .getSingleResult();
            }
            throw new NullPointerException("El repositorio es nulo");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
 
