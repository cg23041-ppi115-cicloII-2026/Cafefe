package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.TipoDescuentoRepository;
import sv.edu.ues.ppi115.cafefe.entity.TipoDescuento;

/**
 * reutiliza AbstractModel (CRUD generico con ESTADO_CRUD) y TipoDescuentoRepository.
 */
@Named
@ViewScoped
public class TipoDescuentoModel extends AbstractModel<TipoDescuento, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private TipoDescuentoRepository tipoDescuentoRepository;

    @Override
    public DefaultDAO<TipoDescuento, UUID> getDao() {
        return tipoDescuentoRepository;
    }

    @Override
    public TipoDescuento instanciarRegistro() {
        // Siempre con id nuevo: el PK es NOT NULL en la base de datos
        TipoDescuento r = new TipoDescuento(UUID.randomUUID());
        r.setActivo(Boolean.TRUE);
        r.setObservaciones("");
        r.setDescuentoMaximo(0);
        return r;
    }

    @Override
    public TipoDescuento getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(r -> r.getIdTipoDescuento().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(TipoDescuento dato) {
        return dato != null ? dato.getIdTipoDescuento() : null;
    }
}
