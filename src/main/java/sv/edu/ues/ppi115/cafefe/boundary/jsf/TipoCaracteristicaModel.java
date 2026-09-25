package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.TipoCaracteristicaRepository;
import sv.edu.ues.ppi115.cafefe.entity.TipoCaracteristica;

/**
 *
 * @author 659684
 */
@Named
@ViewScoped
public class TipoCaracteristicaModel extends AbstractModel<TipoCaracteristica, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private TipoCaracteristicaRepository tipoCaracteristicaDAO;

    @Override
    public DefaultDAO<TipoCaracteristica, UUID> getDao() {
        return tipoCaracteristicaDAO;
    }

    @Override
    public TipoCaracteristica instanciarRegistro() {
        TipoCaracteristica r = new TipoCaracteristica(UUID.randomUUID());
        r.setActivo(Boolean.TRUE);
        r.setObservaciones("");
        return r;
    }

    @Override
    public TipoCaracteristica getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(r -> r.getIdTipoCaracteristica().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(TipoCaracteristica dato) {
        if (dato != null) {
            return dato.getIdTipoCaracteristica();
        }
        return null;
    }

}
