package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.PagoRepository;
import sv.edu.ues.ppi115.cafefe.entity.Pago;

@Named
@ViewScoped
public class PagoModel extends AbstractModel<Pago, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PagoRepository pagoRepository;

    @Override
    public DefaultDAO<Pago, UUID> getDao() {
        return pagoRepository;
    }

    @Override
    public Pago instanciarRegistro() {
        Pago r = new Pago(UUID.randomUUID());
        r.setEstado("pendiente"); // valores validos: pendiente | cancelado
        //falta asignarle una factura
        r.setObservaciones("");
        return r;
    }

    @Override
    public Pago getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(r -> r.getIdPago().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(Pago dato) {
        return dato != null ? dato.getIdPago() : null;
    }
}
