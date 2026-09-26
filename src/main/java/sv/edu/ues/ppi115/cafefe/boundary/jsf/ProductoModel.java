package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.ProductoRepository;
import sv.edu.ues.ppi115.cafefe.entity.Producto;

/**
 * Producto: reutiliza AbstractModel (CRUD generico con ESTADO_CRUD) y
 * agrega filtros opcionales (nombre, activos, rango de precios).
 */
@Named
@ViewScoped
public class ProductoModel extends AbstractModel<Producto, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ProductoRepository productoRepository;

    // Filtros opcionales para la vista
    private String filtroNombre;
    private BigDecimal filtroPrecioMin;
    private BigDecimal filtroPrecioMax;

    @Override
    public DefaultDAO<Producto, UUID> getDao() {
        return productoRepository;
    }

    @Override
    public Producto instanciarRegistro() {
        // Siempre con id nuevo: el PK es NOT NULL en la base de datos
        Producto r = new Producto(UUID.randomUUID());
        r.setActivo(Boolean.TRUE);
        r.setComentarios("");
        r.setPrecioSugerido(BigDecimal.ZERO); // NOT NULL en BD
        return r;
    }

    @Override
    public Producto getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(r -> r.getIdProducto().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(Producto dato) {
        return dato != null ? dato.getIdProducto() : null;
    }

    // ---- Filtros opcionales ----
    public void buscarPorNombre() {
        if (filtroNombre != null && !filtroNombre.isEmpty()) {
            this.registros = productoRepository.findByNombreLike(filtroNombre);
        } else {
            this.cargarRegistros();
        }
    }

    public void buscarActivos() {
        this.registros = productoRepository.findActivos();
    }

    public void buscarPorPrecio() {
        if (filtroPrecioMin != null && filtroPrecioMax != null) {
            this.registros = productoRepository.findByPrecioRange(filtroPrecioMin, filtroPrecioMax);
        } else {
            this.cargarRegistros();
        }
    }

    public String getFiltroNombre() {
        return filtroNombre;
    }

    public void setFiltroNombre(String filtroNombre) {
        this.filtroNombre = filtroNombre;
    }

    public BigDecimal getFiltroPrecioMin() {
        return filtroPrecioMin;
    }

    public void setFiltroPrecioMin(BigDecimal filtroPrecioMin) {
        this.filtroPrecioMin = filtroPrecioMin;
    }

    public BigDecimal getFiltroPrecioMax() {
        return filtroPrecioMax;
    }

    public void setFiltroPrecioMax(BigDecimal filtroPrecioMax) {
        this.filtroPrecioMax = filtroPrecioMax;
    }
}
