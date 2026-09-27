package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.Orden;

/**
 *
 * @author mil
 */
@Stateless
@LocalBean
public class OrdenRepository extends DefaultDAO<Orden, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public OrdenRepository() {
        super(Orden.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Elimina una Orden re-atachándola primero al EntityManager actual.
     * Evita "Removing a detached instance" cuando el objeto viene desde
     * el @ViewScoped.
     */
    @Override
    public void eliminar(Orden registro) {
        if (registro == null || registro.getIdOrden() == null) {
            return;
        }
        Orden managed = getEntityManager().contains(registro)
                ? registro
                : getEntityManager().find(Orden.class, registro.getIdOrden());

        if (managed != null) {
            getEntityManager().remove(managed);
        }
    }
}

