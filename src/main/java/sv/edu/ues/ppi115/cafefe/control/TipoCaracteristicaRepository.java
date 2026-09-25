package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.TipoCaracteristica;

@Stateless
public class TipoCaracteristicaRepository extends DefaultDAO<TipoCaracteristica, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public TipoCaracteristicaRepository() {
        super(TipoCaracteristica.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}