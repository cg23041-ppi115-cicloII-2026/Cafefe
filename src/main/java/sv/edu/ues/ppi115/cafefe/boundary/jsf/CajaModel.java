package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.CajaRepository;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.entity.Caja;

/**
 * Caja: reutiliza AbstractModel (CRUD generico con ESTADO_CRUD).
 */
@Named
@ViewScoped
public class CajaModel extends AbstractModel<Caja, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CajaRepository cajaRepository;

    @Override
    public DefaultDAO<Caja, UUID> getDao() {
        return cajaRepository;
    }

    @Override
    public Caja instanciarRegistro() {
        // Siempre con id nuevo: el PK es NOT NULL en la base de datos
        Caja r = new Caja(UUID.randomUUID());
        r.setActivo(Boolean.TRUE);
        r.setObservaciones("");
        return r;
    }

    @Override
    public Caja getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(r -> r.getIdCaja().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(Caja dato) {
        return dato != null ? dato.getIdCaja() : null;
    }
}
