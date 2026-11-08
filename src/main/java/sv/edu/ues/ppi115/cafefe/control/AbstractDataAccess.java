package sv.edu.ues.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * Clase base para el acceso a datos de cualquier entidad.
 * Cada service concreto solo indica su EntityManager y su clase de entidad.
 *
 * @param <T> tipo de la entidad
 */
public abstract class AbstractDataAccess<T> implements ServiceInterface<T> {


    final Class<T> entityClass;

    public AbstractDataAccess(Class<T> entityClass){
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();

    /**
     * Almacena registros en el repositorio, el objeto a almacenar no puede ser nulo, de lo contrario se lanzará una IllegalArgumentException. Si el repositorio es nulo se lanzará una NullPointerException. Cualquier otra excepción se encapsulará en una IllegalStateException.
     * @param entity el objeto a almacenar
     * @throws IllegalArgumentException si el objeto a almacenar es nulo
     * @throws NullPointerException si el repositorio es nulo
     * @throws IllegalStateException si ocurre cualquier otra excepción
     */

    public void create(T entity){
        if (entity != null){
            try {
                EntityManager em = this.getEntityManager();
                if (em != null){
                    em.persist(entity);
                    return;
                }
                throw new NullPointerException("El repositorio es nulo");
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            throw new IllegalArgumentException("entity no puede ser nulo");
        }
    }

    /**
     * Elimina un registro del repositorio, el objeto a eliminar no puede ser nulo, de lo contrario se lanzará una IllegalArgumentException. Si el repositorio es nulo se lanzará una NullPointerException. Cualquier otra excepción se encapsulará en una IllegalStateException.
     * @param entity el objeto a eliminar
     * @throws IllegalArgumentException si el objeto a eliminar es nulo
     * @throws NullPointerException si el repositorio es nulo
     * @throws IllegalStateException si ocurre cualquier otra excepción
     */
    public void delete(T entity){
        if (entity != null){
            try {
                EntityManager em = this.getEntityManager();
                if (em == null){
                    throw new NullPointerException("El repositorio es nulo");
                }
                if (em.contains(entity)){
                    em.remove(entity);
                }else {
                    em.remove(em.merge(entity));
                }
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            throw new IllegalArgumentException("entity no puede ser nulo");
        }
    }

    /**
     * Busca registros en el repositorio, los parámetros first y max no pueden ser negativos, de lo contrario se lanzará una IllegalArgumentException. Si el repositorio es nulo se lanzará una NullPointerException. Cualquier otra excepción se encapsulará en una IllegalStateException.
     * @param first el índice del primer registro a buscar
     * @param max el número máximo de registros a buscar
     * @return una lista de registros encontrados
     * @throws IllegalArgumentException si los parámetros first o max son negativos
     * @throws NullPointerException si el repositorio es nulo
     * @throws IllegalStateException si ocurre cualquier otra excepción
     */
    public List<T> findRange(int first, int max){
        if (first < 0 || max < 0){
            throw new IllegalArgumentException("Parametro no válido: first o max");
        }
        try {
            EntityManager em = this.getEntityManager();
            if (em != null){
                return em.createNamedQuery(entityClass.getSimpleName() + ".findAll", entityClass)
                        .setFirstResult(first)
                        .setMaxResults(max)
                        .getResultList();
            }
            throw new NullPointerException("El repositorio es nulo");
        }catch (Exception ex){
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Busca todos los registros del repositorio. Si el repositorio es nulo se lanzará un NullPointerException. Cualquier otra excepción se encapsulará en una IllegalStateException.
     * @return una lista con todos los registros encontrados
     * @throws NullPointerException si el repositorio es nulo
     * @throws IllegalStateException si ocurre cualquier otra excepción
     */
    public List<T> findAll(){
        EntityManager em = this.getEntityManager();
        if (em == null){
            throw new NullPointerException("El repositorio es nulo");
        }
        try {
            return em.createNamedQuery(entityClass.getSimpleName() + ".findAll", entityClass)
                    .getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Cuenta el número total de registros en el repositorio. Si el repositorio es nulo se lanzará una NullPointerException. Cualquier otra excepción se encapsulará en una IllegalStateException.
     * @return el número total de registros en el repositorio
     * @throws NullPointerException si el repositorio es nulo
     * @throws IllegalStateException si ocurre cualquier otra excepción
     */
    public Long contar() throws IllegalStateException {
        try {
            EntityManager em = getEntityManager();

            if (em == null) {
                throw new IllegalStateException("Error al acceder al repositorio");
            }

            TypedQuery<Long> query = em.createNamedQuery(
                    entityClass.getSimpleName() + ".countAll", Long.class);
            return query.getSingleResult();

        } catch (Exception e) {
            throw new IllegalStateException("Error al acceder al repositorio", e);
        }
    }

    /**
     * Actualiza un registro en el repositorio, el objeto a actualizar no puede ser nulo, de lo contrario se lanzará una IllegalArgumentException. Si el repositorio es nulo se lanzará una NullPointerException. Cualquier otra excepción se encapsulará en una IllegalStateException.
     * @param entity el objeto a actualizar
     * @return el objeto actualizado
     * @throws IllegalArgumentException si el objeto a actualizar es nulo
     * @throws NullPointerException si el repositorio es nulo
     * @throws IllegalStateException si ocurre cualquier otra excepción
     */
    public T update(T entity) throws IllegalStateException, IllegalArgumentException {
        if (entity != null){
            try {
                EntityManager em = this.getEntityManager();
                if (em != null){
                    return em.merge(entity);
                }
                throw new NullPointerException("El repositorio es nulo");
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            throw new IllegalArgumentException("entity no puede ser nulo");
        }
    }

    /**
     * Busca un registro por su identificador, el identificador no puede ser nulo, de lo contrario se lanzará una IllegalArgumentException. Si el repositorio es nulo se lanzará una NullPointerException. Cualquier otra excepción se encapsulará en una IllegalStateException.
     * @param id el identificador del registro a buscar
     * @return el registro encontrado o null si no se encuentra
     * @throws IllegalArgumentException si el identificador es nulo
     * @throws NullPointerException si el repositorio es nulo
     * @throws IllegalStateException si ocurre cualquier otra excepción
     */
    public T findById(Object id){
        if (id!=null){
            try {
                EntityManager em = this.getEntityManager();
                if (em != null){
                    return em.find(entityClass, id);
                }
                throw new NullPointerException("El repositorio es nulo");
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        throw new IllegalArgumentException("id no puede ser nulo");
    }
}