package sv.edu.ues.ppi115.cafefe.control;

import java.util.List;

public interface ServiceInterface<T> {

    void create(T entity);

    void delete(T entity);

    List<T> findRange(int first, int max);

    Long contar();

    T update(T entity);

    T findById(Object id);
}