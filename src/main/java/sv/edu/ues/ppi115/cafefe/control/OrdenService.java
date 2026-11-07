package sv.edu.ues.ppi115.cafefe.control;
 
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.Orden;
 
@Stateless
@LocalBean
public class OrdenService extends AbstractDataAccess<Orden> {
 
    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;
 
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
 
    public OrdenService() {
        super(Orden.class);
    }
 
    /**
     * Busca órdenes por empleado y rol en un rango paginado.
     * @param idEmpleadoRol identificador de la relación empleado-rol
     * @param first índice inicial del rango
     * @param max cantidad máxima de registros
     * @return lista de órdenes encontradas
     * @throws IllegalArgumentException si algún parámetro es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<Orden> findByEmpleadoRol(UUID idEmpleadoRol, int first, int max) {
        if (idEmpleadoRol != null && first >= 0 && max > 0) {
            try {
                if (em != null) {
                    return em.createNamedQuery("Orden.findByEmpleadoRol", Orden.class)
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
     * Cuenta órdenes por empleado y rol.
     * @param idEmpleadoRol identificador de la relación empleado-rol
     * @return total de órdenes encontradas
     * @throws IllegalArgumentException si el identificador es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public long contarPorEmpleadoRol(UUID idEmpleadoRol) {
        if (idEmpleadoRol == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em != null) {
                return em.createNamedQuery("Orden.contarPorEmpleadoRol", Long.class)
                        .setParameter("idEmpleadoRol", idEmpleadoRol)
                        .getSingleResult();
            }
            throw new NullPointerException("El repositorio es nulo");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
 
