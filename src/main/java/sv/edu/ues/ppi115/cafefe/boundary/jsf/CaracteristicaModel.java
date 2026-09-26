package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.CaracteristicaRepository;
import sv.edu.ues.ppi115.cafefe.entity.Caracteristica;

/**
 * Modelo de vista para el CRUD Caracteristica.
 *
 * Hereda de AbstractModel el ciclo completo del CRUD
 * (nuevo, editar, guardar, eliminar, seleccionar fila).
 */
@Named
@ViewScoped
public class CaracteristicaModel extends AbstractModel<Caracteristica, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CaracteristicaRepository caracteristicaRepository;

    @Override
    public Caracteristica instanciarRegistro() {
        Caracteristica c = new Caracteristica(UUID.randomUUID());
        c.setActivo(Boolean.TRUE);
        c.setObservaciones("");
        return c;
    }

    @Override
    public Caracteristica getRegistroById(Object id) {
        return caracteristicaRepository.findById((UUID) id);
    }

    @Override
    public Object getIdByRegistro(Caracteristica dato) {
        return dato != null ? dato.getIdCaracteristica() : null;
    }

    @Override
    public CaracteristicaRepository getDao() {
        return caracteristicaRepository;
    }
}
