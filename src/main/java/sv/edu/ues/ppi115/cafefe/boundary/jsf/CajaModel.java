package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.CajaRepository;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;
import sv.edu.ues.ppi115.cafefe.entity.Caja;

@Named
@ViewScoped
public class CajaModel extends AbstractModel<Caja, UUID> {

    @Inject
    private CajaRepository cajaRepository;

    public CajaRepository getRepository() {
        return cajaRepository;
    }

    @Override
    public Caja instanciarRegistro() {
        Caja caja = new Caja(UUID.randomUUID());
        caja.setActivo(Boolean.TRUE);
        caja.setObservaciones("");
        return caja;
    }

    @Override
    public Caja getRegistroById(Object id) {
        return id == null ? null : cajaRepository.findById((UUID) id);
    }

    @Override
    public Object getIdByRegistro(Caja dato) {
        return dato != null ? dato.getIdCaja() : null;
    }

    @Override
    public DefaultDAO<Caja, UUID> getDao() {
       return cajaRepository; // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}


