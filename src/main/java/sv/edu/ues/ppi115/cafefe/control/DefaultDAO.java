package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

/**
 * Clase genérica base que implementa las operaciones CRUD y Criteria API 
 * para no duplicar código en los DAOs de cada entidad.
 * 
 * @param <T> Clase del Entity
 * @param <ID> Tipo del identificador (llave primaria) del Entity
 */
public abstract class DefaultDAO<T, ID> implements DAOInterface<T, ID> {

    private final Class<T> entityClass;

    public DefaultDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // Método abstracto para que cada DAO hijo inyecte su propio EntityManager (@PersistenceContext)
    protected abstract EntityManager getEntityManager();

    @Override
    public void crear(T entity) {
        getEntityManager().persist(entity);
    }

    @Override
    public T modificar(T entity) {
        return getEntityManager().merge(entity);
    }

    @Override
    public void eliminar(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    @Override
    public T findById(ID id) {
        return getEntityManager().find(entityClass, id);
    }

    @Override
    public List<T> findAll() {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        cq.select(cq.from(entityClass));
        return em.createQuery(cq).getResultList();
    }

    @Override
    public List<T> findRange(int start, int max) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        cq.select(cq.from(entityClass));
        return em.createQuery(cq)
                 .setFirstResult(start)
                 .setMaxResults(max)
                 .getResultList();
    }

    @Override
    public Long count() {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> rt = cq.from(entityClass);
        cq.select(cb.count(rt));
        return em.createQuery(cq).getSingleResult();
    }
}