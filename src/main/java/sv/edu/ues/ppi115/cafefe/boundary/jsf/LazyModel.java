package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;

/**
 * Puente lazy generico entre p:dataTable (lazy="true" paginator="true")
 * y el DefaultDAO.
 *
 *  - CONTAR -> getRowCount() -> DefaultDAO.count()     (total para el paginador)
 *  - CARGAR  -> load(...)     -> DefaultDAO.findRange() (solo la pagina pedida)
 *
 * PrimeFaces pagina contra la BD con LIMIT/OFFSET: nunca se cargan todos
 * los registros en memoria. Tambien resuelve la seleccion de fila con lazy
 * (rowKey = id del registro), que los botones Editar/Eliminar necesitan.
 */
public class LazyModel<T> extends LazyDataModel<T> {

    private static final long serialVersionUID = 1L;

    private final DefaultDAO<T, ?> dao;
    private final Function<T, Object> idDe;

    /**
     * @param dao  repositorio que cuenta y pagina (DefaultDAO)
     * @param idDe funcion que extrae el id de un registro (getIdByRegistro)
     */
    public LazyModel(DefaultDAO<T, ?> dao, Function<T, Object> idDe) {
        this.dao = dao;
        this.idDe = idDe;
    }

    /**
     * CONTAR: total de filas que hay en la tabla (para armar el paginador).
     * Siempre consulta a la BD para no quedar desactualizado tras un
     * guardar/eliminar.
     */
    @Override
    public int count(Map<String, FilterMeta> filterMeta) {
        if (dao == null) {
            return 0;
        }
        Long total = dao.count();
        return total != null ? total.intValue() : 0;
    }

    @Override
    public int getRowCount() {
        return count(null);
    }

    /**
     * CARGAR: la pagina que PrimeFaces pide (first + pageSize),
     * traducida a OFFSET/LIMIT por DefaultDAO.findRange().
     *
     * Nota v1: sortMeta y filterMeta todavia no se aplican (vienen despues).
     */
    @Override
    public List<T> load(int first, int pageSize, Map<String, SortMeta> sortMeta,
                        Map<String, FilterMeta> filterMeta) {
        if (dao == null || pageSize <= 0) {
            return Collections.emptyList();
        }
        return dao.findRange(first, pageSize);
    }

    /**
     * Seleccion de fila con lazy: registro -> clave de fila (String).
     * Sin esto, Editar/Eliminar no saben que fila se clickeo.
     */
    @Override
    public String getRowKey(T object) {
        Object id = (object != null && idDe != null) ? idDe.apply(object) : null;
        return id != null ? id.toString() : null;
    }

    /**
     * Seleccion de fila con lazy: clave de fila -> registro
     * de la pagina actual (getWrappedData = lo que cargo load()).
     */
    @Override
    public T getRowData(String rowKey) {
        List<T> pagina = getWrappedData();
        if (pagina != null && rowKey != null) {
            for (T objeto : pagina) {
                if (rowKey.equals(String.valueOf(idDe.apply(objeto)))) {
                    return objeto;
                }
            }
        }
        return null;
    }
}
