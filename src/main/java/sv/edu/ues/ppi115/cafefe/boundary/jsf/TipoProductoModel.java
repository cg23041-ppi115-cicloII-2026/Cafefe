package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.TipoProductoDAO;
import sv.edu.ues.ppi115.cafefe.entity.TipoProducto;

@Named
@ViewScoped
public class TipoProductoModel extends AbstractModel<TipoProducto, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private TipoProductoDAO tipoProductoDAO;

    @Override
    public DefaultDAO<TipoProducto, UUID> getDao() {
        return tipoProductoDAO;
    }

    @Override
    public TipoProducto instanciarRegistro() {
        TipoProducto r = new TipoProducto(UUID.randomUUID());
        r.setActivo(Boolean.TRUE);
        r.setObservaciones("");
        return r;
    }

    @Override
    public TipoProducto getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(r -> r.getIdTipoProducto().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(TipoProducto dato) {
        if (dato != null) {
            return dato.getIdTipoProducto();
        }
        return null;
    }
}