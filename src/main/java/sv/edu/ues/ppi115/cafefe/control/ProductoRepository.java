package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.Producto;

@Stateless
@LocalBean

public class ProductoRepository extends DefaultDAO<Producto, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public ProductoRepository() {
        super(Producto.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<Producto> findActivos() {
        return getEntityManager()
                .createNamedQuery("Producto.findActivos", Producto.class)
                .getResultList();
    }

    public List<Producto> findByNombreLike(String nombre) {
        return getEntityManager()
                .createNamedQuery("Producto.findByNombreLike", Producto.class)
                .setParameter("nombre", "%" + nombre + "%")
                .getResultList();
    }

    public List<Producto> findByPrecioRange(BigDecimal min, BigDecimal max) {
        return getEntityManager()
                .createNamedQuery("Producto.findByPrecioRange", Producto.class)
                .setParameter("min", min)
                .setParameter("max", max)
                .getResultList();
    }

    /**
     * Paginacion de productos SOLO activos con busqueda opcional por
     * nombre (tabla lazy de la pestana "Tomar Orden"). Si nombre viene
     * null o vacio, no filtra por texto.
     */
    public List<Producto> findActivosRange(int start, int max, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return getEntityManager()
                    .createQuery("SELECT p FROM Producto p WHERE p.activo = true "
                            + "ORDER BY p.nombre", Producto.class)
                    .setFirstResult(start)
                    .setMaxResults(max)
                    .getResultList();
        }
        return getEntityManager()
                .createQuery("SELECT p FROM Producto p WHERE p.activo = true "
                        + "AND LOWER(p.nombre) LIKE LOWER(:patron) "
                        + "ORDER BY p.nombre", Producto.class)
                .setParameter("patron", "%" + nombre + "%")
                .setFirstResult(start)
                .setMaxResults(max)
                .getResultList();
    }

    /**
     * Total de productos activos (con el mismo filtro del rango) para el
     * paginador de la tabla lazy.
     */
    public Long countActivos(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return getEntityManager()
                    .createQuery("SELECT COUNT(p) FROM Producto p "
                            + "WHERE p.activo = true", Long.class)
                    .getSingleResult();
        }
        return getEntityManager()
                .createQuery("SELECT COUNT(p) FROM Producto p WHERE p.activo = true "
                        + "AND LOWER(p.nombre) LIKE LOWER(:patron)", Long.class)
                .setParameter("patron", "%" + nombre + "%")
                .getSingleResult();
    }

    /**
     * Eliminar un producto: NO se borra en cascada ninguna de sus
     * relaciones (asi lo exige la regla: si hay relacion, el borrado se
     * bloquea). Si el producto tiene tipos, caracteristicas, descuentos u
     * ordenes, la clave foranea ON DELETE RESTRICT de la base de datos
     * rechaza el borrado y ProductoModel muestra el aviso amigable (un
     * solo mensaje limpio). Solo se pueden eliminar productos "suelos",
     * sin ninguna relacion.
     */
    @Override
    public void eliminar(Producto entity) {
        if (entity == null || entity.getIdProducto() == null) {
            return;
        }
        super.eliminar(entity);
    }
}


