package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.OrdenProductoRepository;
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;

/**
 * Modelo de vista para el CRUD de OrdenProducto.
 *
 * Hereda de AbstractModel el ciclo completo del CRUD
 * (nuevo, editar, guardar, eliminar, seleccionar fila) y agrega
 * los filtros por orden y por producto.
 */
@Named
@ViewScoped
public class OrdenProductoModel extends AbstractModel<OrdenProducto, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private OrdenProductoRepository ordenProductoRepository;

    // Filtros: los UUID llegan como texto desde el formulario
    private String filtroOrden;
    private String filtroProducto;

    public OrdenProductoModel() {
    }

    @Override
    public OrdenProducto instanciarRegistro() {
        return new OrdenProducto();
    }

    @Override
    public OrdenProducto getRegistroById(Object id) {
        return ordenProductoRepository.findById((UUID) id);
    }

    @Override
    public Object getIdByRegistro(OrdenProducto dato) {
        return dato != null ? dato.getIdOrdenProducto() : null;
    }

    @Override
    public OrdenProductoRepository getDao() {
        return ordenProductoRepository;
    }

    // ---------------------------------------------------------------
    // Filtros
    // ---------------------------------------------------------------
    public void buscarPorOrden() {
        UUID id = convertirUUID(filtroOrden);
        if (id != null) {
            this.registros = ordenProductoRepository.findByOrden(id);
        } else {
            this.cargarRegistros(); // vacio o texto invalido: se muestra todo
        }
    }

    public void buscarPorProducto() {
        UUID id = convertirUUID(filtroProducto);
        if (id != null) {
            this.registros = ordenProductoRepository.findByProducto(id);
        } else {
            this.cargarRegistros();
        }
    }

    /**
     * Convierte el texto del formulario a UUID de forma segura;
     * devuelve null si el texto esta vacio o no es un UUID valido
     * (UUID.fromString lanza IllegalArgumentException si no lo es).
     */
    private UUID convertirUUID(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(texto.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ---------------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------------
    public String getFiltroOrden() {
        return filtroOrden;
    }

    public void setFiltroOrden(String filtroOrden) {
        this.filtroOrden = filtroOrden;
    }

    public String getFiltroProducto() {
        return filtroProducto;
    }

    public void setFiltroProducto(String filtroProducto) {
        this.filtroProducto = filtroProducto;
    }
}
