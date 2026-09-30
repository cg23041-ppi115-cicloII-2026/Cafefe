package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.OrdenProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.OrdenRepository;
import sv.edu.ues.ppi115.cafefe.entity.Orden;
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;

/**
 * Pestana 2 de Orden.xhtml: consulta de ordenes ya cerradas.
 *
 * Reutiliza AbstractModel (tabla lazy + seleccion), pero a proposito NO
 * tiene formulario ni Editar: una orden cerrada no se edita, solo se
 * anula. Al seleccionar una fila se carga el detalle de sus lineas
 * (OrdenProducto.findByOrden).
 */
@Named
@ViewScoped
public class OrdenModel extends AbstractModel<Orden, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private OrdenRepository ordenRepository;

    @Inject
    private OrdenProductoRepository ordenProductoRepository;

    /** Lineas de la orden seleccionada (detalle bajo la tabla). */
    private List<OrdenProducto> lineasSeleccion = new ArrayList<>();

    @Override
    public DefaultDAO<Orden, UUID> getDao() {
        return ordenRepository;
    }

    @Override
    public Orden instanciarRegistro() {
        // No se usa para crear (la orden nace en TomarOrdenModel), pero
        // AbstractModel.limpiar() lo necesita con id nuevo
        return new Orden(UUID.randomUUID());
    }

    @Override
    public Orden getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(o -> o.getIdOrden().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(Orden dato) {
        return dato != null ? dato.getIdOrden() : null;
    }

    /**
     * rowSelect/rowUnselect de la tabla de ordenes: carga (o limpia) el
     * detalle de lineas de la orden marcada.
     */
    public void onOrdenSeleccionada() {
        this.lineasSeleccion = (this.seleccion != null)
                ? ordenProductoRepository.findByOrden(this.seleccion.getIdOrden())
                : new ArrayList<>();
    }

    /**
     * Anular una orden: primero borra sus lineas y luego la orden, todo
     * en UNA transaccion (OrdenRepository). Si alguna linea ya fue
     * facturada, la FK de factura_orden_producto revierte el borrado
     * completo y se muestra el aviso.
     */
    @Override
    public void btnEliminarHandler() {
        try {
            super.btnEliminarHandler();
            this.lineasSeleccion = new ArrayList<>();
        } catch (Exception e) {
            this.limpiar();
            this.formVisible = false;
            this.lineasSeleccion = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "No se pudo anular la orden (puede que ya este facturada)", null));
        }
    }

    public List<OrdenProducto> getLineasSeleccion() {
        return lineasSeleccion;
    }

    public void setLineasSeleccion(List<OrdenProducto> lineasSeleccion) {
        this.lineasSeleccion = lineasSeleccion;
    }

    /** Subtotal de una linea del detalle: cada linea es una unidad, asi que es el precio. */
    public java.math.BigDecimal getSubtotal(OrdenProducto linea) {
        if (linea == null || linea.getPrecio() == null) {
            return java.math.BigDecimal.ZERO;
        }
        return linea.getPrecio();
    }
}
