package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.control.PagoDetalleRepository;
import sv.edu.ues.ppi115.cafefe.entity.PagoDetalle;

@Named
@ViewScoped
public class PagoDetalleModel extends AbstractModel<PagoDetalle, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PagoDetalleRepository pagoDetalleRepository;

    @Override
    public DefaultDAO<PagoDetalle, UUID> getDao() {
        return pagoDetalleRepository;
    }

    @Override
    public PagoDetalle instanciarRegistro() {
        PagoDetalle r = new PagoDetalle(UUID.randomUUID());
        r.setMonto(BigDecimal.ZERO);
        r.setTipoPago("Efectivo"); // valores validos: Efectivo | Tarjeta | Transferencia
        r.setObservaciones("");
        //tambien falta referenciar el idPago
        r.setReferenciaExterna("");
        return r;
    }

    @Override
    public PagoDetalle getRegistroById(Object id) {
        if (id != null && this.registros != null && !this.registros.isEmpty()) {
            UUID busca = (UUID) id;
            return this.registros.stream()
                    .filter(r -> r.getIdPagoDetalle().equals(busca))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object getIdByRegistro(PagoDetalle dato) {
        return dato != null ? dato.getIdPagoDetalle() : null;
    }
}
