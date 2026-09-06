package sv.edu.ues.ppi115.cafefe.control;

import java.util.List;

public interface DAOInterface<T, ID> {

    void crear(T entity);

    T modificar(T entity);

    void eliminar(T entity);

    T findById(ID id);

    List<T> findAll();

    List<T> findRange(int start, int max);

    Long count();
}