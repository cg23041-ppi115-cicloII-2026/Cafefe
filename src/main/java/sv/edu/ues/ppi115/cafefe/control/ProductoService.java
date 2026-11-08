package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import sv.edu.ues.ppi115.cafefe.entity.Producto;

@Stateless
@LocalBean
public class ProductoService extends AbstractDataAccess<Producto> {

    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProductoService() {
        super(Producto.class);
    }

    /**
     * Lista los productos ACTIVOS, sin paginar. Lo usan los autocomplete y
     * combos: los inactivos no se listan, asi no se pueden elegir.
     * @return lista de productos activos
     * @throws NullPointerException si el repositorio es nulo
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<Producto> findActivos() {
        try {
            if (em != null) {
                return em.createNamedQuery("Producto.findActivos", Producto.class)
                        .getResultList();
            }
            throw new NullPointerException("El repositorio es nulo");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Busca productos cuyo nombre CONTENGA el texto indicado, sin paginar.
     * El texto se envuelve en %...% para que "caf" encuentre "Cafe Moca";
     * la comparacion ignora mayusculas/minusculas.
     * @param nombre texto a buscar en el nombre del producto
     * @return lista de productos cuyo nombre contiene el texto
     * @throws IllegalArgumentException si el nombre es nulo
     * @throws NullPointerException si el repositorio es nulo
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<Producto> findByNombreLike(String nombre) {
        if (nombre == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em != null) {
                return em.createNamedQuery("Producto.findByNombreLike", Producto.class)
                        .setParameter("nombre", "%" + nombre + "%")
                        .getResultList();
            }
            throw new NullPointerException("El repositorio es nulo");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Busca productos cuyo precio sugerido este dentro del rango indicado
     * (BETWEEN, ambos extremos incluidos), sin paginar.
     * @param min precio minimo del rango
     * @param max precio maximo del rango
     * @return lista de productos dentro del rango de precio
     * @throws IllegalArgumentException si alguno de los precios es nulo
     * @throws NullPointerException si el repositorio es nulo
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<Producto> findByPrecioRange(BigDecimal min, BigDecimal max) {
        if (min == null || max == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em != null) {
                return em.createNamedQuery("Producto.findByPrecioRange", Producto.class)
                        .setParameter("min", min)
                        .setParameter("max", max)
                        .getResultList();
            }
            throw new NullPointerException("El repositorio es nulo");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
