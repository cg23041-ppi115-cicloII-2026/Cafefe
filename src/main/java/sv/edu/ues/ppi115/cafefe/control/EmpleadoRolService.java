package sv.edu.ues.ppi115.cafefe.control;
 
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.Empleado;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;
import sv.edu.ues.ppi115.cafefe.entity.Rol;
 
@Stateless
@LocalBean
public class EmpleadoRolService extends AbstractDataAccess<EmpleadoRol> {
 
    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;
 
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public EmpleadoRolService() {
        super(EmpleadoRol.class);
    }

    /**
     * Busca relaciones empleado-rol por empleado en un rango paginado.
     * @param idEmpleado identificador del empleado
     * @param first índice inicial del rango
     * @param max cantidad máxima de registros
     * @return lista de relaciones encontradas
     * @throws IllegalArgumentException si algún parámetro es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<EmpleadoRol> findByEmpleado(UUID idEmpleado, int first, int max) {
        if (idEmpleado != null && first >= 0 && max > 0) {
            try {
                if (em != null) {
                    return em.createNamedQuery("EmpleadoRol.findByEmpleado", EmpleadoRol.class)
                            .setParameter("idEmpleado", idEmpleado)
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
     * Cuenta relaciones empleado-rol por empleado.
     * @param idEmpleado identificador del empleado
     * @return total de relaciones encontradas
     * @throws IllegalArgumentException si el identificador es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public long contarPorEmpleado(UUID idEmpleado) {
        if (idEmpleado == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em != null) {
                return em.createNamedQuery("EmpleadoRol.contarPorEmpleado", Long.class)
                        .setParameter("idEmpleado", idEmpleado)
                        .getSingleResult();
            }
            throw new NullPointerException("El repositorio es nulo");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
    /**
     * Busca relaciones empleado-rol por rol en un rango paginado.
     * @param idRol identificador del rol
     * @param first índice inicial del rango
     * @param max cantidad máxima de registros
     * @return lista de relaciones encontradas
     * @throws IllegalArgumentException si algún parámetro es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<EmpleadoRol> findByRol(UUID idRol, int first, int max) {
        if (idRol != null && first >= 0 && max > 0) {
            try {
                if (em != null) {
                    return em.createNamedQuery("EmpleadoRol.findByRol", EmpleadoRol.class)
                            .setParameter("idRol", idRol)
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
     * Cuenta relaciones empleado-rol por rol.
     * @param idRol identificador del rol
     * @return total de relaciones encontradas
     * @throws IllegalArgumentException si el identificador es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public long contarPorRol(UUID idRol) {
        if (idRol == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em != null) {
                return em.createNamedQuery("EmpleadoRol.contarPorRol", Long.class)
                        .setParameter("idRol", idRol)
                        .getSingleResult();
            }
            throw new NullPointerException("El repositorio es nulo");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public EmpleadoRol findByIdYEmpleado(UUID id, UUID idEmpleado) {
        if (id == null || idEmpleado == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em == null) {
                throw new NullPointerException("El repositorio es nulo");
            }
            return em.createNamedQuery("EmpleadoRol.findByIdYEmpleado", EmpleadoRol.class)
                    .setParameter("id", id)
                    .setParameter("idEmpleado", idEmpleado)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
 