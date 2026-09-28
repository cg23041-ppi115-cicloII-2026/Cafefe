package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

/**
 * LazyDataModel generico para consultas FILTRADAS a la BD.
 *
 * A diferencia de LazyModel (que pagina el findAll generico del DefaultDAO),
 * este recibe del modelo dos funciones: una que devuelve la pagina pedida
 * (offset + limite) con los filtros actuales y otra que cuenta el total,
 * tambien filtrado. Sirve por ejemplo para la tabla de productos ACTIVOS
 * con busqueda por nombre en la pestana "Tomar Orden".
 *
 * Igual que LazyModel v1: sortMeta y filterMeta todavia no se aplican y la
 * seleccion de fila se resuelve por el id del registro.
 */
public class LazyModelConsulta<T> extends LazyDataModel<T> {

    private static final long serialVersionUID = 1L;

    /**
     * Devuelve la pagina [primero, primero+tamano) ya filtrada.
     * Se evalua en cada load(), asi que lee los filtros del modelo en vivo.
     */
    private final BiFunction<Integer, Integer, List<T>> paginas;

    /** Total de filas filtradas (para el paginador). */
    private final Supplier<Long> total;

    /** Extrae el id de un registro (para la seleccion de fila). */
    private final Function<T, Object> idDe;

    public LazyModelConsulta(BiFunction<Integer, Integer, List<T>> paginas,
                             Supplier<Long> total,
                             Function<T, Object> idDe) {
        this.paginas = paginas;
        this.total = total;
        this.idDe = idDe;
    }

    /**
     * CONTAR: total filtrado consultado a la BD en cada pintado.
     */
    @Override
    public int count(Map<String, FilterMeta> filterMeta) {
        if (total == null) {
            return 0;
        }
        Long t = total.get();
        return t != null ? t.intValue() : 0;
    }

    @Override
    public int getRowCount() {
        return count(null);
    }

    /**
     * CARGAR: la pagina que PrimeFaces pide, ya con los filtros aplicados
     * por las funciones que entrego el modelo.
     */
    @Override
    public List<T> load(int first, int pageSize, Map<String, SortMeta> sortMeta,
                        Map<String, FilterMeta> filterMeta) {
        if (paginas == null || pageSize <= 0) {
            return Collections.emptyList();
        }
        return paginas.apply(first, pageSize);
    }

    /**
     * Seleccion de fila con lazy: registro -> clave de fila (String).
     */
    @Override
    public String getRowKey(T object) {
        Object id = (object != null && idDe != null) ? idDe.apply(object) : null;
        return id != null ? id.toString() : null;
    }

    /**
     * Seleccion de fila con lazy: clave de fila -> registro de la pagina
     * actual (getWrappedData = lo que cargo load()).
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
